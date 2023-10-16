package com.backbase.productled.service;

import static com.backbase.stream.legalentity.model.LegalEntityType.CUSTOMER;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.backbase.marqeta.clients.api.CardProductsApi;
import com.backbase.marqeta.clients.api.CardsApi;
import com.backbase.marqeta.clients.api.UsersApi;
import com.backbase.marqeta.clients.api.VelocityControlsApi;
import com.backbase.marqeta.clients.model.CardHolderModel;
import com.backbase.marqeta.clients.model.CardLifeCycle;
import com.backbase.marqeta.clients.model.CardProductConfig;
import com.backbase.marqeta.clients.model.CardProductFulfillment;
import com.backbase.marqeta.clients.model.CardProductFulfillment.PaymentInstrumentEnum;
import com.backbase.marqeta.clients.model.CardProductRequest;
import com.backbase.marqeta.clients.model.CardRequest;
import com.backbase.marqeta.clients.model.CardResponse;
import com.backbase.marqeta.clients.model.CardResponse.StateEnum;
import com.backbase.marqeta.clients.model.Poi;
import com.backbase.marqeta.clients.model.SpendControlAssociation;
import com.backbase.marqeta.clients.model.VelocityControlRequest;
import com.backbase.marqeta.clients.model.VelocityControlRequest.VelocityWindowEnum;
import com.backbase.productled.config.properties.CardConfigurationProperties;
import com.backbase.productled.config.properties.CardUsersProperties;
import com.backbase.productled.config.properties.EnvironmentConfigurationProperties;
import com.backbase.productled.config.properties.MarqetaConfigurationProperties;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.JobProfileUser;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.worker.StreamTaskExecutor;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.ContinueSpan;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

/**
 * Orchestrate the business logic to create resources in Marqeta
 */
@Slf4j
@AllArgsConstructor
@Service
public class MarqetaService implements StreamTaskExecutor<LegalEntityTask> {

    private static final String SPAN_ID = "create-marqeta-resources";
    private static final String CARD_HOLDER_NAME = "cardHolderName";
    private static final String NAME = "name";

    private static final String CARD_TYPE_PROPERTY = "cardType";

    private final UsersApi usersApi;
    private final CardsApi cardsApi;
    private final VelocityControlsApi velocityControlsApi;
    private final CardProductsApi cardProductsApi;
    private final MarqetaConfigurationProperties marqetaConfig;
    private final EnvironmentConfigurationProperties environmentConfiguration;

    @Override
    public Mono<LegalEntityTask> rollBack(LegalEntityTask legalEntityTask) {
        return null;
    }

    @NewSpan
    @Override
    public Mono<LegalEntityTask> executeTask(@SpanTag(value = "streamTask") LegalEntityTask streamTask) {
        if (marqetaConfig.isBootstrapResourcesEnabled()) {
            return createMarqetaResources(streamTask);
        } else {
            log.warn("Flag 'marqeta.bootstrapResourcesEnabled' is set to false. Skipping resource creation in Marqeta");
            return Mono.just(streamTask);
        }
    }

    @ContinueSpan(log = SPAN_ID)
    private Mono<LegalEntityTask> createMarqetaResources(LegalEntityTask task) {
        task.info("marqeta", "create", "", task.getData().getExternalId(), null,
            "Creating Marqeta resources for Legal Entity with External ID: %s", task.getData().getExternalId());
        modifyMarqetaConfiguration();
        processSubsidiaries(task.getData().getSubsidiaries());
        return Mono.just(task);
    }

    private void modifyMarqetaConfiguration() {
        CardUsersProperties cardUsersProperties = marqetaConfig.getCardUsers();
        cardUsersProperties.setDebitCard(replaceUserIds(cardUsersProperties.getDebitCard()));
        cardUsersProperties.setCreditCard(replaceUserIds(cardUsersProperties.getCreditCard()));
    }

    private List<String> replaceUserIds(List<String> userNames) {
        return userNames.stream()
                .map(userName -> userName.replace("$installation", environmentConfiguration.getInstallation()))
                .map(userName -> userName.replace("$runtime", environmentConfiguration.getRuntime()))
                .collect(Collectors.toList());
    }

    private void processSubsidiaries(List<LegalEntity> subsidiaries) {
        Optional.ofNullable(subsidiaries)
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .forEach(legalEntity -> {
                if (CUSTOMER.equals(legalEntity.getLegalEntityType())) {
                    lookupAndCreateUser(legalEntity);
                    CardType cardType = getCardType(legalEntity.getExternalId());
                    if (!getCardLinkedToUser(legalEntity.getExternalId(), cardType).isPresent()) {
                        log.info("Creating {} card for user {}", cardType, legalEntity.getExternalId());
                        createAndLinkCardToUser(legalEntity, cardType);
                    }
                }
                processSubsidiaries(legalEntity.getSubsidiaries());
            });

    }

    private void lookupAndCreateUser(LegalEntity legalEntity) {
        try {
            usersApi.getUsersToken(legalEntity.getExternalId(), null);
            log.info("Marqeta User {} already exist", legalEntity.getExternalId());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                log.error("Exception while calling Marqeta User api", e);
                return;
            }
            log.info("Creating Marqeta User {} as it is not found, ", legalEntity.getExternalId());
            usersApi.postUsers(
                new CardHolderModel()
                    .active(true)
                    .token(legalEntity.getExternalId())
                    .firstName(legalEntity.getName()));
            log.info("Marqeta User {} created successfully", legalEntity.getExternalId());
        }
    }

    private CardType getCardType(String userId) {
        boolean hasDebitCard = marqetaConfig.getCardUsers().getDebitCard().contains(userId);
        boolean hasCreditCard = marqetaConfig.getCardUsers().getCreditCard().contains(userId);

        if (hasDebitCard) {
            if (hasCreditCard) {
                log.warn("User {} has been configured to have both `debit` and `credit` card, but will have only `debit` card created", userId);
            }
            return CardType.DEBIT;
        }
        if (hasCreditCard) {
            return CardType.CREDIT;
        }

        CardType defaultCardType = CardType.DEFAULT;
        log.warn("User {} hasn't been configured to have any card type, choosing to create `{}` card as a default",
                userId,
                defaultCardType.type.toLowerCase()
        );
        return defaultCardType;
    }

    private Optional<CardResponse> getCardLinkedToUser(String userToken, CardType cardType) {
        return requireNonNull(cardsApi.getCardsUserToken(userToken, 100, 0, "token,state,metadata,type", null).getData())
            .stream()
            .filter(cardResponse -> cardType.equalsIgnoreCase(cardResponse.getMetadata().get("type")))
            .filter(cardResponse -> Arrays.asList(StateEnum.UNACTIVATED, StateEnum.SUSPENDED, StateEnum.ACTIVE)
                .contains(cardResponse.getState()))
            .findFirst();
    }

    private void createAndLinkCardToUser(LegalEntity legalEntity, CardType cardType) {
        CardConfigurationProperties cardConfiguration = getConfigurationForCardType(cardType);
        String cardProductToken = createCardProduct(cardConfiguration);
        createCardProductLimit(cardProductToken, cardConfiguration);
        createNewCard(legalEntity, cardProductToken, cardConfiguration);
    }

    private CardConfigurationProperties getConfigurationForCardType(CardType cardType) {
        if (cardType == CardType.DEBIT) {
            return marqetaConfig.getDebitCard();
        }
        if (cardType == CardType.CREDIT) {
            return marqetaConfig.getCreditCard();
        }
        throw new IllegalStateException("Unsupported card type " + cardType);
    }

    private String createCardProduct(CardConfigurationProperties cardConfiguration) {
        return cardProductsApi
            .postCardproducts(new CardProductRequest().startDate(LocalDate.now().toString())
                .name(cardConfiguration.getName())
                .config(new CardProductConfig()
                    .cardLifeCycle(new CardLifeCycle().activateUponIssue(false))
                    .fulfillment(new CardProductFulfillment().paymentInstrument(PaymentInstrumentEnum.VIRTUAL_PAN))
                    .poi(new Poi().ecommerce(true).atm(true)))).getToken();
    }

    private void createCardProductLimit(String cardProductToken, CardConfigurationProperties cardConfiguration) {
        Arrays.asList(true, false)
            .forEach(includeWithdrawal -> velocityControlsApi.postVelocitycontrols(
                new VelocityControlRequest()
                    .usageLimit(cardConfiguration.getUsageLimit())
                    .amountLimit(cardConfiguration.getAmountLimit())
                    .velocityWindow(VelocityWindowEnum.DAY)
                    .association(new SpendControlAssociation().cardProductToken(cardProductToken))
                    .currencyCode(cardConfiguration.getCurrencyCode())
                    .includePurchases(true)
                    .includeTransfers(true)
                    .active(true)
                    .includeWithdrawals(includeWithdrawal)));
    }

    private void createNewCard(LegalEntity legalEntity, String cardProductToken, CardConfigurationProperties cardConfiguration) {
        CardRequest cardRequest = new CardRequest()
            .cardProductToken(cardProductToken)
            .userToken(legalEntity.getExternalId())
            .expedite(false)
            .putMetadataItem(CARD_HOLDER_NAME, legalEntity.getName())
            .putMetadataItem(NAME, legalEntity.getName().concat(cardConfiguration.getName()));
        cardConfiguration.getMetaData().forEach(cardRequest::putMetadataItem);
        cardsApi.postCards(false, false, cardRequest);
    }

    private enum CardType {
        DEBIT("Debit"),
        CREDIT("Credit");

        final static CardType DEFAULT = DEBIT;

        private final String type;

        CardType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        boolean equalsIgnoreCase(String other) {
            return type.equalsIgnoreCase(other);
        }

    }

}
