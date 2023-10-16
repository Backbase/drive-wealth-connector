package com.backbase.modelbank.service.impl;

import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsFeed;
import com.backbase.modelbank.config.YahooFinanceConfigurationProperties;
import com.backbase.modelbank.mapper.NewsMapper;
import com.backbase.modelbank.model.GetNewsEntriesWrapper;
import com.backbase.modelbank.service.YahooFinanceNewsService;
import com.backbase.yahoofinance.news.clients.api.YahooMarketNewsApi;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.*;



@RequiredArgsConstructor
@Service
@Slf4j
public class YahooFinanceNewsServiceImpl implements YahooFinanceNewsService {

    private final YahooMarketNewsApi yahooMarketNewsApi;
    private final NewsMapper mapper;
    private final Cache<String, List<NewsEntry>> newsFeedCache;

    private final YahooFinanceConfigurationProperties configurationProperties;

    public GetNewsEntriesWrapper getFeedNews(String feedId, Integer from, Integer size) {
        try {
            List<NewsEntry> cachedNews = newsFeedCache.getOrDefault(feedId, new ArrayList<>());

            var newsEntries = cachedNews.stream()
                    .skip((long) from * size)
                    .limit(size)
                    .toList();

            return GetNewsEntriesWrapper.builder()
                    .totalCount(cachedNews.size())
                    .newsEntries(newsEntries).build();
        } catch (RestClientException ex) {
            log.error("Error while trying get news feed", ex);
            throw new InternalServerErrorException(ex.getMessage());
        }
    }


    public GetNewsEntriesWrapper searchNews(String query, Integer from, Integer size) {
        try {
            var searchResponse = yahooMarketNewsApi.getMarketNews(query);

            if (searchResponse == null) {
                return GetNewsEntriesWrapper.builder().newsEntries(Collections.emptyList()).totalCount(0).build();
            }

            var newsList = Optional.of(searchResponse.getNews()).orElse(Collections.emptyList());
            var newsEntries = newsList.stream()
                    .skip((long) from * size)
                    .limit(size)
                    .map(mapper::mapNewsEntry)
                    .toList();

            return GetNewsEntriesWrapper.builder()
                    .totalCount(newsList.size())
                    .newsEntries(newsEntries).build();
        } catch (RestClientException ex) {
            log.error("Error while trying get search news", ex);
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

    @Override
    public List<NewsFeed> getFeeds() {
        return configurationProperties.getFeeds();
    }

}
