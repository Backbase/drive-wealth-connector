# Update Portfolio Transactions

### [https://developer.drivewealth.com/reference/list-all-account-transactions](https://developer.drivewealth.com/reference/list-all-account-transactions)  

BB API: `POST /integration-api/v1/portfolios/{code}/transactions`
DW API: `GET /back-office/accounts/{accountID}/transactions`

**API mapping**

| **BB Field **                 | **DW Field**                                   | **Description**                                                                                                           |
| ----------------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| positionId###### REQUIRED
    | `accountID` + “-” +`instrument`.`instrumentID` | StringPosition ID.                                                                                                        |
| transactionId###### REQUIRED
 | `finTranID`                                    | StringUnique transaction ID to identify a transaction.                                                                    |
| transactionDate               | `tranWhen`                                     | DateTimeThe date on which the transaction was booked.                                                                     |
| valueDate                     | `tranWhen`                                     | DateTimeThe date when transaction was executed.                                                                           |
| transactionCategory           | `finTranTypeID`                                | StringTransaction Category key.                                                                                           |
| exchange                      | instrument.exchange                            | StringThe marketplace where the transaction was executed.                                                                 |
| orderType                     | `orderType`                                    | StringType of the order.

Additional call to DW Order API to get type                                                     |
| counterpartyName              | N/A                                            | StringA name of the transaction counterparty.

Not relevant for equity txn                                                |
| counterpartyAccount           | N/A                                            | StringThe account id of the transaction counterparty.

Not relevant for equity txn                                        |
| quantity                      | `fillQty`                                      | BigDecimalThe number of units in the transaction operation.                                                               |
| price                         | `fillPx`                                       | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| amount                        | `tranAmount`                                   | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| amountGross                   | `tranAmount` + sum of Fees from DW             | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| fxRate                        | ONE                                            | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| localTaxes                    | Zero                                           | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| localFees                     | Sum of all fees                                | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| foreignTaxes                  | Zero                                           | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| foreignFees                   | Zero                                           | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| officialCode                  | `instrument.ISIN`                              | StringOfficial code to identify the instrument.                                                                           |
| ISIN                          | `instrument.ISIN`                              | StringThe instrument international code.                                                                                  |
| balanceAsset                  | `accountBalance`                               | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| balanceAmount                 | `accountBalance`                               | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-integration-inbound-api/1.0.11/models/money/)   |
| statusId                      | `GbCUNyBjXCXNdV`                               | StringA status identificator of the transaction. String type to be able to accomodate both numerical and string statuses. |
| statusName                    | Accepted                                       | StringA name or description of the transaction status. E.g. Live, Pending                                                 |
| statusAbbr                    | Accepted                                       | StringThe abbreviation of the transaction status the way it must be displayed on the UI.                                  |
| notes                         | N/A                                            | StringThe description or notes related to the operation.                                                                  |
