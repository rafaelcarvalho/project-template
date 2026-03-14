import org.gradle.api.attributes.Bundling

plugins {
    `kotlin-dsl`
    id("dev.detekt") version "2.0.0-alpha.2"
}

group = "io.github.rafaelcarvalho.architecture"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

val ktlintCli =
    configurations.create("ktlintCli") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.SHADOWED))
        }
    }

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.spring.gradle.plugin)
    implementation(libs.spring.boot.gradle.plugin)
    implementation(libs.spring.dependency.management.plugin.marker)
    implementation(libs.spring.dependency.management.gradle.plugin)
    implementation(libs.graalvm.native.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)

    add(ktlintCli.name, "com.pinterest.ktlint:ktlint-cli:1.5.0")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.lemonappdev:konsist:0.17.3")
}

// src/shared-test/kotlin contains ModuleArchitectureTest, which is
// injected by the architecture plugin into each domain module.
// It is also compiled here to ensure it stays valid.

sourceSets {
    test {
        kotlin.srcDir("src/shared-test/kotlin")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    baseline = file("config/detekt/baseline.xml")
    config.setFrom(file("config/detekt/detekt.yml"))
}

val analyzedSources =
    fileTree(rootDir.parentFile) {
        include("*/src/**/*.kt")
        include("*/src/**/*.kts")
        include("*.gradle.kts")
        include("settings.gradle.kts")
        include("architecture/src/**/*.kts")
        exclude("**/build/**")
        // shared-test sources are test fixtures injected into domain modules;
        // they are analysed there and should not be linted in the architecture build.
        exclude("**/shared-test/**")
    }

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    setSource(analyzedSources)
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**")

    reports {
        html.required.set(true)
        sarif.required.set(true)
    }
}

tasks.register<JavaExec>("ktlintAll") {
    group = "verification"
    description = "Runs Ktlint over all repository Kotlin sources"

    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir.parentFile
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    args(
        "--relative",
        "--reporter=plain",
        "--reporter=checkstyle,output=architecture/build/reports/ktlint/ktlint-checkstyle.xml",
        "*/src/**/*.kt",
        "!*/src/shared-test/**",
        "*.gradle.kts",
        "settings.gradle.kts",
        "architecture/src/**/*.kts",
    )
}

tasks.register<JavaExec>("ktlintFormatAll") {
    group = "formatting"
    description = "Applies Ktlint auto-formatting over all repository Kotlin sources"

    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir.parentFile
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    args(
        "-F",
        "--relative",
        "*/src/**/*.kt",
        "!*/src/shared-test/**",
        "*.gradle.kts",
        "settings.gradle.kts",
        "architecture/src/**/*.kts",
    )
}

tasks.test {
    useJUnitPlatform()
    workingDir = rootDir.parentFile
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.named("check") {
    dependsOn(tasks.named("ktlintAll"))
}
