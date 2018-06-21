package org.janiskirsteins.accounts.api.v1.transfers;

import java.math.BigInteger;
import java.util.List;

import org.janiskirsteins.accounts.api.model_base.BaseModel;

public class TransferRecipient extends BaseModel
{
    private String targetAccountVisualId;
    private BigInteger transferAmount;

    public TransferRecipient(String targetAccountVisualId, BigInteger transferAmount)
    {
        this.targetAccountVisualId = targetAccountVisualId;
        this.transferAmount = transferAmount;
    }

	public String getTargetAccountVisualId() {
		return this.targetAccountVisualId;
    }

    public BigInteger getTransferAmount() {
        return this.transferAmount;
    }
}
