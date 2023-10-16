**INFO**

This folder contains shell scripts to automate manual tasks that BE developers perform before running bootstrap task
with clean data setup.

## Disclaimer
> These scripts are provided as an example to clean up a development runtime 
> and not it's recommended to use on production runtime. 

## Pre-requisite

* You are already connected to k8 cluster from your terminal
* You have already installed [jq](https://stedolan.github.io/jq/download/)

## Script overview

### [env_backup.sh](env_backup.sh)
Helper script to gather backups of the following resources -
* All realms in Identity
* All roles, clients, users in each realm in Identity

### [digital-banking-runtime-cleanup.sh](digital-banking-runtime-cleanup.sh)
Helper script to delete dbs capabilities database resources from a BaaS env
The following will be performed -
* Delete all users from all realms in Identity
* Delete the following capabilities databases based on input argument
  * db-access-control
  * db-arrangement-manager
  * db-user-manager
  * db-transaction-manager
  * db-contact-manager
  * db-approval-service
  * db-messages

### [digital-onboarding-runtime-cleanup.sh](digital-onboarding-runtime-cleanup.sh)
Helper script to delete dbs capabilities database resources from a BaaS env
The following will be performed -
* Delete all users from all realms in Identity
* Delete the following capabilities databases based on input argument 
  * db-access-control 
  * db-arrangement-manager 
  * db-user-manager 
  * db-transaction-manager 
  * db-contact-manager

### [runtime-cleanup.sh](runtime-cleanup.sh)
Helper script to delete dbs capabilities database resources from a BaaS env
The following will be performed -
* Delete all users from all realms in Identity
* Delete the capabilities databases based on input argument


### [port-forward.sh](port-forward.sh)
Helper script to start port-forward session for all service described in the script file. 

### [approval-cleanup.sh](approval-cleanup.sh)
Helper script to clean up only approval service database.

### [topics-cleanup.sh](topics-cleanup.sh)
Helper script to clean up only messaging topics for Messages DBS Service database.

> All the above scripts are using [poststarthook.yaml](poststarthook.yaml) helper config map to  store all DB sql drop scripts for SQL Server DB

At the end of the script, when the flux sync is run, the entire env, along with all deleted resources will get recreated, including databases themselves.
The delete process would take about 5-10 minutes, and recreating would take about 20-30 minutes.

## Steps to Run

To Run cleanup script for a runtime
#### Digital Baning Runtime Cleanup 
Script [digital-banking-runtime-cleanup.sh](digital-banking-runtime-cleanup.sh)

Example:
```
sh digital-banking-runtime-cleanup.sh
```
#### Digital Onboarding Runtime Cleanup
Script [digital-onboarding-runtime-cleanup.sh](digital-onboarding-runtime-cleanup.sh)

Example:
```
sh digital-onboarding-runtime-cleanup.sh
```

## Run on Windows

If you have problems to run scripts on Windows(WSL), try to change first line of a script:

`#!/bin/sh` -> `#!/bin/bash`

You may also need to convert file format from Windows to Unix - you can use `dos2unix` command