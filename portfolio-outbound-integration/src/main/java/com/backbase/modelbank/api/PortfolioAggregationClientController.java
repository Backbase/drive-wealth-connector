package com.backbase.modelbank.api;

import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.api.PortfolioAggregationClientApi;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.PortfolioSummaryAggregationGet;
import com.backbase.modelbank.service.PortfolioPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PortfolioAggregationClientController implements PortfolioAggregationClientApi {

    private final PortfolioPerformanceService service;

    @Override
    public ResponseEntity<PortfolioSummaryAggregationGet> getAggregation(List<String> portfolioIds, String currencyCode, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(service.getAggregation(portfolioIds, currencyCode));
    }
}
