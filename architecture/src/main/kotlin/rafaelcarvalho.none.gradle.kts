import io.github.rafaelcarvalho.architecture.cloud.CloudCapability
import io.github.rafaelcarvalho.architecture.cloud.CloudDependency
import io.github.rafaelcarvalho.architecture.cloud.CloudProvider
import io.github.rafaelcarvalho.architecture.cloud.configureCloudDependencies

configureCloudDependencies(
    provider = CloudProvider.NONE,
    baseLibraries = emptyList(),
    capabilityLibraries =
        mapOf(
            CloudCapability.RDS to
                listOf(
                    CloudDependency.library("postgresql"),
                    CloudDependency.testLibrary("testcontainers-postgresql"),
                ),
            CloudCapability.NOSQL to
                listOf(
                    CloudDependency.library("spring-boot-starter-data-mongodb-reactive"),
                    CloudDependency.testLibrary("testcontainers-mongodb"),
                ),
            CloudCapability.BROKER to
                listOf(
                    CloudDependency.library("spring-kafka"),
                    CloudDependency.testLibrary("testcontainers-kafka"),
                ),
            CloudCapability.STORAGE to
                listOf(
                    CloudDependency.library("minio"),
                ),
        ),
)
