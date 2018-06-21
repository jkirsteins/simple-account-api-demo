package org.janiskirsteins.accounts.api.v1.transfers;

import org.janiskirsteins.accounts.api.model_base.GenericDAO;

public interface TransferRequestDAO extends GenericDAO<TransferRequest>
{
    void insert(TransferRequest newRequest);
}
