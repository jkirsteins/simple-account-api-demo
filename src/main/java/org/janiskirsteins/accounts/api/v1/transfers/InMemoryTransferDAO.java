package org.janiskirsteins.accounts.api.v1.transfers;

import java.util.stream.Collectors;

import org.janiskirsteins.accounts.api.model_base.BaseDAO;

public class InMemoryTransferDAO extends BaseDAO<Transfer> implements TransferDAO
{
	@Override
    public Transfer findByTransferRequestIdOrNull(int transferRequestId)
    {
		return this.all().stream().filter(item -> item.getTransferRequestId() == transferRequestId).findFirst().orElse(null);
	}
}
