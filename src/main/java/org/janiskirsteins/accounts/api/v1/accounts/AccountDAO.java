package org.janiskirsteins.accounts.api.v1.accounts;

import org.janiskirsteins.accounts.api.model_base.GenericDAO;

public interface AccountDAO extends GenericDAO<Account>
{
    Account findByVisualIdOrNull(String visualId);
}
