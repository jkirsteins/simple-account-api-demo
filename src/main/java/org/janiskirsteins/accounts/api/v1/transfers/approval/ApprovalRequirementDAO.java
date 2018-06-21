package org.janiskirsteins.accounts.api.v1.transfers.approval;

import java.util.List;

import org.janiskirsteins.accounts.api.model_base.GenericDAO;

interface ApprovalRequirementDAO extends GenericDAO<ApprovalRequirement>
{
    List<ApprovalRequirement> findAllByTransferRequestId(int transferRequestId);
}
