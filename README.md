# MPServer
Server Implementation for the [ModelPolisher 2.1](https://github.com/draeger-lab/ModelPolisher/tree/2.1).

The corresponding Python client library stub can be found [here](https://github.com/draeger-lab/MPClient). Information on how to generate it is [here](#python-client).

See the [Releases Page](https://github.com/draeger-lab/MPServer/releases) for a download of the standalone JAR.
You can run it with 
``` bash
java -jar model-polisher-server-2.1.0-standalone.jar --config-file path/to/server-config.edn
```
The [`server-config.edn`](resources/server-config.edn) determines server behaviour and where to find systems the server depends on.

A [`config.json`](resources/default-request-config.json) can be used to determine default behaviour for all options that the client does not provide values for.

**Note**: 
- Currently, it is intended for this project to run on Java 17. However, this is untested. It is known to run on Java 21.

## Example Usage

``` bash
curl -v -F "parameters=<config.json;type=application/json" \
        -F "model-file=@e_coli_core.xml" \
        localhost:3000/submit/file -o result.json
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

## Development

### Docker

The container `schmirgel/mpserver:development` is configured to build ModelPolisher and MPServer from VCS on startup.

### Python Client

The Python client library can be generated using the [codegen script](development/codegen.sh). This requires to be the swagger-codegen-cli jar to be in the directory. It can be downloaded from Maven Central.

## Swagger UI

While the server is running, you can find the SwaggerUI at [http://localhost:3000/api/docs/index.html](http://localhost:3000/api/docs/index.html).
Note that this is not completely up to date.
