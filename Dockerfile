FROM tomcat:9.0-jdk17-corretto

RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY . /usr/local/tomcat/webapps/PharmaMate/

EXPOSE 8080

CMD ["catalina.sh", "run"]
