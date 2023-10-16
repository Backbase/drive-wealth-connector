# Update Portfolio Allocation

### [https://developer.drivewealth.com/reference/get-account-summary](https://developer.drivewealth.com/reference/get-account-summary) 

BB API: `PUT /integration-api/v1/portfolios/{code}/allocations`
DW API: `GET /back-office/accounts/{accountID}/summary`

**API mapping**

| **BB Field**                               | **DW Field**                                   | **Description**                                                                                                                                                   |
| ------------------------------------------ | ---------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| allocationType###### REQUIRED
             | Constant: `BY_CURRENCY`                        | [**allocation-type**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/allocation-type/)                       |
| classifierType###### REQUIRED
             | Constant: `CURRENCY`or Constant: `ASSET_CLASS` | [**allocation-classifier-type**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/allocation-classifier-type/) |
| classifierName###### REQUIRED
             | Constant: `USD`or Constant: `Equity`           | StringClassifier name denotes the name of the selected classifier type; can be either an asset class name or a currency name, specified in ISO 4217 format.       |
| allocationMaxPct                           | Not required                                   | BigDecimalMaximum allocation percentage that the item can amount to in a portfolio.                                                                               |
| allocationMinPct                           | Not required                                   | BigDecimalMinimum allocation percentage that the item can be equal to in a portfolio.                                                                             |
| allocationPct###### REQUIRED
              | (equity / (equity + cash)) * 100               | BigDecimalAllocation in percent for the given item.                                                                                                               |
| valuation###### REQUIRED
                  | `cash`.`cashBalance`+ `equity`.`equityValue`   | Value is based on classifierName                                                                                                                                  |
| allocations.classifierType###### REQUIRED
 | Constant: `CURRENCY`or Constant: `ASSET_CLASS` | [**allocation-classifier-type**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/allocation-classifier-type/) |
| allocations.classifierName###### REQUIRED
 | Constant: `USD`or Constant: `Equity`           | StringClassifier name denotes the name of the selected classifier type; can be either an asset class name or a currency name, specified in ISO 4217 format.       |
| allocations.allocationPct###### REQUIRED
  | `cash`.`cashBalance`or `equity`.`equityValue`  | BigDecimalAllocation in percent for the given item.value is based on classifierName                                                                               |
| allocations.valuation###### REQUIRED
      | `cash`.`cashBalance`or `equity`.`equityValue`  | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)                                           |
