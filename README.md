# OptiFarm: a DevOps pipeline for a farm-management application

OptiFarm is a farm-management web app. It manages fields, crops, seeds, equipment, offers a resource-constrained task scheduling and data
visualization. It originated from a student group project in another course and was deployed there on a different infrastructure. 
This repository takes the application and builds a complete DevOps workflow around it, for the KTH DevOps
course (DD2482). it implements continuous integration, continuous delivery, infrastructure as code, and quality and security
automation, all on GitHub.

## Overview

| Component | Technology |
|---|---|
| Backend | Spring Boot 3, Java 21, Maven; JWT authentication, Flyway migrations |
| Database | PostgreSQL with PostGIS (spatial data for fields) |
| Frontend | Angular 19 (Node 22), served by nginx |
| Containers | Docker |
| CI/CD | GitHub Actions |
| Image registry | GitHub Container Registry (`ghcr.io`) |
| Infrastructure | Terraform, Google Cloud (Compute Engine VM) |

## Architecture

At runtime, one VM runs three containers with Docker Compose. Only port 80 (and SSH on port 22) is reachable from
the internet. The frontend container is an nginx server: it serves the built Angular files and forwards every
request under `/api/` to the backend container. The browser therefore only ever talks to one address, so the
frontend can reach the API on the same host without cross-origin requests.

The backend and database ports are not published on the host. They are only reachable inside the Compose network
(`backend:8080`, `postgres:5432`).

## The pipeline (CI/CD)

Both workflows are in `.github/workflows/`.

### Continuous integration (`ci.yml`, "CI Pipeline")

Runs on every pull request, every push to `main`, and manually.

- **Backend:** Java 21 (Temurin), `mvn verify`. Runs Checkstyle, unit tests and integration tests. The
  integration tests start a PostGIS database with Testcontainers.
- **Frontend:** Node 22, `npm ci`, lint, unit tests in headless Chromium, production build.
- **Terraform:** runs static terraform checks (`fmt -check`, `validate`) on the `infra/` directory.
- **Docker:** builds both images. On pull requests this only proves the Dockerfiles build.
- **Publish:** on a push (or manual run) on `main`, the images are pushed to GHCR. They are identified by the commit SHA, which makes rollbacks possible.

### Continuous deployment (`cd.yml`, "CD pipeline")

Starts after a CI run on `main` finished successfully (or manually on `main`). The CD workflow constists of two jobs: 

#### Infra job
- Authenticates to Google Cloud with a service account and workload identity federation.
- Set up Terraform (pinned version)
- Derive SSH public key from `VM_SSH_KEY`.
- Runs `terraform init` and `terraform apply` in `infra/`. The VM is created or updated.

#### Deploy job
- Checks out the commit that CI ran on.
- Prepares SSH (private key and pinned host key from secrets).
- Wait for VM to be ready.
- Copies `deploy/docker-compose.prod.yml` to `/opt/optifarm/` on the VM.
- Writes `/opt/optifarm/.env` on the VM from GitHub secrets (file mode 600) and sets `IMAGE_TAG` to the commit SHA.
- Logs the VM in to GHCR with the workflow's temporary `GITHUB_TOKEN`.
- Runs `docker compose pull` and `docker compose up -d`.
-  Health check from the runner: the frontend must answer, and a login request with wrong credentials must return
   HTTP 401. That response proves that nginx, the backend and the database all answer. On failure, container
   status and logs are printed.

Deployments never run in parallel (`concurrency` group).

## Infrastructure as code

The VM and its network are provisioned in Terraform (`infra/`).

Terraform manages the machine. The application layer on the VM is described by `deploy/docker-compose.prod.yml`
and deployed by the CD workflow. The VM is disposable.

The terraform state lives in a versioned GC storage bucket.

## Quality and security automation

| Tool | What it does | Status |
|---|---|---|
| Checkstyle | style rules, fails the build on violations; runs in `mvn verify` | active |
| Unit and integration tests, JaCoCo | tests run in CI; JaCoCo writes a coverage report | active |
| Angular lint and unit tests | `ng lint` and the Karma tests run in CI | active |
| Dependabot | pull requests for outdated or vulnerable dependencies | **TODO** (in progress) |
| Gitleaks | secret scanning in the pipeline | **TODO** (in progress) |

## Configuration and secrets

No secrets are committed. Each environment gets its values from a different place:

| Environment | Where the values come from |
|---|---|
| Local, full stack in containers | `backend/.env` (copy of `backend/.env.example`), read by Docker Compose |
| Local development (`mvn spring-boot:run`) | database defaults in `backend/src/main/resources/application.properties`; `JWT_SECRET` as environment variable |
| Tests | `backend/src/test/resources/application-test.yml` (a dummy JWT secret) |
| Production VM | `/opt/optifarm/.env`, written by the CD workflow from GitHub secrets |

GitHub Actions secrets used by the workflows:

-  `VM_HOST`:  IP address of the VM
-  `VM_SSH_KEY`:  private SSH key of the `deploy` user
-  `VM_HOST_KEY`:  the VM's SSH host key as a `known_hosts` line (`<IP> ssh-ed25519 <key>`)
-  `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`:  database settings
-  `JWT_SECRET`:  signing key for authentication tokens (long random value)
-  `GCP_PROJECT_ID`:  the ID of the Google Cloud project
-  `GCP_SERVICE_ACCOUNT`:  the service account to use for authentication
-  `GCP_WIF_PROVIDER`:  the workload identity provider to use for authentication

The last three are not really secret; they are put there for convenience. They are used to authenticate the Terraform job to Google Cloud.

When running the workflow, two more variables are used:
- `GITHUB_TOKEN`:  temporary token for the workflow to access the repository and GHCR, set by GitHub Actions
- `IMAGE_TAG`:  the commit SHA of the current run, used to pull the correct images on the VM. Set in the CD workflow.

## Running the application locally

Requirements: Docker, Java 21 and Node 22 (for development mode).

### Option 1: everything in containers

```bash
cp backend/.env.example backend/.env      # edit the values, set a real JWT_SECRET
docker compose -f complete-docker-compose.yml up --build
```

Open <http://localhost>. Log in with a generated demo user, for example `0farmer@mail.com` with the password
`password` (demo data is generated on first start).

### Option 2: development mode

```bash
docker compose -f backend/postgres-docker-compose.yml up -d     # Postgresql with PostGIS, needs backend/.env
export JWT_SECRET="$(openssl rand -base64 48)"
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=generateData
cd frontend && npm ci --legacy-peer-deps && npm start             # http://localhost:4200
```

The backend reads its database settings from `application.properties`, Spring does not read `.env`. So either use these values in `backend/.env` or export
`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_HOST` and `POSTGRES_PORT` before starting the backend.

### Tests

```bash
cd backend && mvn verify                  # Docker must be running (for the Testcontainers)
cd frontend && npm run lint && npm test -- --watch=false --browsers=ChromeHeadlessNoSandbox
```

## Repository layout

```
.github/workflows/    CI and CD workflows
backend/              Spring Boot application, Dockerfile, local compose file for PostgreSQL
frontend/             Angular application, Dockerfile, nginx.conf
deploy/               production Docker Compose file and .env.example for the VM
infra/                Terraform configuration and VM startup script
complete-docker-compose.yml   whole stack for local use
```

## Use of AI-assisted tools

see [AI_USAGE.md](AI_Usage.md).
