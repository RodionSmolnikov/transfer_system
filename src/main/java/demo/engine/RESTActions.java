package demo.engine;

import demo.datasource.Constants;
import io.javalin.Context;
import io.javalin.Javalin;
import org.eclipse.jetty.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static io.javalin.ApiBuilder.*;
import static io.javalin.ApiBuilder.get;
import static io.javalin.ApiBuilder.post;

public class RESTActions {

    private static BuisnessOperationExecutor executor = BuisnessOperationExecutor.getInstance();
    private static final String ROOT_PATH = "/app";
    private static final String ACCOUNT_PATH = ROOT_PATH + "/account";
    private static final String TRANSFER_OPERATION_PATH = ROOT_PATH + "/operation";

    public static Javalin setUpAPI(int port) {
        Javalin restAPI = Javalin.create();
        restAPI.port(port);

        restAPI.routes(() -> {
            path(ACCOUNT_PATH, () -> {
                put(RESTActions::createAccount);
                path(":id", () -> {
                    get(RESTActions::getAccount);
                    post(RESTActions::updateAccount);
                    delete(RESTActions::deleteAccount);
                });
            });
            path(TRANSFER_OPERATION_PATH, () -> {
                path(":id", () -> {
                    get(RESTActions::getOperation);
                });
                path("account/:id", () -> {
                    path("search", () -> {
                        post(RESTActions::getOperationsForAccount);
                    });
                    put(RESTActions::topUpBalance);
                    delete(RESTActions::withdraw);
                    post(RESTActions::transfer);
                });
            });
        });
        return restAPI;
    }

    public static void createAccount(Context var1) {
        setResult(var1, executor.createAccount(formatParameters(var1.formParamMap())));
    }

    public static void updateAccount(Context var1) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.Account.ID_FIELD ,var1.param(":id"));
        params.putAll(formatParameters(var1.formParamMap()));
        setResult(var1, executor.updateAccount(params));
    }


    public static void getOperation(Context var1) {
        setResult(var1, executor.getOperation(var1.param(":id")));
    }

    public static void topUpBalance(Context var1) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD ,var1.param(":id"));
        params.putAll(formatParameters(var1.formParamMap()));
        setResult(var1, executor.topUpBalance(params));
    }

    public static void withdraw(Context var1) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD ,var1.param(":id"));
        params.putAll(formatParameters(var1.formParamMap()));
        setResult(var1, executor.withdraw(params));
    }

    public static void transfer(Context var1) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD ,var1.param(":id"));
        params.putAll(formatParameters(var1.formParamMap()));
        setResult(var1, executor.transfer(params));
    }

    public static void getOperationsForAccount(Context var1) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.TransferOperation.ACCOUNT_ID_FIELD ,var1.param(":id"));
        params.putAll(formatParameters(var1.formParamMap()));
        setResult(var1, executor.getOperations(params));
    }



    public static void deleteAccount(Context var1) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.Account.ID_FIELD ,var1.param(":id"));
        setResult(var1, executor.deleteAccount(params));
    }

    public static void getAccount(Context var1) {
        setResult(var1, executor.getAccount(var1.param(":id")));
    }

    private boolean checkSummFormat() {
        return true;
    }

    private static void setResult(Context context, Map<String, Object> params) {
        if (params.containsKey(Constants.Request.RESULT))
            context.result(params.get(Constants.Request.RESULT).toString());
        else
            context.result("Unexpected error");

        if (params.containsKey(Constants.Request.CODE))
            context.status(((HttpStatus.Code)params.get(Constants.Request.CODE)).getCode());
        else
            context.status(500);

    }

    //format params for applications/x-www-form-urlencoded just for simplify
    private static Map<String, Object> formatParameters(Map<String, String[]> formParam) {
        Map<String, Object> params = new HashMap<>();
        formParam.keySet().stream().forEach(key -> {
                    switch (key) {
                        case Constants.Account.BALANCE_FIELD:
                        case Constants.TransferOperation.SUM_FIELD:
                            Double money = Double.valueOf(formParam.get(key)[0]);
                            money =(double) Math.round(money*100)/100;
                            params.put(key, money);
                            break;
                        case Constants.TransferOperation.LAST_OPERATION_NUMBER:
                            params.put(key, Integer.valueOf((formParam.get(key))[0]));
                            break;
                        default:
                            params.put(key, (formParam.get(key))[0]);
                    }
                });
        return params;
    }

}
