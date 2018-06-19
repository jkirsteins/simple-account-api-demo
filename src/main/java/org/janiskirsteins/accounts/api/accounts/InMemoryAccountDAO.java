package org.janiskirsteins.accounts.api.accounts;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAccountDAO implements AccountDAO
{
	@Override
	public Account findByVisualIdOrNull(String visualId) {
		return null;
	}

	@Override
	public List<Account> all() {
		return new ArrayList<>();
	}
}
