package com.backbase.modelbank.mapper;

import com.backbase.stream.portfolio.model.PortfolioPositionsHierarchy.ItemTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;

import static com.backbase.modelbank.util.TestFactory.getAccountSummary;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PositionsHierarchyMapperTest {

    private final PositionsHierarchyMapper mapper =
        new com.backbase.modelbank.mapper.PositionsHierarchyMapperImpl();

    @Test
    void testPositionHierarchMapping() throws IOException {

        // When
        var result = mapper.map(getAccountSummary());

        // Then
        assertEquals(ItemTypeEnum.ASSET_CLASS, result.getItemType());
        assertEquals("1", result.getExternalId());
        assertEquals("Equity", result.getName());
        assertEquals(new BigDecimal("45.01"), result.getUnrealizedPL().getAmount());
        assertEquals(new BigDecimal("5371.81"), result.getValuation().getAmount());

        var regionItem = result.getItems().get(0);
        assertEquals(1, result.getItems().size());
        assertEquals(ItemTypeEnum.REGION, regionItem.getItemType());
        assertEquals("na", regionItem.getExternalId());
        assertEquals("North America", regionItem.getName());
        assertEquals(new BigDecimal("45.01"), regionItem.getUnrealizedPL().getAmount());
        assertEquals(new BigDecimal("5371.81"), regionItem.getValuation().getAmount());

        var countryItem = regionItem.getItems().get(0);
        assertEquals(1, regionItem.getItems().size());
        assertEquals(ItemTypeEnum.COUNTRY, countryItem.getItemType());
        assertEquals("us", countryItem.getExternalId());
        assertEquals("United States", countryItem.getName());
        assertEquals(new BigDecimal("45.01"), countryItem.getUnrealizedPL().getAmount());
        assertEquals(new BigDecimal("5371.81"), countryItem.getValuation().getAmount());

    }
}