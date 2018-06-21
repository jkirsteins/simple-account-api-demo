package org.janiskirsteins.accounts.api.v1.transfers;

import org.janiskirsteins.accounts.api.model_base.GenericDAO;

public interface TransferDAO extends GenericDAO<Transfer>
{
	Transfer findByTransferRequestIdOrNull(int transferRequestId);
}