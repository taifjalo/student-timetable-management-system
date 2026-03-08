FROM eclipse-temurin:21-jdk
LABEL authors="taifjalo1"

ENV DISPLAY=host.docker.internal:0.0

RUN apt-get update && \
    apt-get install -y maven wget unzip libgtk-3-0 libgbm1 libx11-6 && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN wget https://download2.gluonhq.com/openjfx/21/openjfx-21_linux-x64_bin-sdk.zip -O /tmp/openjfx.zip && \
    unzip /tmp/openjfx.zip -d /opt && \
    rm /tmp/openjfx.zip

WORKDIR /app

COPY pom.xml .

# Download dependencies as a separate layer
RUN mvn dependency:go-offline -q

COPY src ./src

# Build in a separate layer
RUN mvn clean package -DskipTests -q

RUN ls -l target

COPY target/student-timetable-1.0-SNAPSHOT.jar app.jar

CMD ["java", "--module-path", "/opt/javafx-sdk-21/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "app.jar"]