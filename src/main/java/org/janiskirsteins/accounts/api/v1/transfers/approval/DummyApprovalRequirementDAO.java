// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers.approval;

import java.util.List;
import java.util.stream.Collectors;

import org.janiskirsteins.accounts.api.model_base.BaseDAO;

/**
 * In-memory data access object for approval requirements.
 */
public class DummyApprovalRequirementDAO extends BaseDAO<ApprovalRequirement> implements ApprovalRequirementDAO
{
	@Override
	public List<ApprovalRequirement> findAllByTransferRequestId(int transferRequestId) {
		return this.all().stream().filter(item -> item.transferRequestId == transferRequestId).collect(Collectors.toList());
	}
}