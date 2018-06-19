package org.janiskirsteins.accounts.api.accounts;

import java.util.List;

public interface AccountDAO
{
    Account findByVisualIdOrNull(String visualId);
    List<Account> all();
}
