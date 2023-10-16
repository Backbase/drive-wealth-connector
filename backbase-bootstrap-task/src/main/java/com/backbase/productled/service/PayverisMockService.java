package com.backbase.productled.service;

import com.backbase.payveris.mock.clients.api.MockApi;
import com.backbase.payveris.mock.clients.model.AccountModel;
import com.backbase.payveris.mock.clients.model.AddressModel;
import com.backbase.payveris.mock.clients.model.ProfileHolderModel;
import com.backbase.payveris.mock.clients.model.ProfileModel;
import com.backbase.productled.config.properties.PayverisMockConfigurationProperties;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.legalentity.model.BaseProductGroup;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.worker.StreamTaskExecutor;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@AllArgsConstructor
@Service
public class PayverisMockService implements StreamTaskExecutor<LegalEntityTask> {

    private static final String TYPE_CHECKING = "Checking";
    private static final String TYPE_SAVING = "Savings";

    private final MockApi mockApi;
    private final PayverisMockConfigurationProperties mockConfig;

    @Override
    public Mono<LegalEntityTask> executeTask(LegalEntityTask legalEntityTask) {
        if (Boolean.TRUE.equals(mockConfig.getBootstrapPayverisMockEnabled())) {
            return resetData(legalEntityTask)
                .flatMap(this::createUsers);
        } else {
            log.warn("Flag 'payveris.bootstrapPayverisMockEnabled' is set to false. Skipping adding profile.");
        }
        return Mono.just(legalEntityTask);
    }

    private Mono<LegalEntityTask> resetData(LegalEntityTask legalEntityTask) {
        return mockApi.resetData()
            .thenReturn(legalEntityTask)
            .doOnSuccess(task -> log.info("The data deleted in payveris-mock."));
    }

    private Mono<LegalEntityTask> createUsers(LegalEntityTask legalEntityTask) {
        return Flux.fromIterable(legalEntityTask.getData().getSubsidiaries())
            .flatMapIterable(LegalEntity::getSubsidiaries)
            .map(this::buildProfileModel)
            .map(profileModel -> new ProfileHolderModel().profile(profileModel))
            .flatMap(this::addProfile)
            .then(Mono.just(legalEntityTask));
    }

    private Mono<Void> addProfile(ProfileHolderModel requestModel) {
        var externalId = requestModel.getProfile().getUserCode().replace("_", "");
        return mockApi.addProfile(externalId, requestModel)
            .doOnSuccess(p -> log.info("Profile created in paveris-mock for user {}", externalId));
    }

    private ProfileModel buildProfileModel(LegalEntity legalEntity) {
        var externalId = legalEntity.getExternalId().toLowerCase();
        var checkingAccountStream = legalEntity.getProductGroups()
            .stream()
            .map(BaseProductGroup::getCurrentAccounts)
            .flatMap(Collection::stream)
            .map(currentAccount -> new AccountModel()
                .accountNumber(currentAccount.getExternalId())
                .type(TYPE_CHECKING));

        var savingAccountStream = legalEntity.getProductGroups()
            .stream()
            .map(BaseProductGroup::getSavingAccounts)
            .flatMap(Collection::stream)
            .map(savingAccount -> new AccountModel()
                .accountNumber(savingAccount.getExternalId())
                .type(TYPE_SAVING));

        var accounts = Stream.concat(checkingAccountStream, savingAccountStream)
            .collect(Collectors.toList());

        return new ProfileModel()
            .firstName(externalId)
            .lastName(externalId)
            .emailAddresses(List.of(externalId + "@backbase-mambu.com"))
            .address(new AddressModel())
            .accounts(accounts)
            .customerCode("_" + externalId)
            .userCode("_" + externalId);
    }

    @Override
    public Mono<LegalEntityTask> rollBack(LegalEntityTask legalEntityTask) {
        return null;
    }
}
