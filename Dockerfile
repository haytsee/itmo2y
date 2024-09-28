FROM tomcat:9.0.1-jre8-alpine

ADD ./src/main/webapp /usr/local/tomcat/webapps/webapp

EXPOSE 2313

CMD ["catalina.sh", "run"]
