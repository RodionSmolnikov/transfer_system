package demo.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import demo.engine.exeption.OperationException;
import demo.engine.model.Account;
import demo.engine.model.Operation;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;
import lombok.extern.slf4j.Slf4j;
import demo.engine.model.Error;

import java.util.List;
import java.util.concurrent.Future;

import static io.javalin.apibuilder.ApiBuilder.*;

@Slf4j
public class RESTActions {

    //default thread count = 5
    private static BuisnessOperationExecutor executor = new BuisnessOperationExecutor(
            System.getenv("THREAD_COUNT") == null ? 5: Integer.parseInt(System.getenv("THREAD_COUNT")));

    private static final String VERSION = "/v1";
    private static final String ROOT_PATH = "/app";
    private static final String ACCOUNT_PATH = ROOT_PATH + VERSION + "/account";
    private static final String TRANSFER_OPERATION_PATH = ROOT_PATH  + VERSION + "/operation";

    public static Javalin setUpAPI() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        JavalinJackson.configure(mapper);

        Javalin restAPI = Javalin.create(config -> {
                config.defaultContentType = "application/json";
                config.requestLogger((ctx, ms) -> {
                    log.info("Executed in {} ms. {} {} -> \n {}", ms, ctx.method(), ctx.url(), ctx.body());
                });
            });

        restAPI.routes(() -> {
            path(ACCOUNT_PATH, () -> {
                put(RESTActions::createAccount);
                path(":id", () -> {
                    get(RESTActions::getAccount);
                    post(RESTActions::updateAccount);
                });
            });
            path(TRANSFER_OPERATION_PATH, () -> {
                post(RESTActions::transfer);
                path(":id", () -> get(RESTActions::getOperation));
                path("account/:id", () -> get(RESTActions::getOperationsForAccount));
            });
        });
        return restAPI;
    }

    public static void createAccount(Context ctx) {
        try {
            Future<Account> execution = executor.createAccount(ctx.bodyAsClass(Account.class));
            ctx.json(execution.get());
            ctx.status(201);
        } catch (OperationException e) {
            ctx.status(401);
            ctx.json(new Error(e));
        } catch (Exception e) {
            if (e.getCause() instanceof OperationException) {
                ctx.status(401);
                ctx.json(new Error((OperationException) e.getCause()));
            } else {
                ctx.status(500);
                ctx.json(new Error(e));
            }
        }
    }

    public static void updateAccount(Context ctx) {
        Account account = ctx.bodyAsClass(Account.class);
        account.setId(ctx.pathParam("id"));
        try {
            Future<Account> execution = executor.updateAccount(account);
            ctx.json(execution.get());
            ctx.status(202);
        } catch (OperationException e) {
            ctx.status(401);
            ctx.json(new Error(e));
        } catch (Exception e) {
            if (e.getCause() instanceof OperationException) {
                ctx.status(401);
                ctx.json(new Error((OperationException) e.getCause()));
            } else {
                ctx.status(500);
                ctx.json(new Error(e));
            }
        }
    }

    public static void getOperation(Context ctx) {
        try {
            ctx.json(executor.getOperation(ctx.pathParam("id")));
            ctx.status(200);
        } catch (OperationException e) {
            ctx.status(404);
            ctx.json(new Error(e));
        } catch (Exception e) {
            if (e.getCause() instanceof OperationException) {
                ctx.status(404);
                ctx.json(new Error((OperationException) e.getCause()));
            } else {
                ctx.status(500);
                ctx.json(new Error(e));
            }
        }
    }

    public static void transfer(Context ctx) {
        try {
            Future<Operation> execution = executor.transferMoney(ctx.bodyAsClass(Operation.class));
            ctx.json(execution.get());
            ctx.status(201);
        } catch (OperationException e) {
            ctx.status(401);
            ctx.json(new Error(e));
        } catch (Exception e) {
            if (e.getCause() instanceof OperationException) {
                ctx.status(401);
                ctx.json(new Error((OperationException) e.getCause()));
            } else {
                ctx.status(500);
                ctx.json(new Error(e));
            }
        }
    }

    public static void getOperationsForAccount(Context ctx) {
        try {
            List<Operation> operations = executor.getOperationsForAccount(ctx.pathParam(":id"));
            ctx.json(operations);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new Error(e));
        }
    }

    public static void getAccount(Context ctx) {
        try {
            Account account = executor.getAccount(ctx.pathParam(":id"));
            ctx.json(account);
            ctx.status(200);
        } catch (OperationException e) {
            ctx.status(404);
            ctx.json(new Error(e));
        } catch (Exception e) {
            if (e.getCause() instanceof OperationException) {
                ctx.status(404);
                ctx.json(new Error((OperationException) e.getCause()));
            } else {
                ctx.status(500);
                ctx.json(new Error(e));
            }
        }
    }
}
