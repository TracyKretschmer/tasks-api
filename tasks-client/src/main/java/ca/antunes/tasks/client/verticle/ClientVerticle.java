package ca.antunes.tasks.client.verticle;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ClientVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ClientVerticle.class);

    private String apiHostPath;
    private Integer apiHostPort;


    public ClientVerticle(
            @Value("${api.hostPath}") String apiHostPath,
            @Value("${api.hostPort}") Integer apiHostPort) {
        this.apiHostPath = apiHostPath;
        this.apiHostPort = apiHostPort;
    }

    @Override
    public void start() throws Exception {
        WebClientOptions options = new WebClientOptions();
        options.setKeepAlive(false);
        WebClient client = WebClient.create(vertx, options);

        JsonObject task = new JsonObject();
        task.put("title", "Create Container");
        task.put("description", "Create a docker container");
        task.put("startDate", 152755200000L);
        task.put("endDate", 152755200000L);
        task.put("priority", 2);
        task.put("category", "docker");
        task.put("status", "scheduled");

        logger.info("Test: it should be able to insert a new task.");
        Assert.isTrue(insertTask(client, task) == 201, "Insert Task Failed");
        logger.info("Test Passed!");
        logger.info("Test: it should be able to retrieve the inserted task.");
        Assert.isTrue(retrieveTask(client, task) == 200, "Retrieve Task Failed");
        logger.info("Test Passed!");
        logger.info("Test: it should find the inserted task in the task list.");
        Assert.isTrue(retrieveTasks(client, task) == 1, "Retrieve Tasks Failed");
        logger.info("Test Passed!");
        logger.info("Test: it should be able to delete the inserted task.");
        Assert.isTrue(deleteTask(client, task) == 204, "Delete task failed");
        logger.info("Test Passed!");
        logger.info("Test: it should not find the deleted task in the task list.");
        Assert.isTrue(retrieveTasks(client, task) == 0, "Retrieve Tasks Failed");
        logger.info("Test Passed!");


    }

    private int deleteTask(WebClient client, JsonObject task) {

        PublishSubject<Integer> resultSubject = PublishSubject.create();

        Single<Integer> observer = resultSubject.firstOrError();

        String uri = String.format("/tasks/%s", task.getString("id"));

        observer.doOnSubscribe(event -> {
            client
                    .delete(apiHostPort, apiHostPath, uri)
                    .send(ar -> {
                        if (ar.succeeded()) {
                            logger.debug("Successful Deletion {}", ar.result().statusCode());
                            resultSubject.onNext(ar.result().statusCode());
                            resultSubject.onComplete();
                        } else {
                            logger.error("Error Deleting Task", ar.cause());
                            resultSubject.onNext(-1);
                            resultSubject.onComplete();
                        }
                    });
        }).subscribeOn(Schedulers.io()).subscribe();

        return observer.blockingGet();
    }

    private int retrieveTasks(WebClient client, final JsonObject task) {

        PublishSubject<Integer> resultSubject = PublishSubject.create();

        Single<Integer> observer = resultSubject.firstOrError();

        observer.doOnSubscribe(event -> {
            client
                    .get(apiHostPort, apiHostPath, "/tasks")
                    .send(ar -> {
                        if (ar.succeeded()) {
                            JsonArray tasks = ar.result().bodyAsJsonArray();
                            boolean found = false;
                            for (int i = 0; i < tasks.getList().size(); i++) {
                                JsonObject retrievedTask = tasks.getJsonObject(i);
                                if (retrievedTask.getString("id").equals(task.getString("id"))) {
                                    logger.debug("Task Found");
                                    resultSubject.onNext(1);
                                    resultSubject.onComplete();
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                logger.debug("Task not Found");
                                resultSubject.onNext(0);
                                resultSubject.onComplete();
                            }
                        } else {
                            logger.error("Error Retrieving Task", ar.cause());
                            resultSubject.onNext(-1);
                            resultSubject.onComplete();
                        }
                    });
        }).subscribeOn(Schedulers.io()).subscribe();

        return observer.blockingGet();
    }

    private int retrieveTask(WebClient client, JsonObject task) {

        PublishSubject<Integer> resultSubject = PublishSubject.create();

        Single<Integer> observer = resultSubject.firstOrError();

        String uri = String.format("/tasks/%s", task.getString("id"));

        observer.doOnSubscribe(event -> {
            client
                    .get(apiHostPort, apiHostPath, uri)
                    .send(ar -> {
                        if (ar.succeeded()) {
                            logger.debug("Successful Retrieval {}", ar.result().statusCode());
                            resultSubject.onNext(ar.result().statusCode());
                            resultSubject.onComplete();
                        } else {
                            logger.error("Error Retrieving Task", ar.cause());
                            resultSubject.onNext(-1);
                            resultSubject.onComplete();
                        }
                    });
        }).subscribeOn(Schedulers.io()).subscribe();

        return observer.blockingGet();
    }

    private int insertTask(WebClient client, final JsonObject task) {

        PublishSubject<Integer> resultSubject = PublishSubject.create();

        Single<Integer> observer = resultSubject.firstOrError();

        observer.doOnSubscribe(event -> {
            client
                    .post(apiHostPort, apiHostPath, "/tasks")
                    .sendJsonObject(task, ar -> {
                        if (ar.succeeded()) {
                            logger.debug("Successful insert {}", ar.result().statusCode());
                            task.put("id", ar.result().bodyAsJsonObject().getValue("id"));
                            resultSubject.onNext(ar.result().statusCode());
                            resultSubject.onComplete();
                        } else {
                            logger.error("Error Inserting Task", ar.cause());
                            resultSubject.onNext(-1);
                            resultSubject.onComplete();
                        }
                    });
        }).subscribeOn(Schedulers.io()).subscribe();

        return observer.blockingGet();
    }
}
