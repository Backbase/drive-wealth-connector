#!/bin/sh

# Digital Onboarding (relevant) database names
DB_TO_DELETE=(db-access-control db-arrangement-manager db-user-manager db-transaction-manager db-contact-manager)

clear

# Run Digital Onboarding runtime clean up
echo "====================== Digital Onboarding Runtime Clean Up Start ====================== "
./runtime-cleanup.sh ${DB_TO_DELETE[*]}
echo "====================== Digital Onboarding Runtime Clean Up Finish====================== "