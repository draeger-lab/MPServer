#!/bin/bash

# Exit on any error
set -e

cd /opt
rm -rf /opt/ModelPolisher || true  >/dev/null 2>&1
rm -rf /opt/MPServer || true  >/dev/null 2>&1

# Pull projects
echo "Pulling Model Polisher"
git clone https://github.com/draeger-lab/ModelPolisher.git >/dev/null 2>&1 \
    && cd /opt/ModelPolisher \
    && git pull >/dev/null 2>&1 \
    && git checkout 2.1 >/dev/null 2>&1

echo "Checked out revision: $(git rev-parse HEAD)"

echo "Bulding Model Polisher"
./gradlew jar -x test >/dev/null 2>&1

echo "Installing Jar in local Maven repo"
lein localrepo install target/ModelPolisher-2.1.jar edu.ucsd.sbrg.ModelPolisher 2.1 >/dev/null 2>&1

cd /opt

echo "Pulling MPServer"
git clone https://github.com/draeger-lab/MPServer.git >/dev/null 2>&1 \
    && cd /opt/MPServer \
    && git pull >/dev/null 2>&1

sed -i 's/\(edu.ucsd.sbrg.ModelPolisher "\)[^"]*\(".*\)/\12.1\2/' project.clj

echo "Checked out revision: $(git rev-parse HEAD)"

echo "Building MPServer"
lein uberjar >/dev/null 2>&1

cd /opt

echo "Starting Server"
exec java -jar MPServer/target/model-polisher-server-1.0.0-SNAPSHOT-standalone.jar


