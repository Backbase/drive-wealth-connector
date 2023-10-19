package com.backbase.modelbank.service;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsFeed;
import com.backbase.modelbank.model.GetNewsEntriesWrapper;

import java.util.List;

/**
 * Yahoo Finance Integration service to schedule and retrieve article news using Yahoo Finance Client API
 */
public interface YahooFinanceNewsService {
    /**
     * Get news feed from yahoo finance client API
     *
     * @param feedId news feedId
     * @param from   page number
     * @param size   size of the page
     * @return A wrapper {@link GetNewsEntriesWrapper} of market news list and total count of news
     */
    GetNewsEntriesWrapper getFeedNews(String feedId, Integer from, Integer size);

    /**
     * Search yahoo finance api based query
     *
     * @param query search query
     * @param from  page number
     * @param size  size of the page
     * @return A wrapper {@link GetNewsEntriesWrapper} of market news list and total count of news
     */
    GetNewsEntriesWrapper searchNews(String query, Integer from, Integer size);

    /**
     * @return Return configured feeds
     */
    List<NewsFeed> getFeeds();
}
