package ca.antunes.tasks.server;

import ca.antunes.tasks.handler.TasksHandler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenApi3Server extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(OpenApi3Server.class);

    private final int serverPort;
    private final String mongoHost;
    private final Integer mongoPort;

    public OpenApi3Server(
            @Value("${http.server.port}") int serverPort,
            @Value("${mongo.server.host}")String mongoHost,
            @Value("${mongo.server.port}")int mongoPort) {
        this.serverPort = serverPort;
        this.mongoHost = mongoHost;
        this.mongoPort = mongoPort;
    }

    @Override
    public void start() {


        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("host", mongoHost);
        mongoConfig.put("port", mongoPort);
        MongoClient mongoClient = MongoClient.createShared(vertx, mongoConfig);


        TasksHandler tasksHandler = new TasksHandler(mongoClient);

        OpenAPI3RouterFactory
                .rxCreate(vertx, "api/api.yaml")
                .flatMap(routerFactory -> {
                    RouterFactoryOptions options = new RouterFactoryOptions();

                    options.setOperationModelKey("operationPOJO");
                    routerFactory.setOptions(options);

                    routerFactory.addHandlerByOperationId("getTasks", tasksHandler);
                    routerFactory.addHandlerByOperationId("getTask", tasksHandler);
                    routerFactory.addHandlerByOperationId("createTask", tasksHandler);
                    routerFactory.addHandlerByOperationId("updateTask", tasksHandler);
                    routerFactory.addHandlerByOperationId("deleteTask", tasksHandler);

                    Router router = routerFactory.getRouter();

                    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(serverPort));
                    return server.requestHandler(router).rxListen();


                })
                .subscribe(
                        httpServer -> logger.info("Server up and running listening at {}", serverPort),
                        throwable -> logger.error("Error while starting server", throwable));
    }
}
