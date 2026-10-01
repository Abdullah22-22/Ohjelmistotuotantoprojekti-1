FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y \
    libgtk-3-0 libgl1 libxtst6 libxrender1 libxi6 \
    libasound2t64 fontconfig \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/week3-junit-1.0-SNAPSHOT.jar app.jar

ENV DISPLAY=host.docker.internal:0.0
ENV DB_URL=jdbc:mariadb://host.docker.internal:3307/tempdb
ENV DB_USER=root
ENV DB_PASS=root

CMD ["java", "-jar", "app.jar"]