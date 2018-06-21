package org.janiskirsteins.accounts.api.v1.transfers.approval;

import java.rmi.server.Operation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.model_base.BaseDAO;
import org.janiskirsteins.accounts.api.v1.transfers.Transfer;
import org.janiskirsteins.accounts.api.v1.transfers.TransferDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequest;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalRequirement.RequiredApprovalType;

public class DummyApprovalService implements ApprovalService
{
    ApprovalRequirementDAO arDao;
    TransferDAO transferDao;
    TransferRequestDAO transferRequestDao;

    @Inject
    public DummyApprovalService(TransferDAO transferDao, TransferRequestDAO transferRequestDao)
    {
        this.arDao = new DummyApprovalRequirementDAO();
        this.transferDao = transferDao;
        this.transferRequestDao = transferRequestDao;
    }

	@Override
    public Collection<ApprovalRequirement> getApprovalRequirements(int transferRequestId)
    {
        Collection<ApprovalRequirement> result = arDao.findAllByTransferRequestId(transferRequestId);
        if (result.isEmpty())
        {
            ApprovalRequirement req = new ApprovalRequirement(transferRequestId,
                RequiredApprovalType.Debug_PutAPPROVEDInResponse);
            arDao.insert(req);

            return arDao.findAllByTransferRequestId(transferRequestId);
        }

        return result;
	}

	@Override
	public ApprovalStatus getApprovalStatusForTransferRequest(int transferRequestId) {
        Collection<ApprovalRequirement> allRequirements = getApprovalRequirements(transferRequestId);

        for (ApprovalRequirement approvalReq : allRequirements)
        {
            if (approvalReq.requiredApprovalType == RequiredApprovalType.Debug_PutAPPROVEDInResponse)
            {
                if (approvalReq.response != null)
                {
                    if (Objects.equals(approvalReq.response, "APPROVED"))
                    {
                        return ApprovalStatus.ApprovalFine_CanProceed;
                    }
                    else
                    {
                        return ApprovalStatus.ApprovalDenied_WillNotProceed;
                    }
                }
            }
        }

        return ApprovalStatus.PendingResolution;
	}

	@Override
	public ApprovalStatus submitChallengeResponse(int transferRequestId, int approvalId, String response)
			throws TransferDeniedException, OperationNotSupportedException {

        ApprovalStatus existingStatus = this.getApprovalStatusForTransferRequest(transferRequestId);
        if (existingStatus == ApprovalStatus.ApprovalDenied_WillNotProceed)
        {
            throw new TransferDeniedException("Transfer is denied permanently and can not be approved.");
        }

        ApprovalRequirement requirement = arDao.findById(approvalId);
        if (requirement == null)
        {
            throw new UnsupportedOperationException("Requirement does not exist.");
        }
        if (requirement.transferRequestId != transferRequestId)
        {
            throw new UnsupportedOperationException("Requirement is not associated with this transfer request.");
        }
        if (requirement.requiredApprovalType != RequiredApprovalType.Debug_PutAPPROVEDInResponse)
        {
            throw new UnsupportedOperationException("Dummy approval service only supports the Debug approval method. The rest are examples only.");
        }

        // since we use an in-memory data store, it is updated automatically
        requirement.response = response;

        return this.getApprovalStatusForTransferRequest(transferRequestId);
	}

    public void requireApprovedOrThrow(int transferRequestId, String sourceAccountVisualId) throws TransferDeniedException
    {
        TransferRequest transferRequest = transferRequestDao.findById(transferRequestId);

        if (transferRequest == null)
        {
            throw new TransferDeniedException("Invalid transfer request ID");
        }

        if (!Objects.equals(transferRequest.getSourceAccountVisualId(), sourceAccountVisualId))
        {
            throw new TransferDeniedException(
                String.format(
                    "Mismatched accounts (expected %s got %s)",
                    sourceAccountVisualId,
                    transferRequest.getSourceAccountVisualId()));
        }

        ApprovalStatus status = this.getApprovalStatusForTransferRequest(transferRequestId);
        if (status != ApprovalStatus.ApprovalFine_CanProceed)
        {
            throw new TransferDeniedException("Transfer has not been approved.");
        }
	}
}
