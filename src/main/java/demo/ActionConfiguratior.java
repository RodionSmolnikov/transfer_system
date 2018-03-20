package demo;

import demo.engine.RESTActions;
import io.javalin.Javalin;

import static io.javalin.ApiBuilder.*;

public class ActionConfiguratior {

    private static final String ROOT_PATH = "/app";
    private static final String ACCOUNT_PATH = ROOT_PATH + "/account";
    private static final String TRANSFER_OPERATION_PATH = ROOT_PATH + "/operation";


    public Javalin setUpAPI(int port) {
        Javalin restAPI = Javalin.create();
        restAPI.port(port);

        restAPI.routes(() -> {
            path(ACCOUNT_PATH, () -> {
                put(RESTActions::createAccount);
                path(":id", () -> {
                    get(RESTActions::getAccount);
                    delete(RESTActions::deleteAccount);
                });
            });
            path(TRANSFER_OPERATION_PATH, () -> {
                path(":id", () -> {
                    get(RESTActions::getOperation);
                });
                path("account/:id", () -> {
                    put(RESTActions::topUpBalance);
                    delete(RESTActions::withdraw);
                    post(RESTActions::transfer);
                    get(RESTActions::getOperationsForAccount);
                });
            });
        });
        return restAPI;
    }

}
