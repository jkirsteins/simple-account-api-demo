// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.accounts.routes.v1;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.model_base.GenericCreateRequest;
import org.janiskirsteins.accounts.api.v1.ApiResponse;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.accounts.CreateAccountPOSTRequest;
import org.janiskirsteins.accounts.api.v1.transfers.CreateTransferPOSTRequest;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestRoutesV1;
import org.janiskirsteins.accounts.api.v1.transfers.routes.v1.TransferRoutesV1;

import spark.Request;
import spark.Response;

/**
 * Class responsible for initializing the "/account" API routes.
 */
public class AccountRoutesV1
{
    /**
     * Root path part, under which this class will
     * register routes.
     *
     * Pass it as the 1st parameter to Spark.path().
     *
     * @see this::populatePath
     */
    public final String ROOT = "/account";

    private AccountDAO accountDao = null;
    private TransferRequestRoutesV1 transferRequestRoutesV1 = null;
    private Gson gson = new Gson();
	private DataStoreConcurrencyScheduler concurrencyScheduler;

	private TransferRoutesV1 transferRoutesV1;

    /**
     * Constructor for dependency injection.
     *
     * @param accountDao
     * @param concurrencyScheduler
     * @param transferRequestRoutesV1
     * @param transferRoutesV1
     */
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

    /**
     * Registers Spark routes. Pass it as the 2nd parameter to
     * Spark.path().
     *
     * @see this::ROOT
     */
    public void populatePath() {
        post("/", this::createAccount, gson::toJson);

        path("/:visual_id", () -> {
            get("", this::showAccount, gson::toJson);

            path(transferRequestRoutesV1.ROOT, transferRequestRoutesV1::populatePath);
            path(transferRoutesV1.ROOT, transferRoutesV1::populatePath);
        });
    }

    /**
     * Maps to "GET {ROOT}/:visual_id".
     *
     * Loads an Account object (with the "TICKER:NAME" matching :visual_id)
     * and returns it (through ApiResponse).
     *
     * @see ApiResponse#respondWithResourceOrNull(Response, Object)
     *
     * @param request
     * @param response
     *
     * @return ApiResponse
     */
    public Object showAccount(Request request, Response response)
    {
        Account result = accountDao.findByVisualIdOrNull(request.params(":visual_id"));
        return ApiResponse.respondWithResourceOrNull(response, result);
    }

    /**
     * Maps to "POST {ROOT}/".
     *
     * Deserializes POST body to CreateAccountPOSTRequest, and uses that to
     * create am Account resource.
     *
     * @see CreateAccountPOSTRequest
     * @see ApiResponse#responseFromCreateRequestInTransaction(DataStoreConcurrencyScheduler, Response, GenericCreateRequest)
     *
     * @param request
     * @param response
     * @return
     */
    public Object createAccount(Request request, Response response)
    {
        CreateAccountPOSTRequest createRequest = gson.fromJson(request.body(), CreateAccountPOSTRequest.class);
        createRequest.prepareForValidation(accountDao);
        return ApiResponse.responseFromCreateRequestInTransaction(concurrencyScheduler, response, createRequest);
    }
}
