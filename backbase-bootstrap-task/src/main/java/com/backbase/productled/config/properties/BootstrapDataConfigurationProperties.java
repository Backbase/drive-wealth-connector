package com.backbase.productled.config.properties;

import java.util.List;

import com.backbase.productled.model.FidoApplication;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for data file location
 */
@Data
@Component
@ConfigurationProperties(prefix = "backbase.stream.bootstrap")
public class BootstrapDataConfigurationProperties {

    private LegalEntity legalEntity;
    private ProductCatalog productCatalog;
    private GlobalLimits globalLimits;
    private Approvals approvals;
    private FidoApplications fidoApplications;
    private AdminAps adminAps;
    private Message message;
    private ContentRepository contentRepository;
    private PushIntegration pushIntegration;
    private AssetClasses assetClasses;
    private Regions regions;
    private TransactionCategories transactionCategories;
    private PortfolioBundles portfolioBundles;
    private AggregatePortfolios aggregatePortfolios;
    private PositionBundles positionBundles;
    private InstrumentBundles instrumentBundles;

    public static class LegalEntity extends Base { }
    public static class ProductCatalog extends Base { }
    public static class GlobalLimits extends Base { }
    public static class Approvals extends Base { }
    public static class FidoApplications extends FidoBase { }
    public static class AdminAps extends Base { }
    public static class Message extends MessageBase { }
    public static class ContentRepository extends Base { }
    public static class AssetClasses extends Base { }
    public static class Regions extends Base { }
    public static class TransactionCategories extends Base { }
    public static class PortfolioBundles extends Base { }
    public static class AggregatePortfolios extends Base { }
    public static class PositionBundles extends Base { }
    public static class InstrumentBundles extends Base { }

    @Data
    public static class FidoBase {
        private boolean bootstrapResourcesEnabled;
        private List<FidoApplication> applications;
    }

    @Data
    public static class Base {
        private boolean bootstrapResourcesEnabled;
        private List<String> location;
    }

    @Data
    public static class MessageBase {
        private boolean bootstrapResourcesEnabled;
        private String topics;
    }

    @Data
    public static class PushIntegration {
        private boolean bootstrapResourcesEnabled;
        private List<Certificate> certificates;

        @Data
        public static class Certificate {
            private Platform platform;
            private String location;
            private Boolean isProduction;
            private String apnsTopic;
            private String appName;
            /**
             * Optional password of the APNS p12 certificate, not applicable for FCM credentials
             */
            private String password;
        }

        public enum Platform {
            ANDROID, IOS;
        }
    }

}
