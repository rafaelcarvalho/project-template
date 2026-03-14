package io.github.rafaelcarvalho.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Standard architecture rules automatically applied to every module that
 * declares the `rafaelcarvalho.architecture` convention plugin.
 *
 * The module under test is identified by the `module.name` system
 * property set by the plugin — nothing needs to be written in each module.
 *
 * To add module-specific rules, create your own test class in the module's
 * `src/test/kotlin` and scope it with:
 *   Konsist.scopeFromDirectory("${project.name}/src/main/kotlin")
 */
class ModuleArchitectureTest {
    private val moduleName: String =
        System.getProperty("module.name") ?: ""

    private val sourcePath = "$moduleName/src/main/kotlin"

    private val scope by lazy {
        assumeTrue(
            moduleName.isNotEmpty(),
            "module.name not set — not running in a domain module context, skipping.",
        )
        assumeTrue(
            Files.exists(Paths.get(sourcePath)),
            "Source path '$sourcePath' does not exist — skipping architecture checks.",
        )
        Konsist.scopeFromDirectory(sourcePath)
    }

    @Test
    fun `layered architecture is respected`() {
        scope.files.assertTrue { file ->
            val pkg = file.packagee?.name ?: return@assertTrue true
            val segments = pkg.split(".")
            when {
                segments.any { it == "core" } ->
                    file.imports.none { import ->
                        val imp = import.name
                        imp.contains(".applications.") ||
                            imp.contains(".adapters.") ||
                            imp.contains(".configurations.")
                    }
                segments.any { it == "applications" } ->
                    file.imports.none { import ->
                        val imp = import.name
                        imp.contains(".adapters.") || imp.contains(".configurations.")
                    }
                segments.any { it == "adapters" } ->
                    file.imports.none { import ->
                        val imp = import.name
                        imp.contains(".applications.") || imp.contains(".configurations.")
                    }
                segments.any { it == "configurations" } ->
                    file.imports.none { import ->
                        import.name.contains(".applications.")
                    }
                else -> true
            }
        }
    }

    @Test
    fun `core never imports spring`() {
        scope.files.assertTrue { file ->
            val isCore = file.packagee?.name?.contains(".core") == true
            !isCore || file.imports.none { it.name.startsWith("org.springframework") }
        }
    }

    @Test
    fun `classes with Adapter suffix must reside in adapters package`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("Adapter") || declaration.resideInPackage("..adapters..")
        }
    }

    @Test
    fun `adapters must implement an interface from core gateways`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("Adapter") ||
                declaration.interfaces().any { i -> i.resideInPackage("..core.gateways..") }
        }
    }

    @Test
    fun `classes with Producer suffix must reside in adapters producers package`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("Producer") || declaration.resideInPackage("..adapters.producers..")
        }
    }

    @Test
    fun `event DTOs in adapters layer must reside in adapters producers events package`() {
        scope.classes().assertTrue { declaration ->
            val inAdapters = declaration.resideInPackage("..adapters..")
            val isEvent = declaration.name.endsWith("Event")
            !inAdapters || !isEvent || declaration.resideInPackage("..adapters.producers.events..")
        }
    }

    @Test
    fun `Databases classes must reside in adapters databases package`() {
        scope.classes().assertTrue { declaration ->
            val isDatabase = declaration.name.endsWith("Entity") || declaration.name.endsWith("Repository")
            !isDatabase || declaration.resideInPackage("..adapters.databases..")
        }
    }

    @Test
    fun `http clients must reside in adapters clients package`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("Client") || declaration.resideInPackage("..adapters.clients..")
        }
    }

    @Test
    fun `http client requests DTOs must reside in adapters clients requests package`() {
        scope.classes().assertTrue { declaration ->
            val inAdapters = declaration.resideInPackage("..adapters..")
            val isRequest = declaration.name.endsWith("Request")
            !inAdapters || !isRequest || declaration.resideInPackage("..adapters.clients.requests..")
        }
    }

    @Test
    fun `http client responses DTOs must reside in adapters clients responses package`() {
        scope.classes().assertTrue { declaration ->
            val inAdapters = declaration.resideInPackage("..adapters..")
            val isResponse = declaration.name.endsWith("Response")
            !inAdapters || !isResponse || declaration.resideInPackage("..adapters.clients.responses..")
        }
    }

    @Test
    fun `constant objects must reside in adapters constants package`() {
        scope.objects().assertTrue { declaration ->
            !declaration.name.endsWith("Constant") || declaration.resideInPackage("..adapters.constants..")
        }
    }

    @Test
    fun `constants must be object declarations not regular classes`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("Constant")
        }
    }

    @Test
    fun `controllers must reside in applications controllers package`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("Controller") || declaration.resideInPackage("..applications.controllers..")
        }
    }

    @Test
    fun `controller request DTOs must reside in applications controllers requests package`() {
        scope.classes().assertTrue { declaration ->
            val inApplications = declaration.resideInPackage("..applications..")
            val isRequest = declaration.name.endsWith("Request")
            !inApplications || !isRequest || declaration.resideInPackage("..applications.controllers.requests..")
        }
    }

    @Test
    fun `controller response DTOs must reside in applications controllers responses package`() {
        scope.classes().assertTrue { declaration ->
            val inApplications = declaration.resideInPackage("..applications..")
            val isResponse = declaration.name.endsWith("Response")
            !inApplications || !isResponse || declaration.resideInPackage("..applications.controllers.responses..")
        }
    }

    @Test
    fun `consumers must reside in applications consumers package`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("Consumer") || declaration.resideInPackage("..applications.consumers..")
        }
    }

    @Test
    fun `consumer event DTOs must reside in applications consumers events package`() {
        scope.classes().assertTrue { declaration ->
            val inConsumers = declaration.resideInPackage("..applications.consumers..")
            val isEvent = declaration.name.endsWith("Event")
            !inConsumers || !isEvent || declaration.resideInPackage("..applications.consumers.events..")
        }
    }

    @Test
    fun `exceptions must reside in core exceptions package and not import frameworks`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("Exception") ||
                (
                    declaration.resideInPackage("..core.exceptions..") &&
                        declaration.containingFile.imports.none { import ->
                            import.name.startsWith("org.springframework") ||
                                import.name.startsWith("software.amazon")
                        }
                )
        }
    }

    @Test
    fun `models must reside in core models package`() {
        scope.classes().assertTrue { declaration ->
            !declaration.resideInPackage("..core.models..") ||
                declaration.containingFile.imports.none { import ->
                    import.name.startsWith("org.springframework") ||
                        import.name.startsWith("software.amazon") ||
                        (import.name.contains(".core.") && !import.name.contains(".core.models."))
                }
        }
    }

    @Test
    fun `gateway interfaces must reside in core gateways package`() {
        scope.interfaces().assertTrue { declaration ->
            !declaration.name.endsWith("Gateway") || declaration.resideInPackage("..core.gateways..")
        }
    }

    @Test
    fun `gateway interfaces must not import frameworks`() {
        scope.interfaces().assertTrue { declaration ->
            !declaration.resideInPackage("..core.gateways..") ||
                declaration.containingFile.imports.none { import ->
                    import.name.startsWith("org.springframework") ||
                        import.name.startsWith("software.amazon")
                }
        }
    }

    @Test
    fun `gateway interfaces can only depend on core models`() {
        scope.interfaces().assertTrue { declaration ->
            !declaration.resideInPackage("..core.gateways..") ||
                declaration.containingFile.imports.none { import ->
                    import.name.contains(".core.") && !import.name.contains(".core.models.")
                }
        }
    }

    @Test
    fun `service interfaces must reside in core services package`() {
        scope.interfaces().assertTrue { declaration ->
            !declaration.name.endsWith("Service") || declaration.resideInPackage("..core.services..")
        }
    }

    @Test
    fun `service implementations must reside in core services impl package`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("ImplService") || declaration.resideInPackage("..core.services.impl..")
        }
    }

    @Test
    fun `service implementations must implement a Service interface`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("ImplService") ||
                declaration.interfaces().any { i ->
                    i.name.endsWith("Service") && !i.name.endsWith("ImplService")
                }
        }
    }

    @Test
    fun `use cases must reside in core usecases package`() {
        scope.classes().assertTrue { declaration ->
            !declaration.name.endsWith("UseCase") || declaration.resideInPackage("..core.usecases..")
        }
    }
}
