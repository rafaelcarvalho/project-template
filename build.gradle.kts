plugins {
    base
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.graalvm.native) apply false
}

extra["cloud.platform"] = "none"

subprojects {
    group = "io.github.rafaelcarvalho"
    version =
        providers
            .environmentVariable("SEMANTIC_VERSION")
            .orElse("0.1.0-SNAPSHOT")
            .get()
}

tasks.register<GradleBuild>("quality") {
    dir = file("architecture")
    tasks = listOf("check")
}

tasks.register<GradleBuild>("format") {
    dir = file("architecture")
    tasks = listOf("ktlintFormatAll")
}

tasks.named("check") {
    dependsOn("quality")
}
