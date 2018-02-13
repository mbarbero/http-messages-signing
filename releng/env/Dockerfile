FROM debian:9.3

LABEL maintainer "Mikaël Barbero <mikael.barbero@gmail.com>"

RUN apt-get update && apt-get install -y \
	locales \
	openjdk-8-jdk \
	maven \
	openssl \
	gnupg \
	--no-install-recommends \
	&& rm -rf /var/lib/apt/lists/*

RUN locale-gen en_US.UTF-8  
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
	
CMD ["bash"]