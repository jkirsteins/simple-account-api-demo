// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.accounts;

import java.util.Objects;

import org.janiskirsteins.accounts.api.model_base.BaseDAO;

/**
 * Dummy data access object for Accounts.
 */
public class InMemoryAccountDAO extends BaseDAO<Account> implements AccountDAO
{
	@Override
	public Account findByVisualIdOrNull(String visualId) {
		return all().stream().filter(item -> Objects.equals(visualId, item.getVisualId())).findAny().orElse(null);
	}
}
