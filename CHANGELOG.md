# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Upgraded
- Upgraded [Keyple Java BOM](https://github.com/eclipse-keyple/keyple-java-bom) to `2025.11.21`.

## [2025-10-29]
### Changed
- Switched to [Keyple Java BOM](https://github.com/eclipse-keyple/keyple-java-bom) `2025.10.24` for dependency
  management, replacing individual Keyple component definitions.
- Changed License from `EPL v1.0` to `EDL v1.0`
- Migrated the CI pipeline from Jenkins to GitHub Actions.

## [2025-03-21]
### Upgraded
- `keypop-calypso-card-java-api:2.1.2`
- `keypop-calypso-crypto-legacysam-java-api:0.7.0`
- `keyple-service-java-lib:3.3.5`
- `keyple-distributed-network-java-lib:2.5.1`
- `keyple-distributed-local-java-lib:2.5.2`
- `keyple-distributed-remote-java-lib:2.5.1`
- `keyple-card-generic-java-lib:3.1.2`
- `keyple-card-calypso-java-lib:3.1.7`
- `keyple-card-calypso-crypto-legacysam-java-lib:0.9.0`

## [2024-09-10]
### Upgraded
- `keypop-calypso-crypto-legacysam-java-api:0.6.0`
- `keyple-service-java-lib:3.3.0`
- `keyple-service-resource-java-lib:3.1.0`
- `keyple-distributed-network-java-lib:2.5.0`
- `keyple-distributed-local-java-lib:2.5.0`
- `keyple-distributed-remote-java-lib:2.5.0`
- `keyple-card-calypso-java-lib:3.1.2`
- `keyple-card-generic-java-lib:3.1.0`
- `keyple-card-calypso-crypto-legacysam-java-lib:0.8.0`
- `keyple-plugin-cardresource-java-lib:2.1.0`

## [2024-04-15]
### Changed
- Java source and target levels `1.6` -> `1.8`
### Upgraded
- Gradle `6.8.3` -> `7.6.4`
- `keypop-reader-java-api:2.0.1`
- `keypop-calypso-card-java-api:2.1.0`
- `keypop-calypso-crypto-legacysam-java-api:0.5.0`
- `keyple-common-java-api:2.0.1`
- `keyple-util-java-lib:2.3.2`
- `keyple-service-java-lib:3.2.1`
- `keyple-service-resource-java-lib:3.0.1`
- `keyple-distributed-network-java-lib:2.3.1`
- `keyple-distributed-local-java-lib:2.3.1`
- `keyple-distributed-remote-java-lib:2.3.1`
- `keyple-card-generic-java-lib:3.0.1`
- `keyple-card-calypso-java-lib:3.1.1`
- `keyple-card-calypso-crypto-legacysam-java-lib:0.6.0`
- `keyple-plugin-cardresource-java-lib:2.0.1`
- `keyple-plugin-stub-java-lib:2.2.1`

## [2023-11-30]
:warning: Major version! Following the migration of the "Calypsonet Terminal" APIs to the
[Eclipse Keypop project](https://keypop.org), this integration tests now implements Keypop interfaces.
### Removed
- Calypsonet Terminal dependencies.
### Upgraded
- `keypop-reader-java-api:2.0.0`
- `keypop-calypso-card-java-api:2.0.0`
- `keypop-calypso-crypto-legacysam-java-api:0.3.0`
- `keyple-common-java-api:2.0.0`
- `keyple-distributed-network-java-lib:2.3.0`
- `keyple-distributed-local-java-lib:2.3.0`
- `keyple-distributed-remote-java-lib:2.3.0`
- `keyple-service-java-lib:3.0.0`
- `keyple-service-resource-java-lib:3.0.0`
- `keyple-plugin-cardresource-java-lib:2.0.0`
- `keyple-plugin-stub-java-lib:2.2.0-SNAPSHOT`
- `keyple-card-generic-java-lib:3.0.0`
- `keyple-card-calypso-java-lib:3.0.0`
- `keyple-card-calypso-crypto-legacysam-java-lib:0.4.0`
- `keyple-util-java-lib:2.3.1`

## [2023-05-23]
### Upgraded
- Calypsonet Terminal Reader API `1.2.0` -> `1.3.0`
- Keyple Service Lib `2.2.0` -> `2.3.0`
- Keyple Distributed Remote Lib `2.2.0` -> `2.2.1`
- Keyple Card Calypso Lib `2.3.4` -> `2.3.5`

## [2023-04-27]
### Added
- "Keyple Plugin Card Resource Library" to version `1.0.1`
### Upgraded
- "Calypsonet Terminal Reader API" to version `1.2.0`
- "Calypsonet Terminal Calypso API" to version `1.8.0`
- "Keyple Common API" to version `2.0.0`
- "Keyple Service Library" to version `2.2.0`
- "Keyple Service Resource Library" to version `2.1.1`
- "Keyple Util Library" to version `2.3.0`

## [2023-04-05]
### Upgraded
- "Keyple Service Library" to version `2.1.4`
- "Keyple Distributed Local Library" to version `2.2.0`
- "Keyple Distributed Network Library" to version `2.2.0`
- "Keyple Distributed Remote Library" to version `2.2.0`
- "Calypsonet Terminal Calypso API" to version `1.8.0`
- "Keyple Calypso Card Library" to version `2.3.4`
- "SLF4J API" to version `2.0.5`
- "SLF4J Simple" to version `2.0.5`
- "AssertJ" to version `3.24.2`
- "Mockito" to version `5.2.0`
- "Awaitility" to version `4.2.0`
- "Jackson" to version `2.14.2`

## [2023-02-23]
### Upgraded
- "Keyple Service Library" to version `2.1.3`
- "Keyple Distributed Remote Library" to version `2.1.0`
- "Calypsonet Terminal Calypso API" to version `1.6.0`
- "Keyple Calypso Card Library" to version `2.3.2`
- "Google Gson library" (com.google.code.gson) to version `2.10.1`
- "AssertJ" to version `3.23.1`
- "Mockito" to version `3.3.3`
- "Jackson" to version `2.12.7.1`

## [2023-01-10]
### Upgraded
- "Calypsonet Terminal Reader API" to `1.2.0`
- "Keyple Service Library" to version `2.1.2`
- "Keyple Calypso Card Library" to version `2.3.1`

## [2022-12-06]
### Upgraded
- "Keyple Calypso Library" to version `2.3.0`

## [2022-10-27]
### Added
- `CardSelectionManagerTest` for import/export card selection feature.
### Upgraded
- "Calypsonet Terminal Reader API" to version `1.1.0`
- "Calypsonet Terminal Calypso API" to version `1.4.0`
- "Keyple Calypso Library" to version `2.2.3`
- "Keyple Service Library" to version `2.1.1`

## [2022-05-30]
### Upgraded
- "Keyple Util Library" to version `2.1.0`

## [2021-12-20]
### Upgraded
- "Keyple Card Generic Library" to version `2.0.2`

## [2021-12-08]
### Upgraded
- "Keyple Service Library" to version `2.0.1`

## [2021-11-22]
### Upgraded
- "Keyple Card Generic Library" to version `2.0.1`

## [2021-10-06]
### Added
- `CHANGELOG.md` file (issue [eclipse-keyple/keyple#6]).
- Tests for distributed components.
- Uses of released dependencies:
  - org.calypsonet.terminal:calypsonet-terminal-reader-java-api:1.0.+
  - org.eclipse.keyple:keyple-common-java-api:2.0.+
  - org.eclipse.keyple:keyple-distributed-network-java-lib:2.0.0
  - org.eclipse.keyple:keyple-distributed-local-java-lib:2.0.0
  - org.eclipse.keyple:keyple-distributed-remote-java-lib:2.0.0
  - org.eclipse.keyple:keyple-service-java-lib:2.0.0
  - org.eclipse.keyple:keyple-plugin-stub-java-lib:2.0.0
  - org.eclipse.keyple:keyple-card-generic-java-lib:2.0.0
  - org.eclipse.keyple:keyple-util-java-lib:2.+

[unreleased]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2025-10-29...HEAD
[2025-10-29]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2025-03-21...2025-10-29
[2025-03-21]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2024-09-10...2025-03-21
[2024-09-10]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2024-04-15...2024-09-10
[2024-04-15]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2023-11-30...2024-04-15
[2023-11-30]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2023-05-23...2023-11-30
[2023-05-23]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2023-04-27...2023-05-23
[2023-04-27]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2023-04-05...2023-04-27
[2023-04-05]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2023-02-23...2023-04-05
[2023-02-23]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2022-12-06...2023-02-23
[2022-12-06]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2022-10-27...2022-12-06
[2022-10-27]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2022-05-30...2022-10-27
[2022-05-30]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2021-12-20...2022-05-30
[2021-12-20]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2021-12-08...2021-12-20
[2021-12-08]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2021-11-22...2021-12-08
[2021-11-22]: https://github.com/eclipse-keyple/keyple-integration-java-test/compare/2021-10-06...2021-11-22
[2021-10-06]: https://github.com/eclipse-keyple/keyple-integration-java-test/releases/tag/2021-10-06

[eclipse-keyple/keyple#6]: https://github.com/eclipse-keyple/keyple/issues/6