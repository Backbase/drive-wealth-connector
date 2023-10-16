package com.backbase.modelbank.model;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetNewsEntriesWrapper {
    private int totalCount;
    private List<NewsEntry> newsEntries;
}
