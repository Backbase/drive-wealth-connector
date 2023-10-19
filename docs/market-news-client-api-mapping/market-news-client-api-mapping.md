# Market News Client API Mapping

# Yahoo Finance News API Example

# Sequence Diagram

![market-news-integration-api-Market News Integration.svg](./attachments/market-news-integration-api-Market%20News%20Integration.svg)


# Get Feed News [https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/operations/News/getFeedNews/](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/operations/News/getFeedNews/)

`GET /client-api/v1/news/feeds/{feedId}`



Yahoo Finance API:

[https://query1.finance.yahoo.com/v11/finance/quoteSummary/aapl?modules=financialData](https://query1.finance.yahoo.com/v11/finance/quoteSummary/aapl?modules=financialData)

### Request Mapping

| **BB Field**    | **News Field** | **Description**  |
| --------------- | -------------- | ---------------- |
| feedId          | `q`            | String News feed |

### Response Mapping

| **BB Field**                 | **News Field**                  | **Description**                                                                                                                                                                                |
| ---------------------------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| title###### REQUIRED
        | `news.title`                    | StringNews title first 50 character                                                                                                                                                            |
| text###### REQUIRED
         | `news.title`                    | String                                                                                                                                                                                         |
| link###### REQUIRED
         | `news.link`                     | StringLink to the full article                                                                                                                                                                 |
| publishDate                  | `providerPublishTime`           | DateTimePublication date of the article                                                                                                                                                        |
| tickers[*].name              | `relatedTickers`                | [**array[news_entry_tickers]**](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news_entry_tickers/)Tickers of the related investment instruments |
| tickers[*].performanceChange | -                               | Skip                                                                                                                                                                                           |
| source                       | `publisher`                     | StringName of the news source                                                                                                                                                                  |
| media.contentType            | Constant: `image/jpeg`          | [**news_entry_media**](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news_entry_media/)                                                         |
| media.url                    | `thumbnail`.`resolutions`.`url` | if tag == original                                                                                                                                                                             |

# Get Feeds[https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/operations/News/getFeeds/](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/operations/News/getFeeds/)

| **BB Field**         | **News Field**                         | **Description**                     |
| -------------------- | -------------------------------------- | ----------------------------------- |
| id###### REQUIRED
   | Constant:   [Latest News, World News]  | StringThe identifier of a news feed |
| name###### REQUIRED
 | Constant:   [Latest News, World News]  | StringThe name of the news feed     |

# Get Instrument News [https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news-entry/](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news-entry/)

### Request Mapping

| **BB Field**    | **News Field** | **Description**   |
| --------------- | -------------- | ----------------- |
| #### ticker
    | `q`            | String News query |
| #### from
      | *              |                   |
| #### size
      | *              |                   |

### Response Mapping

| **BB Field**                 | **News Field**                  | **Description**                                                                                                                                                                                |
| ---------------------------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| title###### REQUIRED
        | `news.title`                    | StringNews title first 50 character                                                                                                                                                            |
| text###### REQUIRED
         | `news.title`                    | String                                                                                                                                                                                         |
| link###### REQUIRED
         | `news.link`                     | StringLink to the full article                                                                                                                                                                 |
| publishDate                  | `providerPublishTime`           | DateTimePublication date of the article                                                                                                                                                        |
| tickers[*].name              | `relatedTickers`                | [**array[news_entry_tickers]**](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news_entry_tickers/)Tickers of the related investment instruments |
| tickers[*].performanceChange | -                               | Skip                                                                                                                                                                                           |
| source                       | `publisher`                     | StringName of the news source                                                                                                                                                                  |
| media.contentType            | Constant: `image/jpeg`          | [**news_entry_media**](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news_entry_media/)                                                         |
| media.url                    | `thumbnail`.`resolutions`.`url` | if tag == original                                                                                                                                                                             |

# Search News [https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news-entry/](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news-entry/)

| **BB Field**                 | **News Field**                  | **Description**                                                                                                                                                                                |
| ---------------------------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| title###### REQUIRED
        | `news.title`                    | StringNews title first 50 character                                                                                                                                                            |
| text###### REQUIRED
         | `news.title`                    | String                                                                                                                                                                                         |
| link###### REQUIRED
         | `news.link`                     | StringLink to the full article                                                                                                                                                                 |
| publishDate                  | `providerPublishTime`           | DateTimePublication date of the article                                                                                                                                                        |
| tickers[*].name              | `relatedTickers`                | [**array[news_entry_tickers]**](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news_entry_tickers/)Tickers of the related investment instruments |
| tickers[*].performanceChange | -                               | Skip                                                                                                                                                                                           |
| source                       | `publisher`                     | StringName of the news source                                                                                                                                                                  |
| media.contentType            | Constant: `image/jpeg`          | [**news_entry_media**](https://developer.backbase.com/apis/specs/portfolio/market-news-client-api/1.0.2-beta/models/news_entry_media/)                                                         |
| media.url                    | `thumbnail`.`resolutions`.`url` | if tag == original                                                                                                                                                                             |
