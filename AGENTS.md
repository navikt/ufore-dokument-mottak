# AGENTS.md

Instructions for AI coding agents working in this repository.

## Project overview

`ufore-dokument-mottak` is a minimal Kotlin/JVM application scaffold for Team Uføre at Nav. 
The purpose of this repository is...

- Language/runtime: Kotlin, JDK 25 (via Gradle toolchain)
- Build tool: Gradle (Kotlin DSL), wrapper included (`./gradlew`)
- Package root: `no.nav.uføre`
- Entry point: `src/main/kotlin/no/nav/uføre/App.kt` (`no.nav.uføre.AppKt`)
- Logging: `kotlin-logging-jvm` + `logback-classic`
- Testing: JUnit Jupiter + MockK
- Deployment: Nais, manifest at `.nais/app.yaml` (templated, team `ufore`,
  namespace `ufore`)
- Container: `Dockerfile` copies the built jar from `build/libs/` and runs it

## Setup commands

- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run the app locally: `./gradlew run`

No additional services, databases, or environment variables are required to
build or test this template as-is.

## Code style

- Standard Kotlin conventions; keep package names under `no.nav.uføre`.
- Prefer `kotlin-logging-jvm`'s `KotlinLogging.logger {}` for logging, not
  `println`.
- Keep the template minimal and generic — avoid adding team- or
  product-specific business logic unless explicitly requested.

## Testing instructions

- Tests live under `src/test/kotlin/no/nav/uføre/` and use JUnit 5 + MockK.
- Run the full suite with `./gradlew test` before considering a change done.
- Add or update tests alongside any behavioral change in `src/main`.

## PR instructions

- Keep changes focused and avoid unrelated edits to generated/build
  directories (`build/`, `.gradle/`, `.kotlin/`).
- Update `README.md` and `.nais/app.yaml` if setup, deployment, or ownership
  details change.
- CODEOWNERS is `@navikt/ufore`.
