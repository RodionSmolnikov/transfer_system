package demo;


import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {
    public static void main (String [] args) {
        System.out.println("I'm alive!");
        //TODO: place in properties
        ActionConfiguratior configuratior = new ActionConfiguratior();
        Javalin app = configuratior.setUpAPI(8080);
        app.enableStandardRequestLogging();
        app.start();
    }
}
