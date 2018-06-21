package org.janiskirsteins.accounts.api.v1.transfers;

import org.janiskirsteins.accounts.api.model_base.BaseModel;

public class Transfer extends BaseModel
{
    private int transferRequestId;
    private boolean isConfirmed;

    public Transfer(int transferRequestId, boolean isConfirmed)
    {
        this.transferRequestId = transferRequestId;
        this.setIsConfirmed(isConfirmed);
	}

	public Transfer(int transferRequestId)
    {
        this(transferRequestId, false);
	}

	/**
	 * @return the isConfirmed
	 */
	public boolean isConfirmed() {
		return isConfirmed;
	}

	/**
	 * @param isConfirmed the isConfirmed to set
	 */
	public void setIsConfirmed(boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

	/**
	 * @return the transferRequestId
	 */
	public int getTransferRequestId() {
		return transferRequestId;
	}
}
