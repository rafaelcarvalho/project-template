package io.github.rafaelcarvalho.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["io.github.rafaelcarvalho"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
