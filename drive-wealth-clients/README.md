# DriveWealth Clients

## Overview
This project is used to generate the DriveWealth Client Libraries used by the DBS integration services.

### How to generate the clients
If you have updated an existing specification or added a new, run the following command to generate the new client(s):

`mvn clean install -Pgenerate-clients`

Note: As the generated clients are stored in Git you are not required to run this command if you've made no changes.  

### How to install the clients
To install the generated clients into you local Maven repository run:

`mvn clean install -Pinclude-clients`
