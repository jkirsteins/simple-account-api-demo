package org.janiskirsteins.accounts.api.v1.transfers.approval;

import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.v1.transfers.Transfer;

public interface ApprovalService
{
    Collection<ApprovalRequirement> getApprovalRequirements(int transferRequestId);
    ApprovalStatus getApprovalStatusForTransferRequest(int transferRequestId);
    ApprovalStatus submitChallengeResponse(int transferRequestId, int approvalId, String response) throws TransferDeniedException, OperationNotSupportedException;
    void requireApprovedOrThrow(int transferRequestId, String sourceAccountVisualId) throws TransferDeniedException;
}
