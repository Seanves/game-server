FROM openjdk:21-oracle

WORKDIR /app

COPY ./target/gameserver-1.0.jar .

EXPOSE 8080

CMD ["java", "-jar", "gameserver-1.0.jar"]