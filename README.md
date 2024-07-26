# MPServer
Server Implementation for the [ModelPolisher 2.1](https://github.com/draeger-lab/ModelPolisher/tree/2.1).

The corresponding (auto-generated) Python client library stub can be found [here](https://github.com/draeger-lab/MPClient).

See the [Releases Page](https://github.com/draeger-lab/MPServer/releases/tag/pre-release) for a download of the standalone JAR.
You can run it with 
``` bash
java -jar model-polisher-server-1.0.0-SNAPSHOT-standalone.jar
```

[`config.json`](examples/config.json) can be used for testing specific flags.

**Note**: 
- Using BiGGDB will not work without a running BiGGDB instance. Right now the the DB-Config is hardcoded to use hostname `bigg`, which likely means that *for now you should not bother trying to use this*
- I run Java 21 locally; the project is likely compiled against it and you won't be able to run it with an older version right now

## Example Usage

``` bash
curl -v -F "parameters=<config.json;type=application/json" \
        -F "model-file=@e_coli_core.xml" \
        localhost:3000/api/submit/file -o result.json
```

**Note**: This assumes that in your current directory, you have [`config.json`](examples/config.json) and [`e_coli_core.xml`](http://bigg.ucsd.edu/models/e_coli_core). 
Further note the difference between `<` and `@` in `curl`:
- `<` inserts the *content* of a file
- `@` includes the actual *file*

This is intended. The python wrapper will of course cover this up.

The `result.json` (or whatever you name it) should contain 
- a `runId` UUID which should be reflected in the server logs
- a `diff` which reflects the changes to the model
- `modelFile` which is the base64 encoded output file.

## Swagger UI

While the server is running, you can find the SwaggerUI at [http://localhost:3000/api/docs/index.html](http://localhost:3000/api/docs/index.html).
Note that this is not completely up to date.
