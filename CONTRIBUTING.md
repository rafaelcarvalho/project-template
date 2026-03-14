# Contributing to Project Template

Thank you for your interest in contributing to Project Template! This document outlines our development process and requirements.

## 🎯 Philosophy

This project maintains high engineering standards:

- Architecture is enforced through **fitness functions**, not code review opinions
- Changes must preserve **domain purity**
- Tests are **mandatory** for all logic changes
- Build performance matters — avoid slow plugins

## 🔄 Development Workflow

### 1. Fork and Clone

```bash
git clone https://github.com/rafaelcarvalho/project-template.git
cd stellar-project-template
```

### 2. Create a Branch

Use descriptive branch names:

```bash
git checkout -b feature/add-issue-name
git checkout -b fix/issue-name
git checkout -b docs/update-architecture-guide
```

### 3. Make Changes

Run checks locally before pushing:

```bash
# Full validation
./gradlew build

# Architecture tests only
./gradlew architecture

# Code quality
./gradlew quality

# Architecture fitness functions
./gradlew architecture
```

### 4. Commit

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add orbital decay calculation
fix: correct lorentz factor computation
docs: clarify modular architecture in README
test: add coverage for Position3D
refactor: extract velocity calculation
build: upgrade Kotlin to 2.2.21
```

**Allowed types:**
- `feat:` — New feature
- `fix:` — Bug fix
- `docs:` — Documentation
- `test:` — Test additions/changes
- `refactor:` — Code restructuring (no behavior change)
- `perf:` — Performance improvement
- `build:` — Build system/dependencies
- `ci:` — CI configuration
- `chore:` — Maintenance tasks

### 5. Push and Create PR

```bash
git push origin feature/issue-name
```

Create a pull request with:
- Descriptive title (conventional commit format)
- Clear motivation and implementation summary
- Reference to related issues

## ✅ PR Requirements

**All checks must pass on GitHub before a PR can be opened or merged.** Do not request review before the CI is green — branch protection rules enforce this automatically.

The CI runs two mandatory jobs on every push and PR:

| Job | What it validates |
|-----|-------------------|
| `build` | Compile → test → Jacoco → Detekt → Ktlint → Konsist |
| `docker-image` | Distroless Java image build (jar runtime) |

`docker-image` only runs after `build` passes.

### Configuring Branch Protection (required after creating a repository from this template)

In **Settings → Branches → Branch protection rules** for `main` and `develop`:

- ✅ Require status checks to pass before merging
  - Required checks: `build`, `docker-image`
- ✅ Require branches to be up to date before merging
- ✅ Do not allow bypassing the above settings

This guarantees that no PR reaches review with a broken build or an invalid Docker image build.

> `publish-image` is NOT a required check — it only runs on push to `main`, never on PRs.

### Secret necessário para publicação

Configure em **Settings → Secrets → Actions** do repositório:

| Secret | Descrição |
|--------|-----------|
| `DOCKER_PASSWORD` | Access token Docker Hub do usuário `rafac` |

### Architecture Rules

The build **will fail** if you:

- Import framework dependencies into the `core` layer
- Create circular module dependencies
- Violate layered architecture boundaries
- Break naming conventions per layer

These are not suggestions — they are **enforced at build time**.

## 🧪 Testing Requirements

### Domain Logic

Pure domain code must have:
- Unit tests with mathematical validation
- No framework dependencies in tests

```kotlin
@Test
fun `should calculate time dilation correctly`() {
    val properTime = 1.0
    val velocity = Velocity(lightSpeed * 0.5, 0.0, 0.0)
    
    val dilatedTime = RelativisticPhysics.timeDilation(properTime, velocity)
    
    assertEquals(1.1547, dilatedTime, 0.001)
}
```

### Use Cases

Application layer tests must:
- Use `StepVerifier` for reactive types
- Not depend on Spring context

```kotlin
@Test
fun `should emit course solution reactively`() {
    val result = useCase.execute(origin, destination)
    
    StepVerifier.create(result)
        .expectNextMatches { it.euclideanDistance == 5.0 }
        .verifyComplete()
}
```

### Controllers

Adapter tests use:
- `@WebFluxTest` for isolated testing
- `MockkBean` (never Mockito)

```kotlin
@WebFluxTest(NavigationController::class)
class NavigationControllerTest {
    @MockkBean
    private lateinit var useCase: ComputeCourseUseCase
    
    // Tests...
}
```

## 📋 Code Style

We use:
- **Ktlint** for formatting (enforced)
- **Detekt** for code quality (enforced)

Run formatters:

```bash
./gradlew format
```

## 🏗️ Architecture Principles

### Module Structure

Each business module follows:

```
module/
├── domain/        # Pure business logic (no frameworks)
├── application/   # Use cases (reactive)
├── adapters/      # External integrations
└── gateways/      # Outbound ports (interfaces)
```

### Dependencies

- Domain: **depends on nothing**
- Application: depends on domain only
- Adapters: depends on domain + application
- Modules: **no cross-module dependencies**

## 🚫 Avoid

Do not:
- Add heavy dependencies without discussion
- Use blocking APIs (JDBC, RestTemplate, etc.)
- Implement logic in the `app` module (bootstrap only)
- Create anemic domain models
- Skip tests
- Violate architectural boundaries

## 🔍 Review Process

PRs are reviewed for:
1. Correctness — Does it solve the problem?
2. Tests — Is it validated?
3. Architecture — Does it respect boundaries?
4. Clarity — Is it understandable?

**Automated checks are non-negotiable.** If fitness functions fail, the design needs revision, not the tests.

## 📚 Resources

- [Architecture Decision Records](docs/adr/)
- [Konsist Documentation](https://docs.konsist.lemonappdev.com/)
- [Reactive Programming Guide](https://projectreactor.io/docs)

## 🤝 Questions?

- Open a [Discussion](https://github.com/your-org/your-repo/discussions)
- Check [SUPPORT.md](SUPPORT.md)

Thank you for contributing to maintainable, professional software! 🚀
