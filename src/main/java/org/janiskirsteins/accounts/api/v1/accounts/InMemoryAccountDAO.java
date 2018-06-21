package org.janiskirsteins.accounts.api.v1.accounts;

import java.util.Objects;

import org.janiskirsteins.accounts.api.model_base.BaseDAO;

public class InMemoryAccountDAO extends BaseDAO<Account> implements AccountDAO
{
	@Override
	public Account findByVisualIdOrNull(String visualId) {
		System.out.println("Looking for: " + visualId);
		System.out.println("Have: " + findById(1).getVisualId());

		return all().stream().filter(item -> Objects.equals(visualId, item.getVisualId())).findAny().orElse(null);
	}
}
