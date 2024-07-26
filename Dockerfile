FROM openjdk:21-slim

# Set the maintainer email for the image
LABEL maintainer="dario.eltzner@student.uni-tuebingen.de"

COPY target/model-polisher-server-1.0.0-SNAPSHOT-standalone.jar /

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/model-polisher-server-1.0.0-SNAPSHOT-standalone.jar"]
