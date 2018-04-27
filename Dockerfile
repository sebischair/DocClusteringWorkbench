FROM anapsix/alpine-java:8_jdk

ENV SBT_URL=https://dl.bintray.com/sbt/native-packages/sbt
ENV SBT_VERSION 0.13.15
ENV INSTALL_DIR /usr/local
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin

# Install sbt
RUN apk add --no-cache --update bash wget && mkdir -p "$SBT_HOME" && \
    wget -qO - --no-check-certificate "https://dl.bintray.com/sbt/native-packages/sbt/$SBT_VERSION/sbt-$SBT_VERSION.tgz" |  tar xz -C $INSTALL_DIR && \
    echo -ne "- with sbt $SBT_VERSION\n" >> /root/.built

ENV PROJECT_HOME /usr/src
COPY dist/docclustering-1.0.zip ${PROJECT_HOME}/docclustering-1.0.zip

RUN cd ${PROJECT_HOME} && \
    unzip docclustering-1.0.zip && \
    chmod +x ${PROJECT_HOME}/docclustering-1.0.zip/bin/docclustering

CMD ["/usr/src/docclustering-1.0.zip/bin/docclustering", "-Dhttp.port=3006"]

# Expose port 3006
EXPOSE 3006