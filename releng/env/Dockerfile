FROM debian:9.3

LABEL maintainer "Mikaël Barbero <mikael.barbero@gmail.com>"

RUN apt-get update && apt-get install -y \
	locales \
	wget \
	openjdk-8-jdk \
	openssl \
	gnupg \
	--no-install-recommends \
	&& rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/local/ && cd /usr/local/ \
	&& wget http://apache.mirrors.ovh.net/ftp.apache.org/dist/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz \
	&& tar zxf apache-maven-3.5.2-bin.tar.gz \
	&& ln -s /usr/local/apache-maven-3.5.2/bin/mvn /usr/local/bin/mvn

RUN locale-gen en_US.UTF-8  
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
	
CMD ["bash"]