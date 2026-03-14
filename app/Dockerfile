FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace

# Copia apenas os arquivos de build para criar uma camada de cache de dependencias.
# Esta camada só é invalidada quando os arquivos de configuração mudam,
# nao quando o codigo-fonte muda - reduz significativamente o tempo de rebuild.
COPY gradlew gradlew.bat settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY architecture/ architecture/
COPY app/build.gradle.kts app/build.gradle.kts

RUN chmod +x gradlew
RUN ./gradlew :app:dependencies --no-daemon --no-configuration-cache -q 2>/dev/null || true

# Copia o restante do codigo-fonte e compila o jar da aplicacao
COPY . .
RUN ./gradlew :app:bootJar -x test --no-daemon --no-configuration-cache

# wget estatico via busybox:musl - binario com link estatico, sem dependencias de
# glibc. Funciona em imagens distroless sem shell nem curl.
# Usado pelo HEALTHCHECK para que ECS / Kubernetes / Docker Compose consigam
# determinar se a aplicacao esta saudavel apos o start-up.
FROM busybox:musl AS wget-provider

FROM gcr.io/distroless/java25-debian12:nonroot

WORKDIR /app
COPY --from=builder /workspace/app/build/libs/*.jar /app/application.jar
COPY --from=wget-provider /bin/wget /app/wget

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD ["/app/wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
