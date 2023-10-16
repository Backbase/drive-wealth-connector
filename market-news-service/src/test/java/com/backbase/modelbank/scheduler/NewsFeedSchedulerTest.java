package com.backbase.modelbank.scheduler;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsFeed;
import com.backbase.modelbank.MarketNewsApplication;
import com.backbase.modelbank.config.YahooFinanceConfigurationProperties;
import com.backbase.modelbank.mapper.NewsMapper;
import com.backbase.modelbank.utils.TestUtils;
import com.backbase.yahoofinance.news.clients.api.YahooMarketNewsApi;
import org.apache.logging.log4j.Logger;
import org.infinispan.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

import javax.swing.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@ActiveProfiles("it")
@DirtiesContext
@SpringBootTest(classes = {MarketNewsApplication.class, NewsFeedScheduler.class}, properties = "yahoo.finance.scheduling.cron=*/5 * * * * ?")
class NewsFeedSchedulerTest {
    @MockBean
    YahooMarketNewsApi yahooMarketNewsApi;
    @MockBean
    NewsMapper mapper;
    @MockBean
    YahooFinanceConfigurationProperties configurationProperties;
    @MockBean
    Cache<String, List<NewsEntry>> newsFeedCache;
    @SpyBean
    NewsFeedScheduler newsFeedScheduler;

    @Test
    void testScheduledNewsSearch() {

        // Given
        when(configurationProperties.getFeeds()).thenReturn(List.of(new NewsFeed()
                .id("id")
                .name("name"))
        );
        when(yahooMarketNewsApi.getMarketNews(any()))
                .thenReturn(TestUtils.getNewsMock());
        when(newsFeedCache.getOrDefault(anyString(), any()))
                .thenReturn(new ArrayList<>());

        // When
        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> verify(newsFeedScheduler, atLeastOnce()).scheduledNewsSearch());

        verify(newsFeedCache, atMostOnce()).getOrDefault(anyString(), any());
        verify(newsFeedCache, atMostOnce()).put(anyString(), any());
    }

    @Test
    void testScheduledNewsSearch_Error() {

        // Given
        when(configurationProperties.getFeeds()).thenReturn(List.of(new NewsFeed()
                .id("id")
                .name("name"))
        );
        when(yahooMarketNewsApi.getMarketNews(any()))
                .thenThrow(RestClientException.class);

        // When
        // Then
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> verify(newsFeedScheduler, atLeastOnce()).scheduledNewsSearch());

    }
}

