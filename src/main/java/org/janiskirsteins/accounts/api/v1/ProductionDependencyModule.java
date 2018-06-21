package org.janiskirsteins.accounts.api.v1;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.accounts.InMemoryAccountDAO;
import org.janiskirsteins.accounts.api.v1.transfers.DummyTransferService;
import org.janiskirsteins.accounts.api.v1.transfers.InMemoryTransferDAO;
import org.janiskirsteins.accounts.api.v1.transfers.InMemoryTransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.DummyApprovalService;

public class ProductionDependencyModule implements Module
{
	@Override
	public void configure(Binder binder) {
		binder.bind(AccountDAO.class).to(InMemoryAccountDAO.class).in(Singleton.class);
		binder.bind(ApprovalService.class).to(DummyApprovalService.class).in(Singleton.class);
		binder.bind(TransferService.class).to(DummyTransferService.class).in(Singleton.class);
		binder.bind(TransferRequestDAO.class).to(InMemoryTransferRequestDAO.class).in(Singleton.class);
		binder.bind(TransferDAO.class).to(InMemoryTransferDAO.class).in(Singleton.class);
		binder.bind(DataStoreConcurrencyScheduler.class).to(DummyDataStoreConcurrencyScheduler.class).in(Singleton.class);
	}
}

