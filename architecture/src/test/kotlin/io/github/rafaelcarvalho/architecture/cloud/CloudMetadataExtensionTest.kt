package io.github.rafaelcarvalho.architecture.cloud

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CloudMetadataExtensionTest {
    @Test
    fun `registers provider and capabilities from ext declarations`() {
        val metadata = createCloudMetadataExtension()

        metadata("cloud.platform:aws")
        metadata("cloud.capabilities:rds, storage")

        assertEquals(CloudProvider.AWS, metadata.platformProvider().get())
        assertEquals(setOf(CloudCapability.RDS, CloudCapability.STORAGE), metadata.capabilitiesProvider().get())
    }

    @Test
    fun `rejects unsupported declaration key`() {
        val metadata = createCloudMetadataExtension()

        val exception =
            assertThrows(GradleException::class.java) {
                metadata("cloud.region:sa-east-1")
            }

        assertEquals(
            "Unsupported cloud metadata key 'cloud.region'. Supported keys: cloud.platform, cloud.capabilities.",
            exception.message,
        )
    }

    @Test
    fun `rejects provider mixing`() {
        val metadata = createCloudMetadataExtension()

        metadata("cloud.platform:aws")

        val exception =
            assertThrows(GradleException::class.java) {
                metadata("cloud.platform:gcp")
            }

        assertEquals(
            "Cloud provider 'aws' is already configured and cannot be mixed with 'gcp'.",
            exception.message,
        )
    }

    private fun createCloudMetadataExtension(): CloudMetadataExtension =
        ProjectBuilder
            .builder()
            .build()
            .objects
            .newInstance(CloudMetadataExtension::class.java)
}
