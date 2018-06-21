// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import org.janiskirsteins.accounts.api.model_base.BaseCreateRequest;
import org.janiskirsteins.accounts.api.model_base.GenericCreateRequest;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import spark.Response;

/**
 * POST request for creating transfer requests.
 *
 * When a POST request is sent to e.g. a transfer creation route (/account/xxx/transfer_request/)
 * then the request contents are deserialized from JSON To this object.
 *
 * Then it is passed to ApiResponse for processing and generating a response, which
 * is returned to the API client.
 *
 * @see org.janiskirsteins.accounts.api.v1.ApiResponse#responseFromCreateRequestInTransaction(DataStoreConcurrencyScheduler, Response, GenericCreateRequest)
 */
class CreateTransferRequestPOSTRequest extends BaseCreateRequest<TransferRequest>
{
    private String sourceAccountVisualId;
    private boolean allowOverdraft;
    private List<TransferRecipient> recipients;
    private AccountDAO accountDao;
    private TransferRequestDAO trDao;

    /**
     * Constructor.
     * @param recipients
     * @param allowOverdraft
     */
	public CreateTransferRequestPOSTRequest(
        List<TransferRecipient> recipients,
        boolean allowOverdraft)
    {
        this.recipients = recipients;
        this.allowOverdraft = allowOverdraft;
    }

    /**
     * This function allows for dependency injection, which can not happen
     * through the constructor, because instances of this class are deserialized
     * automatically from JSON.
     *
     * @param accountDao
     * @param trDao
     * @param sourceAccountVisualId set this to the expected source account
     */
    public void prepareForValidation(AccountDAO accountDao, TransferRequestDAO trDao, String sourceAccountVisualId)
    {
        this.accountDao = accountDao;
        this.trDao = trDao;
        this.sourceAccountVisualId = sourceAccountVisualId;
    }

    /**
     * This should not be invoked directly. It assumes invocation
     * within a transaction.
     *
     * @see BaseCreateRequest#validateWithinTransaction()
     * @throws UnsupportedOperationException if the dependencies have not been injected via this#prepareForValidation
     * @throws UnsupportedOperationException if the sourceAccountVisualId value is null
     * @throws InvalidRequestException if the sourceAccountVisualId refers to an invalid (not found) account
     * @throws InvalidRequestException if the recipient list is empty
     * @throws InvalidRequestException if the recipient list contains an invalid (not found) account
     * @throws InvalidRequestException if the recipient list contains an account with different tickerSymbol than the source account
     * @throws InvalidRequestException if the transfer size exceeds the source account's available balance (and overdraft is not allowed)
     */
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

    /**
     * This should not be invoked directly. It assumes invocation
     * within a transaction.
     *
     * @see BaseCreateRequest#createWithinTransaction()
     * @return
     */
	@Override
    protected TransferRequest createWithinTransaction()
    {
        TransferRequest tr = new TransferRequest(sourceAccountVisualId, recipients);
        trDao.insert(tr);
        return tr;
	}
}
