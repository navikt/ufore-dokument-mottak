# Mal for Team Uføres Kotlin-repositories

Beskrivelse av hva appen/tjenesten/tingen som lages i dette repositoryet gjør.

## Komme i gang
### Forutsetninger
- [JDK](https://jdk.java.net/)
- [Kotlin](https://kotlinlang.org/)
- [Gradle](https://gradle.org/) brukes som byggeverktøy og er inkludert i oppsettet

For hvilke versjoner som brukes, [se versjonsfilen](/gradle/libs.versions.toml)

### Følgende steg må gjennomføres for å få repoet ditt i kjørbar tilstand
- søk etter teksten `ufore-soknad-mottak` og erstatt med navnet på ditt repo. Her er en kommando som gjør det for deg på macOS dersom du står i repoets rotkatalog
```sh
repo_name=$(basename "$PWD") && grep -Irwl --null --exclude-dir=.git --exclude-dir=build -e 'ufore-soknad-mottak' . | xargs -0 env REPO_NAME="$repo_name" perl -pi -e 's/\bufore-xxx-yyy\b/$ENV{REPO_NAME}/g'
```
- gi repoet et eierskap på https://console.nav.cloud.nais.io/team/ufore/repositories
- på repoets side på GitHub → Settings → Secrets and variables → Actions
  - ny repository **variabel** `TEAM_UFORE_CLIENT_ID`. Verdien hentes fra https://github.com/organizations/navikt/settings/apps/team-ufore
  - ny repository **secret** `TEAM_UFORE_PRIVATE_KEY`. Verdien hentes fra NAIS-console → Secrets → TEAM-UFORE
- for repoets side på GitHub → Settings → General → Pull request
  - aktiver "Allow rebase merging" 
  - aktiver "Allow auto-merge"  
- legg til navnet på ditt repo til [Team Uføres](https://github.com/apps/team-ufore/installations/98878472) "repository access" på GitHub
- i `main.yaml` er det en rad som forhindrer deployment til `prod-gcp`. Fjern den raden når du er klar for deployment til prod
- slett dette avsnittet fra README.md og oppdater med relevant beskrivelse av ditt nye repo

### Bygging og denslags
For å bygge artifaktene:

```sh
./gradlew build
```
---

## Henvendelser

Spørsmål knyttet til koden eller repositoryet kan stilles som issues her på GitHub

### For Nav-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-uføre.

# TODO for å få denne malen til å bli fullt brukbar
- [x] Auto-merge Dependabot PRs
- [x] Dependabot - automerge må forutsette grønt bygg
- [x] Pin actions til SHA
- [x] Følg anbefalt oppsett for NAIS: https://docs.nais.io/build/how-to/deploy-pipeline/
- [ ] Skal vi bygge artifaktet som en fet JAR, eller finnes det bedre måter?
- [x] Deployment til dev-gcp og prod-gcp
- [x] Legg til en server for å unngå CrashLoopBackOff ved deploy
- [ ] Fiks alerts
- [ ] Fjern dette avsnittet
