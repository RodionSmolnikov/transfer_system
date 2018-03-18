package demo.engine;

import io.javalin.Context;

public class RESTActions {

    public static void createAccount(Context var1) {
        var1.status(200);
        var1.result("user_stub_created");
    }

    public static void deleteAccount(Context var1) {
        var1.status(200);
        var1.result("user_stub_deleted");
    }

    public static void getAccount(Context var1) {
        var1.status(200);
        var1.result(var1.param(":id"));
    }

}
