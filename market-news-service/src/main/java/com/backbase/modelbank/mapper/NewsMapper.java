package com.backbase.modelbank.mapper;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntryMedia;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntryTickers;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInner;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInnerThumbnail;
import com.backbase.yahoofinance.news.clients.model.SearchResponseNewsInnerThumbnailResolutionsInner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Maps model between DBS and yahoo news.
 */
@Mapper(componentModel = "spring")
public interface NewsMapper {
    @Mapping(target = "title", expression = "java(searchResponseNewsInner.getTitle().substring(0,Math.min(50, searchResponseNewsInner.getTitle().length())) + \"...\")")
    @Mapping(target = "text", source = "title")
    @Mapping(target = "link", source = "link")
    @Mapping(target = "publishDate", source = "providerPublishTime", qualifiedByName = "timestampConversion")
    @Mapping(target = "tickers", source = "relatedTickers", qualifiedByName = "mapTickers")
    @Mapping(target = "source", source = "publisher")
    @Mapping(target = "media", source = "thumbnail", qualifiedByName = "mapEntryMedia")
    NewsEntry mapNewsEntry(SearchResponseNewsInner searchResponseNewsInner);

    @Named("mapEntryMedia")
    default NewsEntryMedia mapEntryMedia(SearchResponseNewsInnerThumbnail searchResponseNewsInnerThumbnail) {
        if (searchResponseNewsInnerThumbnail == null) {
            return null;
        }

        var resolutionsOptional = Optional.of(searchResponseNewsInnerThumbnail.getResolutions());


        var thumbnail = resolutionsOptional.get().stream()
                .filter(image -> !"original".equalsIgnoreCase(image.getTag())).findFirst();


        return new NewsEntryMedia()
                .contentType(MediaType.IMAGE_JPEG_VALUE)
                .url(thumbnail.map(SearchResponseNewsInnerThumbnailResolutionsInner::getUrl).orElse(""))
                .alt(thumbnail.map(SearchResponseNewsInnerThumbnailResolutionsInner::getTag).orElse(""));
    }

    @Named("timestampConversion")
    default OffsetDateTime timestampConversion(Integer publishDate) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(publishDate), ZoneId.systemDefault());
    }

    @Named("mapTickers")
    default List<NewsEntryTickers> mapTrackers(List<String> relatedTickers) {
        return relatedTickers.stream().map(name -> new NewsEntryTickers().name(name))
                .collect(Collectors.toList());
    }
}
