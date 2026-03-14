// Convention plugin: rafaelcarvalho.architecture
//
// When applied to a module, automatically enforces the project's standard
// architectural rules (layers, package conventions, naming, etc.) by
// injecting the shared test source from the `architecture` included build
// into the module's test source set.
//
// No test code needs to be written in each module — just declare:
//   plugins { id("rafaelcarvalho.architecture") }
//
// The standard rules run as part of `./gradlew :<module>:test`, scoped
// exclusively to that module's src/main/kotlin.
//
// To add module-specific rules, create a test class in src/test/kotlin and
// scope it with:
//   Konsist.scopeFromDirectory("${project.name}/src/main/kotlin")

plugins {
    `java-base` // required for `sourceSets` extension to be in scope
}

val sharedTestSrc = file("${rootProject.projectDir}/architecture/src/shared-test/kotlin")

sourceSets.named("test") {
    java.srcDir(sharedTestSrc)
}

dependencies {
    "testImplementation"(platform("org.junit:junit-bom:5.13.4"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    "testImplementation"("com.lemonappdev:konsist:0.17.3")
}

// workingDir = repo root so Konsist resolves "moduleName/src/main/kotlin" correctly.
// module.name is consumed by ModuleArchitectureTest to scope itself.

tasks.withType<Test>().configureEach {
    workingDir = rootProject.projectDir
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    systemProperty("module.name", project.name)
}
