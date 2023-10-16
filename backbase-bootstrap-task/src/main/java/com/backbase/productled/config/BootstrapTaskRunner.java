package com.backbase.productled.config;

import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.hasLength;

import com.backbase.dbs.limit.api.client.v2.model.UpsertGlobalLimitsRequestBody;
import com.backbase.productled.config.properties.BootstrapDataConfigurationProperties;
import com.backbase.productled.model.ContentRepositoryItem;
import com.backbase.productled.model.FidoApplication;
import com.backbase.productled.service.AdminApsService;
import com.backbase.productled.service.AdminService;
import com.backbase.productled.service.ContentRepositoryService;
import com.backbase.productled.service.DataIngestionValidatorService;
import com.backbase.productled.service.DriveWealthService;
import com.backbase.productled.service.EnvironmentDetailsEnricher;
import com.backbase.productled.service.FidoService;
import com.backbase.productled.service.LegalEntityFilterService;
import com.backbase.productled.service.LimitsService;
import com.backbase.productled.service.MambuService;
import com.backbase.productled.service.MarqetaService;
import com.backbase.productled.service.MessageService;
import com.backbase.productled.service.PayverisMockService;
import com.backbase.productled.service.PushIntegrationService;
import com.backbase.stream.ApprovalSaga;
import com.backbase.stream.ApprovalTask;
import com.backbase.stream.LegalEntitySaga;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.approval.model.Approval;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.portfolio.model.AggregatePortfolio;
import com.backbase.stream.portfolio.model.InstrumentBundle;
import com.backbase.stream.portfolio.model.PortfolioBundle;
import com.backbase.stream.portfolio.model.WealthAssetBundle;
import com.backbase.stream.portfolio.model.WealthPositionsBundle;
import com.backbase.stream.portfolio.model.WealthRegionsBundle;
import com.backbase.stream.portfolio.model.WealthTransactionCategoriesBundle;
import com.backbase.stream.portfolio.service.InstrumentIngestionService;
import com.backbase.stream.portfolio.service.PortfolioIngestionService;
import com.backbase.stream.portfolio.service.PortfolioIntegrationService;
import com.backbase.stream.productcatalog.ProductCatalogService;
import com.backbase.stream.productcatalog.model.ProductCatalog;
import com.backbase.stream.worker.exception.StreamTaskException;
import com.backbase.stream.worker.model.StreamTask;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

/**
 * Create task from legalEntity and call BB stream
 */
@EnableTask
@Configuration
@AllArgsConstructor
@Slf4j
public class BootstrapTaskRunner {

    private final LegalEntityFilterService legalEntityFilterService;
    private final LegalEntitySaga legalEntitySaga;
    private final LimitsService limitsService;
    private final ApprovalSaga approvalSaga;
    private final MambuService mambuService;
    private final MarqetaService marqetaService;
    private final DriveWealthService driveWealthService;
    private final AdminService adminService;
    private final PayverisMockService payverisMockService;
    private final DataIngestionValidatorService validatorService;
    private final ProductCatalogService productCatalogService;
    private final InstrumentIngestionService instrumentIngestionReactiveService;
    private final PortfolioIngestionService portfolioIngestionService;
    private final PortfolioIntegrationService portfolioIntegrationService;
    private final EnvironmentDetailsEnricher environmentDetailsEnricher;
    private final FidoService fidoService;
    private final MessageService messageService;
    private final List<ProductCatalog> productCatalogs;
    private final List<LegalEntity> legalEntities;
    private final List<UpsertGlobalLimitsRequestBody> globalLimits;
    private final List<Approval> approvals;
    private final AdminApsService adminApsService;
    private final List<FidoApplication> fidoApplications;
    private final List<ContentRepositoryItem> contentRepositoryItems;
    private final List<WealthAssetBundle> assets;
    private final List<WealthRegionsBundle> regions;
    private final List<WealthTransactionCategoriesBundle> transactionCategories;
    private final List<PortfolioBundle> portfolioBundles;
    private final List<AggregatePortfolio> aggregatePortfolios;
    private final List<WealthPositionsBundle> positionBundles;
    private final List<InstrumentBundle> instrumentBundles;
    private final ContentRepositoryService contentRepositoryService;
    private final PushIntegrationService pushIntegrationService;
    private final BootstrapDataConfigurationProperties configurationProperties;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return this::run;
    }

    private void run(String... args) {

        ofNullable(assets)
            .filter(wealthAssets -> configurationProperties.getAssetClasses() != null &&
                configurationProperties.getAssetClasses().isBootstrapResourcesEnabled() &&
                configurationProperties.getAssetClasses().getLocation() != null)
            .ifPresentOrElse(this::bootstrapWealthAssetBundle,
                () -> log.info("Wealth asset bundles are disabled in bootstrap config. Skipping creation"));

        ofNullable(regions)
            .filter(regionBundles -> configurationProperties.getRegions() != null &&
                configurationProperties.getRegions().isBootstrapResourcesEnabled() &&
                configurationProperties.getRegions().getLocation() != null)
            .ifPresentOrElse(this::bootstrapRegions,
                () -> log.info("Wealth region are disabled in bootstrap config. Skipping creation"));

        ofNullable(transactionCategories)
            .filter(transactionCategoriesBundle -> configurationProperties.getTransactionCategories() != null &&
                configurationProperties.getTransactionCategories().isBootstrapResourcesEnabled() &&
                configurationProperties.getTransactionCategories().getLocation() != null)
            .ifPresentOrElse(this::bootstrapTransactionCategories,
                () -> log.info("Wealth Transaction Categories are disabled in bootstrap config. Skipping creation"));

        ofNullable(instrumentBundles)
            .filter(transactionCategoriesBundle -> configurationProperties.getInstrumentBundles() != null &&
                configurationProperties.getInstrumentBundles().isBootstrapResourcesEnabled() &&
                configurationProperties.getInstrumentBundles().getLocation() != null)
            .ifPresentOrElse(this::bootstrapInstrumentBundles,
                () -> log.info("Wealth Instruments bundles are disabled in bootstrap config. Skipping creation"));

        ofNullable(fidoApplications)
            .filter(applications1 -> configurationProperties.getFidoApplications() != null
                && configurationProperties.getFidoApplications().isBootstrapResourcesEnabled())
            .ifPresentOrElse(fidoService::bootstrapFidoFacetIds,
                () -> log.info("Fido application not found or disabled in bootstrap config. Skipping creation"));

        ofNullable(productCatalogs)
            .filter(productCatalog1 -> configurationProperties.getProductCatalog() != null &&
                configurationProperties.getProductCatalog().isBootstrapResourcesEnabled())
            .ifPresentOrElse(this::bootstrapProductCatalog,
                () -> log.info("Product Catalog not found or disabled in bootstrap config. Skipping creation"));

        ofNullable(legalEntities)
            .filter(legalEntities1 -> configurationProperties.getLegalEntity() != null &&
                configurationProperties.getLegalEntity().isBootstrapResourcesEnabled())
            .ifPresentOrElse(this::bootstrapLegalEntityHierarchy,
                () -> log.info("Legal Entity not found or disabled in bootstrap config. Skipping creation"));

        ofNullable(configurationProperties.getMessage())
            .filter(messageTopic -> hasLength(messageTopic.getTopics()) && configurationProperties.getMessage().isBootstrapResourcesEnabled())
            .map(s ->  Arrays.asList(s.getTopics().split("\\s*,\\s*")))
            .ifPresentOrElse(messageService::ingestTopics,
                () -> log.info("Message Topics not found or disabled in bootstrap config. Skipping creation"));

        ofNullable(globalLimits)
            .filter(globalLimits1 -> configurationProperties.getGlobalLimits() != null &&
                configurationProperties.getGlobalLimits().isBootstrapResourcesEnabled())
            .ifPresentOrElse(limits -> limits.forEach(limitsService::boostrapGlobalLimits),
                () -> log.info("Global Limits not found or disabled in bootstrap config. Skipping creation"));

        ofNullable(approvals)
            .filter(approvals1 -> configurationProperties.getApprovals() != null &&
                configurationProperties.getApprovals().isBootstrapResourcesEnabled())
            .ifPresentOrElse(this::bootstrapApproval,
                () -> log.info("Approvals not found or disabled in bootstrap config. Skipping creation"));

        ofNullable(contentRepositoryItems)
                .filter(contentRepositoryItems -> configurationProperties.getContentRepository() != null &&
                        configurationProperties.getContentRepository().isBootstrapResourcesEnabled())
                .ifPresentOrElse(this::bootstrapContentRepositories,
                        () -> log.info("Content repository not found or disabled in bootstrap config. Skipping creation"));

        ofNullable(configurationProperties.getPushIntegration().getCertificates())
                .filter(pushCertificates -> configurationProperties.getPushIntegration().isBootstrapResourcesEnabled())
                .ifPresentOrElse(this::bootstrapPushIntegrationCertificates,
                        () -> log.info("Push integration certificate are disabled in bootstrap config. Skipping creation"));

        ofNullable(portfolioBundles)
            .filter(regionBundles -> configurationProperties.getPortfolioBundles() != null &&
                configurationProperties.getPortfolioBundles().isBootstrapResourcesEnabled() &&
                configurationProperties.getPortfolioBundles().getLocation() != null)
            .ifPresentOrElse(this::bootstrapPortfolioHierarchyBundles,
                () -> log.info("Wealth Portfolio Bundles are disabled in bootstrap config. Skipping creation"));

        ofNullable(positionBundles)
            .filter(regionBundles -> configurationProperties.getPositionBundles() != null &&
                configurationProperties.getPositionBundles().isBootstrapResourcesEnabled() &&
                configurationProperties.getPositionBundles().getLocation() != null)
            .ifPresentOrElse(this::bootstrapPortfolioPositionBundles,
                () -> log.info(
                    "Position Bundles are disabled in bootstrap config. Skipping creation"));

        ofNullable(aggregatePortfolios)
            .filter(regionBundles -> configurationProperties.getAggregatePortfolios() != null &&
                configurationProperties.getAggregatePortfolios().isBootstrapResourcesEnabled() &&
                configurationProperties.getAggregatePortfolios().getLocation() != null)
            .ifPresentOrElse(this::bootstrapAggregatePortfolios,
                () -> log.info(
                    "Wealth Aggregate Portfolio are disabled in bootstrap config. Skipping creation"));


        System.exit(0);
    }

    private void bootstrapPushIntegrationCertificates(List<BootstrapDataConfigurationProperties.PushIntegration.Certificate> certificates) {
        log.info("Bootstrapping Push Integration Certificates");

        Flux.fromIterable(certificates)
            .flatMapSequential(pushIntegrationService::bootstrapPushIntegrationCertificate)
                .onErrorContinue((ex, element) -> log.error("Error Bootstrap push integration certificates", ex))
                .collectList()
                .block();

        log.info("Finished bootstrapping Push Integration Certificates");
    }

    private void bootstrapContentRepositories(List<ContentRepositoryItem> contentRepositoryItems) {
        log.info("Bootstrapping Content Repository Templates");

        Flux.fromIterable(contentRepositoryItems)
            .flatMapSequential(contentRepositoryService::bootstrapContentRepository)
                .onErrorContinue((ex, element) -> log.error("Error Bootstrap content templates", ex))
                .collectList()
                .block();

        log.info("Finished bootstrapping Content Repository Structure");
    }

    private void bootstrapApproval(List<Approval> approvals) {
        log.info("Bootstrapping Root Approvals Structure");

        Flux.fromIterable(approvals)
            .map(ApprovalTask::new)
            .flatMapSequential(approvalSaga::executeTask)
            .onErrorContinue((ex, element) -> log.error("Approval errors are suppress on purpose, run approval-cleanup.sh script before bootstrap for re-ingestion of approvals " + ex))
            .collectList()
            .block();

        log.info("Finished bootstrapping Approvals Structure");
    }

    private void bootstrapLegalEntityHierarchy(List<LegalEntity> legalEntities) {
        legalEntities
            .forEach(legalEntity -> {
                log.info("Processing Legal Entity {} with External ID {}", legalEntity.getName(), legalEntity.getExternalId());
                environmentDetailsEnricher.enrich(legalEntity);
                bootstrapLegalEntities(legalEntity);
                validatorService.validateIngestedData(legalEntity);
            });
        adminService.executeTask();
    }

    private void bootstrapLegalEntities(LegalEntity legalEntity) {
        log.info("Bootstrapping Root Legal Entity Structure: {}", legalEntity.getName());
        List<LegalEntity> aggregates = Collections.singletonList(legalEntity);

        Flux.fromIterable(aggregates)
            .map(LegalEntityTask::new)
            .flatMapSequential(driveWealthService::executeTask)
            .flatMapSequential(mambuService::executeTask)
            .flatMapSequential(marqetaService::executeTask)
            .flatMapSequential(legalEntityFilterService::executeTask)
            .flatMapSequential(legalEntitySaga::executeTask)
            .flatMapSequential(adminApsService::executeTask)
            .flatMapSequential(payverisMockService::executeTask)
            .doOnNext(StreamTask::logSummary)
            .doOnError(StreamTaskException.class, throwable -> {
                log.error("Failed to bootstrap legal entities: ", throwable);
                var task = throwable.getTask();
                throwable.getTask().logSummary();
                task.getHistory() // ensure history order by time (parallel execution is possible - BTW, the history list is not thread safe)
                    .sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
                task.getHistory()
                  .forEach(item -> {
                  switch (item.getSeverity()) {
                      case INFO -> log.info(item.toDisplayString());
                      case WARN -> log.warn(item.toDisplayString());
                      case ERROR -> log.error(item.toDisplayString());
                  }
                });
            })
            .collectList()
            .block();

        log.info("Finished bootstrapping Legal Entity Structure");
    }

    private void bootstrapProductCatalog(List<ProductCatalog> productCatalogs) {
        log.info("Bootstrapping Product Catalog");
        productCatalogs.forEach(productCatalog -> {
            productCatalogService.setupProductCatalog(productCatalog);
            validatorService.validateIngestedData(productCatalog);
        });
        log.info("Successfully Bootstrapped Product Catalog");
    }

    private void bootstrapWealthAssetBundle(List<WealthAssetBundle> assets) {
        try {
            log.info("Bootstrapping Wealth Asset Bundles");

            Flux.fromIterable(assets)
                .map(WealthAssetBundle::getAssetClasses)
                .flatMapSequential(assetClasses -> instrumentIngestionReactiveService.ingestWealthAssets(
                    Flux.fromIterable(assetClasses)))
                .onErrorContinue((ex, element) -> log.error("Error Bootstrap Wealth Asset Bundles", ex))
                .collectList()
                .block();

            log.info("Finished bootstrapping Wealth Asset Bundles");
        } catch (Exception e) {
            log.error("Bootstrapping Wealth Asset Bundle Failed: ", e);
        }
    }

    private void bootstrapRegions(List<WealthRegionsBundle> regions) {
        try {
            log.info("Bootstrapping Wealth Regions");

            Flux.fromIterable(regions)
                .map(WealthRegionsBundle::getRegions)
                .flatMapSequential(regionBundles -> instrumentIngestionReactiveService.ingestRegionBundles(
                    Flux.fromIterable(regionBundles)))
                .onErrorContinue((ex, element) -> log.error("Error Bootstrap Wealth Regions", ex))
                .collectList()
                .block();

            log.info("Finished bootstrapping Wealth Regions");
        } catch (Exception e) {
            log.error("Bootstrapping Wealth Regions Failed: ", e);
        }
    }

    private void bootstrapPortfolioHierarchyBundles(List<PortfolioBundle> portfolioBundles) {
        try {
            log.info("Bootstrapping Portfolio Bundles");

            Flux.fromIterable(portfolioBundles)
                .flatMapSequential(portfolioIntegrationService::upsertPortfolio)
                .onErrorContinue(
                    (ex, element) -> log.error("Error Bootstrap Wealth Portfolio Bundles bundle ", ex))
                .collectList()
                .block();

            log.info("Finished bootstrapping Portfolio Bundles");
        } catch (Exception e) {
            log.error("Bootstrapping Portfolio Bundles Failed", e);
        }
    }

    private void bootstrapPortfolioPositionBundles(List<WealthPositionsBundle> positionBundle) {
        try {
            log.info("Bootstrapping Portfolio Positions Bundles");

            Flux.fromIterable(positionBundle)
                .map(WealthPositionsBundle::getPositions)
                .flatMapSequential(positions -> portfolioIngestionService.ingestPositions(Flux.fromIterable(positions)))
                .onErrorContinue(
                    (ex, element) -> log.error("Error Bootstrap Wealth Portfolio Positions Bundles bundle ", ex))
                .collectList()
                .block();

            log.info("Finished bootstrapping Portfolio Positions Bundles");
        } catch (Exception e) {
            log.error("Bootstrapping Positions Bundle Failed", e);
        }
    }

    private void bootstrapAggregatePortfolios(List<AggregatePortfolio> aggregatePortfolios) {
        try {
            log.info("Bootstrapping Aggregate Portfolio");

            Flux.fromIterable(aggregatePortfolios)
                .flatMapSequential(portfolioIntegrationService::createAggregatePortfolio)
                .onErrorContinue(
                    (ex, element) -> log.error("Error Bootstrap Aggregate Portfolio  ", ex))
                .collectList()
                .block();

            log.info("Finished bootstrapping Aggregate Portfolio");
        } catch (Exception e) {
            log.error("Bootstrapping Aggregate Portfolio Failed", e);
        }
    }

    private void bootstrapTransactionCategories(List<WealthTransactionCategoriesBundle> transactionCategories) {
        try {
            log.info("Bootstrapping Wealth Transaction Categories");

            Flux.fromIterable(transactionCategories)
                .map(WealthTransactionCategoriesBundle::getTransactionCategories)
                .flatMapSequential(transactionCategoriesBundle -> portfolioIngestionService.ingestTransactionCategories(
                    Flux.fromIterable(transactionCategoriesBundle)))
                .onErrorContinue((ex, element) -> log.error("Error Bootstrap Wealth Transaction Categories", ex))
                .collectList()
                .block();

            log.info("Finished bootstrapping Wealth Transaction Categories");
        } catch (Exception e) {
            log.error("Bootstrapping Wealth Transaction Categories Failed: ", e);
        }
    }

    private void bootstrapInstrumentBundles(List<InstrumentBundle> instrumentBundles) {
        log.info("Bootstrapping Wealth Instrument Bundles");

        instrumentIngestionReactiveService.ingestInstruments(Flux.fromIterable(instrumentBundles))
            .onErrorContinue((ex, element) -> log.error("Error Bootstrap  Wealth Instrument Bundles", ex))
            .collectList()
            .block();

        log.info("============= Finished bootstrapping Wealth Instrument Structure =============");
    }

}
