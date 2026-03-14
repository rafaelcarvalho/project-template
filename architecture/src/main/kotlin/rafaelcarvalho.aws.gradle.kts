import io.github.rafaelcarvalho.architecture.cloud.CloudCapability
import io.github.rafaelcarvalho.architecture.cloud.CloudDependency
import io.github.rafaelcarvalho.architecture.cloud.CloudProvider
import io.github.rafaelcarvalho.architecture.cloud.configureCloudDependencies

configureCloudDependencies(
    provider = CloudProvider.AWS,
    baseLibraries =
        listOf(
            CloudDependency.platform("aws-bom"),
            CloudDependency.library("aws-url-connection-client"),
        ),
    capabilityLibraries =
        mapOf(
            CloudCapability.RDS to
                listOf(
                    CloudDependency.library("postgresql"),
                    CloudDependency.library("aws-rds"),
                    CloudDependency.library("aws-rdsdata"),
                    CloudDependency.library("aws-secretsmanager"),
                ),
            CloudCapability.NOSQL to listOf(CloudDependency.library("aws-dynamodb")),
            CloudCapability.BROKER to
                listOf(
                    CloudDependency.library("aws-sns"),
                    CloudDependency.library("aws-sqs"),
                ),
            CloudCapability.STORAGE to listOf(CloudDependency.library("aws-s3")),
        ),
)
