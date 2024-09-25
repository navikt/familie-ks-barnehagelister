# familie-ks-barnehagelister

### Database

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


## Kode generert av GitHub Copilot

Dette repoet bruker GitHub Copilot til å generere kode.
