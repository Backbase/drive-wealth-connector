# Model & Ingest static data json in Bootstrap

## Table of Contents 
 -- [User Story ](#user-story-)
-- [Sequence Diagram](#sequence-diagram)
-- [Ingest Assets Classes](#ingest-assets-classes)
-- [Regions and Countries](#regions-and-countries)
-- [Transaction Categories](#transaction-categories)
-- [Corporate action types](#corporate-action-types)
-- [Ingestion Dummy Corporate Action ](#ingestion-dummy-corporate-action-)
-- [Instruments & Prices & Exchange](#instruments-&-prices-&-exchange)
-- [Pull Request](#pull-request)
## User Story 

[https://backbase.atlassian.net/browse/MWM-35](https://backbase.atlassian.net/browse/MWM-35) 

## Sequence Diagram

![tLZVRzis47xNNt587xm1IvoqNL4Kw5RRITi0wLR4tlgmxU4idMqYDAcHfzXUqVzz92tBCiqOMH0CHLr0JUwx7zztF8fqtbX6mzLSPfqPSsbVTRkbHEKj9xcUTotzNP7AwRWGE3Kupp8MB0c6Z2noA90HX9gIPQ4L20Nd_M665MjLpSTaCdiZL8ak4EQu_43MufI6uX-25wUdMJRMIpWQOtupHal7OwtPii7ocD7U76KVqRWQ-20221228-10252](./attachments/tLZVRzis47xNNt587xm1IvoqNL4Kw5RRITi0wLR4tlgmxU4idMqYDAcHfzXUqVzz92tBCiqOMH0CHLr0JUwx7zztF8fqtbX6mzLSPfqPSsbVTRkbHEKj9xcUTotzNP7AwRWGE3Kupp8MB0c6Z2noA90HX9gIPQ4L20Nd_M665MjLpSTaCdiZL8ak4EQu_43MufI6uX-25wUdMJRMIpWQOtupHal7OwtPii7ocD7U76KVqRWQ-20221228-10252)
PlantUML Server Link : [http://www.plantuml.com/plantuml/uml/tLXjJoCt4Fw-lyBYFgIL6W8SFPsGQ9K4k8l4jIUIzZwq_U3ipYOMpdfhUmcvq_ttZhqlyJhUbAEeGe08u7bxdfcnnxkzfTQvEXiCSaupNQpuOInM0mL_5v35q4yONKYwYYBDD0SorLIpc2HKKyAo1IZDH8P_aOlHDAA55bcnceECr2tBSeg6P4NlFu1IT05JzXd8cwEZA9gBUt8mf_7jd2hepuNGIakQzpLLjmVHHoen1iiH3ZcOWYxoQrXGVfaXXCrx1fBAUBaPYonbYq9IW-20K4KKudl5sMAfvxo02BA4OA0gsgYAXWmaq9KNPP8XW685cPxKfAOWxrWCrZDZolNSClqef4u5Po9ecRSjmvWyC9XAycVM-BL-alaUFnUItG5P0-LwwVao8OX8qnfFiWwwYKpk-Z_P59wHyHBGQfAIF-eQ7IgRjSEcL8TKAT3zc9lVQhj-0qeKCWPrcT4vXuISdvEPBE2l2C6HeJ4YOsD4cdIHIhN6O99zLZNN9yl9cgd8251kKd56Sg6qNI-zORv-dSx8W6KQ5cLFz6dE1dV7WnPIG_aHR2KiqEE3UTwKwW_ZM2gFYYpX421PYlSHhC0zaXfhMl_1zBQxPGz5xBGpyWxBFUISE44Y53G0NK-SAOqqtrtEY47RBxjsu9Z-hF3qATJvS3oR_7uPSI5o6s6hGXBGb755KY49zGBxm2Q-MHLzq15-y6NxpoJvsYAo0w5TY0yq9nUVN2qjp97fhinj1xJjXaaILZD7hacOtS8c4Q7EQgMtihD6Eqtcf69Fexd9V5opuKUcQ6oTnXG32CaUVdpCjjPat5Xtjs3kvCAntPf6kP-8NRNDleo4-LJ9YId2mF57Ze-EZiYQ9Nhfp4MJf8KKM9MnuDYjwoNJuCYdIvg8DO8dAUNAbOoC4PMRaUV1Wa4Lh4z8MyW4PAdvwlZaxJ6D7QsBSiplLqeWfGNNCxZNGnmYcUsN63zQ0tzQNHsggMyrTxLk80LfrCgGioMBRpEq82UHerLVGyYNgFUR0ga8jICBATdIVsSsXzdhS4zNEOVl8u9VmrWBwVjzxOlQBBV2FLbjbAv4fZliZUWNkW9o8-P2YxPWgZVO4tF14s_TN8sEJyl51ORoKTHXUaCfnLgLYkRF3YIrgEMuMVHGcFKE7svTpE8Dn9fc2mxXz8x4_GxozBNvRiI1tFV6DATpnl5wLwk5QLK2FxtLiaUhNhKPU5CjXNTEEqRtVd7bNGXDQsx2A4l3AMu447gJGzY5lT1qHH-BLIuoqo0T2XViZg5W53nTN8L_iG-MjnA6SJj2dxeZikLeQkGARFMmLNla6kUP6LYQP08dvH98-V2XyU2nPk-GMTx5m17wtMj58O2DmDqlpQBNhRC6Z3rwpFHLMuFtG3jJv2a4KMp5ErWSKRlLhraAroo3hfGqyZrzN-jiaMyPLqyq9Kld-Q8hdglIsOYEai_M5xNPUb8_YvvuFxfWc-oPJKRSmv_fcgtmURb3mtP8E7WZyc4tWZ8HRxUBVemVew_EYCGjOiOZdYz-SQ_ibv44o-IxFSOrVBoNEw17TCVsShU3GdHtUROhxHNNfMKkLNSu49w4skcpeYRh6Ve2kSLS5CcBv6NUChoKRTQxGdjL7-eEo_fs4MJwpLHFdWolUTzedYXTiFLQk2hDhVF_XcfIszndMGjnAeqd0pTuvVwl74wVZ0EvlCTBQaOvKHkbOQNCwmRprXLdTwmB2QHwBQnQWoZPHowGy6_TIa-yftCCZm-ePVyy5tWzyr1OJhuwnp_OQsR2KdihmIVkS7VbvbfZNuCygmtn3m00](http://www.plantuml.com/plantuml/uml/tLXjJoCt4Fw-lyBYFgIL6W8SFPsGQ9K4k8l4jIUIzZwq_U3ipYOMpdfhUmcvq_ttZhqlyJhUbAEeGe08u7bxdfcnnxkzfTQvEXiCSaupNQpuOInM0mL_5v35q4yONKYwYYBDD0SorLIpc2HKKyAo1IZDH8P_aOlHDAA55bcnceECr2tBSeg6P4NlFu1IT05JzXd8cwEZA9gBUt8mf_7jd2hepuNGIakQzpLLjmVHHoen1iiH3ZcOWYxoQrXGVfaXXCrx1fBAUBaPYonbYq9IW-20K4KKudl5sMAfvxo02BA4OA0gsgYAXWmaq9KNPP8XW685cPxKfAOWxrWCrZDZolNSClqef4u5Po9ecRSjmvWyC9XAycVM-BL-alaUFnUItG5P0-LwwVao8OX8qnfFiWwwYKpk-Z_P59wHyHBGQfAIF-eQ7IgRjSEcL8TKAT3zc9lVQhj-0qeKCWPrcT4vXuISdvEPBE2l2C6HeJ4YOsD4cdIHIhN6O99zLZNN9yl9cgd8251kKd56Sg6qNI-zORv-dSx8W6KQ5cLFz6dE1dV7WnPIG_aHR2KiqEE3UTwKwW_ZM2gFYYpX421PYlSHhC0zaXfhMl_1zBQxPGz5xBGpyWxBFUISE44Y53G0NK-SAOqqtrtEY47RBxjsu9Z-hF3qATJvS3oR_7uPSI5o6s6hGXBGb755KY49zGBxm2Q-MHLzq15-y6NxpoJvsYAo0w5TY0yq9nUVN2qjp97fhinj1xJjXaaILZD7hacOtS8c4Q7EQgMtihD6Eqtcf69Fexd9V5opuKUcQ6oTnXG32CaUVdpCjjPat5Xtjs3kvCAntPf6kP-8NRNDleo4-LJ9YId2mF57Ze-EZiYQ9Nhfp4MJf8KKM9MnuDYjwoNJuCYdIvg8DO8dAUNAbOoC4PMRaUV1Wa4Lh4z8MyW4PAdvwlZaxJ6D7QsBSiplLqeWfGNNCxZNGnmYcUsN63zQ0tzQNHsggMyrTxLk80LfrCgGioMBRpEq82UHerLVGyYNgFUR0ga8jICBATdIVsSsXzdhS4zNEOVl8u9VmrWBwVjzxOlQBBV2FLbjbAv4fZliZUWNkW9o8-P2YxPWgZVO4tF14s_TN8sEJyl51ORoKTHXUaCfnLgLYkRF3YIrgEMuMVHGcFKE7svTpE8Dn9fc2mxXz8x4_GxozBNvRiI1tFV6DATpnl5wLwk5QLK2FxtLiaUhNhKPU5CjXNTEEqRtVd7bNGXDQsx2A4l3AMu447gJGzY5lT1qHH-BLIuoqo0T2XViZg5W53nTN8L_iG-MjnA6SJj2dxeZikLeQkGARFMmLNla6kUP6LYQP08dvH98-V2XyU2nPk-GMTx5m17wtMj58O2DmDqlpQBNhRC6Z3rwpFHLMuFtG3jJv2a4KMp5ErWSKRlLhraAroo3hfGqyZrzN-jiaMyPLqyq9Kld-Q8hdglIsOYEai_M5xNPUb8_YvvuFxfWc-oPJKRSmv_fcgtmURb3mtP8E7WZyc4tWZ8HRxUBVemVew_EYCGjOiOZdYz-SQ_ibv44o-IxFSOrVBoNEw17TCVsShU3GdHtUROhxHNNfMKkLNSu49w4skcpeYRh6Ve2kSLS5CcBv6NUChoKRTQxGdjL7-eEo_fs4MJwpLHFdWolUTzedYXTiFLQk2hDhVF_XcfIszndMGjnAeqd0pTuvVwl74wVZ0EvlCTBQaOvKHkbOQNCwmRprXLdTwmB2QHwBQnQWoZPHowGy6_TIa-yftCCZm-ePVyy5tWzyr1OJhuwnp_OQsR2KdihmIVkS7VbvbfZNuCygmtn3m00)  

## Ingest Assets Classes

Ingesting the following assets classes 

| **Asset class** | **Asset Code** | **Description **                                                                          |
| --------------- | -------------- | ----------------------------------------------------------------------------------------- |
| Equity          | equity         | A "Equity" class the represents the type of available securities supported by DW          |
| Cash            | cash           | A "cash" class represents the full amount for securities purchased using their own funds. |
| Other           | other          | Others                                                                                    |

 

> Stream Wealth Bundle Example
>
> ``` 
> {
>   "assetClasses": [
>     {
>       "assetClass": {
>         "name": "Equity",
>         "code": "1"
>       },
>       "subAssetClasses": [
>         {
>           "name": "Equity",
>           "code": "equity"
>         },
>         {
>           "name": "ETF",
>           "code": "etf"
>         },
>         {
>           "name": "ADR",
>           "code": "adr"
>         }
>       ]
>     },
>     {
>       "assetClass": {
>         "name": "Cash",
>         "code": "3"
>       },
>       "subAssetClasses": [
>         {
>           "name": "Cash",
>           "code": "cash"
>         }
>       ]
>     },
>     {
>       "assetClass": {
>         "name": "Other",
>         "code": "4"
>       },
>       "subAssetClasses": [
>         {
>           "name": "Other",
>           "code": "other"
>         }
>       ]
>     }
>   ]
> }
> ```

## Regions and Countries

Ingest the following Regions

| **Region Name** | **Code** | **Countries in The Region** | **Countery Code** |
| --------------- | -------- | --------------------------- | ----------------- |
| North America   | US       | USA                         | usa               |
| Other           | other    | Other                       | other             |

> Region And Countery Stream Bundle Example
>
> ``` 
> {
>   "regions": [
>     {
>       "region": {
>         "name": "North America",
>         "code": "na" //code limits is 12
>       },
>       "countries": [
>         {
>           "name": "USA",
>           "code": "usa"
>         }
>       ]
>     },
>     {
>       "region": {
>         "name": "Other",
>         "code": "other"
>       },
>       "countries": [
>         {
>           "name": "Other",
>           "code": "Other"
>         }
>       ]
>     }
>   ]
> }
> ```

## Transaction Categories

Ingest the Drive Wealth Transactions Types from [https://developer.drivewealth.com/reference/transaction-types-explained](https://developer.drivewealth.com/reference/transaction-types-explained) 

> Stream Bundle Transaction Types Example
>
> ```json 
> {
>   "transaction-categories": [
>     {
>       "key": "CSR",
>       "alias": "CSR",
>       "description": "A deposit"
>     }
>   ]
> }
> ```

> DriveWealth transaction categories file
>
> [transaction-categories.json](./attachments/transaction-categories.json)

## Corporate action types

Ingest corporate actions types from here [https://developer.drivewealth.com/reference/corporate-actions](https://developer.drivewealth.com/reference/corporate-actions) 

> Corporate Action Type ingestion is missing on Stream Portfolio Saga, and needed to be implemented first

## Ingestion Dummy Corporate Action 

Ingest dummy corporate action so the user journey can be tested  

> Dividend Action Example
>
> DW Example:
> ``` 
> {
>         "accountID": "b25f0d36-b4e4-41f8-b3d9-9249e46402cd.1491330741850",
>         "accountNo": "DWEF000010",
>         "userID": "b25f0d36-b4e4-41f8-b3d9-9249e46402cd",
>         "transaction": {
>             "accountAmount": 2.29,
>             "accountBalance": 128195.27,
>             "comment": "BPY dividend, $0.3043/share",
>             "finTranID": "GF.861e931d-e7aa-47c8-b87a-b1e55acf3862",
>             "wlpFinTranTypeID": "e8ff5103-ad40-4ed9-b2ee-fd96826bf935",
>             "finTranTypeID": "DIV",
>             "feeSec": 0,
>             "feeTaf": 0,
>             "feeBase": 0,
>             "feeXtraShares": 0,
>             "feeExchange": 0,
>             "instrument": {
>                 "id": "476b40ee-3ff9-46af-85af-fec572d13d23",
>                 "symbol": "BPY",
>                 "name": "Brookfield Property Partners L.P."
>             },
>             "dividend": {
>                 "type": "CASH",
>                 "amountPerShare": 0.3043,
>                 "taxCode": "NON_TAXABLE"
>     }
> }
> ```
> BB Example
> POST /integration-api/v1/portfolio-corporate-actions
> ``` 
> {
>   "id": "f8bc42eb-b973-434c-a100-69e9d248b568",
>   "portfolioId": "9b5f82f7-2336-4efa-aff8-67e74960ec51",
>   "corporateActionId": "e8656f12-45c5-4dc9-a945-1b7b4fd873f3",
>   "electionQuantity": 17,
>   "electionValue": {
>     "amount": 34.58,
>     "currency": "USD"
>   },
>   "amountPerShare": 2.03,
>   "additions": {
>     "someKey": "someValue"
>   }
> }
> ```

> Field Mapping
>
> | **BB Field**            | **DW Field**                          | **Description **                                |
> | ----------------------- | ------------------------------------- | ----------------------------------------------- |
> | portfolioId             | resolvePortfolioInternalId(accountNo) | resolve portfolio internal id by account number |
> | corporateActionId       | finTranTypeID                         |                                                 |
> | electionQuantity.amount | accountAmount                         |                                                 |
> | amountPerShare          | transaction.dividend.amountPerShare   |                                                 |
> | additions.taxCode       | transaction.dividend.taxCode          |                                                 |
> | additions.type          | transaction.dividend.type             |                                                 |

## Instruments & Prices & Exchange

Get all available instruments, prices, and their Exchange from these endpoints 

Instrument List: [https://developer.drivewealth.com/reference/list-all-instruments](https://developer.drivewealth.com/reference/list-all-instruments)  

Exchange & Price: [https://developer.drivewealth.com/reference/get-instrument-details-fundamentals](https://developer.drivewealth.com/reference/get-instrument-details-fundamentals) 

The below table mentions what instruments to ingest in DBS

| **Instrument type** | **Adoption in Modelbank** |
| ------------------- | ------------------------- |
| ADR                 | Yes                       |
| ALTERNATIVE_ASSET   | :cross_mark:              |
| CRYPTO              | :cross_mark:              |
| CYBER_SECURITY      | :cross_mark:              |
| EQUITY              | Yes                       |
| ETF                 | Yes                       |
| ETN                 | :cross_mark:              |
| FX                  | :cross_mark:              |
| INTEREST_RATE       | :cross_mark:              |
| METALS              | :cross_mark:              |
| MUTUAL_FUND         | :cross_mark:              |

> Instruments Stream Bundle Example
>
> ```json 
> {
>     "id": "d01c0638-fd68-4b08-a0b6-dba4c26a0673",
>     "name": "GoHealth, Inc.",
>     "instrumentNameLong": "GoHealth, Inc.",
>     "code": "GOCO",
>     "instrumentCodeCustom": "GOCO",
>     "priceData": {
>       "price": {
>         "amount": 13.3,
>         "currencyCode": "USD"
>       },
>       "closePrice": 13.3
>     },
>     "price": {
>       "amount": 13.3,
>       "currencyCode": "USD"
>     },
>     "subAssetClassCode": "EQUITY",
>     "country": "US",
>     "exchange": "NAQ",
>     "about": {
>       "about": "GoHealth, Inc. is a Medicare-focused digital health company. Its technology platform leverages modern machine-learning algorithms powered by insurance behavioral data to reimagine the optimal process for helping individuals find the health insurance plan for their specific needs. The Company operates its business in four segments: Medicare-Internal, Medicare-External, Individual and Family Plans (IFP) and Other-Internal, and IFP and Other-External. The Medicare segments focus on sales of Medicare Advantage, Medicare Supplement, Medicare prescription drug plans, and Medicare Special Needs Plans (SNPs) for multiple carriers. The Medicare Internal and External segments primarily consist of sales of products and plans through agents or external agencies. The IFP and Other segments focuses on sales of individual and family plans, dental plans, vision plans and other ancillary plans to individuals that are not Medicare-eligible. It offers services through its Encompass Platform.",
>       "link": "https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=0001808220&owner=include&count=40&hidefilings=0"
>     },
>     "canTrade": true
>   }
> ```

> DriveWealth Instruments file
>
> [instruments.json](./attachments/instruments.json)

> API Mapping
>
> | **BB API Field**                        | **Source system**               | **value**                               | **Descritpion**                                                                     |
> | --------------------------------------- | ------------------------------- | --------------------------------------- | ----------------------------------------------------------------------------------- |
> | id                                      | DriveWealth                     | Id                                      |                                                                                     |
> | name                                    | DriveWealth                     | name                                    |                                                                                     |
> | instrumentNameLong                      | DriveWealth                     | name                                    |                                                                                     |
> | code                                    | DriveWealth                     | ISIN                                    |                                                                                     |
> | instrumentCodeCustom                    | DriveWealth                     | symbol                                  |                                                                                     |
> | priceData.price.amount                  | DriveWealth                     | lastTrade                               | current price from referen tial quotes                                              |
> | priceData.price.currencyCode            | Constant                        | USD                                     |                                                                                     |
> | priceData.closePrice                    | DriveWealth                     | ClosePrior                              |                                                                                     |
> | buyPrice.amount                         | DriveWealth                     | buyPrice                                | Not visible on UI
> Q : is bidPrice from DBS == buyPrice from DW
> will be deprecated  |
> | buyPrice.currencyCode                   | Constant                        | USD                                     |                                                                                     |
> | sellPrice.amount                        | DriveWealth                     | askPrice                                | Not visible on UI
> Q : is sellPrice from DBS == askPrice from DW
> will be deprecated |
> | sellPrice.currencyCode                  | Constant                        | USD                                     |                                                                                     |
> | priceData.askPrice                      | DriveWealth                     | askPrice                                |                                                                                     |
> | priceData.bidPrice                      | DriveWealth                     | bidPrice                                |                                                                                     |
> | priceData.closePrice                    | DriveWealth                     | ClosePrior                              |                                                                                     |
> | priceData.highPrice                     | DriveWealth                     | highPrice                               |                                                                                     |
> | priceData.lowPrice                      | DriveWealth                     | lowPrice                                |                                                                                     |
> | priceData.openPrice                     | DriveWealth                     | openPrice                               | When the market is closed DW return openPrice as 0.0                                |
> | priceData.pricePerformance              | DriveWealth                     | (current price - closePrior)/closePrior |                                                                                     |
> | priceData.pricePerformanceAbs           | DriveWealth                     | current price - closePrior              |                                                                                     |
> | priceData.totalVolume                   | DriveWealth                     | cumulativeVolume                        |                                                                                     |
> | priceData.week52Range.min               | DriveWealth                     | fiftyTwoWeekLowPrice                    | will be deprecated and replaced as separate field                                   |
> | priceData.week52Range.max               | DriveWealth                     | fiftyTwoWeekHighPrice                   | will be deprecated as separate field                                                |
> | pricePerformance                        | DriveWealth                     |                                         |                                                                                     |
> | subAssetClassCode                       | DriveWealth                     | type                                    |                                                                                     |
> | country                                 | Constant                        | USA                                     |                                                                                     |
> | exchange                                | DriveWealth                     | exchange                                |                                                                                     |
> | about.about                             | DriveWealth                     | description                             |                                                                                     |
> | about.link                              | DriveWealth                     | url                                     |                                                                                     |
> | canTrade                                | DriveWealth                     | status                                  | If status Active then yes else false                                                |
> | iconUrl                                 | DriveWealth                     | image                                   |                                                                                     |
> | keyStatistics.fundStatus                | DriveWealth                     | status                                  | not related to equrity                                                              |
> | keyStatistics.portfolioAssetsAllocation | DriveWealth or Constant or skip | type                                    | not related to equrity                                                              |
> | keyStatistics.priceToEarningsRatio      | DriveWealth                     | fundamentalDataModel.peRatio            |                                                                                     |
> | keyStatistics.sharesOutstanding         | DriveWealth                     | fundamentalDataModel.sharesOutstanding  |                                                                                     |
> | keyStatistics.priceToBookRatio          | DriveWealth                     | fundamentalDataModel.pbRatio            |                                                                                     |
> | keyStatistics.earningsPerShare          | DriveWealth                     | fundamentalDataModel.earningsPerShare   |                                                                                     |
> | keyStatistics.dividendYield             | DriveWealth                     | fundamentalDataModel.dividendYield      |                                                                                     |



## Pull Request



TBD
