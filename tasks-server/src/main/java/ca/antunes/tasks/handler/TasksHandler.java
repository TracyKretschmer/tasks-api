package ca.antunes.tasks.handler;

import io.swagger.v3.oas.models.Operation;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.RoutingContext;

public class TasksHandler extends AbstractHandler {

    private static final String COLLECTION_NAME = "tasks";
    private static final String ID_FIELD_NAME = "taskId";


    public TasksHandler(MongoClient mongoClient) {
        super(mongoClient);
    }

    @Override
    String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    String getIdFieldName() {
        return ID_FIELD_NAME;
    }

    @Override
    public void handle(RoutingContext routingContext) {

        Operation operation = routingContext.get("operationPOJO");

        logger.info("Handling operation {}", operation.getOperationId());

        switch (operation.getOperationId()) {
            case "getTasks":
                handleGetAll(routingContext);
                break;
            case "createTask":
                handleCreate(routingContext);
                break;
            case "getTask":
                handleGetOne(routingContext);
                break;
            case "updateTask":
                handleUpdate(routingContext);
                break;
            case "deleteTask":
                handleDelete(routingContext);
                break;
            default:
                routingContext
                        .response()
                        .setStatusCode(404)
                        .setStatusMessage("NOT FOUND")
                        .end();
                break;
        }
    }
}
