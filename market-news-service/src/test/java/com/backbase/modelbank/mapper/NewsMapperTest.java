package com.backbase.modelbank.mapper;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntryMedia;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntryTickers;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInnerThumbnail;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInnerThumbnailResolutionsInner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static com.backbase.modelbank.utils.TestUtils.getMockEntryMedia;


class NewsMapperTest {
    NewsMapper newsMapper = new NewsMapperImpl();

    @Test
    void testMapEntryMedia() {
        // Given
        // When
        NewsEntryMedia result = newsMapper.mapEntryMedia(getMockEntryMedia());
        // Then
        Assertions.assertEquals(new NewsEntryMedia()
                .url("url")
                .contentType("image/jpeg")
                .alt("tag"), result);
    }

    @Test
    void testTimestampConversion() {
        // Given
        var publishDate = 1676907003;
        var expected = OffsetDateTime.ofInstant(Instant.ofEpochSecond(publishDate), ZoneId.systemDefault());

        // When
        OffsetDateTime result = newsMapper.timestampConversion(publishDate);

        // Then
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testMapTrackers() {
        // Given
        // When
        List<NewsEntryTickers> result = newsMapper.mapTrackers(List.of("String"));

        // Then
        Assertions.assertEquals(List.of(new NewsEntryTickers()
                .name("String")), result);
    }


}
