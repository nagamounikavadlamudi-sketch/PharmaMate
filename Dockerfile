FROM tomcat:9.0-jdk17-corretto

# Disable the internal shutdown port
RUN sed -i 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml

# Clear default ROOT directory
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy your project files into ROOT
COPY . /usr/local/tomcat/webapps/ROOT/

# CRITICAL STEP: Recompile Java files so the environment variables are actually read
RUN find /usr/local/tomcat/webapps/ROOT/WEB-INF/classes -name "*.java" -exec javac -cp "/usr/local/tomcat/lib/*:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib/*" {} +

EXPOSE 8080

# Clean dynamic port binding for Render
CMD sed -i "s/port=\"8080\"/port=\"${PORT:-8080}\"/" /usr/local/tomcat/conf/server.xml && catalina.sh run
