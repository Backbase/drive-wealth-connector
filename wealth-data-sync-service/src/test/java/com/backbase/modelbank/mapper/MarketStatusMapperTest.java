package com.backbase.modelbank.mapper;

import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Market;
import com.backbase.portfolio.instrument.integration.api.service.v1.model.MarketStatusEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketStatusMapperTest {

    private final MarketStatusMapper mapper = new com.backbase.modelbank.mapper.MarketStatusMapperImpl();

    @Test
    void mapMarketStatusPutRequestBody_WhenOpenMarket() {

        // Given
        var marketStatus = "Open";

        // When
        var result = mapper.mapMarketStatusPutRequestBody(marketStatus);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(MarketStatusEnum.OPEN, result.getStatus());
    }

    @Test
    void mapMarketStatusPutRequestBody_WhenCloseMarket() {

        // Given
        var marketStatus = "Close";

        // When
        var result = mapper.mapMarketStatusPutRequestBody(marketStatus);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(MarketStatusEnum.CLOSE, result.getStatus());
    }

    @Test
    void mapMarket_WhenCloseMarket() {

        // Given
        var marketStatus = "Close";
        var market = new Market("nsq", "NSQ");

        // When
        var result = mapper.mapMarket(market, marketStatus);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(MarketStatusEnum.CLOSE, result.getStatus());
    }

    @Test
    void mapMarket_WhenOpenMarket() {

        // Given
        var marketStatus = "Open";
        var market = new Market("nsq", "NSQ");

        // When
        var result = mapper.mapMarket(market, marketStatus);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(MarketStatusEnum.OPEN, result.getStatus());
    }

}