package com.backbase.productled.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Marqeta Card config properties
 */
@Component
@ConfigurationProperties(prefix = "card-users")
@Data
public class CardUsersProperties {
    private List<String> debitCard;
    private List<String> creditCard;
}
