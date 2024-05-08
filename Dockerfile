FROM openjdk:17-jdk-alpine

# Create Maven 3.9.6
RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
RUN tar -zxvf  apache-maven-3.9.6-bin.tar.gz

RUN cp -R apache-maven-3.9.6 /usr/local/bin
RUN export PATH=apache-maven-3.9.6/bin:$PATH
RUN export PATH=/usr/local/bin/apache-maven-3.9.6/bin:$PATH
RUN ln -s /usr/local/bin/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn
#RUN ls -l /usr/local/bin
RUN echo $PATH


# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/user-center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]