package demo;


import demo.engine.RESTActions;
import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {
    public static void main (String [] args) {
        String port = System.getProperty("port");
        Javalin app = RESTActions.setUpAPI(port == null ? 8080 : Integer.valueOf(port));
        app.enableStandardRequestLogging();
        app.start();
    }
}
