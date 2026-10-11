# bytechef-embedded-integration-sample-code-workflow-java

A sample ByteChef **embedded code integration** written in Java. It is a plain jar: a class implementing
`com.bytechef.embedded.integration.IntegrationHandler`, registered through `META-INF/services` by `@AutoService`,
that describes the integration and its workflows with `IntegrationDsl` and `WorkflowDsl`. The server finds the
handler with `ServiceLoader` and runs each task's `perform` itself.

A code integration is keyed by the component it integrates, `integration(componentName, componentVersion)`, and
that component must exist on the server. This sample uses the built-in HTTP Client, `httpClient` version 1. It has
one workflow, `echoMessage`: it takes a `message` input, runs one task, `shout`, that upper-cases it, and exposes
that as the `result` output.

## Build

Requires JDK 25 (Gradle's toolchain selects it).

```bash
./gradlew build
```

The jar to deploy is `build/libs/sample-code-integration-java-1.0.0.jar`.

`libs/` holds the ByteChef SDK jars the code compiles against: `integration-api-1.0.jar` and `workflow-api-1.0.jar`.
They are `compileOnly` - the server provides them at runtime, so they must not be packed into your jar. To refresh
them, run `./gradlew :sdks:backend:java:workflow-api:jar :sdks:backend:embedded:integration-api:jar` in the ByteChef
repository and copy the jars from each module's `build/libs/`.

## Deploy

Uploading a jar requires `bytechef.workflow.code-workflow.java-enabled: true` on the server (the default). When it is
off, the import dialog only offers `.js` and `.py` files and the API rejects jars.

Deploying again for the same component deploys a new version of the existing code integration instead of creating a
second one.

### From the UI

Embedded → **Integrations** → the arrow next to **New Integration** → **Import Code Integration**, then pick the jar.

### Through the public API

`POST /api/embedded/v1/integration-code-workflows/deploy` with a `multipart/form-data` body holding one file part,
`integrationFile`: the jar. The file name must end in `.jar`; the server picks the language from the extension.
A successful deploy returns `204 No Content`.

Authenticate with `Authorization: Bearer <API key>`, using an **Embedded API key** (Embedded → Settings →
**API Keys**) that belongs to an administrator - deploying requires the admin role. This endpoint takes only a typed
API key: an organization admin key (Settings → Organization → **Admin API Keys**) is rejected with `401`. The key is
looked up in the environment named by the `X-ENVIRONMENT` header (`DEVELOPMENT`, `STAGING` or `PRODUCTION`;
`PRODUCTION` when the header is absent), so send the environment the key was created in. The request runs as the
key's own ByteChef user, not as a connected user.

```bash
curl -X POST "$BYTECHEF_URL/api/embedded/v1/integration-code-workflows/deploy" \
  -H "Authorization: Bearer $BYTECHEF_EMBEDDED_API_KEY" \
  -H "X-ENVIRONMENT: DEVELOPMENT" \
  -F "integrationFile=@build/libs/sample-code-integration-java-1.0.0.jar"
```

`BYTECHEF_URL` is the server's base URL, for example `http://localhost:9555` for a local development server.
