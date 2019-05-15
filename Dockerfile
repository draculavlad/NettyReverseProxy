FROM dockette/mvn:latest

COPY target/SocksProxy.jar /opt/SocksProxy.jar

ENV exec_args=${exec_args}

CMD java -jar /opt/SocksProxy.jar ${exec_args} 
