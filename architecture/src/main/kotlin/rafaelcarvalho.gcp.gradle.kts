import io.github.rafaelcarvalho.architecture.cloud.CloudCapability
import io.github.rafaelcarvalho.architecture.cloud.CloudDependency
import io.github.rafaelcarvalho.architecture.cloud.CloudProvider
import io.github.rafaelcarvalho.architecture.cloud.configureCloudDependencies

configureCloudDependencies(
    provider = CloudProvider.GCP,
    baseLibraries =
        listOf(
            CloudDependency.platform("gcp-bom"),
            CloudDependency.library("gcp-core"),
        ),
    capabilityLibraries =
        mapOf(
            CloudCapability.RDS to
                listOf(
                    CloudDependency.library("postgresql"),
                    CloudDependency.library("gcp-alloydb"),
                    CloudDependency.library("gcp-alloydb-connectors"),
                ),
            CloudCapability.NOSQL to
                listOf(
                    CloudDependency.library("gcp-firestore"),
                    CloudDependency.testLibrary("testcontainers-gcloud"),
                ),
            CloudCapability.BROKER to
                listOf(
                    CloudDependency.library("gcp-pubsub"),
                    CloudDependency.testLibrary("testcontainers-gcloud"),
                ),
            CloudCapability.STORAGE to listOf(CloudDependency.library("gcp-storage")),
        ),
)
