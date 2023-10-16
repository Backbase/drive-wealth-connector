# Update Instrument Holdings

###  [https://developer.drivewealth.com/reference/get-account-summary](https://developer.drivewealth.com/reference/get-account-summary) 

BB API: `POST /integration-api/v1/instrument-holdings`
DW API: `GET /back-office/accounts/{accountID}/summary`



**API mapping **

| **BB Field**                    | **DW Field**                                                   | Description                                                                                                                                             |
| ------------------------------- | -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| externalId###### REQUIRED
      | `symble + "_" + userId`                                        | StringInstrument holdings external id.**Constraints***Max Length: 36*                                                                                   |
| instrumentId###### REQUIRED
    | `instrumentID`                                                 | StringInstrument external id that will be assigned to the holdings.**Constraints***Max Length: 36*                                                      |
| portfolioIds###### REQUIRED
    | `accountNo`                                                    | array[String]Portfolio external ids that will be assigned to the holdings.**Constraints***Minimum items:1*                                              |
| totalPLPct###### REQUIRED
      | `totalPL / (totalHoldings * avgBuyPrice.getAsDouble()) * 100d` | BigDecimalTotal profit or loss on an open positions in all portfolios, expressed as a percentage.                                                       |
| totalPL###### REQUIRED
         | `unrealizedPL`                                                 | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)                                 |
| todayPLPct###### REQUIRED
      | `unrealizedDayPLPercent`                                       | BigDecimalToday profit or loss on an open positions in all portfolios, expressed as a percentage.                                                       |
| todayPL###### REQUIRED
         | `unrealizedDayPL`                                              | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)                                 |
| pctOfPortfolios###### REQUIRED
 | `(total marketValue / totalEquity) * 100d`                     | BigDecimalPercentage instrument valuation in total portfolios valuation,
where totalEquity taken to the calculations is a sum of both: equity and cash. |
| buyPrice###### REQUIRED
        | `avgPrice`                                                     | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)                                 |
