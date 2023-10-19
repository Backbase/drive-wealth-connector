package com.backbase.modelbank.config;

import com.backbase.stream.portfolio.model.PortfolioAttorney;
import com.backbase.stream.portfolio.model.PortfolioManager;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Valid
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "drive-wealth")
public class DriveWealthConfigurationProperties {

    /**
     * drive-wealth base URL.
     */
    @NotEmpty
    private String baseUrl;
    /**
     * drive-wealth api client key.
     */
    @NotEmpty
    private String dwClientAppKey;

    /**
     * drive-wealth api client id.
     */
    @NotEmpty
    private String clientID;

    /**
     * drive-wealth api client secret.
     */
    @NotEmpty
    private String clientSecret;

    /**
     * List of Portfolio Managers.
     */
    @NotNull
    @Min(1)
    private List<PortfolioManager> portfolioManagers;

    /**
     * List of Portfolio Attorneys.
     */
    @NotNull
    @Min(1)
    private List<PortfolioAttorney> portfolioAttorneys;

    /**
     * drive-wealth api transaction refresh window in days for various Events.
     */
    @NotNull
    private Transactions transactions;

    /**
     * drive-wealth api order refresh window in months for various Events.
     */
    @NotNull
    private Orders orders;

    /**
     * List of drive-wealth market exchanges.
     */
    @NotNull
    private MarketStatus marketStatus;

    @Data
    public static class Transactions {

        private List<Event> daysRefreshWindow;
    }

    @Data
    public static class Orders {

        private List<Event> monthsRefreshWindow;
    }

    public record Event(String eventName, int period) {}

    public record MarketStatus(Interval open, Interval close, List<Market> markets, List<Holiday> publicHolidays) {}

    public record Interval(String intervalInCron) {}

    public record Market(String id, String name) {}

    public record Holiday(int day, int month) {}
}
