package org.janiskirsteins.accounts.api.v1.transfers;

import com.google.gson.Gson;
import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.model_base.BaseCreateRequest;
import org.janiskirsteins.accounts.api.model_base.BaseModel;
import org.janiskirsteins.accounts.api.model_base.GenericDAO;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;
import org.janiskirsteins.accounts.api.v1.ApiResponse;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
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
 * Hello world!
 */
public class TransferRoutesV1
{
    public final String ROOT = "/transfer";

    private TransferRequestDAO transferRequestDao = null;
    private ApprovalService approvalService = null;
    private AccountDAO accountDao = null;
    private DataStoreConcurrencyScheduler concurrencyScheduler = null;
    private TransferDAO transferDao;
    private TransferService transferService;
    private Gson gson = new Gson();

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

    public void populatePath() {
        post("/", this::createTransfer, gson::toJson);
        path("/:transfer_id", () -> {
            get("", this::showTransfer, gson::toJson);
            put("", this::confirmTransfer, gson::toJson);
        });
    }

    public Object createTransfer(Request request, Response response)
    {
        CreateTransferPOSTRequest createTransfer = gson.fromJson(request.body(), CreateTransferPOSTRequest.class);
        createTransfer.prepareForValidation(transferRequestDao, transferService, approvalService, request.params(":visual_id"));
        return ApiResponse.responseFromCreateRequestInTransaction(concurrencyScheduler, response, createTransfer);
    }

    public Object showTransfer(Request request, Response response)
    {
        Transfer result = transferDao.findById(getTransferId(request));
        System.out.println(String.format("Showing transfer %d", getTransferId(request)));
        System.out.println(String.format("Known transfers %s", transferDao.all()));
        return ApiResponse.respondWithResourceOrNull(response, result);
    }

    public Object confirmTransfer(Request request, Response response)
    {
        try
        {
            Transfer result = transferService.finalizeTransfer(getTransferId(request));
            throw new InterruptedException(String.format("Got result: %s from %d", result, getTransferId(request)));
            // return ApiResponse.respondWithResourceOrNull(response, result);
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
