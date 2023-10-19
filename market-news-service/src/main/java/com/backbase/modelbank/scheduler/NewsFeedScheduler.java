package com.backbase.modelbank.scheduler;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import com.backbase.modelbank.config.YahooFinanceConfigurationProperties;
import com.backbase.modelbank.mapper.NewsMapper;
import com.backbase.yahoofinance.news.clients.api.YahooMarketNewsApi;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInner;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.infinispan.Cache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Component
public class NewsFeedScheduler {

    private final YahooMarketNewsApi yahooMarketNewsApi;
    private final NewsMapper mapper;
    private final YahooFinanceConfigurationProperties configurationProperties;
    private final Cache<String, List<NewsEntry>> newsFeedCache;

    @Scheduled(cron =  "${yahoo.finance.scheduling.cron}")
    public void scheduledNewsSearch() {
        try {
            configurationProperties.getFeeds().forEach(newsFeed -> {

                // Retrieve latest news
                var searchResponse = yahooMarketNewsApi.getMarketNews(newsFeed.getId());

                if (searchResponse == null) {
                    return;
                }

                // Map yahoo finance news response
                List<SearchResponseNewsInner> newsListResponse = Optional.of(searchResponse.getNews()).orElse(new ArrayList<>());

                List<NewsEntry> newsEntries = newsListResponse.stream()
                        .map(mapper::mapNewsEntry)
                        .collect(Collectors.toCollection(ArrayList::new));

                // Get cached news
                List<NewsEntry> newsFeedCachedList = newsFeedCache.getOrDefault(newsFeed.getId(), new ArrayList<>());

                newsEntries.addAll(newsFeedCachedList);

                // Sort news before caching
                List<NewsEntry> sortedNews = newsEntries.stream()
                        .distinct()
                        .sorted(Comparator.comparing(NewsEntry::getPublishDate).reversed())
                        .collect(Collectors.toCollection(ArrayList::new));

                // Append the latest news to the cache
                newsFeedCache.put(newsFeed.getId(), sortedNews);
            });
        } catch (Exception ex) {
            log.error("Error while trying to cache yahoo news", ex);
        }
    }
}
