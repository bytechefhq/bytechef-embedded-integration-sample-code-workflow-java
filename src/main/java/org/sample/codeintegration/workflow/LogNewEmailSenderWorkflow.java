package org.sample.codeintegration.workflow;

import static com.bytechef.workflow.definition.WorkflowDsl.componentTask;
import static com.bytechef.workflow.definition.WorkflowDsl.task;
import static com.bytechef.workflow.definition.WorkflowDsl.trigger;
import static com.bytechef.workflow.definition.WorkflowDsl.workflow;

import com.bytechef.workflow.definition.TaskContext;
import com.bytechef.workflow.definition.WorkflowDsl.ModifiableWorkflowDefinition;
import java.util.Map;

public class LogNewEmailSenderWorkflow {

    public static final ModifiableWorkflowDefinition WORKFLOW_DEFINITION = workflow("logNewEmailSender")
        .label("Log New Email Sender")
        .description("Logs the sender of every new email in the connected user's inbox with the Logger component.")
        .triggers(
            trigger("newEmail", "googleMail/v1/newEmailPolling")
                .parameters(Map.of("format", "SIMPLE")))
        .tasks(
            task("describe")
                .label("Describe")
                .perform(LogNewEmailSenderWorkflow::describe),
            componentTask("logSender", "logger/v1/info")
                .label("Log Sender")
                .description("Writes the sender and subject to the execution log with the Logger Info action.")
                .parameters(Map.of("text", "New email from ${describe.sender}: ${describe.subject}")));

    private static Object describe(Map<String, ?> input, TaskContext context) {
        Map<?, ?> email = asMap(input.get("newEmail"));

        context.log(TaskContext.LogLevel.INFO, "Describing email " + email.get("id"));

        return Map.of("sender", String.valueOf(email.get("from")), "subject", String.valueOf(email.get("subject")));
    }

    private static Map<?, ?> asMap(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }
}
