[Java CI with Maven](https://github.com/AuScope/AuScope-Portal-API/workflows/build-dev/badge.svg?branch=master) [Coverage](.github/coverage/jacoco.csv)

## AuScope-Portal-API

Back-end API server for AuScope Portal ([https://portal.auscope.org.au](https://portal.auscope.org.au))

#### To build and test:

```text
mvn clean install
```

#### To run:

```text
java --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED -jar .\target\auscope-portal-api-6.11.0-SNAPSHOT.war
```

NB: Use forward slashes in the `.war` file path on Linux.

#### Registry profiles

The active Spring profile determines which primary registry is used and whether the external registries are also loaded.

If no profile is specified, `devRegistryWithExternals` is used by default.

| Profile | Primary registry | External registries |
|---|---|---|
| `devRegistryWithExternals` | Development registry | Included |
| `prodRegistryWithExternals` | Production registry | Included |
| `devRegistryOnly` | Development registry | Not included |
| `prodRegistryOnly` | Production registry | Not included |

The profiles are configured as Spring profile groups:

```yaml
spring:
  profiles:
    default: devRegistryWithExternals
    group:
      devRegistryWithExternals:
        - devRegistry
        - externalRegistries
      prodRegistryWithExternals:
        - prodRegistry
        - externalRegistries
      devRegistryOnly:
        - devRegistry
      prodRegistryOnly:
        - prodRegistry
```

To select a profile, add the Spring Boot argument after the `.war` file path. For example:

```text
java --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED -jar .\target\auscope-portal-api-6.11.0-SNAPSHOT.war --spring.profiles.active=prodRegistryWithExternals
```

To run with only the development registry and no external registries:

```text
java --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED -jar .\target\auscope-portal-api-6.11.0-SNAPSHOT.war --spring.profiles.active=devRegistryOnly
```

#### IMPORTANT NOTES:

- Configuration files are in [https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources](https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources). You must create a valid `application.yaml` file before starting. `application.yaml.default` is a suitable template.
- Elastic Search: A separate Elastic Search instance is required to run AuScope Portal API. Configure its connection details in `application.yaml`.
- Registry configuration is split across `devRegistry`, `prodRegistry`, and `externalRegistries`. Use the profile groups above rather than activating these component profiles individually.
- Vocabularies, known layers, registries, etc. are defined in [https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources](https://github.com/AuScope/AuScope-Portal-API/tree/master/src/main/resources).
