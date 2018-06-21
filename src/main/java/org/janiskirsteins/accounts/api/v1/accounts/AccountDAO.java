// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.accounts;

import org.janiskirsteins.accounts.api.model_base.GenericDAO;

/**
 * Data access object for Accounts.
 *
 * @see Account
 * @see GenericDAO
 */
public interface AccountDAO extends GenericDAO<Account>
{
    /**
     * Find an account by its visual ID.
     *
     * Visual ID is a computed property that consists of '{TICKER_SYMBOL}:{NAME}',
     * e.g. "ETH:0x000000..." (Ethereum account) or "EUR:LV40HABA..." (SEPA account)
     *
     * @param visualId
     * @return
     */
    Account findByVisualIdOrNull(String visualId);
}
