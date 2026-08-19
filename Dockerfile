# ---------- BUILD STAGE ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

# Download Maven dependencies into WEB-INF/lib
RUN mvn -q dependency:copy-dependencies \
    -DoutputDirectory=WEB-INF/lib

# Compile the existing Java source files
RUN mvn -q clean compile


# ---------- RUNTIME STAGE ----------
FROM tomcat:9.0-jdk17-corretto

# Disable Tomcat shutdown port
RUN sed -i 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml

# Remove default ROOT application
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy the complete PharmaMate application
COPY --from=build /app/ /usr/local/tomcat/webapps/ROOT/

EXPOSE 8080

# Render dynamic port
CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-8080}\\\"/\" /usr/local/tomcat/conf/server.xml && catalina.sh run"]
