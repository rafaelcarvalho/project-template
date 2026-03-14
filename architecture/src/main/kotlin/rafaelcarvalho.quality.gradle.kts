// Convention plugin: rafaelcarvalho.quality
//
// Enables quality tooling for a module:
//   - detekt  : static analysis (extend config at config/detekt/detekt.yml)
//   - ktlint  : Kotlin linting via the ktlint Gradle plugin
//   - jacoco  : test coverage report
//
// Usage:
//   plugins { id("rafaelcarvalho.quality") }
//
// Override defaults by reconfiguring any of these in your module's build.gradle.kts:
//   detekt { config.setFrom("...") }
//   ktlint { version.set("...") }
//   tasks.jacocoTestReport { ... }

plugins {
    id("dev.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    jacoco
}

// detekt 2.0.0-alpha.2 was compiled with Kotlin 2.3.0, but Spring Boot BOM (via
// io.spring.dependency-management) and the KGP alignment rule both force kotlin to
// 2.2.21 across all configurations. We use dependencySubstitution (which runs before
// component metadata rules) to pin Kotlin to 2.3.0 exclusively in the detekt config.
configurations.named("detekt") {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.kotlin:kotlin-compiler"))
            .using(module("org.jetbrains.kotlin:kotlin-compiler:2.3.0"))
        substitute(module("org.jetbrains.kotlin:kotlin-stdlib"))
            .using(module("org.jetbrains.kotlin:kotlin-stdlib:2.3.0"))
        substitute(module("org.jetbrains.kotlin:kotlin-reflect"))
            .using(module("org.jetbrains.kotlin:kotlin-reflect:2.3.0"))
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
}

ktlint {
    version.set("1.8.0")
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.withType<Test>().configureEach {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named("jacocoTestReport") {
    dependsOn(tasks.withType<Test>())
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestReport"))
}
