#!/bin/bash

java \
    -Dapis \
    -Dmodels \
    -DsupportingFiles \
    -jar swagger-codegen-cli-3.0.59.jar generate \
    -i ../resources/openapi.json \
    --remove-operation-id-prefix true \
    --model-package parameters \
    --api-package endpoints \
    --flatten-inline-schema true \
    -l python \
    -c swagger-codegen-config.json \
    -o ../../MPClient


 


    # -DmodelTests=false \
    # -Dapis \
    # --ignore-file-override=/home/rick/Software/SystemsBiology/MPClient/.swagger-codegen-ignore \
#     --ignore-import-mapping false \
    # --import-mappings config=my.models.myconfig \
    # --type-mappings config=my.models.myconfig \
