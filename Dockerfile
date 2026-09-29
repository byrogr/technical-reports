# Imagen de la API de informes técnicos. Compila con Maven dentro de Docker, así que el
# equipo donde se construye no necesita tener Java instalado.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -q package -DskipTests

FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --from=build /workspace/target/technical-reports-*.jar app.jar
USER app
# Zona horaria de Perú para createdAt y la fecha de generación del PDF
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=America/Lima"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
