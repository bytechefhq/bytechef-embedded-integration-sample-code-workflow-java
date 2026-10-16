package org.sample.codeintegration;

import static com.bytechef.embedded.integration.definition.IntegrationDsl.integration;

import com.bytechef.embedded.integration.IntegrationHandler;
import com.bytechef.embedded.integration.definition.IntegrationDefinition;
import com.google.auto.service.AutoService;
import org.sample.codeintegration.workflow.FetchRecentEmailsWorkflow;
import org.sample.codeintegration.workflow.NotifySlackOnNewEmailWorkflow;
import org.sample.codeintegration.workflow.SummarizeNewEmailWorkflow;

@AutoService(IntegrationHandler.class)
public class SampleIntegrationHandler implements IntegrationHandler {

    private static final IntegrationDefinition INTEGRATION_DEFINITION = integration("googleMail", 1)
        .description("A sample code integration built on the Gmail component.")
        .version("0.0.1")
        .workflows(
            SummarizeNewEmailWorkflow.WORKFLOW_DEFINITION, FetchRecentEmailsWorkflow.WORKFLOW_DEFINITION,
            NotifySlackOnNewEmailWorkflow.WORKFLOW_DEFINITION);

    @Override
    public IntegrationDefinition getDefinition() {
        return INTEGRATION_DEFINITION;
    }
}
