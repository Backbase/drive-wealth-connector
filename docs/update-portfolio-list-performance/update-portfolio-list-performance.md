# Update Portfolio List Performance

## Story

[https://backbase.atlassian.net/browse/MWM-75](https://backbase.atlassian.net/browse/MWM-75) 

###  [https://developer.drivewealth.com/reference/get-account-summary](https://developer.drivewealth.com/reference/get-account-summary)

BB API: `PUT /integration-api/v1/portfolios/{code}`
DW API: `GET /back-office/accounts/{accountID}/summary`

## **API Mapping**

| **FE Field Lable**                        | **BB Field**         | **DW Field**                                                                                                                           | Description                                                                                               |
| ----------------------------------------- | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| Value without label (portfolio Valuation) | valuation            | `accountSummary`.`cash`.`cachBalance` + `accountSummary`.`equity`.`equityValue`                                                        | Monetary amount of portfolio valuation.                                                                   |
| Ytd Performance                           | ytdPerformance       | [https://backbase.atlassian.net/wiki/spaces/PROMO/pages/4249290148](https://backbase.atlassian.net/wiki/spaces/PROMO/pages/4249290148) | Refer to: PORTFOLIO OVERVIEW                                                                              |
| YTD Performance Value                     | ytdPerformanceValue  | [https://backbase.atlassian.net/wiki/spaces/PROMO/pages/4249290148](https://backbase.atlassian.net/wiki/spaces/PROMO/pages/4249290148) | Refer to: PORTFOLIO OVERVIEW                                                                              |
| MTD Performance                           | mtdPerformance       | [https://backbase.atlassian.net/wiki/spaces/PROMO/pages/4249290148](https://backbase.atlassian.net/wiki/spaces/PROMO/pages/4249290148) | Refer to: PORTFOLIO OVERVIEW                                                                              |
| MTD Performance Value                     | mtdPerformanceValue  | [https://backbase.atlassian.net/wiki/spaces/PROMO/pages/4249290148](https://backbase.atlassian.net/wiki/spaces/PROMO/pages/4249290148) | Refer to: PORTFOLIO OVERVIEW                                                                              |
| In                                        | inValue              | Sum of all **deposits**                                                                                                                | [Deposits API](https://developer.drivewealth.com/reference/get_accounts-accountid-funding-deposits)       |
| Out                                       | outValue             | Sum of all **withdrawals**                                                                                                             | [Withdrawals API](https://developer.drivewealth.com/reference/get_accounts-accountid-funding-redemptions) |
| Net                                       | netValue             | inValue-outValue                                                                                                                       |                                                                                                           |
| Value without label (portfolio Valuation) | valuationRefreshDate | lastUpdated                                                                                                                            | DateTimeDate of last refresh of valuation numbers for portfolio.                                          |
