FROM gradle:jdk20 AS build
WORKDIR /home/gradle/src
ADD ./build.gradle.kts build.gradle.kts
ADD ./settings.gradle.kts settings.gradle.kts
ADD ./src src
RUN gradle clean build --no-daemon

FROM azul/zulu-openjdk:20-jre-headless
COPY --from=build /home/gradle/src/build/libs/diplom-bot-1.0-SNAPSHOT.jar diplom-bot-1.0-SNAPSHOT.jar
ENTRYPOINT java -jar diplom-bot-1.0-SNAPSHOT.jar
