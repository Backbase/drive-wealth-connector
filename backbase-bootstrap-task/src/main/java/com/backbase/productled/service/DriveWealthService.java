package com.backbase.productled.service;

import static com.backbase.productled.utils.DriveWealthConstants.DW_USER_ID;
import static com.backbase.productled.utils.DriveWealthConstants.IGNORE_MARKET_HOURS_FOR_TEST;
import static com.backbase.productled.utils.DriveWealthConstants.OPERATION;
import static com.backbase.productled.utils.DriveWealthConstants.RESOURCE;
import static com.backbase.productled.utils.DriveWealthConstants.SPAN_ID;
import static com.backbase.stream.LegalEntitySaga.CREATED;
import static java.util.Collections.singletonMap;

import com.backbase.dbs.user.manager.rest.serviceapi.spec.v2.IdentityManagementApi;
import com.backbase.drivewealth.clients.accounts.api.AccountsApi;
import com.backbase.drivewealth.clients.accounts.model.CreateAccountResponse;
import com.backbase.drivewealth.clients.deposits.api.DepositsApi;
import com.backbase.drivewealth.clients.instrument.api.InstrumentApi;
import com.backbase.drivewealth.clients.orders.api.OrdersApi;
import com.backbase.drivewealth.clients.users.api.UsersApi;
import com.backbase.productled.config.properties.DriveWealthConfigurationProperties;
import com.backbase.productled.mapper.DriveWealthMapper;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.InvestmentAccount;
import com.backbase.stream.legalentity.model.JobProfileUser;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.User;
import com.backbase.stream.portfolio.model.AggregatePortfolio;
import com.backbase.stream.portfolio.model.PortfolioBundle;
import com.backbase.stream.portfolio.model.Position;
import com.backbase.stream.portfolio.model.WealthPositionsBundle;
import com.backbase.stream.worker.StreamTaskExecutor;
import com.fasterxml.jackson.jaxrs.util.LRUMap;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.sleuth.annotation.ContinueSpan;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriveWealthService implements StreamTaskExecutor<LegalEntityTask> {

    private final LRUMap<String, String> userCache = new LRUMap<>(50, Integer.MAX_VALUE);

    private final IdentityManagementApi identityManagementApi;
    private final InstrumentApi instrumentApi;
    private final AccountsApi accountsApi;
    private final DepositsApi depositsApi;
    private final OrdersApi ordersApi;
    @Qualifier("dwUsersApi")
    private final UsersApi usersApi;
    private final DriveWealthMapper mapper;
    private final DriveWealthConfigurationProperties dwConfig;
    private final List<WealthPositionsBundle> positionsBundles;
    private final List<PortfolioBundle> portfolioBundles;
    private final List<AggregatePortfolio> aggregatePortfolios;

    @NewSpan
    public Mono<LegalEntityTask> executeTask(@SpanTag(value = "streamTask") LegalEntityTask streamTask) {
        if (dwConfig.bootstrapResourcesEnabled()) {
            log.info("DriveWealth Flag is enabled");
            return createDriveWealthResources(streamTask);
        } else {
            log.warn(
                "Flag 'driveWealth.bootstrapResourcesEnabled' is set to false. Skipping resource creation in DriveWealth");
            return Mono.just(streamTask);
        }
    }

    @ContinueSpan(log = SPAN_ID)
    private Mono<LegalEntityTask> createDriveWealthResources(LegalEntityTask task) {

        task.info(RESOURCE, OPERATION, "", task.getData().getExternalId(), null,
            "Creating DriveWealth resources for Legal Entity with External ID: %s", task.getData().getExternalId());
        LegalEntity legalEntity = task.getData();

        // create profile of all users under root entity in drivewealth
        processUsers(legalEntity);
        processProductGroups(legalEntity);

        processSubsidiaries(legalEntity.getSubsidiaries());

        task.info(RESOURCE, OPERATION, CREATED, legalEntity.getExternalId(), legalEntity.getInternalId(),
            "Created DriveWealth resources for Legal Entity with External ID: %s", legalEntity.getExternalId());

        return Mono.just(task);
    }

    private void processSubsidiaries(List<LegalEntity> subsidiaries) {
        Optional.ofNullable(subsidiaries)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(Objects::nonNull)
            .forEach(legalEntity -> {
                processUsers(legalEntity);
                processProductGroups(legalEntity);
                processSubsidiaries(legalEntity.getSubsidiaries());
            });
    }

    private void processProductGroups(LegalEntity legalEntity) {

        if (!CollectionUtils.isEmpty(legalEntity.getProductGroups())) {
            legalEntity.getProductGroups()
                .forEach(productGroup -> Optional.ofNullable(productGroup.getInvestmentAccounts())
                    .ifPresent(investmentAccounts -> investmentAccounts
                        .forEach(this::createInvestmentAccounts))
                );
        }

    }

    private void createInvestmentAccounts(InvestmentAccount investmentAccount) {

        String dwAccountId;
        String dwAccountNo;
        OffsetDateTime accountOpeningDate;

        var userId = getUserId(investmentAccount.getAccountHolderNames());
        if (userId != null) {
            var userAccounts = accountsApi.listUserAccounts(userId).stream()
                .filter(account -> !Objects.equals(Objects.requireNonNull(account.getStatus()).getName(), "CLOSED"))
                .filter(
                    account -> account.getMetadata() != null && Objects.equals(account.getMetadata().getExternalId(),
                        investmentAccount.getExternalId()))
                .findFirst();

            if (userAccounts.isPresent()) {
                log.info("Skipping Account Creation as account already exist with id {} and account Number {}",
                    userAccounts.get().getId(), userAccounts.get().getAccountNo());
                dwAccountNo = userAccounts.get().getAccountNo();
                dwAccountId = userAccounts.get().getId();
                accountOpeningDate = userAccounts.get().getOpenedWhen();
            } else {

                log.info("Account containing externalId {} does not exist. Creating a account in driveWealth",
                    investmentAccount.getExternalId());

                var accountResponse = Objects.requireNonNull(accountsApi
                    .createAccount(mapper.map(userId, investmentAccount.getExternalId(),
                        Objects.equals(investmentAccount.getAdditions().get(IGNORE_MARKET_HOURS_FOR_TEST), "true"))));

                accountOpeningDate = accountResponse.getOpenedWhen();

                log.info("Account Created in DriveWealth with Id {} and No {}", accountResponse.getId(),
                    accountResponse.getAccountNo());
                dwAccountNo = accountResponse.getAccountNo();
                dwAccountId = accountResponse.getId();

                depositFundsAndCreateOrders(investmentAccount, accountResponse);
            }

            updatePortfolioCodeInAggregator(investmentAccount.getBBAN(), dwAccountNo);
            updatePortfolioCodeInPortfolioBundle(investmentAccount.getBBAN(), dwAccountNo);
            updatePortfolioCodeInPostionBundle(investmentAccount.getBBAN(), dwAccountNo);

            investmentAccount.setBBAN(dwAccountId);
            investmentAccount.setExternalId(dwAccountNo);
            investmentAccount.setAccountOpeningDate(accountOpeningDate);
        }

    }

    private void depositFundsAndCreateOrders(InvestmentAccount investmentAccount, CreateAccountResponse accountResponse) {
        depositsApi.depositFunds(
            mapper.map(accountResponse.getAccountNo(), investmentAccount.getCurrentInvestment().getAmount()));

        log.info("Deposited {} to an Account {}", investmentAccount.getCurrentInvestment().getAmount(),
            accountResponse.getAccountNo());

        var positions = positionsBundles.stream()
            .flatMap(bundle -> bundle.getPositions().stream())
            .filter(bundle -> Objects.equals(bundle.getPortfolioCode(), investmentAccount.getExternalId()))
            .toList();

        positions.forEach(
            position -> {
                log.info("Creating an order for instrument {} with quantity {}", position.getInstrumentId(),
                    position.getQuantity());
                ordersApi.createOrder(mapper.map(position, accountResponse.getAccountNo(),
                    instrumentApi.getInstrumentById(position.getInstrumentId())));
            });
    }

    private void updatePortfolioCodeInPostionBundle(String oldValue, String newValue) {
        for (WealthPositionsBundle positionsBundle : positionsBundles) {
            for (Position position : positionsBundle.getPositions()) {
                if (Objects.equals(position.getPortfolioCode(), oldValue)) {
                    position.setPortfolioCode(newValue);
                }
            }
        }
    }

    private void updatePortfolioCodeInPortfolioBundle(String oldValue, String newValue) {
        for (PortfolioBundle portfolioBundle : portfolioBundles) {
            if (Objects.equals(portfolioBundle.getPortfolio().getCode(), oldValue)) {
                portfolioBundle.getPortfolio().setCode(newValue);
                portfolioBundle.getPortfolio().setArrangementId(newValue);
                portfolioBundle.getPortfolio().setIban(newValue);
            }
        }
    }

    private void updatePortfolioCodeInAggregator(String oldValue, String newValue) {
        for (AggregatePortfolio aggregatePortfolio : aggregatePortfolios) {
            if (aggregatePortfolio.getPortfolios().contains(oldValue)) {
                ListIterator<String> iterator = aggregatePortfolio.getPortfolios().listIterator();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    if (next.equals(oldValue)) {
                        iterator.set(newValue);
                    }
                }
            }
        }
    }

    private String getUserId(String accountHolderName) {
        var cacheUserId = userCache.get(accountHolderName);
        if (cacheUserId != null) {
            log.info("User Id {} for Account Holder {} found in cache", cacheUserId, accountHolderName);
            return cacheUserId;
        }
        return null;
    }

    private void processUsers(LegalEntity legalEntity) {
        if (!CollectionUtils.isEmpty(legalEntity.getUsers())) {
            legalEntity.getUsers().forEach(user -> {
                var dwUserId = Optional.ofNullable(getSearchUserResponse(user));

                dwUserId.filter(StringUtils::isNoneEmpty)
                    .ifPresentOrElse(userId -> userCache.put(user.getUser().getFullName(), userId), () -> {
                        log.info("User {} Doesn't Exist, Creating a user", user.getUser().getExternalId());
                        createOrUpdateUser(user.getUser());
                    });
                user.getUser().setAttributes(singletonMap(DW_USER_ID, userCache.get(user.getUser().getFullName())));
                Optional.ofNullable(legalEntity.getAdministrators())
                        .ifPresent(administrators -> {
                            if (!administrators.isEmpty()) {
                                administrators.get(0)
                                        .setAttributes(singletonMap(DW_USER_ID, userCache.get(user.getUser().getFullName())));
                            }
                        });
            });
        }
    }

    private String getSearchUserResponse(JobProfileUser user) {

        return identityManagementApi.getIdentities(user.getUser().getExternalId(), null, null, 1)
            .filter(getIdentities -> getIdentities != null && getIdentities.getTotalElements() != 0L &&
                getIdentities.getIdentities().get(0).getId() != null)
            .map(getIdentities -> getIdentities.getIdentities().get(0).getId())
            .flatMap(identityManagementApi::getIdentity)
            .map(identity -> Objects.requireNonNull(identity.getAttributes())
                .getOrDefault(DW_USER_ID, StringUtils.EMPTY))
            .block();
    }

    @SneakyThrows
    private void createOrUpdateUser(User user) {
        var response = usersApi.createUser(mapper.map(user));

        Optional.ofNullable(response).ifPresent(r -> {
            log.info("Created User with username {} and Id {}", user.getExternalId(), r.getId());
            log.info("Putting User with name {} with Id {} in Cache", user.getFullName(), r.getId());
            userCache.put(user.getFullName(), r.getId());
        });
    }

    @Override
    public Mono<LegalEntityTask> rollBack(LegalEntityTask legalEntityTask) {
        return null;
    }

}