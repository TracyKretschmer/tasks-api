package ca.antunes.tasks.client;


import ca.antunes.tasks.client.verticle.ClientVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class TasksClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(TasksClientApplication.class);

    @Autowired
    ClientVerticle clientVerticle;

    public static void main(String[] args) {
        SpringApplication.run(TasksClientApplication.class, args);
    }

    @PostConstruct
    void deployClient() {

        Vertx vertx = Vertx.vertx();

        DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);

        vertx.deployVerticle(clientVerticle, deploymentOptions, result -> {
            if (result.succeeded()) {
                logger.info("Tests Passed!");
                System.exit(0);
            } else {
                logger.error("Client Error", result.cause());
                System.exit(1);
            }
        });

    }
}
