// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1;

/**
 * An interface for managing data access transactions.
 *
 * This does not do anything if the data access objects are e.g. in-memory arrays,
 * but with a more sophisticated data storage mechanism, this is needed to be explicit
 * about where data access needs to happen together (and with locking).
 *
 * For example, if we are creating transfer requests, we need to have locks on the data to avoid
 * a situation where:
 * - two threads separately try to create a transfer request, and the account has enough balance for either transfer
 *   request individually (but not both)
 * - first transfer request is created, reducing the account balance
 * - IMPORTANT: if we are not performing "balance checks and updates" in a single transaction, then
 *   we could now create the second request and update account balances (even though they have since become insufficient)
 *
 * This is what we need to avoid.
 */
public interface DataStoreConcurrencyScheduler
{
    void startTransaction() throws InterruptedException;
    void rollbackTransaction();
    void commitTransaction();
}