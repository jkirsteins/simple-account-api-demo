// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers.approval;

import java.util.List;

import org.janiskirsteins.accounts.api.model_base.GenericDAO;

/**
 * In-memory data access object for ApprovalRequirements.
 *
 * Should not be used directly - instead go through ApprovalService.
 *
 * @see ApprovalService
 */
interface ApprovalRequirementDAO extends GenericDAO<ApprovalRequirement>
{
    List<ApprovalRequirement> findAllByTransferRequestId(int transferRequestId);
}
