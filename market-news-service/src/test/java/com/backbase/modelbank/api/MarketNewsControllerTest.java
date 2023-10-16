package com.backbase.modelbank.api;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsFeed;
import com.backbase.modelbank.MarketNewsApplication;
import com.backbase.modelbank.model.GetNewsEntriesWrapper;
import com.backbase.modelbank.service.impl.YahooFinanceNewsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.backbase.modelbank.api.MarketNewsController.X_TOTAL_COUNT;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("it")
@AutoConfigureMockMvc
@DirtiesContext
@SpringBootTest(classes = MarketNewsApplication.class)
class MarketNewsControllerTest {

    private static final String FEED_ID = "FeedId";
    private static final String TICKER = "GOOL";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    YahooFinanceNewsServiceImpl yahooFinanceNewsService;


    @Test
    @DisplayName("Test News Feed")
    void testGetNewsFeed() throws Exception {
        // Given
        when(yahooFinanceNewsService.getFeeds())
                .thenReturn(List.of(new NewsFeed()
                        .id("Id")
                        .name("Name")));

        // When
        // Then
        this.mockMvc.perform(get("/client-api/v1/news/feeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feeds").isNotEmpty())
                .andExpect(jsonPath("$.feeds").isArray())
                .andExpect(jsonPath("$.feeds", hasSize(1)));
    }

    @Test
    @DisplayName("Test Feed News ")
    void testGetFeedNews() throws Exception {
        // Given
        when(yahooFinanceNewsService.getFeedNews(FEED_ID, 0, 10))
                .thenReturn(getMockNewsWrapper());

        // When
        // Then
        this.mockMvc.perform(get("/client-api/v1/news/feeds/" + FEED_ID))
                .andExpect(status().isOk())
                .andExpect(header().exists(X_TOTAL_COUNT))
                .andExpect(header().string(X_TOTAL_COUNT, "1"))
                .andExpect(jsonPath("$.entries").isNotEmpty())
                .andExpect(jsonPath("$.entries").isArray())
                .andExpect(jsonPath("$.entries", hasSize(1)));
    }

    @Test
    @DisplayName("Test Instrument News ")
    void testGetInstrumentNews() throws Exception {
        // Given
        when(yahooFinanceNewsService.searchNews(TICKER, 0, 10))
                .thenReturn(getMockNewsWrapper());

        // When
        // Then
        this.mockMvc.perform(get("/client-api/v1/news/instrument/" + TICKER))
                .andExpect(status().isOk())
                .andExpect(header().exists(X_TOTAL_COUNT))
                .andExpect(header().string(X_TOTAL_COUNT, "1"))
                .andExpect(jsonPath("$.entries").isNotEmpty())
                .andExpect(jsonPath("$.entries").isArray())
                .andExpect(jsonPath("$.entries", hasSize(1)));
    }

    @Test
    @DisplayName("Test Search News ")
    void testSearchNews() throws Exception {
        // Given
        when(yahooFinanceNewsService.searchNews(TICKER, 0, 10))
                .thenReturn(getMockNewsWrapper());

        // When
        // Then
        this.mockMvc.perform(get("/client-api/v1/news/search/").param("query", TICKER))
                .andExpect(status().isOk())
                .andExpect(header().exists(X_TOTAL_COUNT))
                .andExpect(header().string(X_TOTAL_COUNT, "1"))
                .andExpect(jsonPath("$.entries").isNotEmpty())
                .andExpect(jsonPath("$.entries").isArray())
                .andExpect(jsonPath("$.entries", hasSize(1)));
    }

    private static GetNewsEntriesWrapper getMockNewsWrapper() {
        return GetNewsEntriesWrapper.builder()
                .newsEntries(List.of(
                        new NewsEntry()
                                .title("Test Title")
                                .text("Test Text")
                ))
                .totalCount(1)
                .build();
    }

}
