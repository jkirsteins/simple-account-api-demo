package org.janiskirsteins.accounts.api;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.janiskirsteins.accounts.api.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.accounts.InMemoryAccountDAO;

public class ProductionDependencyModule implements Module
{
	@Override
	public void configure(Binder binder) {
        binder.bind(AccountDAO.class).to(InMemoryAccountDAO.class);
	}
}

