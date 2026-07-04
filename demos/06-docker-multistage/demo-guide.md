# Demo: Module 06 — Containerisation Deeper Dive

**Duration:** 12 minutes
**Prerequisite:** Docker Desktop running. The `starter/` project from this lab. GitHub Copilot
Chat available.

## Part 1: Recap, quickly (2 min)

Narration: Sprint 1 Module 11 covered containers vs VMs and images vs containers, and had you
write a minimal single-stage Dockerfile, copying in a jar you'd already built locally with
Maven. Module 11 also showed (but didn't have you write) a multi-stage example, using GenAI
just to explain what it did. Module 13 then used that same single-stage pattern to containerise
your team's actual project skeleton. Today, you write the multi-stage version yourself, for
real, and go further: best practices, networking, and docker-compose.

## Part 2: Why multi-stage? (2 min)

```bash
docker images
```

Show the current single-stage image size. Narration: this image only contains a JRE and a jar,
because the jar was built locally, outside Docker, before `docker build` ever ran. That's fine
for a skeleton, but it means the *build* isn't reproducible from the Dockerfile alone, anyone
without Maven configured identically to yours could get a different jar. A multi-stage build
fixes that: the build step happens *inside* Docker too, using a full Maven+JDK image, and only
the final runtime stage (JRE + jar) ships.

## Part 3: Writing the multi-stage Dockerfile (4 min)

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /src/target/team-skeleton-0.1.0.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Narration, pointing out three best practices:
- **Layer caching**: `COPY pom.xml .` happens before `COPY src ./src`, so Docker only re-downloads
  dependencies when `pom.xml` changes, not on every source edit
- **Least-privilege user**: a non-root `appuser` runs the application, so a container escape
  doesn't hand an attacker root
- **Minimal base image**: the final stage never contains Maven or the JDK, only what's needed
  to run

```bash
docker build -t team-skeleton:multistage .
docker images
```

Compare the size against the original image.

## Part 4: Networking and docker-compose (3 min)

```bash
docker compose up -d
docker compose ps
docker exec -it <app-container> sh -c "getent hosts db"
```

Narration: `docker-compose` creates a private **bridge network** for the services it defines.
Containers reach each other by service name (`db`) as a hostname, entirely separate from
anything on the host. The `ports:` mapping in `docker-compose.yml` is the only thing exposing a
container's port to the host machine, everything else stays internal to the bridge network.

## Key message

Sprint 1's Dockerfile got you a working container. Sprint 2's deeper dive gets you one that's
reproducible from source, smaller, safer to run, and able to talk to other containers, the
shape a real team's Dockerfile actually needs to take.
