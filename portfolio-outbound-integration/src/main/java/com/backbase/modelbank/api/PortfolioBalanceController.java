package com.backbase.modelbank.api;


import com.backbase.arrangments.balance.api.integration.v2.BalanceApi;
import com.backbase.arrangments.balance.api.integration.v2.model.BalanceItem;
import com.backbase.modelbank.service.PortfolioBalanceService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PortfolioBalanceController implements BalanceApi {

    private final PortfolioBalanceService portfolioBalanceService;

    @Override
    public ResponseEntity<List<BalanceItem>> getBalances(List<String> arrangementIds,
        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(portfolioBalanceService.getBalances(arrangementIds));
    }
}
