# Imagen del backend Ktor para el PoC en Render. Solo copia lo que permite .dockerignore.

FROM eclipse-temurin:25-jdk AS construccion
WORKDIR /fuente
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN ./gradlew --no-daemon --quiet dependencies > /dev/null
COPY src src
# Las pruebas corren antes del push con `gradlew check`; aquí solo se empaqueta.
RUN ./gradlew --no-daemon installDist -x test

FROM eclipse-temurin:25-jre
RUN useradd --system --no-create-home prefactura
WORKDIR /app
COPY --from=construccion /fuente/build/install/prefactura-consulta-externa ./
COPY config config
COPY assets assets
COPY despliegue/iniciar.sh ./
USER prefactura
# Render enruta al puerto 10000 por defecto; dentro del contenedor hay que escuchar en todas las interfaces.
# enable-native-access: jansi (consola de logback) carga una librería nativa y JDK 25 lo advierte en cada arranque.
ENV HSLV_HOST=0.0.0.0 \
    HSLV_PUERTO=10000 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 --enable-native-access=ALL-UNNAMED"
EXPOSE 10000
ENTRYPOINT ["sh", "iniciar.sh"]
