// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers.routes.v1;

import com.google.gson.Gson;
import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.model_base.*;
import org.janiskirsteins.accounts.api.v1.ApiResponse;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.transfers.*;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalRequirement;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalStatus;
import org.janiskirsteins.accounts.api.v1.transfers.approval.TransferDeniedException;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import spark.Request;
import spark.Response;

/**
 * Class responsible for initializing the "/transfer" API routes.
 *
 * This class assumes it is mapped in a route hierarchy where there is a parent
 * Spark param available called :visual_id, which identifies the source account of the
 * corresponding transactions.
 */
public class TransferRoutesV1
{
    /**
     * Root path part, under which this class will
     * register routes.
     *
     * Pass it as the 1st parameter to Spark.path().
     *
     * @see this::populatePath
     */
    public final String ROOT = "/transfer";

    private TransferRequestDAO transferRequestDao = null;
    private ApprovalService approvalService = null;
    private AccountDAO accountDao = null;
    private DataStoreConcurrencyScheduler concurrencyScheduler = null;
    private TransferDAO transferDao;
    private TransferService transferService;
    private Gson gson = new Gson();

    /**
     * Initializer (invoked by Guice, which injects either the regular,
     * or testing dependencies).
     *
     * @param transferRequestDao
     * @param transferService
     * @param transferDao
     * @param approvalService
     * @param accountDao
     * @param concurrencyScheduler
     */
    @Inject
    public TransferRoutesV1(
        TransferRequestDAO transferRequestDao,
        TransferService transferService,
        TransferDAO transferDao,
        ApprovalService approvalService,
        AccountDAO accountDao,
        DataStoreConcurrencyScheduler concurrencyScheduler)
    {
        this.transferRequestDao = transferRequestDao;
        this.approvalService = approvalService;
        this.transferService = transferService;
        this.accountDao = accountDao;
        this.transferDao = transferDao;
        this.concurrencyScheduler = concurrencyScheduler;
    }

    /**
     * Registers Spark routes. Pass it as the 2nd parameter to
     * Spark.path().
     *
     * @see this::ROOT
     */
    public void populatePath() {
        post("/", this::createTransfer, gson::toJson);
        path("/:transfer_id", () -> {
            get("", this::showTransfer, gson::toJson);
            put("", this::confirmTransfer, gson::toJson);
        });
    }

    /**
     * Maps to "POST {ROOT}/"
     *
     * Deserializes POST body to CreateTransferPOSTRequest, and uses that to
     * create a Transfer object.
     *
     * @see CreateTransferPOSTRequest
     * @see ApiResponse#responseFromCreateRequestInTransaction(DataStoreConcurrencyScheduler, Response, GenericCreateRequest)
     *
     * @param request
     * @param response
     * @return
     */
    public Object createTransfer(Request request, Response response)
    {
        CreateTransferPOSTRequest createTransfer = gson.fromJson(request.body(), CreateTransferPOSTRequest.class);
        createTransfer.prepareForValidation(transferRequestDao, transferService, approvalService, request.params(":visual_id"));
        return ApiResponse.responseFromCreateRequestInTransaction(concurrencyScheduler, response, createTransfer);
    }

    /**
     * Maps to "GET {ROOT}/:transfer_id".
     *
     * Loads a Transfer object (with the primaryKey matching :transfer_id)
     * and returns it (through ApiResponse).
     *
     * @see ApiResponse#respondWithResourceOrNull(Response, Object)
     *
     * @param request
     * @param response
     * @return ApiResponse
     */
    public Object showTransfer(Request request, Response response)
    {
        Transfer result = transferDao.findById(getTransferId(request));
        return ApiResponse.respondWithResourceOrNull(response, result);
    }

    /**
     * Maps to "PUT {ROOT}/:transfer_id".
     *
     * Loads the Transfer object identified by :transfer_id, and attempts
     * to finalize it.
     *
     * @see TransferService#finalizeTransfer(int)
     *
     * @param request
     * @param response
     * @return
     */
    public Object confirmTransfer(Request request, Response response)
    {
        try
        {
            Transfer result = transferService.finalizeTransfer(getTransferId(request));
            return ApiResponse.respondWithResourceOrNull(response, result);
        }
        catch (TransferDeniedException | InterruptedException e)
        {
            return ApiResponse.respondFromException(response, e, 500);
        }
    }

    private int getTransferId(Request request)
    {
		return Integer.parseInt(request.params(":transfer_id"));
	}
}
