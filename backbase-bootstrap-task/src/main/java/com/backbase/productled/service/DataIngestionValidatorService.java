package com.backbase.productled.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.backbase.dbs.accesscontrol.api.service.v3.model.FunctionGroupItem;
import com.backbase.dbs.arrangement.api.service.v2.model.AccountArrangementItem;
import com.backbase.dbs.user.api.service.v2.model.GetUser;
import com.backbase.dbs.user.api.service.v2.model.GetUsersList;
import com.backbase.stream.legalentity.model.JobProfileUser;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.ProductGroup;
import com.backbase.stream.legalentity.model.ReferenceJobRole;
import com.backbase.stream.legalentity.model.ServiceAgreement;
import com.backbase.stream.legalentity.model.User;
import com.backbase.stream.product.service.ArrangementService;
import com.backbase.stream.productcatalog.ProductCatalogService;
import com.backbase.stream.productcatalog.model.ProductCatalog;
import com.backbase.stream.productcatalog.model.ProductKind;
import com.backbase.stream.productcatalog.model.ProductType;
import com.backbase.stream.service.AccessGroupService;
import com.backbase.stream.service.LegalEntityService;
import com.backbase.stream.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Validate that all the provided resources (LEs, Job Roles, Users...) were successfully ingested in DBS
 */
@Slf4j
@AllArgsConstructor
@Service
public class DataIngestionValidatorService {

    private final UserService userService;
    private final AccessGroupService accessGroupService;
    private final LegalEntityService legalEntityService;
    private final ProductCatalogService productCatalogService;
    private final ArrangementService arrangementService;

    public void validateIngestedData(LegalEntity rootLegalEntity) {

        log.info("-------- STARTING DATA VALIDATION --------");
        LegalEntity createdLegalEntity = legalEntityService
            .getLegalEntityByExternalId(rootLegalEntity.getExternalId()).block();
        ServiceAgreement createdMsa = null;
        if (createdLegalEntity != null) {
            createdMsa = legalEntityService
                .getMasterServiceAgreementForInternalLegalEntityId(createdLegalEntity.getInternalId()).block();
        }

        boolean missingDataRootLegalEntity = validateRootLegalEntity(rootLegalEntity, createdLegalEntity, createdMsa);
        if (!missingDataRootLegalEntity) {
            validateFunctionGroups(createdMsa, rootLegalEntity);
            ofNullable(rootLegalEntity.getSubsidiaries()).ifPresent(subsidiaries ->  subsidiaries.forEach(this::validateLegalEntity));
        }
        log.info("-------- FINISHED DATA VALIDATION --------");
    }

    public void validateIngestedData(ProductCatalog productCatalog) {
        if (productCatalog != null) {
            validateProductCatalog(productCatalog);
        }
    }

    /**
     * Validate if the provided Root Legal Entity and it's linked resources were created successfully
     *
     * @param rootLegalEntity provided for ingestion
     * @param createdLegalEntity retrieved from DBS
     * @param createdMsa retrieved from DBS
     * @return if any expected data is missing/not ingested
     */
    private boolean validateRootLegalEntity(LegalEntity rootLegalEntity, LegalEntity createdLegalEntity,
        ServiceAgreement createdMsa) {

        boolean missingData = false;

        if (createdLegalEntity == null || createdLegalEntity.getExternalId() == null) {
            log.error("Validation: Root Legal Entity was not created");
            return true;
        } else {
            log.info("Validation: Root Legal Entity created successfully");
        }

        if (createdMsa == null || createdMsa.getExternalId() == null) {
            missingData = true;
            log.error("Validation: MSA for Root Legal Entity was not created");
        } else {
            log.info("Validation: MSA for Root Legal Entity created successfully");
        }

        GetUsersList userList = userService
            .getUsersByLegalEntity(createdLegalEntity.getInternalId(), 10, 0)
            .block();
        if (userList == null || userList.getUsers() == null || userList.getUsers().isEmpty()) {
            log.error("Validation: Admins/users for Root Legal Entity were not created");
            return true;
        }
        List<String> createdUserIds = userList.getUsers().stream().map(GetUser::getExternalId)
            .collect(Collectors.toList());
        if (createdUserIds.containsAll(
            rootLegalEntity.getAdministrators().stream().map(User::getExternalId).collect(Collectors.toList()))) {
            log.info("Validation: All admin users for Root Legal Entity were created successfully");
        } else {
            missingData = true;
            log.error("Validation: Missing admin users for Root Legal Entity. Created: {} - Expected: {}",
                createdUserIds,
                rootLegalEntity.getAdministrators().stream().map(User::getExternalId).collect(Collectors.toList()));
        }
        if (createdUserIds.containsAll(
            ofNullable(rootLegalEntity.getUsers()).orElse(emptyList()).stream()
                .map(JobProfileUser::getUser).map(User::getExternalId)
                .collect(Collectors.toList()))) {
            log.info("Validation: All users for Root Legal Entity were created successfully");
        } else {
            missingData = true;
            log.error("Validation: Missing users for Root Legal Entity. Created: {} - Expected: {}",
                createdUserIds,
                rootLegalEntity.getUsers().stream().map(JobProfileUser::getUser).map(User::getExternalId)
                    .collect(Collectors.toList()));
        }

        return missingData;
    }

    /**
     * Validate if all Function Groups were created from the provided Reference Job Roles
     *
     * @param rootLegalEntity provided for ingestion
     * @param createdMsa retrieved from DBS
     * @return if any expected data is missing/not ingested
     */
    private boolean validateFunctionGroups(ServiceAgreement createdMsa,
        LegalEntity rootLegalEntity) {

        boolean missingData = false;
        List<FunctionGroupItem> createdFunctionGroups = accessGroupService
            .getFunctionGroupsForServiceAgreement(createdMsa.getInternalId()).block();

        if (createdFunctionGroups == null || createdFunctionGroups.isEmpty()) {
            log.error("Validation: Function groups were not created");
            return true;
        }
        List<String> createdFunctionGroupNames = createdFunctionGroups.stream().map(FunctionGroupItem::getName)
            .collect(Collectors.toList());
        List<String> referenceJobRoleNames = ofNullable(rootLegalEntity.getReferenceJobRoles()).orElse(emptyList()).stream()
            .map(ReferenceJobRole::getName)
            .collect(Collectors.toList());
        if (createdFunctionGroupNames.containsAll(referenceJobRoleNames)) {
            log.info("Validation: All function groups templated from Reference Job Roles were created successfully");
        } else {
            missingData = true;
            log.error(
                "Validation: Missing function groups templated from Reference Job Roles. Created: {} - Expected: {}",
                createdFunctionGroupNames, referenceJobRoleNames);
        }

        return missingData;
    }

    /**
     * Validate if all Product Types and Product Kinds were created successfully
     *
     * @param productCatalog provided for ingestion
     * @return if any expected data is missing/not ingested
     */
    private boolean validateProductCatalog(ProductCatalog productCatalog) {

        boolean missingData = false;
        ProductCatalog createdProductCatalog = productCatalogService.getProductCatalog();
        List<String> createdProductKindIds = createdProductCatalog.getProductKinds().stream()
            .map(ProductKind::getExternalKindId).collect(Collectors.toList());
        List<String> createdProductTypeIds = createdProductCatalog.getProductTypes().stream()
            .map(ProductType::getExternalId).collect(Collectors.toList());

        List<String> providedProductKindIds = new ArrayList<>();
        if (productCatalog.getProductKinds() != null) {
            providedProductKindIds = productCatalog.getProductKinds().stream()
                .map(ProductKind::getExternalKindId)
                .collect(Collectors.toList());
        }
        List<String> providedProductTypeIds = new ArrayList<>();
        if (productCatalog.getProductTypes() != null) {
            providedProductTypeIds = productCatalog.getProductTypes().stream().map(ProductType::getExternalId)
                .collect(Collectors.toList());
        }

        if (createdProductKindIds.isEmpty() || !createdProductKindIds.containsAll(providedProductKindIds)) {
            missingData = true;
            log.error("Validation: Product Kinds were not created. Created: {} - Expected: {}",
                createdProductKindIds, providedProductKindIds);
        } else {
            log.info("Validation: All Product Kinds were created successfully");
            log.info("Created Product Kinds: {}", createdProductKindIds);
        }
        /*
        if (createdProductTypeIds.isEmpty() || !createdProductTypeIds.containsAll(providedProductTypeIds)) {
            missingData = true;
            log.error("Validation: Product Types were not created. Created: {} - Expected: {}",
                createdProductTypeIds, providedProductTypeIds);
        } else {
            log.info("Validation: All Product Types were created successfully");
            log.info("Created Product Types: {}", createdProductTypeIds);
        }
         */
        return missingData;
    }

    private void validateLegalEntity(LegalEntity legalEntity) {

        LegalEntity createdLegalEntity = legalEntityService.getLegalEntityByExternalId(legalEntity.getExternalId())
            .block();
        if (createdLegalEntity == null || createdLegalEntity.getInternalId() == null
            || createdLegalEntity.getLegalEntityType() == null) {
            log.error("Validation: Legal Entity {} was not created or is missing required data",
                legalEntity.getExternalId());
            return;
        } else {
            log.info("Validation: Legal Entity {} was created successfully", legalEntity.getExternalId());
        }
        if (legalEntity.getUsers() != null) {
            legalEntity.getUsers().forEach(this::validateUser);
        }
        if (legalEntity.getProductGroups() != null) {
            legalEntity.getProductGroups().forEach(this::validateProductGroup);
        }
        if (legalEntity.getSubsidiaries() != null) {
            legalEntity.getSubsidiaries().forEach(this::validateLegalEntity);
        }
    }

    private void validateUser(JobProfileUser user) {

        User createdUser = userService.getUserByExternalId(user.getUser().getExternalId()).block();
        if (createdUser == null || createdUser.getInternalId() == null) {
            log.error("Validation: User {} was not created",
                user.getUser().getExternalId());
        } else {
            log.info("Validation: User {} was created successfully", user.getUser().getExternalId());
        }
    }

    private void validateProductGroup(ProductGroup productGroup) {

        Optional.ofNullable(productGroup.getCurrentAccounts()).ifPresent(ca -> ca.forEach(currentAccount -> {
            AccountArrangementItem createdArrangement = arrangementService.getArrangementByExternalId(currentAccount.getExternalId())
                .block();
            if (createdArrangement == null || createdArrangement.getId() == null) {
                log.error("Validation: Arrangement {} was not created",
                    currentAccount.getExternalId());
                return;
            } else {
                log.info("Validation: Arrangement {} was created successfully", currentAccount.getExternalId());
            }
        }));
        Optional.ofNullable(productGroup.getSavingAccounts()).ifPresent(sas -> sas.forEach(savingsAccount -> {
            AccountArrangementItem createdArrangement = arrangementService.getArrangementByExternalId(savingsAccount.getExternalId())
                .block();
            if (createdArrangement == null || createdArrangement.getId() == null) {
                log.error("Validation: Arrangement {} was not created",
                    savingsAccount.getExternalId());
                return;
            } else {
                log.info("Validation: Arrangement {} was created successfully", savingsAccount.getExternalId());
            }
        }));
    }

}
