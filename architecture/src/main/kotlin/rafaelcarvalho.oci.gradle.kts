import io.github.rafaelcarvalho.architecture.cloud.CloudCapability
import io.github.rafaelcarvalho.architecture.cloud.CloudDependency
import io.github.rafaelcarvalho.architecture.cloud.CloudProvider
import io.github.rafaelcarvalho.architecture.cloud.configureCloudDependencies

configureCloudDependencies(
    provider = CloudProvider.OCI,
    baseLibraries =
        listOf(
            CloudDependency.platform("oci-bom"),
            CloudDependency.library("oci-common"),
            CloudDependency.library("oci-common-httpclient-jersey3"),
        ),
    capabilityLibraries =
        mapOf(
            CloudCapability.RDS to
                listOf(
                    CloudDependency.library("oracle-jdbc"),
                    CloudDependency.library("oci-database"),
                ),
            CloudCapability.NOSQL to listOf(CloudDependency.library("oci-nosql")),
            CloudCapability.BROKER to
                listOf(
                    CloudDependency.library("oci-ons"),
                    CloudDependency.library("oci-queue"),
                ),
            CloudCapability.STORAGE to listOf(CloudDependency.library("oci-objectstorage")),
        ),
)
