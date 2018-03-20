package demo.engine;

import demo.datasource.Constants;
import io.javalin.Context;
import org.eclipse.jetty.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class RESTActions {

    private static BuisnessOperationExecutor executor = BuisnessOperationExecutor.getInstance();

    public static void createAccount(Context var1) {
        setResult(var1, executor.createAccount(formatParameters(var1.formParamMap())));
    }

//    public static void updateAccount(Context var1) {
//        setResult(var1, executor.update(formatParameters(var1.formParamMap())));
//    }


    public static void getOperation(Context var1) {
        setResult(var1, executor.createAccount(formatParameters(var1.formParamMap())));
    }

    public static void topUpBalance(Context var1) {

    }

    public static void withdraw(Context var1) {

    }

    public static void transfer(Context var1) {

    }

    public static void getOperationsForAccount(Context var1) {

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
                        default:
                            params.put(key, (formParam.get(key))[0]);
                    }
                });
        return params;
    }

}
