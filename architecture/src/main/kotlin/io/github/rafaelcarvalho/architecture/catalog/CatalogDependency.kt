package io.github.rafaelcarvalho.architecture.catalog

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

internal data class CatalogDependency(
    val alias: String,
    val isPlatform: Boolean = false,
    val scope: String = "implementation",
) {
    companion object {
        fun library(alias: String): CatalogDependency = CatalogDependency(alias = alias)

        fun platform(alias: String): CatalogDependency = CatalogDependency(alias = alias, isPlatform = true)

        fun testLibrary(alias: String): CatalogDependency = CatalogDependency(alias = alias, scope = "testImplementation")
    }
}

internal fun Project.libsCatalog(): VersionCatalog =
    extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

internal fun Project.addCatalogDependencies(dependenciesToAdd: List<CatalogDependency>) {
    val libraries = libsCatalog()

    dependenciesToAdd.forEach { dependency ->
        val dependencyProvider =
            libraries.findLibrary(dependency.alias).orElseThrow {
                GradleException("Library alias '${dependency.alias}' was not found in the libs version catalog.")
            }

        if (dependency.isPlatform) {
            dependencies.add(dependency.scope, dependencies.platform(dependencyProvider.get()))
        } else {
            dependencies.add(dependency.scope, dependencyProvider.get())
        }
    }
}

internal fun Project.requiredVersion(alias: String): String =
    libsCatalog()
        .findVersion(alias)
        .orElseThrow {
            GradleException("Version alias '$alias' not found in libs version catalog.")
        }.requiredVersion
