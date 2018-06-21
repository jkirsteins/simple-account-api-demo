package org.janiskirsteins.accounts.api.v1.transfers;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.activity.InvalidActivityException;
import javax.naming.OperationNotSupportedException;

import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.model_base.BaseDAO;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.transfers.Transfer;
import org.janiskirsteins.accounts.api.v1.transfers.TransferDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequest;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.TransferDeniedException;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalRequirement.RequiredApprovalType;
import org.omg.CORBA.DynAnyPackage.Invalid;

public class DummyTransferService implements TransferService
{
    private TransferDAO transferDao;
    private TransferRequestDAO transferRequestDao;
    private ApprovalService approvalService;
    private DataStoreConcurrencyScheduler concurrencyScheduler;
    private AccountDAO accountDao;

	@Inject
    public DummyTransferService(
        DataStoreConcurrencyScheduler concurrencyScheduler,
        ApprovalService approvalService,
        TransferRequestDAO transferRequestDao,
        TransferDAO transferDao,
        AccountDAO accountDao)
    {
        this.concurrencyScheduler = concurrencyScheduler;
        this.transferDao = transferDao;
        this.transferRequestDao = transferRequestDao;
        this.approvalService = approvalService;
        this.accountDao = accountDao;
    }

    @Override
    public Transfer createTransfer(int transferRequestId, String sourceAccountVisualId) throws TransferDeniedException, InterruptedException
    {
        approvalService.requireApprovedOrThrow(transferRequestId, sourceAccountVisualId);

        try
        {
            concurrencyScheduler.startTransaction();

            Transfer existing = transferDao.findByTransferRequestIdOrNull(transferRequestId);
            if (existing != null)
            {
                throw new UnsupportedOperationException("Transfer already created.");
            }

            Transfer result = new Transfer(transferRequestId);
            applyUnconfirmedTransferToBalances_assumeWithinTransaction(result);
            transferDao.insert(result);

            concurrencyScheduler.commitTransaction();

            return result;
        } catch (TransferDeniedException e)
        {
            concurrencyScheduler.rollbackTransaction();
            throw e;
        }
    }

    @Override
	public Transfer finalizeTransfer(int transferId) throws TransferDeniedException, InterruptedException {
		Transfer transfer = transferDao.findById(transferId);

        if (transfer == null)
        {
            throw new TransferDeniedException(String.format("Could not find and finalize transfer %d", transferId));
        }

        if (transfer.isConfirmed())
        {
            throw new TransferDeniedException(String.format("Transfer %d is already finalized.", transferId));
        }

        concurrencyScheduler.startTransaction();

        confirmTransferBalances_assumeWithinTransaction(transfer);
        transfer.setIsConfirmed(true);
        transferDao.update(transfer);

        concurrencyScheduler.commitTransaction();

        return transfer;
    }

    enum UpdateType
    {
        Unconfirmed,
        Confirmed
    }

    private void confirmTransferBalances_assumeWithinTransaction(Transfer result) throws TransferDeniedException
    {
        if (result.getPrimaryKey() < 0)
        {
            throw new TransferDeniedException("Can not confirm a non-persisted transfer");
        }

        if (result.isConfirmed())
        {
            throw new TransferDeniedException("Can not confirm an already confirmed transfer");
        }

        updateAccountsFromTransferRequest_assumeWithinTransaction(result, UpdateType.Confirmed);
    }

    private void applyUnconfirmedTransferToBalances_assumeWithinTransaction(Transfer result) throws TransferDeniedException
    {
        if (result.getPrimaryKey() > -1)
        {
            throw new TransferDeniedException("Can not apply an already-persisted unconfirmed transfer");
        }

        updateAccountsFromTransferRequest_assumeWithinTransaction(result, UpdateType.Unconfirmed);
    }

    private void updateAccountsFromTransferRequest_assumeWithinTransaction(Transfer result, UpdateType updateType) throws TransferDeniedException
    {
        TransferRequest transferRequest = transferRequestDao.findById(result.getTransferRequestId());

        Account sourceAccount = accountDao.findByVisualIdOrNull(transferRequest.getSourceAccountVisualId());
        if (sourceAccount == null)
        {
            throw new TransferDeniedException(String.format("Source account %s could not be found.", transferRequest.getSourceAccountVisualId()));
        }

        for (TransferRecipient tr : transferRequest.getRecipients())
        {
            Account targetAccount = accountDao.findByVisualIdOrNull(tr.getTargetAccountVisualId());
            if (targetAccount == null)
            {
                throw new TransferDeniedException(String.format("Recipient account %s could not be found.", tr.getTargetAccountVisualId()));
            }

            if (!Objects.equals(sourceAccount.getTickerSymbol(), targetAccount.getTickerSymbol()))
            {
                throw new TransferDeniedException(String.format("Source<->destination account type mismatch (source: %s; target: %s)",
                    sourceAccount.getTickerSymbol(),
                    targetAccount.getTickerSymbol()));
            }

            if (sourceAccount.getAvailableBalance().compareTo(tr.getTransferAmount()) < 0)
            {
                throw new TransferDeniedException(String.format("Source account %s does not have enough funds to complete transfer request %d.",
                    sourceAccount.getVisualId(),
                    transferRequest.getPrimaryKey()));
            }

            BigInteger sourceAmount = tr.getTransferAmount().negate();
            BigInteger targetAmount = tr.getTransferAmount();

            switch (updateType)
            {
                case Confirmed:
                    sourceAccount.modifyTotal(sourceAmount);
                    targetAccount.modifyTotal(targetAmount);
                    targetAccount.modifyAvailable(targetAmount);
                    break;
                case Unconfirmed:
                    sourceAccount.modifyAvailable(sourceAmount);
                    // don't credit the recipient until it is confirmed
                    break;

            }

            accountDao.update(sourceAccount);
            accountDao.update(targetAccount);
        }
	}


}
