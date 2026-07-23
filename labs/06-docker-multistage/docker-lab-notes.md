# Lab 06 — Docker Multi-Stage Build: Lab Notes

## Build results

### Baseline (single-stage, build stage only — used for size comparison)
```
team-skeleton:debug   894 MB (includes Maven, JDK, full build cache)
```

### Multi-stage
```
team-skeleton:multistage   285 MB (JRE-Alpine + compiled jar only)
```

Size reduction: ~68%. The Maven toolchain, JDK, source files, and downloaded dependencies
never make it into the runtime image — the final stage starts fresh and copies only the
compiled jar. That's the entire point of multi-stage builds.

### Bug found in starter
`pom.xml` uses `<finalName>team-skeleton</finalName>` (no version suffix), so the built
jar is `/app/target/team-skeleton.jar` not `team-skeleton-0.1.0.jar` as the lab README
implies. The Dockerfile was corrected accordingly.

---

## Layer caching confirmation

Building after a source-only change (no `pom.xml` change):
- Layer `RUN mvn -B dependency:go-offline` → **CACHED** (dependencies not re-downloaded)
- Layer `COPY src/ src/` → rebuilt (source changed)
- Layer `RUN mvn -B clean package -DskipTests` → rebuilt (source changed)

This confirms the separate `COPY pom.xml` / `RUN dependency:go-offline` / `COPY src/`
ordering is doing its job.

---

## docker-compose networking verification

```
$ docker compose up -d
Container starter-db-1 Started
Container starter-app-1 Started

$ docker compose ps
NAME            IMAGE              STATUS         PORTS
starter-app-1   starter-app        Up             8080/tcp          # no host mapping
starter-db-1    postgres:16-alpine Up             0.0.0.0:5432->5432/tcp

$ docker exec starter-app-1 sh -c "getent hosts db"
172.18.0.2   db  db          # DNS resolution by service name works
```

Both containers joined `starter_default` bridge network automatically. The `app` service
resolved `db` by hostname — no IP was hardcoded anywhere.

Only `db` is reachable from the host (port 5432 mapped). The `app` service has no `ports:`
entry and is unreachable from outside Docker.

---

## docker compose down vs down -v

- `docker compose down` — stops and removes containers and the project network.
  The named volume `starter_db_data` survives; Postgres data persists for the next `up`.
- `docker compose down -v` — also removes named volumes.
  Next `up` starts with an empty database.

`up`/`down` are create/destroy operations, not pause/resume. Each `up` creates a fresh network
with newly assigned container IPs.
