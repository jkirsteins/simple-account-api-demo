// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers.approval;

/**
 * An exception that is thrown if a transfer request has been denied.
 */
public class TransferDeniedException extends Exception
{
    private static final long serialVersionUID = 2611972894975885296L;

	public TransferDeniedException(String message)
    {
        super(message);
    }
}