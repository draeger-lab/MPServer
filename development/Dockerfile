FROM openjdk:21-slim

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install Leiningen
RUN curl -s https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein \
    && chmod +x /usr/local/bin/lein

RUN lein version


RUN mkdir -p /etc/leiningen

RUN echo "{:user {:plugins [[lein-localrepo \"0.5.4\"]]}}" > /etc/leiningen/profiles.clj

WORKDIR /opt

COPY . MPServer

EXPOSE 3000

COPY dev/startup.sh /usr/local/bin/startup.sh

RUN chmod +x /usr/local/bin/startup.sh

ENTRYPOINT ["/usr/local/bin/startup.sh"]
