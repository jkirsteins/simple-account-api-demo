// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers.approval;

import java.util.Collection;
import javax.naming.OperationNotSupportedException;

/**
 * Before transfers can be initiated, they have to be approved. This service is responsible for providing
 * the required information, and validating the information received via the API.
 */
public interface ApprovalService
{
    /**
     * @param transferRequestId a pending transfer request's ID
     * @return a collection of the informatino required to approve the request
     */
    Collection<ApprovalRequirement> getApprovalRequirements(int transferRequestId);

    /**
     * @param transferRequestId a pending transfer request's ID
     * @return whether the transfer can be initiated (ok/not ok/still pending)
     * @see ApprovalStatus
     */
    ApprovalStatus getApprovalStatusForTransferRequest(int transferRequestId);

    /**
     * Submit the required information to approve a transfer request (piece-by-piece)
     * @param transferRequestId a pending transfer request's ID
     * @param approvalId the ID of an approval requirement for which we are submitting the payload
     * @param response the response which should satisfy the approval requirement
     * @return the updated status for the transfer request
     * @see ApprovalStatus
     * @throws TransferDeniedException if the provided information means the transfer request has been permanently denied based on the submitted information
     * @throws TransferDeniedException if the transfer request is not in a pending state
     * @throws OperationNotSupportedException if the service implementation can not process the requirement
     */
    ApprovalStatus submitChallengeResponse(int transferRequestId, int approvalId, String response) throws TransferDeniedException, OperationNotSupportedException;

    /**
     * @param transferRequestId an approved transfer request's ID
     * @param sourceAccountVisualId account ID that is the source of the proposed transfer
     * @throws TransferDeniedException if the transfer request is not approved (i.e. is pending or denied permanently)
     */
    void requireApprovedOrThrow(int transferRequestId, String sourceAccountVisualId) throws TransferDeniedException;
}
