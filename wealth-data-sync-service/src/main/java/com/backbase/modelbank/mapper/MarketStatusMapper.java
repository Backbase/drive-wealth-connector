package com.backbase.modelbank.mapper;

import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Market;
import com.backbase.portfolio.instrument.integration.api.service.v1.model.MarketStatusEnum;
import com.backbase.portfolio.instrument.integration.api.service.v1.model.MarketStatusPutRequestBody;
import java.util.Objects;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MarketStatusMapper {

    default MarketStatusPutRequestBody mapMarketStatusPutRequestBody(String marketStatus) {
        MarketStatusPutRequestBody marketStatusPutRequestBody = new MarketStatusPutRequestBody();
        if (Objects.equals(marketStatus, MarketStatusEnum.OPEN.getValue())) {
            marketStatusPutRequestBody.status(MarketStatusEnum.OPEN);
        }
        return marketStatusPutRequestBody;

    }

    default com.backbase.portfolio.instrument.integration.api.service.v1.model.Market mapMarket(Market marketExchange,
        String marketStatus) {
        return new com.backbase.portfolio.instrument.integration.api.service.v1.model.Market()
            .id(marketExchange.id())
            .name(marketExchange.name())
            .status(Objects.equals(marketStatus, MarketStatusEnum.OPEN.getValue()) ? MarketStatusEnum.OPEN
                : MarketStatusEnum.CLOSE);

    }
}
