// Convention plugin: rafaelcarvalho.demo
//
// Applies the standard module conventions for demo/sample modules and disables
// executable Spring Boot packaging when the Boot plugin is present.

import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("rafaelcarvalho.module")
}

pluginManager.withPlugin("org.springframework.boot") {
    tasks.named<BootJar>("bootJar") {
        enabled = false
    }

    tasks.named<Jar>("jar") {
        enabled = true
    }
}
