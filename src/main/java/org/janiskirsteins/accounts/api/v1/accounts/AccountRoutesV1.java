package org.janiskirsteins.accounts.api.v1.accounts;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.v1.ApiResponse;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestRoutesV1;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRoutesV1;

import spark.Request;
import spark.Response;

/**
 * Hello world!
 */
public class AccountRoutesV1
{
    public final String ROOT = "/account";

    private AccountDAO accountDao = null;
    private TransferRequestRoutesV1 transferRequestRoutesV1 = null;
    private Gson gson = new Gson();
	private DataStoreConcurrencyScheduler concurrencyScheduler;

	private TransferRoutesV1 transferRoutesV1;

    @Inject
    public AccountRoutesV1(
        AccountDAO accountDao,
        DataStoreConcurrencyScheduler concurrencyScheduler,
        TransferRequestRoutesV1 transferRequestRoutesV1,
        TransferRoutesV1 transferRoutesV1)
    {
        this.accountDao = accountDao;
        this.transferRequestRoutesV1 = transferRequestRoutesV1;
        this.concurrencyScheduler = concurrencyScheduler;
        this.transferRoutesV1 = transferRoutesV1;
    }

    public void populatePath() {
        post("/", this::createAccount, gson::toJson);

        path("/:visual_id", () -> {
            get("", this::showAccount, gson::toJson);

            path(transferRequestRoutesV1.ROOT, transferRequestRoutesV1::populatePath);
            path(transferRoutesV1.ROOT, transferRoutesV1::populatePath);
        });
    }

    public Object showAccount(Request request, Response response)
    {
        Account result = accountDao.findByVisualIdOrNull(request.params(":visual_id"));
        return ApiResponse.respondWithResourceOrNull(response, result);
    }

    public Object createAccount(Request request, Response response)
    {
        CreateAccountPOSTRequest createRequest = gson.fromJson(request.body(), CreateAccountPOSTRequest.class);
        createRequest.prepareForValidation(accountDao);
        return ApiResponse.responseFromCreateRequestInTransaction(concurrencyScheduler, response, createRequest);
    }
}
