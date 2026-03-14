dependencies {
    "testImplementation"(platform("org.junit:junit-bom:5.13.4"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    "testImplementation"("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito")
        exclude(group = "org.mockito.kotlin")
    }
    "testImplementation"("io.mockk:mockk:1.14.6")
    "testImplementation"("com.ninja-squad:springmockk:4.0.2")
    "testImplementation"("io.projectreactor:reactor-test")
    "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    "testImplementation"("org.testcontainers:junit-jupiter:1.21.3")
    "testImplementation"("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:4.3.0")
    "testImplementation"("org.springframework:spring-webflux")
}
