# Bulk Funding Settlement

## Background

Each participant of DriveWealth must select one of the two types of money movement flow that are offered. For the purpose of moving money into Drive Wealth, Backbase has chosen to use bulk funding.

Bulk financing occurs automatically each day at night, whereas DW's UAT environment settlement is a manual procedure. For this reason, we designed a Cron task to settle the money from the previous day at  [8 AM Amsterdam time](https://github.com/baas-devops-reference/reference-applications-live/blob/main/azure/runtimes/wealth-stg/apps/values.yaml#L253)


> **Bulk Funding**
> Bulk Funding is aggregating customer deposits and sends a single wire per day to be allocated across customer accounts.

> ### Two Ways Cashless Settlements
> Two Way Cashless Settlements are aggregating all the “buys” orders together and aggregating separately all the “sells” orders for a given trading day to be settled on T+2.
> The result is two numbers +X “buys” and -X “sells.” Our partner will send us the +X and DriveWealth will send the -X for the given settlement day.
> For partners that use two-way cashless, there is no concept of customers self-funding their brokerage account.
> Customer accounts are cashless and all money is transferred, in aggregate, betweenDriveWealth and the Partner.

## Responsible Service

Modelbank [data-sync-service](https://github.com/baas-devops-reference/wealth-data-sync-service) is responsible to automate this manual activity into a corn job

## API Flow

<video src="./attachments/Screen%20Recording%202023-04-25%20at%2014.48.19.mov" placeholder="Screen Recording 2023-04-25 at 14.48.19.mov" autoplay loop controls muted title="Screen Recording 2023-04-25 at 14.48.19.mov">Sorry, your browser doesn't support HTML 5 video.</video>

