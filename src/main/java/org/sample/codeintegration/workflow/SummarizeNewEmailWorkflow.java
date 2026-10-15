package org.sample.codeintegration.workflow;

import static com.bytechef.workflow.definition.WorkflowDsl.output;
import static com.bytechef.workflow.definition.WorkflowDsl.task;
import static com.bytechef.workflow.definition.WorkflowDsl.trigger;
import static com.bytechef.workflow.definition.WorkflowDsl.workflow;

import com.bytechef.workflow.definition.TaskContext;
import com.bytechef.workflow.definition.WorkflowDsl.ModifiableWorkflowDefinition;
import java.util.Map;

/**
 * A Gmail trigger on a Gmail integration runs on the connection the connected user created when they connected the
 * integration - nothing to declare.
 */
public class SummarizeNewEmailWorkflow {

    public static final ModifiableWorkflowDefinition WORKFLOW_DEFINITION = workflow("summarizeNewEmail")
        .label("Summarize New Email")
        .description("Runs for every new email in the connected user's inbox and logs who sent it.")
        .triggers(
            trigger("newEmail", "googleMail/v1/newEmailPolling")
                .parameters(Map.of("format", "SIMPLE")))
        .outputs(
            output("summary")
                .task("summarize"))
        .tasks(
            task("summarize")
                .label("Summarize")
                .description("Builds a one-line summary of the new email.")
                .perform(SummarizeNewEmailWorkflow::summarize));

    private static Object summarize(TaskContext context) {
        Map<?, ?> email = asMap(context.input("newEmail"));

        String summary = "New email from " + email.get("from") + ": " + email.get("subject");

        context.log(TaskContext.LogLevel.INFO, summary);

        return summary;
    }

    private static Map<?, ?> asMap(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }
}
