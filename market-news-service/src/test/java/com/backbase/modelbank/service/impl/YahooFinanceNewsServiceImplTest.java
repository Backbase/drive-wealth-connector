package com.backbase.modelbank.service.impl;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsFeed;
import com.backbase.modelbank.config.YahooFinanceConfigurationProperties;
import com.backbase.modelbank.mapper.NewsMapper;
import com.backbase.modelbank.model.GetNewsEntriesWrapper;
import com.backbase.yahoofinance.news.clients.api.YahooMarketNewsApi;
import com.backbase.yahoofinance.news.clients.model.SearchResponse;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInner;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInnerThumbnail;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInnerThumbnailResolutionsInner;
import org.infinispan.Cache;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

import static com.backbase.modelbank.utils.TestUtils.getCacheResult;
import static com.backbase.modelbank.utils.TestUtils.getNewsMock;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YahooFinanceNewsServiceImplTest {
    private static final String FEED_ID = "feedId";
    public static final String QUERY = "query";
    @Mock
    YahooMarketNewsApi yahooMarketNewsApi;
    @Mock
    NewsMapper mapper;
    @Mock
    Cache<String, List<NewsEntry>> newsFeedCache;
    @Mock
    YahooFinanceConfigurationProperties configurationProperties;
    @Mock
    Logger log;
    @InjectMocks
    YahooFinanceNewsServiceImpl yahooFinanceNewsServiceImpl;


    @Test
    @DisplayName("Should return feed news")
    void testGetFeedNews() {
        // Given
        when(newsFeedCache.getOrDefault(eq(FEED_ID), any()))
                .thenReturn(getCacheResult());
        // When
        GetNewsEntriesWrapper result = yahooFinanceNewsServiceImpl.getFeedNews(FEED_ID, Integer.valueOf(0), Integer.valueOf(0));
        // Then
        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("Should search news")
    void testSearchNews() {
        // Given
        when(yahooMarketNewsApi.getMarketNews(any()))
                .thenReturn(getNewsMock());
        // When
        GetNewsEntriesWrapper result = yahooFinanceNewsServiceImpl.searchNews(QUERY, Integer.valueOf(0), Integer.valueOf(0));
        // Then
        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("Should get news feeds")
    void testGetFeeds() {
        // Given
        when(configurationProperties.getFeeds()).thenReturn(List.of(new NewsFeed()
                .id("id")
                .name("name")
        ));
        // When
        List<NewsFeed> result = yahooFinanceNewsServiceImpl.getFeeds();
        // Then
        Assertions.assertEquals(List.of(new NewsFeed()
                .id("id")
                .name("name")
        ), result);
    }


}

