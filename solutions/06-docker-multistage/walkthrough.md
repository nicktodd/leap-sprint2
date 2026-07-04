# Module 06 Lab — Walkthrough (Instructor Reference)

## Part A: baseline

```bash
cd starter
mvn clean package
docker build -t team-skeleton:baseline .
docker images
# team-skeleton   baseline   ...   ~185MB (JRE-alpine + jar)
```

## Part B: multi-stage refactor

See [`Dockerfile`](Dockerfile) in this folder for the full multi-stage version.

```bash
docker build -t team-skeleton:multistage .
docker images
# team-skeleton   baseline     ...   ~185MB
# team-skeleton   multistage   ...   ~180MB
```

**Note on size:** the final *runtime layer* size difference here is small, because the
single-stage baseline was already copying in a prebuilt jar rather than shipping the Maven/JDK
toolchain itself. The bigger, more important difference to draw out with delegates is what
*doesn't* end up in the final image: no Maven, no JDK, no source code, and no build cache, all
of which would be present if you'd naively tried to `RUN mvn package` inside a single-stage
`FROM maven:...` image and shipped that directly. Ask a delegate to try that as a contrast:

```dockerfile
# What NOT to do: ships the entire Maven/JDK toolchain in the final image
FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests
ENTRYPOINT ["java", "-jar", "target/team-skeleton-0.1.0.jar"]
```

That single-stage-but-builds-from-source version is typically 3-4x larger than the multi-stage
result, *that's* the size reduction the module objective is really about, and it's also a much
larger attack surface (a full build toolchain sitting in a running production container).

## Part C: docker-compose

See [`docker-compose.yml`](docker-compose.yml) in this folder.

```bash
docker compose up -d
docker compose ps
# both app and db show as running

docker exec -it <app-container-name> sh
/app # getent hosts db
172.19.0.2      db
```

This confirms the bridge network: `db` resolves to the Postgres container's internal IP,
entirely separate from anything on the host.

```bash
# From the host, confirming the port mapping:
docker exec -it <db-container-name> psql -U postgres -c "SELECT 1;"
```

Or connect a local Postgres client to `localhost:5432` to confirm the same thing from outside
Docker entirely.

## What to check as an instructor

- The Dockerfile genuinely builds from source (`RUN mvn ... package` in the build stage), not
  just a relabelled copy of the single-stage version.
- `COPY pom.xml` happens before `COPY src`, the actual layer-caching mechanism, not just claimed
  in a comment.
- A non-root `USER` is set in the final stage, and delegates can explain why that matters (a
  container escape or RCE doesn't hand an attacker root on the host).
- Both docker-compose checks (hostname resolution and port mapping) were actually run, not just
  assumed to work.
