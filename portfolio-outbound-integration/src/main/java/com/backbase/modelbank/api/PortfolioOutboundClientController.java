package com.backbase.modelbank.api;

import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.api.PortfolioOutboundClientApi;
import com.backbase.dbs.portfolio.integration.portfolio.rest.spec.model.*;
import com.backbase.modelbank.service.PortfolioPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PortfolioOutboundClientController implements PortfolioOutboundClientApi {

    private final PortfolioPerformanceService service;

    @Override
    public ResponseEntity<CumulativePerformanceBenchmarkDataGet> performanceBenchmarkDataByPortfolio(String portfolioId,
        String uuid, OffsetDateTime fromDate, OffsetDateTime toDate, String calculationType, String calculationMethod,
        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(service.performanceBenchmarkDataByPortfolioId(portfolioId, uuid, fromDate, toDate));
    }

    @Override
    public ResponseEntity<CumulativePerformanceBenchmarkDataGet> performanceBenchmarkDataByPortfolioId(
        String portfolioId, String uuid, OffsetDateTime fromDate, OffsetDateTime toDate,
        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(service.performanceBenchmarkDataByPortfolioId(portfolioId, uuid, fromDate, toDate));
    }

    @Override
    public ResponseEntity<List<PerformanceBenchmarkGetItem>> performanceBenchmarksByPortfolioId(String portfolioId,
        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(service.performanceBenchmarksByPortfolioId());
    }

    @Override
    public ResponseEntity<CumulativePerformanceChartDataGet> performanceChartDataByPortfolioId(String portfolioId,
        OffsetDateTime fromDate, OffsetDateTime toDate, String calculationType, String calculationMethod,
        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(service.performanceChartDataByPortfolioId(portfolioId, fromDate, toDate));
    }

    @Override
    public ResponseEntity<ValuationChartDataGet> valuationChartDataByPortfolioId(String portfolioId,
        OffsetDateTime fromDate, OffsetDateTime toDate, Granularity granularity,
        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(service.valuationChartDataByPortfolioId(portfolioId, fromDate, toDate, granularity));
    }

}
