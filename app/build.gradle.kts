plugins {
    id("rafaelcarvalho.kotlin")
    id("rafaelcarvalho.spring")
    id("rafaelcarvalho.testing")
    id("rafaelcarvalho.quality")
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.kotlinx.coroutines.reactor)
}
