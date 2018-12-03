FROM elasticsearch:2.3.5
MAINTAINER Wildcard Corp

# Update packages
RUN apt-get update -y

# Install Python Setuptools
RUN apt-get install -y git-core maven

RUN cd /opt && git clone https://github.com/castlecms/elasticsearch-castle-scoring.git

RUN export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64

RUN cd /opt/elasticsearch-castle-scoring && mvn package

RUN /usr/share/elasticsearch/bin/plugin install file:///opt/elasticsearch-castle-scoring/target/releases/elasticsearch-castle-script-2.3.5.zip