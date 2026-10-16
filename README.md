# bytechef-embedded-integration-sample-code-workflow-java

A sample ByteChef **embedded code integration** written in Java. It is a plain jar: a class implementing
`com.bytechef.embedded.integration.IntegrationHandler`, registered through `META-INF/services` by `@AutoService`,
that describes the integration and its workflows with `IntegrationDsl` and `WorkflowDsl`. The server finds the
handler with `ServiceLoader` and runs each task's `perform` itself.

A code integration is keyed by the component it integrates, `integration(componentName, componentVersion)`, and
that component must exist on the server. This sample uses the built-in Gmail component, `googleMail` version 1, so
connecting the integration means connecting a Gmail account. It has three workflows, one class each under
`org.sample.codeintegration.workflow`:

- `summarizeNewEmail` (`SummarizeNewEmailWorkflow`) starts with the Gmail **New Email** polling trigger
  (`googleMail/v1/newEmailPolling`). It runs once for every new email and its `summarize` task returns a one-line
  summary as the `summary` output. A Gmail trigger on a Gmail integration runs on the connection the connected user
  created when they connected the integration - there is nothing to declare.
- `fetchRecentEmails` (`FetchRecentEmailsWorkflow`) starts with a request trigger, so you call it over HTTP. Its
  `fetchEmails` task calls the Gmail **Search Email** action and returns the matching messages as the `emails` output.
- `notifySlackOnNewEmail` (`NotifySlackOnNewEmailWorkflow`) starts with the same Gmail trigger and takes a required
  `channel` input; its options come from Slack's own `channel` input (`componentName`, `componentVersion` and
  `groupName` on the input), so it is picked from the Slack workspace's channels. Its `postToSlack` task calls the Slack
  **Send Channel Message** action, and Slack is not the integration's component, so the task declares a `slack`
  connection and passes its name. An administrator picks that connection when enabling the workflow in the integration's
  instance configuration; until one is picked the workflow cannot be enabled, and the channel list stays empty.

Inside `perform`, `context.input()` returns the workflow inputs, the trigger output (under the trigger's name) and the
output of every task that already ran (under the task's name); `context.input("newEmail")` returns one of them, and
`context.log(level, message)` writes to the execution log.

### Connections

A task calls an action of the integration's own component - `googleMail` here - without declaring a connection. It
runs on the connected user's integration connection, which ByteChef binds when the workflow runs:

```java
task("fetchEmails")
    .perform(context -> context.component("googleMail", "searchEmail", Map.of("maxResults", 10)));
```

A task that calls any other component (say `slack`) declares that connection and passes its name. An administrator
chooses it in the integration's instance configuration, and it is the same for every connected user:

```java
task("notify")
    .connections(connection("slack", "slack"))
    .perform(context -> context.component(
        "slack", "sendChannelMessage", Map.of("channel", "C0123456", "text", "New email"), "slack"));
```

## Build

Requires JDK 25 (Gradle's toolchain selects it).

```bash
./gradlew build
```

The jar to deploy is `build/libs/sample-code-integration-java-1.0.0.jar`.

`libs/` holds the ByteChef SDK jars the code compiles against: `integration-api-1.0.jar` and `workflow-api-1.0.jar`.
They are `compileOnly` - the server provides them at runtime, so they must not be packed into your jar. To refresh
them, run `./gradlew :sdks:backend:java:workflow-api:jar :sdks:backend:java:embedded:integration-api:jar` in the
ByteChef repository and copy the jars from each module's `build/libs/`.

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

## Enable and run

Deploying with `?enable=true` also enables the integration in that environment. A Gmail integration needs Gmail OAuth
app credentials first: an administrator enters them in the integration's instance configuration, and until then the
enable is refused.

Once a connected user has connected the integration and enabled `fetchRecentEmails` on their integration instance
(`POST /api/embedded/v1/integration-instances/{id}/workflows/{workflowUuid}/enable`), call it with their JWT:

```bash
curl -X POST "$BYTECHEF_URL/api/embedded/v1/workflows/$WORKFLOW_UUID" \
  -H "Authorization: Bearer $CONNECTED_USER_JWT" \
  -H "X-ENVIRONMENT: DEVELOPMENT" \
  -H "Content-Type: application/json" \
  -d '{"from": "ada@example.com", "maxResults": 5}'
```

The body is optional. `summarizeNewEmail` needs no call: once enabled, it runs whenever a new email arrives.
