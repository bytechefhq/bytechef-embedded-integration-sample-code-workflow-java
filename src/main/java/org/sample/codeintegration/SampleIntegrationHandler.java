package org.sample.codeintegration;

import static com.bytechef.embedded.integration.definition.IntegrationDsl.integration;

import com.bytechef.embedded.integration.IntegrationHandler;
import com.bytechef.embedded.integration.definition.IntegrationDefinition;
import com.google.auto.service.AutoService;
import org.sample.codeintegration.workflow.EchoMessageWorkflow;

/**
 * A code integration is keyed by the component it integrates. The component must exist on the server - here the
 * built-in HTTP Client, version 1.
 */
@AutoService(IntegrationHandler.class)
public class SampleIntegrationHandler implements IntegrationHandler {

    private static final IntegrationDefinition INTEGRATION_DEFINITION = integration("httpClient", 1)
        .description("A sample code integration built on the HTTP Client component.")
        .version("0.0.1")
        .workflows(EchoMessageWorkflow.WORKFLOW_DEFINITION);

    @Override
    public IntegrationDefinition getDefinition() {
        return INTEGRATION_DEFINITION;
    }
}
