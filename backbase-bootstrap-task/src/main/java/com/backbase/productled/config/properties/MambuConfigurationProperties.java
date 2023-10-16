package com.backbase.productled.config.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Configuration properties for integrating with Mambu
 */
@Component
@ConfigurationProperties(prefix = "mambu")
@Data
@Slf4j
public class MambuConfigurationProperties {

    private boolean bootstrapResourcesEnabled = false;
    private String baseUrl;
    private String username;
    private String password;
    private String currentAccountProductKey;
    private String savingsAccountProductKey;
    private String creditCardAccountProductKey;
    private String mortgageProductKey;
    private String lineOfCreditProductKey;
    private String termLoanProductKey;
    private String branchKey;
    // This account will be used as beneficiary for the random transactions
    private String transactionsBeneficiaryAccountKey;
    private boolean ingestPlacesData;
    private String placesDataFile;
    private Map<String, String> currentAccountProductKeys;
    private Map<String, String> savingsAccountProductKeys;
    private Map<String, String> transactionsBeneficiaryAccountKeys;

    @PostConstruct
    void postConstruct() {
        log.debug("currentAccountProductKeys : {}", currentAccountProductKeys);
        log.debug("savingsAccountProductKeys : {}", savingsAccountProductKeys);
        log.debug("transactionsBeneficiaryAccountKeys : {}", transactionsBeneficiaryAccountKeys);
    }

}
