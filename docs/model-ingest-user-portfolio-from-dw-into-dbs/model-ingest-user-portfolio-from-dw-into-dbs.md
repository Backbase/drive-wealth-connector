# Model & Ingest User Portfolio from DW into DBS

## Table of Contents 
 -- [User Story ](#user-story-)
-- [Ingest Portfolios](#ingest-portfolios)
-- [Ingest Aggregated Portfolios](#ingest-aggregated-portfolios)
-- [Create and Assign Benchmarks to Each Portfolio](#create-and-assign-benchmarks-to-each-portfolio)
-- [Ingest Portfolios Position Hierarchies ](#ingest-portfolios-position-hierarchies-)
-- [Pull and Ingest Portfolios Positions](#pull-and-ingest-portfolios-positions)
-- [Pull and Ingest Portfolios Transaction](#pull-and-ingest-portfolios-transaction)
-- [Pull and Ingest Portfolios Valuations](#pull-and-ingest-portfolios-valuations)
-- [Pull and Ingest Portfolio Allocation.](#pull-and-ingest-portfolio-allocation.)
## User Story 

[https://backbase.atlassian.net/browse/MWM-37](https://backbase.atlassian.net/browse/MWM-37) 

## Ingest Portfolios

As the Legal Entities have been created previously by  [https://backbase.atlassian.net/browse/MWM-33](https://backbase.atlassian.net/browse/MWM-33) Ingest User’s portfolios to DBS 

> Sample portfolio Json
>
> ``` 
> {
>   "portfolios": [
>     {
>       "code": "960464",
>       "name": "Peden, Mr Neil - Execution Only - US$",
>       "alias": "Peden, Mr Neil - Execution Only - US$",
>       "iban": "960464",
>       "valuation": {
>         "amount": 1154515.45,
>         "currencyCode": "USD"
>       },
>       "ytdPerformance": -44.77,
>       "ytdPerformanceValue": {
>         "amount": 0,
>         "currencyCode": "USD"
>       },
>       "mtdPerformanceValue": {
>         "amount": 0,
>         "currencyCode": "USD"
>       },
>       "inValue": {
>         "amount": 0,
>         "currencyCode": "USD"
>       },
>       "outValue": {
>         "amount": 0,
>         "currencyCode": "USD"
>       },
>       "netValue": {
>         "amount": 0,
>         "currencyCode": "USD"
>       },
>       "valuationRefreshDate": "2022-11-29T00:00:00.000Z"
>     }
>   ]
> }
> ```

> API Mapping
>
> | **BB API Field**                 | **source**      | **Field**                  | **Description**                                                                                                                                                                       | **Business Description** |
> | -------------------------------- | --------------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------ |
> | code                             | DW              | AccountId or AccountNumber | TBD                                                                                                                                                                                   |                          |
> | name                             | DBS arrangement | arrangement. name          |                                                                                                                                                                                       |                          |
> | alias                            | DBS arrangement | .alias                     |                                                                                                                                                                                       |                          |
> | IBAN                             | DW              | AccountId or AccountNumber |                                                                                                                                                                                       |                          |
> | valuation. amount                | DW              | `0`                        | Monetary amount of portfolio valuation.
> DW API → [https://developer.drivewealth.com/reference/get-account-summary](https://developer.drivewealth.com/reference/get-account-summary)  |                          |
> | valuation.currencyCode           | Constant        | USD                        |                                                                                                                                                                                       |                          |
> | ytdPerformance                   | Constant        | 0                          | Custom Logic in service layer                                                                                                                                                         |                          |
> | ytdPerformanceValue.amount       | Constant        | 0                          | Custom Logic in service layer                                                                                                                                                         |                          |
> | ytdPerformanceValue.currencyCode | Constant        | USD                        | Custom Logic in service layer                                                                                                                                                         |                          |
> | mtdPerformanceValue.amount       | Constant        | 0                          | Custom Logic in service layer                                                                                                                                                         |                          |
> | mtdPerformanceValue.currencyCode | Constant        | USD                        | Custom Logic in service layer                                                                                                                                                         |                          |
> | inValue.amount                   | Constant        | 0                          | Custom Logic in service layer                                                                                                                                                         |                          |
> | inValue.currencyCode             | Constant        | USD                        | Custom Logic in service layer                                                                                                                                                         |                          |
> | outValue.amount                  | Constant        | 0                          | Custom Logic in service layer                                                                                                                                                         |                          |
> | outValue.currencyCode            | Constant        | USD                        | Custom Logic in service layer                                                                                                                                                         |                          |
> | netValue.amount                  | DW              | TBD                        |                                                                                                                                                                                       |                          |
> | netValue.currencyCode            | DW              | TBD                        |                                                                                                                                                                                       |                          |
> | valuationRefreshDate             | Constant        | currentDate                |                                                                                                                                                                                       |                          |

## Ingest Aggregated Portfolios

Aggregated portfolios are a summary of all user’s portfolios

> Aggregated portfolios data is the sumation of the user’s portfolios value
> EX: aggregated portfolios p = p1.equity + p2.equity
> where p1 and p2 are users account.

> API Mapping
>
> | **BB API Field**                 | **DW API Field** | **Description** |
> | -------------------------------- | ---------------- | --------------- |
> | id                               | TBD              | TBD             |
> | name                             |                  |                 |
> | portfoliosCount                  |                  |                 |
> | riskLevel                        |                  |                 |
> | valuation.amount                 |                  |                 |
> | valuation.currencyCode           |                  |                 |
> | ytdPerformance                   |                  |                 |
> | ytdPerformanceValue.amount       |                  |                 |
> | ytdPerformanceValue.currencyCode |                  |                 |
> | mtdPerformance                   |                  |                 |
> | mtdPerformanceValue.amount       |                  |                 |
> | mtdPerformanceValue.currencyCode |                  |                 |
> | inValue.amount                   |                  |                 |
> | inValue.currencyCode             |                  |                 |
> | outValue.amount                  |                  |                 |
> | outValue.currencyCode            |                  |                 |
> | netValue.amount                  |                  |                 |
> | netValue.currencyCode            |                  |                 |
> | portfolios                       |                  |                 |
> | allocations[].name               |                  |                 |
> | percentage[].percentage          |                  |                 |
> | managers.id                      |                  |                 |
> | managers.name                    |                  |                 |
> | managers.link                    |                  |                 |
> | valuationRefreshDate             |                  |                 |

## Create and Assign Benchmarks to Each Portfolio

> Create Benchmark Example Request
>
> `POST /integration-api/v1/benchmarks`
> ``` 
> {
>   "id": "{{benchmarklId}}",
>   "name": "{{BenchmarkName}}"
> }
> ```

| **Benchmark ID **
**(DW InstrumentID)** | **Name** | **DW Instrument** |
| --------------------------------------- | -------- | ----------------- |
| TBD                                     | TBD      | TBD               |
| TBD                                     | TBD      | TBD               |

Assign Benchmarks to each portfolio

> Assign Benchmark to Portfolios
>
> `PUT /integration-api/v1/benchmarks/{id}/portfolios`
> ``` 
> {
>   "portfolios": [
>     "{{portfolioInternalId}}"
>   ]
> }
> ```

> Initialize Benchmark Chart
> Ingest the data for last 1 year from driveWealth
> DriveWealth Api: [https://developer.drivewealth.com/reference/get-charts](https://developer.drivewealth.com/reference/get-charts) 

> Benchmark chart data example
>
> `PUT /integration-api/v1/benchmarks/{id}/charts`
> ``` 
> {
>   "chartData": [
>     {
>       "value": 123.24,
>       "date": "2020-01-01"
>     },
>     {
>       "value": 222.123,
>       "date": "2020-01-01"
>     },
>     {
>       "value": 77.99,
>       "date": "2020-01-02"
>     }
>   ]
> }
> ```

## Ingest Portfolios Position Hierarchies 

Setup position hierarchies for each portfolio as the following

> Position Hierarchies Example
>
> ``` 
> {
>   "batchPortfolioPositionsHierarchies": [
>     {
>       "portfolioCode": "960464",
>       "hierarchies": [
>         {
>           "itemType": "ASSET_CLASS",
>           "externalId": "equity",
>           "name": "Equity",
>           "percentOfParent": 100
>         },
>         {
>           "itemType": "REGION",
>           "externalId": "north-america",
>           "name": "North America Region",
>           "percentOfParent": 100
>         }
>       ]
>     }
>   ]
> }
> ```

## Pull and Ingest Portfolios Positions

Pull account positions from [https://developer.drivewealth.com/reference/position-summary](https://developer.drivewealth.com/reference/position-summary) 

> Position Stream Example
>
> ``` 
> {
>     "externalId": 9659164,
>     "instrumentId": -10020,
>     "portfolioCode": "GYRA0001",
>     "subPortfolioCode": 1549392,
>     "quantity": "537.6000000000017",
>     "absolutePerformance": {
>         "amount": "0.000",
>         "currencyCode": "CAD"
>     },
>     "relativePerformance": "0.00",
>     "valuation": {
>         "amount": "727.14",
>         "currencyCode": "CAD"
>     },
>     "purchasePrice": {
>         "amount": "727.14",
>         "currencyCode": "CAD"
>     },
>     "costPrice": {
>         "amount": "1.00",
>         "currencyCode": "CAD"
>     },
>     "unrealizedPLPct": "0.00",
>     "unrealizedPL": {
>         "amount": "0.00",
>         "currencyCode": "CAD"
>     },
>     "percentPortfolio": "0.25",
>     "positionType": "Cash"
> }
> ```

> API Mapping
>
> | **BB API Field**           | **DW API Field** | **Description** |
> | -------------------------- | ---------------- | --------------- |
> | externalId`                | TBD              | TBD             |
> | instrumentId               |                  |                 |
> | portfolioCode              |                  |                 |
> | subPortfolioCode           |                  |                 |
> | quantity                   |                  |                 |
> | absolutePerformance.amount |                  |                 |
> | relativePerformance        |                  |                 |
> | valuation.amount           |                  |                 |
> | purchasePrice.amount       |                  |                 |
> | costPrice.amount           |                  |                 |
> | unrealizedPLPct            |                  |                 |
> | unrealizedPL.amount        |                  |                 |
> | percentPortfolio           |                  |                 |
> | positionType               |                  |                 |

## Pull and Ingest Portfolios Transaction

Pull account transactions from [https://developer.drivewealth.com/reference/list-all-account-transactions](https://developer.drivewealth.com/reference/list-all-account-transactions) 

> API Mapping
>
> | **BB API Field** | **DW API Field** | **Description** |
> | ---------------- | ---------------- | --------------- |
> | TBD              | TBD              | TBD             |
> |                  |                  |                 |

## Pull and Ingest Portfolios Valuations

Pull account valuation from [https://developer.drivewealth.com/reference/account-performance](https://developer.drivewealth.com/reference/account-performance)  

> API Mapping
>
> | **BB API Field**                                 | **DW API Field** | **Description** |
> | ------------------------------------------------ | ---------------- | --------------- |
> | portfolioCode                                    | TBD              | TBD             |
> | transactions.positionId                          |                  |                 |
> | transactions.transactions.transactionId          |                  |                 |
> | transactions.transactions.transactionDate        |                  |                 |
> | transactions.transactions.valueDate              |                  |                 |
> | transactions.transactions.transactionCategory    |                  |                 |
> | transactions.transactions.exchange               |                  |                 |
> | transactions.transactions.orderType              |                  |                 |
> | transactions.transactions.quantity               |                  |                 |
> | transactions.transactions.price.amount           |                  |                 |
> | transactions.transactions.price.currencyCode     |                  |                 |
> | transactions.transactions.amount.amount          |                  |                 |
> | transactions.transactions.amount.currencyCode    |                  |                 |
> | transactions.transactions.localFees.amount       |                  |                 |
> | transactions.transactions.localFees.currencyCode |                  |                 |
> | transactions.transactions.notes                  |                  |                 |

## Pull and Ingest Portfolio Allocation.

pull account valuation from [https://developer.drivewealth.com/reference/get-account-summary](https://developer.drivewealth.com/reference/get-account-summary) 

> API Mapping
>
> | **BB API Field**                                   | **DW API Field** | **Description** |
> | -------------------------------------------------- | ---------------- | --------------- |
> | portfolioCode                                      | TBD              | TBD             |
> | allocations[].allocationType                       |                  |                 |
> | allocations[].classifierType                       |                  |                 |
> | allocations[].classifierName                       |                  |                 |
> | allocations[].allocationPct                        |                  |                 |
> | allocations[].valuation.amount                     |                  |                 |
> | allocations[].valuation.currencyCode               |                  |                 |
> | allocations[].allocations[].classifierType         |                  |                 |
> | allocations[].allocations[].classifierName         |                  |                 |
> | allocations[].allocations[].allocationPct          |                  |                 |
> | allocations[].allocations[].valuation.amount       |                  |                 |
> | allocations[].allocations[].valuation.currencyCode |                  |                 |
> | allocations[].allocations[].                       |                  |                 |
> | allocations[].allocations[].                       |                  |                 |
> | allocations[].allocations[].                       |                  |                 |


