package org.janiskirsteins.accounts.api.v1.transfers;

import org.janiskirsteins.accounts.api.model_base.BaseCreateRequest;
import org.janiskirsteins.accounts.api.model_base.GenericCreateRequest;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.TransferDeniedException;
import spark.Response;

/**
 * POST request for creating transfers.
 *
 * When a POST request is sent to e.g. a transfer creation route (/account/xxx/transfer/)
 * then the request contents are deserialized from JSON To this object.
 *
 * Then it is passed to ApiResponse for processing and generating a response, which
 * is returned to the API client.
 *
 * @see org.janiskirsteins.accounts.api.v1.ApiResponse#responseFromCreateRequestInTransaction(DataStoreConcurrencyScheduler, Response, GenericCreateRequest)
 */
public class CreateTransferPOSTRequest extends BaseCreateRequest<Transfer>
{
    int transferRequestId;
	String sourceAccountVisualId;
    ApprovalService approvalService;
    TransferService transferService;
    TransferRequestDAO transferRequestDao;

    /**
     * Constructor.
     * @param transferRequestId
     */
	public CreateTransferPOSTRequest(int transferRequestId)
    {
        this.transferRequestId = transferRequestId;
    }

    /**
     * This function allows for dependency injection, which can not happen
     * through the constructor, because instances of this class are deserialized
     * automatically from JSON.
     *
     * @param transferRequestDao
     * @param transferService
     * @param approvalService
     * @param sourceAccountVisualId
     */
    public void prepareForValidation(
        TransferRequestDAO transferRequestDao,
        TransferService transferService,
        ApprovalService approvalService,
        String sourceAccountVisualId)
    {
        this.transferRequestDao = transferRequestDao;
        this.sourceAccountVisualId = sourceAccountVisualId;
        this.approvalService = approvalService;
        this.transferService = transferService;
    }

    /**
     * This should not be invoked directly. It assumes invocation
     * within a transaction.
     *
     * @see BaseCreateRequest#validateWithinTransaction()
     * @see this#prepareForValidation(TransferRequestDAO, TransferService, ApprovalService, String)
     * @throws InvalidRequestException if the approval service denies the transfer
     * @throws UnsupportedOperationException if the dependencies have not been injected via this#prepareForValidation
     * @throws UnsupportedOperationException if the source account visual ID is null
     */
    @Override
    protected void validateWithinTransaction() throws InvalidRequestException, UnsupportedOperationException
    {
        if (transferRequestDao == null || approvalService == null)
        {
            throw new UnsupportedOperationException("Please invoke prepareForValidation before validating.");
        }

        if (this.sourceAccountVisualId == null)
        {
            throw new UnsupportedOperationException("Source account ID not set (this is a server error).");
        }

        try
        {
            approvalService.requireApprovedOrThrow(transferRequestId, this.sourceAccountVisualId);
        }
        catch (TransferDeniedException e)
        {
            throw new InvalidRequestException("Transfer request is not in a state that allows transfer creation.", 400, e);
        }
	}

	/**
     * This should not be invoked directly. It assumes invocation
     * within a transaction.
     *
     * @see BaseCreateRequest#createWithinTransaction()
     * @return
     */
	@Override
    protected Transfer createWithinTransaction() throws Exception
    {
        return transferService.createTransfer(transferRequestId, this.sourceAccountVisualId);
	}
}
