![Java CI with Maven](https://github.com/AuScope/AuScope-Portal-API/workflows/build-dev/badge.svg?branch=master)
[![Coverage](.github/badges/jacoco.svg)](.github/coverage/jacoco.csv)

# AuScope-Portal-API

Back-end API server for AuScope Portal (https://portal.auscope.org.au)

### To build and test:

```
mvn clean install
```

### To run:

```
java -jar --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED .\target\auscope-portal-api-6.11.0-SNAPSHOT.war
```

NB: Use forward slashes in the .war file path for Linux

### IMPORTANT NOTES:

1) Configuration files are in https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources. You must create a valid 'application.yaml' file before starting. 'application.yaml.default' is a suitable template.
2) Elastic Search: A separate elastic search instance is now required to run AuScope Portal API.  Configure its connection details in the application.yaml
3) In 'application.yaml', there are two profiles 'prod' and 'test'. These are for production and test builds, and control the set of map layers available to Auscope Portal
4) Vocabularies, known layers, registries etc. are defined in https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources

### Spring profiles

The active Spring profile selects the set of map layers and the registry (CSW geonetwork)
configuration. Profiles are chosen via `spring.profiles.active` in `application.yaml`:

- `test` — test/dev geonetwork registry
- `prod` — production geonetwork registry

Activating `test` or `prod` automatically also activates the `registries` profile via
`spring.profiles.group` (the Spring Boot 2.4+ replacement for the removed
`spring.profiles.include`). Do not re-introduce `spring.profiles.include`; it is not
honoured on Spring Boot 4.

### Azure App Configuration (runtime config in Azure)

When deployed to Azure, runtime configuration (including the active profile) can be loaded
from Azure App Configuration using Spring Cloud Azure. This replaces the former
`bootstrap.yml` mechanism, which was removed in the Azure App Configuration library v6.0.0
and is unsupported on Spring Boot 4.

It is driven by `spring.config.import: "optional:azureAppConfiguration"` in `application.yaml`,
together with the `spring.cloud.azure.appconfiguration` store/`selects` block. The store is
disabled by default so local builds do not require an App Configuration instance. Enable it
in the deployment environment via these variables:

| Environment variable           | Purpose                                                             |
| ------------------------------- | ------------------------------------------------------------------- |
| `APP_CONFIG_ENABLED`            | Set to `true` to load configuration from Azure App Configuration.   |
| `APP_CONFIGURATION_CONNECTION_STRING`  | Connection string for the App Configuration store (store as a secret). |

The `selects` `label-filter` entries in `application.yaml` decide which App Configuration
key/values are pulled in (e.g. `dev`, `devregistries`, `allportals`).

NB: The dev Azure Container App is updated by the `build-dev` GitHub Action using
`az containerapp`, which does **not** use the Helm chart in `charts/`. Set
`APP_CONFIG_ENABLED` and `APP_CONFIGURATION_CONNECTION_STRING` directly on the Container App
revision (the connection string as a secret) for that deployment.

