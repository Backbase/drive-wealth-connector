package com.backbase.modelbank.config;

import com.backbase.modelbank.models.PortfolioBenchMarkRecord;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;


@Data
@Valid
@Configuration
@ConfigurationProperties(prefix = "drive-wealth")
public class DriveWealthConfigurationProperties {

    @NotNull
    private String baseUrl;
    @NotNull
    private String dwClientAppKey;
    @NotNull
    private String clientID;
    @NotNull
    private String clientSecret;
    @NotNull
    @Size(min = 1)
    private List<PortfolioBenchMarkRecord> portfolioBenchMarkList;
}
