package org.janiskirsteins.accounts.api.v1.transfers;

public class TransferCanNotBeRequestedException extends Exception
{
    private static final long serialVersionUID = 7344568930915823776L;
	private ReasonCode reasonCode;

	TransferCanNotBeRequestedException(String message, ReasonCode reasonCode)
    {
        super(message);
        this.reasonCode = reasonCode;
    }

    TransferCanNotBeRequestedException(ReasonCode reasonCode)
    {
        this(null, reasonCode);
    }

    /**
	 * @return the code
	 */
	public ReasonCode getReasonCode() {
		return reasonCode;
	}

	public enum ReasonCode
    {
        SourceAccountNotFound,
        TargetAccountNotFound,
        MismatchedAccountTypes,
        InsufficientBalance
    }
}