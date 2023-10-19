#!/bin/sh

# Digital Banking (relevant) database names
DB_TO_DELETE=(db-access-control db-arrangement-manager db-user-manager db-transaction-manager db-contact-manager db-approval-service db-messages db-limit db-loan db-payment-order-service)

clear

# Run Digital Banking runtime clean up
echo "====================== Digital Banking Runtime Clean Up Start ====================== "
./runtime-cleanup.sh ${DB_TO_DELETE[*]}
echo "====================== Digital Banking Runtime Clean Up Finish ====================== "