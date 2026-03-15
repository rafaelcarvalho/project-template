// Convention plugin: rafaelcarvalho.module
//
// Master plugin — applies every standard convention to a product module.
// Just add this single plugin to your module's build.gradle.kts:
//
//   plugins {
//       id("rafaelcarvalho.module")
//       alias(libs.plugins.spring.boot)   // if this is a Spring Boot app
//   }
//
// Cloud platform is declared once at the root project:
//
//   extra["cloud.platform"] = "aws"
//
// Module-specific capabilities are declared in each module:
//
//   extra["cloud.capabilities"] = "rds,broker"
//
// Extra dependencies not already provided by the selected platform or testing
// plugins can still be added in the module's `dependencies { }` block.

import io.github.rafaelcarvalho.architecture.module.configureModuleDependencies

plugins {
    id("rafaelcarvalho.kotlin")
    id("rafaelcarvalho.spring")
    id("rafaelcarvalho.testing")
    id("rafaelcarvalho.quality")
    id("rafaelcarvalho.architecture")
}

configureModuleDependencies()
