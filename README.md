# NettyReverseProxy

## build
```
mvn clean package -DskipTests
```

## build docker container
```
docker build -t jacob/netty-proxy-service .
```

## run container
```
docker run -itd -e --name=$c_name exec_args="$remoteIp $remotePort 0.0.0.0 $localPort" jacob/netty-proxy-service
```
