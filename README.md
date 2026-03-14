# 🏛️ Kotlin Spring Boot Template

> Production-ready template for Kotlin applications with Spring Boot WebFlux, Clean Architecture, DDD and Coroutines.

[![CI](https://github.com/rafaelcarvalho/project-template/workflows/CI/badge.svg)](https://github.com/rafaelcarvalho/project-template/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## ✨ What This Template Provides

- 🏗️ **Clean Architecture** — Compile-time layer boundaries enforced automatically
- 🔒 **Architectural Fitness Functions** — Continuous design validation with Konsist
- ⚡ **Coroutine-Native** — Spring Boot WebFlux + Coroutines, zero blocking APIs
- 🧪 **Testing Excellence** — JUnit 5, MockK and Testcontainers pre-configured
- 🛡️ **Self-Protecting Build** — Quality and architecture verified on every `build`
- 📦 **Convention Plugins** — Reusable Gradle plugins for consistent module setup

---

## 📂 Project Structure

```
project-template/
├── app/                        # Spring Boot bootstrap (wiring only, no business logic)
├── architecture/               # Isolated composite build
│   ├── src/main/kotlin/        # Convention plugins (Kotlin DSL)
│   │   ├── rafaelcarvalho.kotlin.gradle.kts         # Kotlin JVM 21, toolchain, compiler flags
│   │   ├── rafaelcarvalho.spring.gradle.kts         # Spring BOM, WebFlux, Actuator
│   │   ├── rafaelcarvalho.testing.gradle.kts        # JUnit 5, MockK, Testcontainers
│   │   ├── rafaelcarvalho.quality.gradle.kts        # Jacoco, Detekt, Ktlint
│   │   └── rafaelcarvalho.architecture.gradle.kts   # Konsist (fitness functions)
│   ├── src/shared-test/kotlin/ # ModuleArchitectureTest — injected into every domain module
│   └── src/test/kotlin/        # ArchitectureIsolationTest — cross-module isolation validation
└── gradle/wrapper/
```

### Business Module Layout

When you add domain modules, they follow this structure:

```
<module>/
├── src/main/kotlin/.../
│   ├── core/           # Entities, Use Cases, Ports (no external dependencies)
│   ├── application/    # Controllers, Consumers, Mappers
│   ├── adapters/       # Persistence, HTTP clients, Publishers
│   └── configurations/ # Spring wiring and bean configuration
└── build.gradle.kts
```

---

## 🚀 Using This Template

### 1. Create a Repository from the Template

On GitHub, click **"Use this template"** → **"Create a new repository"**.

### 2. Rename the Project

Update `settings.gradle.kts`:

```kotlin
rootProject.name = "my-project"
```

Update the `group` in your module build files:

```kotlin
group = "io.github.<your-username>.<project>"
```

### 3. Add Domain Modules

Create the module directory and source set:

```bash
mkdir -p my-module/src/main/kotlin/io/github/<username>/<project>/my-module
```

Create `my-module/build.gradle.kts`:

```kotlin
plugins {
    id("rafaelcarvalho.kotlin")
    id("rafaelcarvalho.spring")
    id("rafaelcarvalho.testing")
    id("rafaelcarvalho.quality")
    id("rafaelcarvalho.architecture") // Enables fitness functions for this module
}
```

Register in `settings.gradle.kts`:

```kotlin
include("my-module")
```

### 4. Build & Run

```bash
# Full build: compile → test → jacoco → check (quality + architecture)
./gradlew build

# Run the application
./gradlew :app:bootRun

# Validations only (no packaging)
./gradlew check

# Format code (ktlint)
./gradlew :architecture:architecture:format
```

---

## 🛡️ Convention Plugins

All domain modules use these plugins via Gradle DSL:

| Plugin | Responsibility |
|--------|----------------|
| `rafaelcarvalho.kotlin` | Kotlin JVM 21, toolchain, compiler flags |
| `rafaelcarvalho.spring` | Spring BOM, WebFlux and Actuator defaults |
| `rafaelcarvalho.testing` | JUnit 5, MockK, Testcontainers, spring-webflux test |
| `rafaelcarvalho.quality` | Jacoco, Detekt and Ktlint wired into the lifecycle |
| `rafaelcarvalho.architecture` | Konsist injected as JUnit tests in each module |

---

## 🧬 Architectural Fitness Functions

The build automatically validates:

- **No cross-module dependencies** — modules cannot import classes from sibling modules
- **Domain purity** — `core` layer cannot depend on Spring, Jackson, or persistence frameworks
- **Layer hierarchy** — `core` ← `application` ← `adapters` ← `configurations`
- **Naming conventions** — `*UseCase`, `*Gateway`, `*Adapter`, etc., enforced per layer

These are not suggestions — they **fail the build** on violation.

```kotlin
// Example: domain must never import Spring
Konsist.scopeFromDirectory("${project.name}/src/main/kotlin")
    .classes().withPackage("..core..")
    .assertTrue {
        it.imports.none { import -> import.name.startsWith("org.springframework") }
    }
```

```bash
# Run fitness functions only
./gradlew :architecture:architecture:test
```

---

## 🚦 CI Pipeline

Cada push e PR executa dois jobs obrigatórios. A publicação da imagem Docker ocorre apenas quando o release-please cria uma tag `vX.Y.Z`:

```
push / pull_request
       |
       v
   +--------------+
   | build        |  compile -> test -> jacoco -> detekt -> ktlint -> konsist
   +------+-------+
          | (only on success)
          v
   +--------------+
   | docker-image |  distroless java image build (jar)   <- required check
   +--------------+


tag vX.Y.Z (created by release-please after merge)
       |
       v
 +------------------+
 | release workflow |  JAR artifact -> GitHub Release
 |                  |  linux/amd64 + linux/arm64 -> rafac/<repo>:1.2.3 + :latest
 +------------------+
```

`build` e `docker-image` são **required status checks** — configure após criar o repositório:

> **Settings -> Branches -> Branch protection rules** -> required checks: `build`, `docker-image`

A imagem Docker é publicada apenas no workflow `release.yml`, disparado pela tag semântica gerada pelo release-please — nunca em PRs ou pushes diretos.

### Secret necessário

No repositório, configure em **Settings -> Secrets -> Actions**:

| Secret | Valor |
|--------|-------|
| `DOCKER_PASSWORD` | Access token Docker Hub do usuário `rafac` |
---

## 📊 Quality Lifecycle

All quality tools are **wired into `build`/`check`** automatically — no direct calls needed:

| Tool | Integration |
|------|-------------|
| Jacoco | `test` → `jacocoTestReport` → `check` |
| Detekt | `check` (via `architecture` composite build) |
| Ktlint | `check` (via `architecture` composite build) |
| Konsist | `test` — JUnit tests in `architecture` and in each domain module |

```bash
# Static analysis only (ktlint + detekt, including .gradle.kts)
./gradlew quality
```

---

## 🧪 Testing Stack

- **JUnit 5** — Test framework
- **MockK** — Idiomatic Kotlin mocking (no Mockito)
- **Testcontainers** — Integration tests with real infrastructure
- **Konsist** — Architecture validation as automated tests

---

## 🔧 Build Performance

Optimized for fast feedback loops out of the box:

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
kotlin.incremental=true
```

---

## 🛠️ Technology Stack

- **Kotlin 2.2** — Coroutines, immutable-first design
- **Spring Boot 4.0** — Reactive WebFlux, no blocking APIs
- **Gradle 9.1** — Kotlin DSL, convention plugins, build caching
- **Java 21** — LTS baseline

---

## 🌍 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development workflow, PR requirements, and commit conventions.

---

## 📝 License

Apache License 2.0 — see [LICENSE](LICENSE) for details.


