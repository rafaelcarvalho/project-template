package io.github.rafaelcarvalho.architecture.module

import io.github.rafaelcarvalho.architecture.catalog.CatalogDependency
import io.github.rafaelcarvalho.architecture.catalog.addCatalogDependencies
import io.github.rafaelcarvalho.architecture.catalog.requiredVersion
import io.github.rafaelcarvalho.architecture.cloud.configureCloudDependencies
import org.gradle.api.Project

/**
 * Configures the standard WebFlux/coroutines runtime dependencies for a product module.
 *
 * Called by the `rafaelcarvalho.module` convention plugin so that the precompiled
 * script plugin itself does not need access to the `libs` type-safe accessor
 * (which is unavailable inside the `architecture` included build).
 */
internal fun Project.configureModuleDependencies() {
    addCatalogDependencies(
        listOf(
            CatalogDependency.library("spring-boot-starter-webflux"),
            CatalogDependency.library("spring-boot-starter-validation"),
            CatalogDependency.library("spring-boot-starter-actuator"),
            CatalogDependency.library("reactor-kotlin-extensions"),
            CatalogDependency.library("kotlinx-coroutines-reactor"),
        ),
    )

    // kotlin-reflect is resolved from the Kotlin gradle plugin toolchain — no catalog alias needed.
    val kotlinVersion = requiredVersion("kotlin")
    dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    configureCloudDependencies()
}
