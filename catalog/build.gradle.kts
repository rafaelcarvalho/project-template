// Apenas declare o plugin master.
// Tudo mais (kotlin, spring, quality, architecture, testing,
// dependências base do WebFlux, coroutines e mapeamento cloud) vem de graça.

plugins {
    id("rafaelcarvalho.demo")
}

extra["cloud.capabilities"] = "rds,broker"
