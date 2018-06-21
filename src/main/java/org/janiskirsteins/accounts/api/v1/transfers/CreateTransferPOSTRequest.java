package org.janiskirsteins.accounts.api.v1.transfers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.janiskirsteins.accounts.api.model_base.BaseCreateRequest;
import org.janiskirsteins.accounts.api.model_base.BaseModel;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;
import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.TransferDeniedException;

class CreateTransferPOSTRequest extends BaseCreateRequest<Transfer>
{
    int transferRequestId;
	String sourceAccountVisualId;
    ApprovalService approvalService;
    TransferService transferService;
    TransferRequestDAO transferRequestDao;

	public CreateTransferPOSTRequest(int transferRequestId)
    {
        this.transferRequestId = transferRequestId;
    }

    /** This method is necessary because we deserialize the requests from POST data, and can not pass this in through the constructor. */
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

	@Override
    protected Transfer createWithinTransaction() throws Exception
    {
        return transferService.createTransfer(transferRequestId, this.sourceAccountVisualId);
	}
}
