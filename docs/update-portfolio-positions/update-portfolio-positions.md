# Update Portfolio Positions

###  [https://developer.drivewealth.com/reference/position-summary](https://developer.drivewealth.com/reference/position-summary)

BB API: `PUT /integration-api/v1/portfolios/{portfolioId}/bulk-positions`
DW API: `GET /back-office/accounts/{accountID}/summary`



**API mapping**



| **BB Field**               | **DW Field**                                 | **Description**                                                                                                          |
| -------------------------- | -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| externalId###### REQUIRED
 | accountNo + "-" + instrumentID               | StringPositions external id.                                                                                             |
| instrumentId               | instrumentID                                 | StringInstrument external id.                                                                                            |
| portfolioCode              | accountNo                                    | StringUnique portfolio identificator.                                                                                    |
| absolutePerformance        | `unrealizedPL`                               | [**money**](https://backbase.io/developers/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.14/models/money/)  |
| relativePerformance        | `unrealizedPL`/`costBasis`                   | BigDecimalRelative price change over a period of time, expressed as a percentage.                                        |
| purchasePrice.amount       | avgPrice                                     | [**money**](https://backbase.io/developers/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.14/models/money/)  |
| unrealizedPLPct            | unrealizedPL / costBasis                     | BigDecimalUnrealized current profit or loss on an open position, expressed as a percentage                               |
| unrealizedPL               | unrealizedPL                                 | [**money**](https://backbase.io/developers/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.14/models/money/)  |
| todayPLPct                 | unrealizedDayPLPercent                       | BigDecimalToday profit or loss on an open position, expressed as a percentage.                                           |
| todayPL.amount             | unrealizedDayPL                              | .[**money**](https://backbase.io/developers/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.14/models/money/) |
| accruedInterest.amount     | 0                                            | [**money**](https://backbase.io/developers/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.14/models/money/)  |
| quantity                   | openQty                                      | BigDecimalPosition quantity; amount of instrument units in a portfolio                                                   |
| valuation.amount           | marketValue                                  | [**money**](https://backbase.io/developers/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.14/models/money/)  |
| costPrice.amount           | costBasis                                    | [**money**](https://backbase.io/developers/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.14/models/money/)  |
| costExchangeRate.amount    | ONE                                          | [**money**](https://backbase.io/developers/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.14/models/money/)  |
| percentAssetClass          | (marketValue/equityValue)* 100               | BigDecimalShare of the instrument position in an asset class, expressed as a percentage.                                 |
| percentPortfolio           | (marketValue/EquityValue + cashBalance)* 100 | BigDecimalShare of the instrument position in a portfolio, expressed as a percentage.                                    |
| `percentParent`            | (marketValue/EquityValue)* 100               | StringA type of the position the given transaction belongs to. E.g. Cash position or Security.                           |
| positionType               | Equity                                       | StringA type of the position the given transaction belongs to. E.g. Cash position or Security.                           |
