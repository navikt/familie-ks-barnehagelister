# familie-ks-barnehagelister

Eksternt API for å motta barnehagelister for Kontantstøtte.  
Swagger:  
[preprod](https://familie-ks-barnehagelister.ekstern.dev.nav.no/swagger-ui/index.html)  
[prod](https://familie-ks-barnehagelister.nav.no/swagger-ui/index.html)

### Database

sdasdgergdf

#### Embedded database

Bruker du `DevLauncherPostgres`, kan du kjøre opp en embedded database. Da må du sette `--dbcontainer`
under `Edit Configurations -> VM Options`

#### Database i egen container

Postgres-databasen kan settes opp slik:

1. Lag en dockercontainer:
```
docker run --name familie-ks-barnehagelister-postgres -e POSTGRES_PASSWORD=test -d -p 5432:5432 postgres:15
```
2. List opp alle containerne og finn container id for container med name = familie-ks-barnehagelister-postgres:

```
docker ps
```
3. Kjør docker container:
```
docker exec -it <container_id> bash
```

4. Åpne postgres som brukeren "postgres":
```
psql -U postgres
```

5. Lag en database med navn "familie-ks-barnehagelister":
```
CREATE DATABASE "familie-ks-barnehagelister";
```

Legg til databasen i Intellij:
1. Trykk på database på høyre side og "+" -> data source -> postgreSQL
2. Fyll inn port=5432, user=postgres, passord=test og database=familie-ks-barnehagelister

OBS: Pass på at du ikke kjører postgres lokalt på samme port (5432)

### Testing av tjeneste i preprod
For å teste tjenesten i preprod, så trenger man å få opprettet en maskinportentoken. Se dokumentasjon 
på [nav-eksterne-api-dok](https://github.com/navikt/nav-ekstern-api-dok/blob/main/api-dok/teste-api/teste-api.md) for å 
lage en konsument og for å generere nytt token. Hvis man velger en ny konsument, så må orgnr legges til i app-dev.yaml.


### Testing av tjenesten lokalt
Lokalt kjører applikasjonen på port 8096. For å teste tjenesten lokalt, så kan man kjøre [DevLauncher](src/test/kotlin/no/nav/familie/ks/barnehagelister/DevLauncher.kt)  
Link til [swagger](http://localhost:8096/swagger-ui/index.html) lokalt

### Kode generert av GitHub Copilot
Dette repoet bruker GitHub Copilot til å generere kode.
