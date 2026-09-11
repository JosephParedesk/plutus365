# --- build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew :app:bootJar -x test --no-daemon

# --- runtime ---
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S plutus && adduser -S plutus -G plutus
WORKDIR /app
COPY --from=build /app/app/build/libs/app-0.0.1-SNAPSHOT.jar app.jar
USER plutus
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
