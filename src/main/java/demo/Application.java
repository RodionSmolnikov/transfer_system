package demo;

import demo.engine.RESTActions;
import io.javalin.Javalin;


public class Application {
    public static void main (String [] args) {
        //simple slf4j little hack
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");

        String port = System.getProperty("port");
        Javalin app = RESTActions.setUpAPI();
        app.start(port == null ? 8080 : Integer.valueOf(port));
    }
}
