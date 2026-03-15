package io.github.rafaelcarvalho.architecture.cloud

import io.github.rafaelcarvalho.architecture.catalog.CatalogDependency
import io.github.rafaelcarvalho.architecture.catalog.addCatalogDependencies
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
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
    NONE(
        id = "none",
        importPrefixes = emptySet(),
    ),
    ;

    companion object {
        fun from(rawProvider: String): CloudProvider {
            val normalizedProvider = rawProvider.trim().lowercase(Locale.ROOT)
            return entries.firstOrNull { provider -> provider.id == normalizedProvider }
                ?: throw GradleException(
                    "Unsupported cloud provider '$rawProvider'. Supported values: ${entries.joinToString { it.id }}.",
                )
        }
    }
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

internal data class CloudProviderMapping(
    val baseLibraries: List<CatalogDependency>,
    val capabilityLibraries: Map<CloudCapability, List<CatalogDependency>>,
)

abstract class CloudMetadataExtension {
    internal abstract val platform: Property<CloudProvider>
    internal abstract val capabilities: SetProperty<CloudCapability>

    init {
        capabilities.convention(emptySet())
    }

    operator fun invoke(declaration: String) {
        val key = declaration.substringBefore(':', missingDelimiterValue = "").trim().lowercase(Locale.ROOT)
        val value = declaration.substringAfter(':', missingDelimiterValue = "").trim()

        if (key.isEmpty() || value.isEmpty()) {
            throw GradleException(
                "Invalid cloud metadata declaration '$declaration'. Expected format: cloud.platform:aws or cloud.capabilities:rds,storage.",
            )
        }

        when (key) {
            "cloud.platform" -> {
                registerPlatform(CloudProvider.from(value))
            }

            "cloud.capabilities", "cloud.capability", "cloud.cababilities" -> {
                registerCapabilities(value.split(',').map(String::trim).filter(String::isNotEmpty))
            }

            else -> {
                throw GradleException(
                    "Unsupported cloud metadata key '$key'. Supported keys: cloud.platform, cloud.capabilities.",
                )
            }
        }
    }

    internal fun platformProvider(): Provider<CloudProvider> = platform

    internal fun capabilitiesProvider(): Provider<Set<CloudCapability>> = capabilities

    private fun registerPlatform(provider: CloudProvider) {
        val existingProvider = platform.orNull
        if (existingProvider != null && existingProvider != provider) {
            throw GradleException(
                "Cloud provider '${existingProvider.id}' is already configured and cannot be mixed with '${provider.id}'.",
            )
        }

        platform.set(provider)
    }

    private fun registerCapabilities(rawCapabilities: Iterable<String>) {
        val updatedCapabilities = capabilities.getOrElse(emptySet()) + rawCapabilities.map(CloudCapability::from)
        capabilities.set(updatedCapabilities.toSet())
    }
}

private const val CLOUD_PLATFORM_KEY = "cloud.platform"
private val CLOUD_CAPABILITIES_KEYS = setOf("cloud.capabilities", "cloud.capability", "cloud.cababilities")

private val CLOUD_PROVIDER_MAPPINGS: Map<CloudProvider, CloudProviderMapping> =
    mapOf(
        CloudProvider.AWS to
            CloudProviderMapping(
                baseLibraries =
                    listOf(
                        CatalogDependency.platform("aws-bom"),
                        CatalogDependency.library("aws-url-connection-client"),
                    ),
                capabilityLibraries =
                    mapOf(
                        CloudCapability.RDS to
                            listOf(
                                CatalogDependency.library("postgresql"),
                                CatalogDependency.library("aws-rds"),
                                CatalogDependency.library("aws-rdsdata"),
                                CatalogDependency.library("aws-secretsmanager"),
                                CatalogDependency.testLibrary("testcontainers-localstack"),
                            ),
                        CloudCapability.NOSQL to
                            listOf(
                                CatalogDependency.library("aws-dynamodb"),
                                CatalogDependency.testLibrary("testcontainers-localstack"),
                            ),
                        CloudCapability.BROKER to
                            listOf(
                                CatalogDependency.library("aws-sns"),
                                CatalogDependency.library("aws-sqs"),
                                CatalogDependency.testLibrary("testcontainers-localstack"),
                            ),
                        CloudCapability.STORAGE to
                            listOf(
                                CatalogDependency.library("aws-s3"),
                                CatalogDependency.testLibrary("testcontainers-localstack"),
                            ),
                    ),
            ),
        CloudProvider.GCP to
            CloudProviderMapping(
                baseLibraries =
                    listOf(
                        CatalogDependency.platform("gcp-bom"),
                        CatalogDependency.library("gcp-core"),
                    ),
                capabilityLibraries =
                    mapOf(
                        CloudCapability.RDS to
                            listOf(
                                CatalogDependency.library("postgresql"),
                                CatalogDependency.library("gcp-alloydb"),
                                CatalogDependency.library("gcp-alloydb-connectors"),
                            ),
                        CloudCapability.NOSQL to
                            listOf(
                                CatalogDependency.library("gcp-firestore"),
                                CatalogDependency.testLibrary("testcontainers-gcloud"),
                            ),
                        CloudCapability.BROKER to
                            listOf(
                                CatalogDependency.library("gcp-pubsub"),
                                CatalogDependency.testLibrary("testcontainers-gcloud"),
                            ),
                        CloudCapability.STORAGE to listOf(CatalogDependency.library("gcp-storage")),
                    ),
            ),
        CloudProvider.OCI to
            CloudProviderMapping(
                baseLibraries =
                    listOf(
                        CatalogDependency.platform("oci-bom"),
                        CatalogDependency.library("oci-common"),
                        CatalogDependency.library("oci-common-httpclient-jersey3"),
                    ),
                capabilityLibraries =
                    mapOf(
                        CloudCapability.RDS to
                            listOf(
                                CatalogDependency.library("oracle-jdbc"),
                                CatalogDependency.library("oci-database"),
                            ),
                        CloudCapability.NOSQL to listOf(CatalogDependency.library("oci-nosql")),
                        CloudCapability.BROKER to
                            listOf(
                                CatalogDependency.library("oci-ons"),
                                CatalogDependency.library("oci-queue"),
                            ),
                        CloudCapability.STORAGE to listOf(CatalogDependency.library("oci-objectstorage")),
                    ),
            ),
        CloudProvider.NONE to
            CloudProviderMapping(
                baseLibraries = emptyList(),
                capabilityLibraries =
                    mapOf(
                        CloudCapability.RDS to
                            listOf(
                                CatalogDependency.library("postgresql"),
                                CatalogDependency.testLibrary("testcontainers-postgresql"),
                            ),
                        CloudCapability.NOSQL to
                            listOf(
                                CatalogDependency.library("spring-boot-starter-data-mongodb-reactive"),
                                CatalogDependency.testLibrary("testcontainers-mongodb"),
                            ),
                        CloudCapability.BROKER to
                            listOf(
                                CatalogDependency.library("spring-kafka"),
                                CatalogDependency.testLibrary("testcontainers-kafka"),
                            ),
                        CloudCapability.STORAGE to listOf(CatalogDependency.library("minio")),
                    ),
            ),
    )

internal fun Project.configureCloudDependencies() {
    val metadata = cloudMetadata()
    val platformProvider = metadata.platformProvider()
    val capabilitiesProvider = metadata.capabilitiesProvider()

    afterEvaluate {
        val requestedCapabilities = (capabilitiesProvider.getOrElse(emptySet()) + capabilitiesFromExtraProperties()).toSet()
        val resolvedProvider = requiredCloudProviderOrNull(platformProvider, requestedCapabilities) ?: return@afterEvaluate
        val providerMapping = resolvedProvider.requiredMapping()

        addCatalogDependencies(providerMapping.baseLibraries)

        requestedCapabilities.forEach { capability ->
            val dependenciesForCapability =
                providerMapping.capabilityLibraries[capability]
                    ?: throw GradleException(
                        "No dependency mapping configured for capability '${capability.id}' on provider '${resolvedProvider.id}'.",
                    )

            addCatalogDependencies(dependenciesForCapability)
        }
    }
}

private fun Project.requiredCloudProviderOrNull(
    platformProvider: Provider<CloudProvider>,
    requestedCapabilities: Set<CloudCapability>,
): CloudProvider? {
    validateNoModuleCloudPlatform(platformProvider)

    val rootProvider = rootProject.rootCloudProviderOrNull()
    if (rootProvider != null) {
        return rootProvider
    }

    if (requestedCapabilities.isNotEmpty()) {
        throw GradleException(
            "Cloud capabilities were declared for module '$path' but no cloud provider was configured. " +
                "Declare extra[\"$CLOUD_PLATFORM_KEY\"] = \"<provider>\" in the root build file.",
        )
    }

    return null
}

private fun Project.validateNoModuleCloudPlatform(platformProvider: Provider<CloudProvider>) {
    if (platformProvider.isPresent) {
        throw GradleException(
            "Cloud platform must be declared once at the root project via extra[\"$CLOUD_PLATFORM_KEY\"] = \"<provider>\". " +
                "Remove module-level platform declarations from '$path'.",
        )
    }

    if (moduleCloudPlatformFromExtraProperties() != null) {
        throw GradleException(
            "Cloud platform must be declared once at the root project via extra[\"$CLOUD_PLATFORM_KEY\"] = \"<provider>\". " +
                "Module '$path' must declare only cloud capabilities.",
        )
    }
}

private fun Project.capabilitiesFromExtraProperties(): Set<CloudCapability> =
    CLOUD_CAPABILITIES_KEYS
        .flatMap(::rawCapabilitiesFromExtraProperty)
        .map(CloudCapability::from)
        .toSet()

private fun Project.rawCapabilitiesFromExtraProperty(key: String): List<String> {
    if (!extensions.extraProperties.has(key)) {
        return emptyList()
    }

    val value = extensions.extraProperties.get(key)
    return when (value) {
        is String -> {
            value
                .split(',')
                .map(String::trim)
                .filter(String::isNotEmpty)
        }

        is Iterable<*> -> {
            value
                .mapNotNull { it?.toString()?.trim() }
                .filter(String::isNotEmpty)
        }

        else -> {
            throw GradleException(
                "Unsupported value for '$key' in module '$path'. Use a comma-separated String or Iterable<String>.",
            )
        }
    }
}

private fun Project.moduleCloudPlatformFromExtraProperties(): CloudProvider? =
    extensions
        .extraProperties
        .takeIf { it.has(CLOUD_PLATFORM_KEY) }
        ?.get(CLOUD_PLATFORM_KEY)
        ?.toString()
        ?.trim()
        ?.ifEmpty { null }
        ?.let(CloudProvider::from)

private fun Project.rootCloudProviderOrNull(): CloudProvider? {
    val rootExtraValue =
        if (extensions.extraProperties.has(CLOUD_PLATFORM_KEY)) {
            extensions
                .extraProperties
                .get(CLOUD_PLATFORM_KEY)
                .toString()
                .trim()
                .ifEmpty { null }
        } else {
            null
        }

    val gradlePropertyValue =
        providers
            .gradleProperty(CLOUD_PLATFORM_KEY)
            .orNull
            ?.trim()
            ?.ifEmpty { null }

    if (
        rootExtraValue != null &&
        gradlePropertyValue != null &&
        !rootExtraValue.equals(gradlePropertyValue, ignoreCase = true)
    ) {
        throw GradleException(
            "Conflicting root cloud platform declarations: extra[\"$CLOUD_PLATFORM_KEY\"]='$rootExtraValue' and -P$CLOUD_PLATFORM_KEY='$gradlePropertyValue'.",
        )
    }

    val resolvedValue = rootExtraValue ?: gradlePropertyValue ?: return null
    return CloudProvider.from(resolvedValue)
}

private fun CloudProvider.requiredMapping(): CloudProviderMapping =
    CLOUD_PROVIDER_MAPPINGS[this]
        ?: throw GradleException("No dependency mapping configured for provider '$id'.")

private fun Project.cloudMetadata(): CloudMetadataExtension {
    if (extensions.findByType(CloudMetadataExtension::class.java) == null) {
        return extensions.create("cloudMetadata", CloudMetadataExtension::class.java)
    }

    return extensions.getByType(CloudMetadataExtension::class.java)
}
