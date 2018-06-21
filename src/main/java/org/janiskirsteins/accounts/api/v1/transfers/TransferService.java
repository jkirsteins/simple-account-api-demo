// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers;

import org.janiskirsteins.accounts.api.v1.transfers.approval.TransferDeniedException;

/**
 * TransferService is responsible for creating and finalizing transactions,
 * and modifying account balances.
 *
 * It should modify balances as follows:
 *  - available balance is debited when an outgoing transfer is created.
 *  - available and Total balances are credited when an incoming transfer is confirmed.
 *  - total balance is debited when an unconfirmed outgoing transfer is confirmed.
 */
public interface TransferService
{
    /**
     * Creates a transfer based on an approved transfer request.
     *
     * This also modifies account total/available balances.
     *
     * @param transferRequestId approved transfer request's ID
     * @param sourceAccountVisualId source account of the transfer
     * @return a newly created (pending) transfer
     * @throws TransferDeniedException if the approval service does not signal the transfer is approved
     * @throws InterruptedException if a data storage transaction could not be started
     */
    Transfer createTransfer(int transferRequestId, String sourceAccountVisualId) throws TransferDeniedException, InterruptedException;

    /**
     * Approves a pending transfer.
     *
     * This also modifies account total/available balances.
     *
     * @param transferId pending transfer's ID
     * @return a confirmed transaction object
     * @throws TransferDeniedException if the transferId does not refer to a pending transfer
     * @throws InterruptedException if a data storage transaction could not be started
     */
    Transfer finalizeTransfer(int transferId) throws TransferDeniedException, InterruptedException;
}
