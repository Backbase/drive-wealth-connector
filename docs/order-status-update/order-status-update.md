# Order Status Update

BB API: `POST /integration-api/v1/trade-orders/{orderId}/status`

> Request Example
>
> ``` 
> {
>   "status": "Rejected",
>   "reason": "Insufficient funds in the investment account"
> }
> ```

DW Event:

> Request Example
>
> ``` 
> ...
> "status": "REJECTED",
> "statusMessage": {
>   "errorCode": "O409",
>   "message": "Order marked preventQueuing=TRUE, market is not open, rejecting."
> },
>     ...
> ```



**API Mapping**

| **BB Field**                  | **DW Field**            | Description                                                                                                                                                |
| ----------------------------- | ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| status###### REQUIRED
        | `status`                | [**target-order-status**](https://developer.backbase.com/apis/specs/portfolio/portfolio-trading-integration-inbound-api/1.0.7/models/target-order-status/) |
| reason                        | `statusMessage.message` | StringDescription of the rejection, cancellation or the failure reason                                                                                     |
| confirmedValue                | `totalOrderAmount`      | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-trading-integration-inbound-api/1.0.7/models/money/)                             |
| confirmedQuantity             | `cumulativeQuantity`    | BigDecimalConfirmed quantity of instruments sold or bought in the order                                                                                    |
| confirmedCommissionFees       | `fees`                  | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-trading-integration-inbound-api/1.0.7/models/money/)                             |
| confirmedFXCurrencyCommission | `0`                     | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-trading-integration-inbound-api/1.0.7/models/money/)                             |
| confirmedInstrumentPrice      | `averagePrice`          | [**money**](https://developer.backbase.com/apis/specs/portfolio/portfolio-trading-integration-inbound-api/1.0.7/models/money/)                             |
| fulfillmentDateTime           | `lastExecuted`          | DateTimeThe time in UTC when the trading platform made partial fulfillment. Only for limit order.                                                          |




