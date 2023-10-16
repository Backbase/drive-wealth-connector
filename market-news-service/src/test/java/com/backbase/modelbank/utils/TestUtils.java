package com.backbase.modelbank.utils;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import com.backbase.yahoofinance.news.clients.model.SearchResponse;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInner;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInnerThumbnail;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInnerThumbnailResolutionsInner;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class TestUtils {

    public static SearchResponse getNewsMock() {
        return new SearchResponse()
                .news(
                        List.of(new SearchResponseNewsInner()
                                .title("Title")
                                .publisher("publisher")
                                .link("Link")
                                .uuid(UUID.randomUUID().toString())
                                .thumbnail(new SearchResponseNewsInnerThumbnail()
                                        .addResolutionsItem(
                                                new SearchResponseNewsInnerThumbnailResolutionsInner()
                                                        .tag("tag")
                                                        .url("URL")
                                        ))
                        )
                );
    }

    public static SearchResponseNewsInnerThumbnail getMockEntryMedia() {
        return new SearchResponseNewsInnerThumbnail()
                .addResolutionsItem(
                        new SearchResponseNewsInnerThumbnailResolutionsInner()
                                .tag("tag")
                                .url("url")
                );
    }

    public static List<NewsEntry> getCacheResult() {
        return List.of(
                new NewsEntry()
                        .text("Title")
                        .text("Text")
        );
    }
}
