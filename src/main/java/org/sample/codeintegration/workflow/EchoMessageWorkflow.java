package org.sample.codeintegration.workflow;

import static com.bytechef.workflow.definition.WorkflowDsl.input;
import static com.bytechef.workflow.definition.WorkflowDsl.output;
import static com.bytechef.workflow.definition.WorkflowDsl.task;
import static com.bytechef.workflow.definition.WorkflowDsl.workflow;

import com.bytechef.workflow.definition.TaskContext;
import com.bytechef.workflow.definition.WorkflowDsl.ModifiableWorkflowDefinition;

public class EchoMessageWorkflow {

    public static final ModifiableWorkflowDefinition WORKFLOW_DEFINITION = workflow("echoMessage")
        .label("Echo Message")
        .description("Returns the message it was given, upper-cased.")
        .inputs(
            input("message")
                .label("Message")
                .type("STRING")
                .required(true))
        .outputs(
            output("result")
                .task("shout"))
        .tasks(
            task("shout")
                .label("Shout")
                .description("Upper-cases the message.")
                .perform(EchoMessageWorkflow::shout));

    private static Object shout(TaskContext context) {
        return String.valueOf(context.input("message"))
            .toUpperCase();
    }
}
