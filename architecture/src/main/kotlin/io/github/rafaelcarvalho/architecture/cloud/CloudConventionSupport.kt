package io.github.rafaelcarvalho.architecture.cloud

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import java.util.Locale

internal enum class CloudProvider(
    val id: String,
    val importPrefixes: Set<String>,
) {
    AWS(
        id = "aws",
        importPrefixes = setOf("software.amazon.awssdk"),
    ),
    GCP(
        id = "gcp",
        importPrefixes = setOf("com.google.cloud", "com.google.api.gax", "com.google.auth"),
    ),
    OCI(
        id = "oci",
        importPrefixes = setOf("com.oracle.bmc"),
    ),
}

internal enum class CloudCapability(
    val id: String,
) {
    RDS("rds"),
    NOSQL("nosql"),
    BROKER("broker"),
    STORAGE("storage"),
    ;

    companion object {
        fun from(rawCapability: String): CloudCapability {
            val normalizedCapability = rawCapability.trim().lowercase(Locale.ROOT)
            return entries.firstOrNull { capability -> capability.id == normalizedCapability }
                ?: throw GradleException(
                    "Unsupported cloud capability '$rawCapability'. Supported values: ${entries.joinToString { it.id }}.",
                )
        }
    }
}

internal open class CloudPlatformExtension internal constructor(
    private val provider: CloudProvider,
) {
    internal val requestedCapabilities = linkedSetOf<CloudCapability>()

    fun platform(vararg capabilities: String) {
        capabilities
            .map(CloudCapability::from)
            .forEach(requestedCapabilities::add)
    }

    operator fun invoke(vararg capabilities: String) {
        platform(*capabilities)
    }

    fun provider(): String = provider.id
}

internal data class CloudDependency(
    val alias: String,
    val isPlatform: Boolean = false,
) {
    companion object {
        fun library(alias: String): CloudDependency = CloudDependency(alias = alias)

        fun platform(alias: String): CloudDependency = CloudDependency(alias = alias, isPlatform = true)
    }
}

private const val CLOUD_PROVIDER_PROPERTY = "io.github.rafaelcarvalho.architecture.cloud.provider"

internal fun Project.configureCloudDependencies(
    provider: CloudProvider,
    baseLibraries: List<CloudDependency>,
    capabilityLibraries: Map<CloudCapability, List<CloudDependency>>,
) {
    val extension = registerCloudPlatformExtension(provider)
    addLibraries(baseLibraries)

    afterEvaluate {
        extension.requestedCapabilities.forEach { capability ->
            val dependenciesForCapability =
                capabilityLibraries[capability]
                    ?: throw GradleException(
                        "No dependency mapping configured for capability '${capability.id}' on provider '${provider.id}'.",
                    )

            addLibraries(dependenciesForCapability)
        }
    }
}

private fun Project.registerCloudPlatformExtension(provider: CloudProvider): CloudPlatformExtension {
    val extraProperties = extensions.extraProperties
    val existingProvider =
        if (extraProperties.has(CLOUD_PROVIDER_PROPERTY)) {
            extraProperties.get(CLOUD_PROVIDER_PROPERTY) as String
        } else {
            null
        }

    if (existingProvider != null && existingProvider != provider.id) {
        throw GradleException(
            "Module '$path' already uses cloud provider '$existingProvider' and cannot mix it with '${provider.id}'.",
        )
    }

    extraProperties.set(CLOUD_PROVIDER_PROPERTY, provider.id)

    return extensions.findByType(CloudPlatformExtension::class.java)
        ?: extensions.create("platform", CloudPlatformExtension::class.java, provider)
}

private fun Project.addLibraries(dependenciesToAdd: List<CloudDependency>) {
    val catalogs = extensions.getByType(VersionCatalogsExtension::class.java)
    val libraries = catalogs.named("libs")

    dependenciesToAdd.forEach { dependency ->
        val dependencyProvider =
            libraries.findLibrary(dependency.alias).orElseThrow {
                GradleException("Library alias '${dependency.alias}' was not found in the libs version catalog.")
            }

        if (dependency.isPlatform) {
            dependencies.add("implementation", dependencies.platform(dependencyProvider.get()))
        } else {
            dependencies.add("implementation", dependencyProvider.get())
        }
    }
}
