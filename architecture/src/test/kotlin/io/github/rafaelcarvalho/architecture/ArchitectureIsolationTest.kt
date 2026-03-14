package io.github.rafaelcarvalho.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString

/**
 * Cross-module isolation tests.
 *
 * Per-module architectural rules (layers, package conventions, naming, etc.)
 * are enforced by [ModuleArchitectureTest], which is injected into every
 * module that declares the `rafaelcarvalho.architecture` convention plugin.
 *
 * This file contains only rules that require visibility of the full set of
 * product modules simultaneously — specifically, that no module imports
 * classes from a sibling module.
 */
class ArchitectureIsolationTest {
    private val productModules: List<ProductModule> = discoverProductModules()

    @Test
    fun `product modules remain decoupled from each other`() {
        productModules.forEach { module ->
            val forbiddenPrefixes =
                productModules
                    .filter { candidate -> candidate.name != module.name }
                    .flatMap { candidate -> candidate.rootPackages }
                    .toSet()

            Konsist
                .scopeFromDirectory(module.mainSourcePath)
                .files
                .assertTrue { file ->
                    file.imports.none { declarationImport ->
                        forbiddenPrefixes.any { prefix -> declarationImport.name.startsWith(prefix) }
                    }
                }
        }
    }

    private fun discoverProductModules(): List<ProductModule> {
        val rootPath = Paths.get(System.getProperty("user.dir"))

        val candidates =
            Files
                .list(rootPath)
                .use { directoryStream ->
                    directoryStream
                        .filter { path ->
                            path.isDirectory() &&
                                path.name !in EXCLUDED_DIRECTORIES &&
                                Files.exists(path.resolve("src/main/kotlin")) &&
                                (
                                    Files.exists(path.resolve("build.gradle.kts")) ||
                                        Files.exists(path.resolve("build.gradle"))
                                )
                        }.map { modulePath ->
                            val sourcePath =
                                rootPath
                                    .relativize(modulePath.resolve("src/main/kotlin"))
                                    .pathString
                            ProductModule(modulePath.name, sourcePath, discoverRootPackages(sourcePath))
                        }.toList()
                }

        assumeFalse(
            candidates.isEmpty(),
            "No product modules were discovered for architecture validation — skipping.",
        )

        return candidates
    }

    private fun discoverRootPackages(sourcePath: String): Set<String> =
        Konsist
            .scopeFromDirectory(sourcePath)
            .files
            .mapNotNull { file ->
                file.packagee
                    ?.name
                    ?.split(".")
                    ?.take(3)
                    ?.joinToString(".")
            }.toSet()

    private data class ProductModule(
        val name: String,
        val mainSourcePath: String,
        val rootPackages: Set<String>,
    )

    private companion object {
        private val EXCLUDED_DIRECTORIES =
            setOf(
                ".git",
                ".gradle",
                ".kotlin",
                "app",
                "build",
                "architecture",
                "gradle",
            )
    }
}
