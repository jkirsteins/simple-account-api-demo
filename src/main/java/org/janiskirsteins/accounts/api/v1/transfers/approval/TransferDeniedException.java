package org.janiskirsteins.accounts.api.v1.transfers.approval;

public class TransferDeniedException extends Exception
{
    private static final long serialVersionUID = 2611972894975885296L;

	public TransferDeniedException(String message)
    {
        super(message);
    }
}