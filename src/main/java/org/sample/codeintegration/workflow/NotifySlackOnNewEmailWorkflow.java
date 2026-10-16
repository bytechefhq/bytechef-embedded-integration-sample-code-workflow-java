package org.sample.codeintegration.workflow;

import static com.bytechef.workflow.definition.WorkflowDsl.connection;
import static com.bytechef.workflow.definition.WorkflowDsl.input;
import static com.bytechef.workflow.definition.WorkflowDsl.output;
import static com.bytechef.workflow.definition.WorkflowDsl.task;
import static com.bytechef.workflow.definition.WorkflowDsl.trigger;
import static com.bytechef.workflow.definition.WorkflowDsl.workflow;

import com.bytechef.workflow.definition.TaskContext;
import com.bytechef.workflow.definition.WorkflowDsl.ModifiableWorkflowDefinition;
import java.util.Map;

/**
 * The Gmail trigger runs on the connected user's integration connection. Slack is not the integration's component, so
 * the task declares its connection and passes the name; an administrator picks the Slack connection when enabling the
 * workflow in the integration's instance configuration.
 */
public class NotifySlackOnNewEmailWorkflow {

    public static final ModifiableWorkflowDefinition WORKFLOW_DEFINITION = workflow("notifySlackOnNewEmail")
        .label("Notify Slack on New Email")
        .description("Posts every new email in the connected user's inbox to a Slack channel.")
        .inputs(
            input("channel")
                .label("Slack Channel ID")
                .type("STRING")
                .required(true))
        .triggers(
            trigger("newEmail", "googleMail/v1/newEmailPolling")
                .parameters(Map.of("format", "SIMPLE")))
        .outputs(
            output("message")
                .task("postToSlack"))
        .tasks(
            task("postToSlack")
                .label("Post to Slack")
                .description("Posts the sender and subject with the Slack Send Channel Message action.")
                .connections(connection("slack", "slack"))
                .perform(NotifySlackOnNewEmailWorkflow::postToSlack));

    private static Object postToSlack(TaskContext context) throws Exception {
        Map<?, ?> email = asMap(context.input("newEmail"));

        String text = "New email from " + email.get("from") + ": " + email.get("subject");

        Object message = context.component(
            "slack", "sendChannelMessage", Map.of("channel", context.input("channel"), "text", text), "slack");

        context.log(TaskContext.LogLevel.INFO, "Posted to Slack: " + text);

        return message;
    }

    private static Map<?, ?> asMap(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }
}
