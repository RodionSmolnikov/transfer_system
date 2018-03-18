package demo;

import demo.engine.RESTActions;
import io.javalin.Javalin;

import static io.javalin.ApiBuilder.*;

public class ActionConfiguratior {

    private static final String ROOT_PATH = "/app";
    private static final String USER_OPERATION_PATH = ROOT_PATH + "/user";

    public Javalin setUpAPI(int port) {
        Javalin restAPI = Javalin.create();
        restAPI.port(port);

        restAPI.routes(() -> {
            path(USER_OPERATION_PATH, () -> {
                post(RESTActions::createAccount);

                path(":id", () -> {
                    get(RESTActions::getAccount);
                    delete(RESTActions::deleteAccount);
                });
            });
        });
        return restAPI;
    }

}
