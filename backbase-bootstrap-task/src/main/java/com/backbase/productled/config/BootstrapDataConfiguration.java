package com.backbase.productled.config;

import com.backbase.dbs.limit.api.client.v2.model.UpsertGlobalLimitsRequestBody;
import com.backbase.productled.config.properties.BootstrapDataConfigurationProperties;
import com.backbase.productled.model.AdminApsModel;
import com.backbase.productled.model.ContentRepositoryItem;
import com.backbase.productled.model.FidoApplication;
import com.backbase.productled.utils.FileUtils;
import com.backbase.stream.approval.model.Approval;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.portfolio.model.AggregatePortfolio;
import com.backbase.stream.portfolio.model.InstrumentBundle;
import com.backbase.stream.portfolio.model.PortfolioBundle;
import com.backbase.stream.portfolio.model.WealthAssetBundle;
import com.backbase.stream.portfolio.model.WealthPositionsBundle;
import com.backbase.stream.portfolio.model.WealthRegionsBundle;
import com.backbase.stream.portfolio.model.WealthTransactionCategoriesBundle;
import com.backbase.stream.productcatalog.model.ProductCatalog;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
@Data
@NoArgsConstructor
@Slf4j
public class BootstrapDataConfiguration {

    @Bean
    public List<LegalEntity> legalEntity(ObjectMapper mapper,
                                         BootstrapDataConfigurationProperties configurationProperties) {
        return configurationProperties.getLegalEntity().getLocation().stream()
                .map(location -> {
                    try {
                        log.info("Parsing LEH {}",location);
                        return mapper.readValue(ResourceUtils.getFile(location), LegalEntity.class);
                    } catch (IOException e) {
                        log.error("Error loading Legal Entity hierarchy JSON {} : File not found in classpath", location,
                                e);
                    throw new RuntimeException("Unable to load LE hierarchy file: " + location, e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Bean
    public List<ProductCatalog> productCatalog(ObjectMapper mapper,
                                               BootstrapDataConfigurationProperties configurationProperties) {
        return configurationProperties.getProductCatalog().getLocation().stream()
                .map(location -> {
                    try {
                        return mapper.readValue(ResourceUtils.getFile(location), ProductCatalog.class);
                    } catch (IOException e) {
                        log.error("Error loading Product Catalog JSON {} : File not found in classpath", location, e);
                    throw new RuntimeException("Unable to load Product Catalog file: " + location, e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Bean
    public List<UpsertGlobalLimitsRequestBody> upsertGlobalLimitsRequestBody(ObjectMapper mapper,
                                                                             BootstrapDataConfigurationProperties configurationProperties) {
        return configurationProperties.getGlobalLimits().getLocation().stream()
                .map(location -> {
                    try {
                        return mapper.readValue(ResourceUtils.getFile(location), UpsertGlobalLimitsRequestBody.class);
                    } catch (IOException e) {
                        log.error("Error loading Global Limits JSON {} : File not found in classpath", location, e);
                    throw new RuntimeException("Unable to load Global Limits file: " + location, e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Bean
    public List<Approval> approval(ObjectMapper mapper, BootstrapDataConfigurationProperties configurationProperties) {
        return configurationProperties.getApprovals().getLocation().stream()
                .map(location -> {
                    try {
                        return mapper.readValue(ResourceUtils.getFile(location), Approval.class);
                    } catch (IOException e) {
                        log.error("Error loading Approval JSON {} : File not found in classpath", location, e);
                    throw new RuntimeException("Unable to load Approval file: " + location, e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Bean
    public List<FidoApplication> fidoApplications(BootstrapDataConfigurationProperties configurationProperties) {
        return configurationProperties.getFidoApplications().getApplications();
    }


    @Bean
    public List<AdminApsModel> adminApsPermissionsSet(ObjectMapper mapper, BootstrapDataConfigurationProperties configurationProperties) {
        return configurationProperties.getAdminAps().getLocation().stream()
                .map(location -> {
                    try {
                        return mapper.readValue(ResourceUtils.getFile(location), AdminApsModel.class);
                    } catch (IOException e) {
                        log.error("Error loading Admin APS Application JSON {} : File not found in classpath", location);
                    throw new RuntimeException("Unable to load Admin APS file: " + location, e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Bean
    public List<ContentRepositoryItem> contentRepositoryItems(BootstrapDataConfigurationProperties configurationProperties) {
        return configurationProperties.getContentRepository().getLocation().stream()
                .map(location -> {
                    try {
                        File repoFile = ResourceUtils.getFile(location);
                        Set<String> subFiles = FileUtils.listFilesUsingFilesList(repoFile.getPath());
                        return ContentRepositoryItem.builder()
                                .repositoryId(repoFile.getName())
                                .templates(FileUtils.getContentTemplate(repoFile.getName(), subFiles))
                                .build();
                    } catch (IOException e) {
                    log.error("Error loading Content Repositories from location {} : Files not found in classpath",
                        location);
                    throw new RuntimeException("Unable to load Content Repositories file: " + location, e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Bean
    public List<WealthAssetBundle> wealthAssetBundles(ObjectMapper mapper,
        BootstrapDataConfigurationProperties configurationProperties) {

        return configurationProperties.getAssetClasses().getLocation().stream()
            .map(location -> {
                try {
                    return mapper.readValue(ResourceUtils.getFile(location), WealthAssetBundle.class);
                } catch (IOException e) {
                    log.error("Error loading asset-classes Application JSON {} : File not found in classpath",
                        location);
                    return null;
                }
            })
            .toList();
    }

    @Bean
    public List<WealthRegionsBundle> regions(ObjectMapper mapper,
        BootstrapDataConfigurationProperties configurationProperties) {

        return configurationProperties.getRegions().getLocation().stream()
            .map(location -> {
                try {
                    return mapper.readValue(ResourceUtils.getFile(location), WealthRegionsBundle.class);
                } catch (IOException e) {
                    log.error("Error loading Region Application JSON {} : File not found in classpath", location);
                    return null;
                }
            })
            .toList();
    }

    @Bean
    public List<WealthTransactionCategoriesBundle> transactionCategories(ObjectMapper mapper,
        BootstrapDataConfigurationProperties configurationProperties) {

        return configurationProperties.getTransactionCategories().getLocation().stream()
            .map(location -> {
                try {
                    return mapper.readValue(ResourceUtils.getFile(location), WealthTransactionCategoriesBundle.class);
                } catch (IOException e) {
                    log.error("Error loading transaction-categories Application JSON {} : File not found in classpath",
                        location);
                    return null;
                }
            })
            .toList();
    }

    @Bean
    public List<WealthPositionsBundle> positionBundles(ObjectMapper mapper,
        BootstrapDataConfigurationProperties configurationProperties) {

        return configurationProperties.getPositionBundles().getLocation().stream()
            .map(location -> {
                try {
                    return mapper.readValue(ResourceUtils.getFile(location), WealthPositionsBundle.class);
                } catch (IOException e) {
                    log.error("Error loading Portfolio poitions JSON {} : File not found in classpath", location);
                    return null;
                }
            })
            .toList();
    }

    @Bean
    public List<PortfolioBundle> portfolioBundles(ObjectMapper mapper,
        BootstrapDataConfigurationProperties configurationProperties) {

        return configurationProperties.getPortfolioBundles().getLocation().stream()
            .map(location -> {
                try {
                    return Arrays.stream(mapper.readValue(ResourceUtils.getFile(location), PortfolioBundle[].class));
                } catch (IOException e) {
                    log.error("Error loading Portfolio Hierarchy bundle JSON {} : File not found in classpath",
                        location);
                    return null;
                }
            }).filter(Objects::nonNull)
            .flatMap(portfolioBundleStream -> portfolioBundleStream)
            .toList();
    }

    @Bean
    public List<AggregatePortfolio> aggregatePortfolios(ObjectMapper mapper,
        BootstrapDataConfigurationProperties configurationProperties) {

        return configurationProperties.getAggregatePortfolios().getLocation().stream()
            .map(location -> {
                try {
                    return Arrays.stream(mapper.readValue(ResourceUtils.getFile(location), AggregatePortfolio[].class));
                } catch (IOException e) {
                    log.error("Error loading Portfolio Hierarchy bundle JSON {} : File not found in classpath",
                        location);
                    return null;
                }
            }).filter(Objects::nonNull)
            .flatMap(aggregatePortfolioStream -> aggregatePortfolioStream)
            .toList();
    }

    @Bean
    public List<InstrumentBundle> instrumentBundles(ObjectMapper mapper,
        BootstrapDataConfigurationProperties configurationProperties) {

        return configurationProperties.getInstrumentBundles().getLocation().stream()
            .map(location -> {
                try {
                    return Arrays.stream(mapper.readValue(ResourceUtils.getFile(location), InstrumentBundle[].class));
                } catch (IOException e) {
                    log.error("Error loading Portfolio Hierarchy bundle JSON {} : File not found in classpath",
                        location);
                    return null;
                }
            }).filter(Objects::nonNull)
            .flatMap(instrumentBundleStream -> instrumentBundleStream)
            .toList();
    }
}
