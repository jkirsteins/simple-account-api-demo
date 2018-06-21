package org.janiskirsteins.accounts.api.v1.transfers;

import java.math.BigInteger;
import java.util.List;

import org.janiskirsteins.accounts.api.model_base.BaseModel;

public class TransferRequest extends BaseModel
{
	private String sourceAccountVisualId;
	private List<TransferRecipient> recipients;

	public TransferRequest(String sourceAccountVisualId, List<TransferRecipient> recipients)
	{
		this.sourceAccountVisualId = sourceAccountVisualId;
		this.recipients = recipients;
	}

	/**
	 * @return the recipients
	 */
	public List<TransferRecipient> getRecipients() {
		return recipients;
	}

	public String getSourceAccountVisualId() {
		return this.sourceAccountVisualId;
	}
}
