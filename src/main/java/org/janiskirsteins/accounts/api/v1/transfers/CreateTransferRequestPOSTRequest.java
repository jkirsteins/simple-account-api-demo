package org.janiskirsteins.accounts.api.v1.transfers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.janiskirsteins.accounts.api.model_base.BaseCreateRequest;
import org.janiskirsteins.accounts.api.model_base.BaseModel;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;
import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;

class CreateTransferRequestPOSTRequest extends BaseCreateRequest<TransferRequest>
{
    private String sourceAccountVisualId;
    private boolean allowOverdraft;
    private List<TransferRecipient> recipients;
    private AccountDAO accountDao;
    private TransferRequestDAO trDao;

	public CreateTransferRequestPOSTRequest(
        List<TransferRecipient> recipients,
        boolean allowOverdraft)
    {
        this.recipients = recipients;
        this.allowOverdraft = allowOverdraft;
    }

    /** This method is necessary because we deserialize the requests from POST data, and can not pass this in through the constructor. */
    public void prepareForValidation(AccountDAO accountDao, TransferRequestDAO trDao, String sourceAccountVisualId)
    {
        this.accountDao = accountDao;
        this.trDao = trDao;
        this.sourceAccountVisualId = sourceAccountVisualId;
    }

    @Override
    protected void validateWithinTransaction() throws InvalidRequestException, UnsupportedOperationException
    {
        if (accountDao == null || trDao == null)
        {
            throw new UnsupportedOperationException("Please invoke #prepareForValidation before validating.");
        }

        if (this.sourceAccountVisualId == null)
        {
            throw new UnsupportedOperationException("Source account ID not set (this is a server error).");
        }

        Account sourceAccount = accountDao.findByVisualIdOrNull(this.sourceAccountVisualId);

        if (sourceAccount == null)
        {
            throw new InvalidRequestException("Invalid source account ID", 404);
        }

        if (this.recipients == null || this.recipients.isEmpty())
        {
            throw new InvalidRequestException("Missing or empty transfer recipient list");
        }

        for (TransferRecipient recipient : this.recipients)
        {
            Account targetAccount = accountDao.findByVisualIdOrNull(recipient.getTargetAccountVisualId());
            if (targetAccount == null)
            {
                throw new InvalidRequestException(String.format("Invalid transfer recipient %s", recipient.getTargetAccountVisualId()));
            }

            if (!Objects.equals(targetAccount.getTickerSymbol(), sourceAccount.getTickerSymbol()))
            {
                throw new InvalidRequestException(String.format("Mismatched source<->destination account denominations (source %s; target %s)",
                    sourceAccount.getTickerSymbol(),
                    targetAccount.getTickerSymbol()));
            }
        }

        BigInteger totalTransferAmount = this.recipients.stream().map(TransferRecipient::getTransferAmount).reduce(BigInteger.ZERO, BigInteger::add);

        if (totalTransferAmount.compareTo(sourceAccount.getAvailableBalance()) > 0)
        {
            if (!this.allowOverdraft)
            {
                throw new InvalidRequestException(
                    String.format("Total transfer amount (%s) exceeds the available balance (%s).", totalTransferAmount, sourceAccount.getAvailableBalance()));
            }
        }
	}

	@Override
    protected TransferRequest createWithinTransaction()
    {
        TransferRequest tr = new TransferRequest(sourceAccountVisualId, recipients);
        trDao.insert(tr);
        return tr;
	}
}
