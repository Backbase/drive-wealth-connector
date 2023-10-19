package com.backbase.modelbank.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backbase.arrangments.balance.api.integration.v2.model.BalanceItem;
import com.backbase.modelbank.service.PortfolioBalanceService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PortfolioBalanceControllerTest {

    private MockMvc mockMvc;
    private PortfolioBalanceService portfolioBalanceService;
    private PortfolioBalanceController portfolioBalanceController;

    @BeforeEach
    void setUp() {
        portfolioBalanceService = mock(PortfolioBalanceService.class);
        portfolioBalanceController = new PortfolioBalanceController(portfolioBalanceService);
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioBalanceController).build();
    }

    @Test
    void testGetBalances() throws Exception {
        // Arrange
        List<String> arrangementIds = Arrays.asList("1234", "5678");
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        List<BalanceItem> expectedBalances = Arrays.asList(
            new BalanceItem().availableBalance(BigDecimal.valueOf(100.0)).bookedBalance(BigDecimal.valueOf(100.0)),
            new BalanceItem().availableBalance(BigDecimal.valueOf(200.0)).bookedBalance(BigDecimal.valueOf(200.0))
        );
        ResponseEntity<List<BalanceItem>> expectedResponse = new ResponseEntity<>(expectedBalances, HttpStatus.OK);
        when(portfolioBalanceService.getBalances(arrangementIds)).thenReturn(expectedBalances);

        // Act
        ResponseEntity<List<BalanceItem>> actualResponse = portfolioBalanceController.getBalances(arrangementIds,
            httpServletRequest);

        // Assert
        assertEquals(expectedResponse, actualResponse);
    }
}
