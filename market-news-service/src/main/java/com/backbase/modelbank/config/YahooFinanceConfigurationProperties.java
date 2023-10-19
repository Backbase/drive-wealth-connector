package com.backbase.modelbank.config;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsFeed;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(value = "yahoo.finance")
@Validated
public class YahooFinanceConfigurationProperties {

    /**
     * Yahoo Finance News Base URL
     */
    @NotBlank
    private String baseUrl;
    /**
     * Configurable Market News Feeds
     */
    @NotEmpty
    private List<NewsFeed> feeds;

}
