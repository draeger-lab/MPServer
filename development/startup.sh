#!/bin/bash

# Exit on any error
set -e

# Pull projects
echo "Pulling Model Polisher"
git clone https://github.com/draeger-lab/ModelPolisher.git >/dev/null 2>&1 \
    && cd /opt/ModelPolisher \
    && git pull >/dev/null 2>&1 \
    && git checkout 2.1 >/dev/null 2>&1

echo "Bulding Model Polisher"
./gradlew jar -x test >/dev/null 2>&1

echo "Installing Jar in local Maven repo"
lein localrepo install target/ModelPolisher-2.1.jar edu.ucsd.sbrg.ModelPolisher 2.1 >/dev/null 2>&1
echo "Installing SysBio Jar in local Maven repo"
lein localrepo install lib/de/zbit/SysBio/1390/SysBio-1390.jar de.zbit.SysBio 1390 >/dev/null 2>&1

cd /opt/MPServer

sed -i 's/\(edu.ucsd.sbrg.ModelPolisher "\)[^"]*\(".*\)/\12.1\2/' project.clj

echo "Building MPServer"
lein uberjar >/dev/null 2>&1

echo "Starting Server"
exec java -jar target/model-polisher-server-1.0.0-SNAPSHOT-standalone.jar


