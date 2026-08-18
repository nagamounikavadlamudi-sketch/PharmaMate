FROM tomcat:9.0-jdk17-corretto

# Disable the internal shutdown port to fix the "Invalid shutdown command" error
RUN sed -i 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml

# Clear default ROOT directory
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy your project files into ROOT
COPY . /usr/local/tomcat/webapps/ROOT/

EXPOSE 8080

# Clean dynamic port binding for Render
CMD sed -i "s/port=\"8080\"/port=\"${PORT:-8080}\"/" /usr/local/tomcat/conf/server.xml && catalina.sh run
