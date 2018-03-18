package demo;


import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    public static void main (String [] args) {
        System.out.println("I'm alive!");
        //TODO: place in properties
        ActionConfiguratior configuratior = new ActionConfiguratior();
        Javalin app = configuratior.setUpAPI(8080);
        app.enableStandardRequestLogging();
        app.start();

        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }
}
