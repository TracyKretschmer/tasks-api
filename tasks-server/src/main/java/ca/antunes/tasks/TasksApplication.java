package ca.antunes.tasks;

import ca.antunes.tasks.server.OpenApi3Server;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class TasksApplication {

    @Autowired
    OpenApi3Server openApi3Server;

    public static void main(String[] args) {
        SpringApplication.run(TasksApplication.class, args);
    }

    @PostConstruct
    void deployServer () {

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(openApi3Server);

    }
}
