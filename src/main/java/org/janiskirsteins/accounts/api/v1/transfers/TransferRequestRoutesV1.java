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
public class TransferRequestRoutesV1
{
    public final String ROOT = "/transfer_request";

    private TransferRequestDAO transferRequestDao = null;
    private ApprovalService approvalService = null;
    private AccountDAO accountDao = null;
    private DataStoreConcurrencyScheduler concurrencyScheduler = null;
    private Gson gson = new Gson();

    @Inject
    public TransferRequestRoutesV1(
        TransferRequestDAO transferRequestDao,
        ApprovalService approvalService,
        AccountDAO accountDao,
        DataStoreConcurrencyScheduler concurrencyScheduler)
    {
        this.transferRequestDao = transferRequestDao;
        this.approvalService = approvalService;
        this.accountDao = accountDao;
        this.concurrencyScheduler = concurrencyScheduler;
    }

    public void populatePath() {
        post("/", this::createTransferRequest, gson::toJson);
        path("/:transfer_request_id", () -> {
            get("/status", this::generateRequestStatusMessage, gson::toJson);

            path("/approval_requirement", () -> {
                put("/:approval_requirement_id", this::putApprovalRequirementResponse, gson::toJson);
                get("/", this::listApprovalRequirements, gson::toJson);
            });
        });
    }

    public Object putApprovalRequirementResponse(Request request, Response response)
    {
        ApprovalRequirement updateObject = gson.fromJson(request.body(), ApprovalRequirement.class);

        ApprovalStatus status;
		try {
            status = approvalService.submitChallengeResponse(getTransferRequestId(request), getApprovalRequirementId(request), updateObject.getResponse());
            return ApiResponse.respondWithResourceOrNull(response, status);
		} catch (OperationNotSupportedException e) {
			return ApiResponse.respondFromException(response, e, 500);
		} catch (TransferDeniedException e) {
			return ApiResponse.respondFromException(response, e, 403);
		}
    }

    public Object generateRequestStatusMessage(Request request, Response response)
    {
        ApprovalStatus status = approvalService.getApprovalStatusForTransferRequest(getTransferRequestId(request));
        return ApiResponse.respondWithResourceOrNull(response, status);
    }

    public Object listApprovalRequirements(Request request, Response response)
    {
        Collection<ApprovalRequirement> requirements = approvalService.getApprovalRequirements(getTransferRequestId(request));
        return ApiResponse.respondWithResourceOrNull(response, requirements);
    }

    public Object createTransferRequest(Request request, Response response)
    {
        CreateTransferRequestPOSTRequest createRequest = gson.fromJson(request.body(), CreateTransferRequestPOSTRequest.class);
        createRequest.prepareForValidation(accountDao, transferRequestDao, request.params(":visual_id"));
        return ApiResponse.responseFromCreateRequestInTransaction(concurrencyScheduler, response, createRequest);
    }

    int getTransferRequestId(Request request)
    {
        return Integer.parseInt(request.params(":transfer_request_id"));
    }

    int getApprovalRequirementId(Request request)
    {
        return Integer.parseInt(request.params(":approval_requirement_id"));
    }
}
