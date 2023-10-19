# Repository Overview

implement cloud-native intuitive, Microservices design patterns, and coding best practices.
- The project follows [**CloudNative**](https://www.cncf.io/) recommendations and the [**twelve-factor app**](https://12factor.net/) methodology for building *software-as-a-service apps* to show how μServices should be developed and deployed.
- This project uses technologies broadly used in Backbase. Like Docker, Kubernetes, Java SE 11, Spring Boot, Spring Cloud
- `portfolio-outbound-integration` has been generated using `core-service-archetype` - [Community guide](https://community.backbase.com/documentation/ServiceSDK/latest/create_a_core_service)
- This service retrieves Portfolio Balances and Performance details from Drive Wealth core API and map them to DBS model. It does not persist Balances and Performance Details into DBS, instead these are always retrieved live from the core.
- The service is implementation of portfolio-outbound-integration and arrangement-integration-balance-api spec
- Refer to [workflow guide](https://github.com/baas-devops-reference/docs/tree/master/backend) for Backend CI Workflow documentation

## Repository Description
### Project Structure
The project structure for each custom integration service follows the pattern as described below :

```
.
├── .github                       # All GitHub Actions files
│   ├── ISSUE_TEMPLATE            # Templates for 'major','minor','patch' releases
│   ├── actions                   # Github reusable actions for CI
│   └── workflows                 # GitHub Actions workflows for CI
├── src                           # Source and Unit Test files
    ├── main                      # Application container projects
    │   ├── java/com/backbase/productled
    │   │   ├── api               # Controller classes
    │   │   │   └── ...
    │   │   ├── config            # Configuration classes
    │   │   │   └── ...
    │   │   ├── mapper            # Model classes
    │   │   │   └── ...
    │   │   └── service           # Service classes
    │   │       └── ...
    │   └── resources             # All resource files except core classes
    │       └── ...
    └── test                      # JUnit test file
        └── ...
```

To view individual classes for this repository, select relevant branch from the GitHub UI and then press ‘.'
This will open the GitHub Web Editor.Alternatively, you can also access the Web Editor by changing .com to .dev in the URL.

Expand each file in the Web Editor for explanation and purpose.

---
## Repository Configurations
### DSC (basic-installation.yml) configuration
TODO update this based on your implementation
```yaml
portfolio-outbound-integration:
  enabled: true
  app:
    image:
      tag: "1.0.0-SNAPSHOT"
      repository: portfolio-outbound-integration
  database: false
  livenessProbe:
    enabled: true
  readinessProbe:
    enabled: true
  env:
    "spring.autoconfigure.exclude": "org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration,org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration"
      drive-wealth.baseUrl: https://bo-api.drivewealth.io
      drive-wealth.dwClientAppKey:
        value:
          secretKeyRef:
            name: drive-wealth-credentials.yaml
            key: dwClientAppKey
      drive-wealth.clientID:
        value:
          secretKeyRef:
            name: drive-wealth-credentials.yaml
            key: clientID
      drive-wealth.clientSecret:
        value:
          secretKeyRef:
            name: drive-wealth-credentials.yaml
            key: clientSecret
      DRIVE-WEALTH_PORTFOLIOBENCHMARKLIST_0_NAME: S&P 500 Vanguard ETF
      DRIVE-WEALTH_PORTFOLIOBENCHMARKLIST_0_UUID: 639247b6-cbcd-4616-841d-890dbb926575
      drive-wealth.auth.scheduler.fixedRate: "18000000"
      drive-wealth.auth.retry.maxAttempt: "10"
      drive-wealth.auth.retry.maxDelay: "10000"
```

---
## Getting Started
### BaaS setup
- [ ] Step 1: Modify https://github.com/baas-devops-reference/ref-self-service/blob/main/self-service.tfvars by adding to `ecr` list name of new repository: `portfolio-outbound-integration'
- [ ] Step 2: Checkout the following repository: https://github.com/baas-devops-reference/ref-applications-live/blob/main/runtimes/dev/basic-installation.yaml apply your deployment configurations example see _DSC (basic-installation.yml) configuration_ above.
- [ ] Step 3: Modify DSC configuration for...
- [ ] Step 4: Run the pre-commit to validate the configurations => ` pre-commit run --all-files --show-diff-on-failure --color=always`
- [ ] Step 5: Commit and Push your changes; wait for the template rendering and lint jobs to complete
- [ ] Step 6: Merge into `main` to trigger deployment.

### Local setup

To be able to build locally, please add this to your local Maven settings in `~/.m2/settings.xml` :
```xml
                <repository>
                    <id>github-baas-ref</id>
                    <url>https://maven.pkg.github.com/baas-devops-reference/*</url>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
```

And generate a Github token and add it here:

Generate a Github token

- Visit the following page (logged in with your EMU user - username ends with _backbase) https://github.com/settings/tokens
- Click on `Generate new token`
- You can name it `mvn-baas-devops-reference` (or whatever makes sense for you)
  — Provide the following permission: read:packages (no other permission needed)
- Authorize the token by clicking on the `Configure SSO` button next to the created token name and `Authorize` the `baas-devops-reference` organization

![GitHubTokenUI](GitHubToken.png)

Add the generate GitHub token to the Maven settings in `~/.m2/settings.xml` :

```xml
        <server>
            <id>github-baas-ref</id>
            <username>[USERNAME]_backbase</username>
            <password>[GITHUB TOKEN]</password>
        </server>
```

- [ ] Step 1: Ensure to check the prerequisites for [local developer environment](https://community.backbase.com/documentation/ServiceSDK/latest/create_developer_environment)
- [ ] Step 2: Create `src/main/resources/application-local.yaml` file, then add and modify:
```yaml
drive-wealth.baseUrl: https://bo-api.drivewealth.io
drive-wealth.dwClientAppKey: <>
drive-wealth.clientID: <>
drive-wealth.clientSecret: <>
DRIVE_WEALTH_PORTFOLIOBENCHMARKLIST_0_NAME: S&P 500 Vanguard ETF (VOO)
DRIVE_WEALTH_PORTFOLIOBENCHMARKLIST_0_UUID: 639247b6-cbcd-4616-841d-890dbb926575
DRIVE_WEALTH_PORTFOLIOBENCHMARKLIST_1_NAME: INVESCO NASDAQ 100 ETF (QQQM)
DRIVE_WEALTH_PORTFOLIOBENCHMARKLIST_1_UUID: d59107e2-9a21-4be8-9fd5-f7cf0cdc9b5c


drive-wealth.auth.scheduler.fixedRate: "18000000"
drive-wealth.auth.retry.maxAttempt: "10"
drive-wealth.auth.retry.maxDelay: "10000"
```
- [ ] Step 3: Run command => `mvn spring-boot:run -Dspring.profiles.active=local`
- [ ] Step 4: To run the service from the built binaries, use => `java -jar target/portfolio-outbound-integration-1.0.0-SNAPSHOT.jar -Dspring.profiles.active=local`
---
## Contributions
Please create a branch and a PR with your contributions. Commit messages should follow [semantic commit messages](https://seesparkbox.com/foundry/semantic_commit_messages) 
