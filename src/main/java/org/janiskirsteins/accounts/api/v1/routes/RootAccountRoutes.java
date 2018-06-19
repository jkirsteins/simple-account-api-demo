package org.janiskirsteins.accounts.api.v1.routes;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.accounts.Account;
import org.janiskirsteins.accounts.api.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.ApiResponse;

import spark.Request;
import spark.Response;

/**
 * Hello world!
 */
public class RootAccountRoutes
{
    public final String ROOT = "/account";

    private AccountDAO accountDao = null;
    private Gson gson = new Gson();

    @Inject
    public RootAccountRoutes(AccountDAO accountDao)
    {
        this.accountDao = accountDao;
    }

    public void populatePath() {
        get("/", this::listAccounts, gson::toJson);
        get("/:visual_id", this::showAccount, gson::toJson);
    }

    public Object listAccounts(Request request, Response response)
    {
        return ApiResponse.fromResult(accountDao.all());
    }

    public Object showAccount(Request request, Response response)
    {
        Account result = accountDao.findByVisualIdOrNull(request.params(":visual_id"));
        return ApiResponse.fromResult(result);
    }
}
