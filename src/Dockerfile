FROM alpine:latest
MAINTAINER zhengxu
WORKDIR /Users/zhengxu/Documents/docker/alpine/
ENV JAVA_DIR=/Users/zhengxu/Documents/docker/alpine/local
ENV jar_home=/Users/zhengxu/Documents/docker/alpine/jar
COPY ./jdk-8u321-linux-x64.tar.gz $JAVA_DIR
COPY /Users/zhengxu/Documents/proxyaka/target/proxyaka-1.jar $jar_home/app.jar
RUN cd $JAVA_HOME \
    && tar -xf ./jdk-8u321-linux-x64.tar.gz \
    && move ./jdk1.8.0_321 ./java8
ENV JAVA_HOME = $JAVA_DIR/java8
ENV PATH = $JAVA_HOME/bin:$PATH
EXPOSE 9999
ENTRYPOINT java -jar app.jar

