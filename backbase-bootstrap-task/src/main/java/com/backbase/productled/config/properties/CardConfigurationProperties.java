package com.backbase.productled.config.properties;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Marqeta Card config properties
 */
@Component
@ConfigurationProperties(prefix = "card")
@Data
public class CardConfigurationProperties {

    private String name;
    private String currencyCode;
    private Integer usageLimit;
    private BigDecimal amountLimit;
    private Map<String, String> metaData;
}
