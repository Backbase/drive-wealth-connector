package com.backbase.modelbank.api;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.api.NewsApi;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.GetNewsEntriesResponse;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.GetNewsFeedsResponse;
import com.backbase.modelbank.service.YahooFinanceNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Market News API CController
 */
@RequiredArgsConstructor
@RestController
@Validated
public class MarketNewsController implements NewsApi {

    public static final String X_TOTAL_COUNT = "x-total-count";
    private final YahooFinanceNewsService yahooFinanceNewsService;

    @Override
    public ResponseEntity<GetNewsEntriesResponse> getFeedNews(String feedId, Integer from, Integer size, HttpServletRequest httpServletRequest) {
        var newsEntriesWrapper = yahooFinanceNewsService.getFeedNews(feedId, from, size);
        return ResponseEntity.ok()
                .header(X_TOTAL_COUNT, String.valueOf(newsEntriesWrapper.getTotalCount()))
                .body(new GetNewsEntriesResponse()
                        .entries(newsEntriesWrapper.getNewsEntries())
                );
    }

    @Override
    public ResponseEntity<GetNewsFeedsResponse> getFeeds(HttpServletRequest httpServletRequest) {
        var newsFeed = new GetNewsFeedsResponse().feeds(yahooFinanceNewsService.getFeeds());
        return ResponseEntity.ok(newsFeed);
    }

    @Override
    public ResponseEntity<GetNewsEntriesResponse> getInstrumentNews(String ticker, Integer from, Integer size, HttpServletRequest httpServletRequest) {
        var newsEntriesWrapper = yahooFinanceNewsService.searchNews(ticker, from, size);
        return ResponseEntity.ok()
                .header(X_TOTAL_COUNT, String.valueOf(newsEntriesWrapper.getTotalCount()))
                .body(new GetNewsEntriesResponse()
                        .entries(newsEntriesWrapper.getNewsEntries())
                );

    }

    @Override
    public ResponseEntity<GetNewsEntriesResponse> searchNews(String query, Integer from, Integer size, HttpServletRequest httpServletRequest) {
        var newsEntriesWrapper = yahooFinanceNewsService.searchNews(query, from, size);
        return ResponseEntity.ok()
                .header(X_TOTAL_COUNT, String.valueOf(newsEntriesWrapper.getTotalCount()))
                .body(new GetNewsEntriesResponse()
                        .entries(newsEntriesWrapper.getNewsEntries())
                );
    }
}