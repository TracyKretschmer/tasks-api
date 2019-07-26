package ca.antunes.tasks.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractHandler implements Handler<RoutingContext> {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String PARSED_PARAMETERS = "parsedParameters";

    private MongoClient mongoClient;

    AbstractHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }



    void handleDelete(RoutingContext routingContext) {

        RequestParameters params = routingContext.get(PARSED_PARAMETERS);

        String idField = params.pathParameter(getIdFieldName()).getString();

        logger.info("Deleting {} id: {}", getCollectionName(), idField);

        JsonObject query = new JsonObject();
        query.put("_id", idField);

        mongoClient
                .rxFindOneAndDelete(getCollectionName(), query)
                .subscribe(result -> routingContext
                        .response()
                        .setStatusCode(204)
                        .setStatusMessage("NO CONTENT")
                        .end());

    }

    void handleUpdate(RoutingContext routingContext) {

        RequestParameters params = routingContext.get(PARSED_PARAMETERS);

        String idField = params.pathParameter(getIdFieldName()).getString();

        logger.info("Updating {} id: {}", getCollectionName(), idField);

        JsonObject query = new JsonObject();
        query.put("_id", idField);

        final JsonObject updatedEntity = params.body().getJsonObject();

        mongoClient
                .rxFindOneAndReplace(getCollectionName(), query, updatedEntity)
                .subscribe(result -> {
                    updatedEntity.put("id", idField);
                    routingContext
                            .response()
                            .setStatusCode(200)
                            .setStatusMessage("OK")
                            .end(updatedEntity.encode());
                });


    }

    void handleGetOne(RoutingContext routingContext) {

        RequestParameters params = routingContext.get(PARSED_PARAMETERS);

        String idField = params.pathParameter(getIdFieldName()).getString();

        logger.info("Retrieving {} id: {}", getCollectionName(), idField);

        JsonObject query = new JsonObject();
        query.put("_id", idField);

        mongoClient.rxFindOne(getCollectionName(), query, null)
                .subscribe(result -> {
                            result.put("id", idField);
                            result.remove("_id");
                            routingContext
                                    .response()
                                    .setStatusCode(200)
                                    .setStatusMessage("OK")
                                    .end(result.encode());
                        },
                        error -> logger.error("Error Updating Entity", error));

    }

    void handleCreate(RoutingContext routingContext) {

        RequestParameters params = routingContext.get(PARSED_PARAMETERS);

        final JsonObject entity = params.body().getJsonObject();

        if (logger.isInfoEnabled()) {
            logger.info("{} being saved: {}", getCollectionName(), entity.encode());
        }
        mongoClient
                .rxSave(getCollectionName(), entity)
                .subscribe(id -> {
                    logger.info("Result {}", id);
                    entity.remove("_id");
                    entity.put("id", id);
                    routingContext
                            .response()
                            .setStatusCode(201)
                            .setStatusMessage("SAVED")
                            .end(entity.encode());

                });

    }

    void handleGetAll(RoutingContext routingContext) {
        JsonObject query = new JsonObject();

        mongoClient.rxFind(getCollectionName(), query)
                .subscribe(result -> {
                    logger.info("Result {}", result);
                    result.parallelStream().forEach(dbObj -> {
                        String id = dbObj.getString("_id");
                        dbObj.remove("_id");
                        dbObj.put("id", id);
                    });
                    JsonArray jsonArray = new JsonArray(result);
                    routingContext
                            .response()
                            .setStatusCode(200)
                            .setStatusMessage("OK")
                            .end(jsonArray.encode());
                });

    }

    abstract String getCollectionName();
    abstract String getIdFieldName();
}
