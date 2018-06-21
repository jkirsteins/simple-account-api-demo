package org.janiskirsteins.accounts.api.v1.transfer;

import static org.junit.Assert.assertEquals;

import org.apache.http.conn.UnsupportedSchemeException;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.transfers.DummyTransferService;
import org.janiskirsteins.accounts.api.v1.transfers.Transfer;
import org.janiskirsteins.accounts.api.v1.transfers.TransferDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRecipient;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequest;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.TransferDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.mockito.MockitoAnnotations;

public class DummyTransferServiceTest
{
    @Mock
    DataStoreConcurrencyScheduler concurrencyScheduler;

    @Mock
    ApprovalService approvalService;

    @Mock
    TransferRequestDAO transferRequestDao;

    @Mock
    TransferDAO transferDao;

    @Mock
    AccountDAO accountDao;

    DummyTransferService sut;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        sut = new DummyTransferService(
            concurrencyScheduler,
            approvalService,
            transferRequestDao,
            transferDao,
            accountDao);
    }

    @Test
    public void createTransfer_validAmounts_createsUnconfirmedTransferAndAdjustsAvailable() throws Exception
    {
        Account source = mock(Account.class);
        Account destination = mock(Account.class);
        TransferRecipient recipient = mock(TransferRecipient.class);
        List<TransferRecipient> recipients = new ArrayList<TransferRecipient>();
        recipients.add(recipient);
        TransferRequest tr = mock(TransferRequest.class);

        when(source.getAvailableBalance()).thenReturn(BigInteger.valueOf(100));
        when(source.getTotalBalance()).thenReturn(BigInteger.valueOf(100));
        when(destination.getAvailableBalance()).thenReturn(BigInteger.valueOf(0));
        when(destination.getTotalBalance()).thenReturn(BigInteger.valueOf(0));
        when(recipient.getTargetAccountVisualId()).thenReturn("dst");
        when(recipient.getTransferAmount()).thenReturn(BigInteger.valueOf(50));
        when(transferRequestDao.findById(1)).thenReturn(tr);
        when(tr.getSourceAccountVisualId()).thenReturn("src");
        when(tr.getRecipients()).thenReturn(recipients);
        when(accountDao.findByVisualIdOrNull("src")).thenReturn(source);
        when(accountDao.findByVisualIdOrNull("dst")).thenReturn(destination);

        sut.createTransfer(1, "src");

        verify(source, times(1)).modifyAvailable(BigInteger.valueOf(-50));
        verify(source, times(0)).modifyTotal(any());
        verify(destination, times(0)).modifyAvailable(any());
        verify(destination, times(0)).modifyTotal(any());
    }

    @Test(expected = TransferDeniedException.class)
    public void createTransfer_unapproved_throws() throws Exception
    {
        doThrow(TransferDeniedException.class).when(approvalService).requireApprovedOrThrow(1, "ETH:0x1");
        sut.createTransfer(1, "ETH:0x1");
    }

    @Test(expected = TransferDeniedException.class)
    public void createTransfer_invalidAmounts_throws() throws Exception
    {
        Account source = mock(Account.class);
        Account destination = mock(Account.class);
        TransferRecipient recipient = mock(TransferRecipient.class);
        List<TransferRecipient> recipients = new ArrayList<TransferRecipient>();
        recipients.add(recipient);
        TransferRequest tr = mock(TransferRequest.class);

        when(source.getAvailableBalance()).thenReturn(BigInteger.valueOf(0));
        when(source.getTotalBalance()).thenReturn(BigInteger.valueOf(0));
        when(destination.getAvailableBalance()).thenReturn(BigInteger.valueOf(0));
        when(destination.getTotalBalance()).thenReturn(BigInteger.valueOf(0));
        when(recipient.getTargetAccountVisualId()).thenReturn("dst");
        when(recipient.getTransferAmount()).thenReturn(BigInteger.valueOf(50));
        when(transferRequestDao.findById(1)).thenReturn(tr);
        when(tr.getSourceAccountVisualId()).thenReturn("src");
        when(tr.getRecipients()).thenReturn(recipients);
        when(accountDao.findByVisualIdOrNull("src")).thenReturn(source);
        when(accountDao.findByVisualIdOrNull("dst")).thenReturn(destination);

        sut.createTransfer(1, "src");
    }

    @Test
    public void finalizeTransfer_confirmPendingTransfer_adjustsTotalAndAvailableCorrectly() throws Exception
    {
        Account source = mock(Account.class);
        Account destination = mock(Account.class);
        TransferRecipient recipient = mock(TransferRecipient.class);
        List<TransferRecipient> recipients = new ArrayList<TransferRecipient>();
        recipients.add(recipient);
        TransferRequest tr = mock(TransferRequest.class);
        Transfer transfer = mock(Transfer.class);
        when(transfer.getTransferRequestId()).thenReturn(1);
        when(transferDao.findById(1)).thenReturn(transfer);
        when(source.getAvailableBalance()).thenReturn(BigInteger.valueOf(50));
        when(source.getTotalBalance()).thenReturn(BigInteger.valueOf(100));
        when(destination.getAvailableBalance()).thenReturn(BigInteger.valueOf(0));
        when(destination.getTotalBalance()).thenReturn(BigInteger.valueOf(0));
        when(recipient.getTargetAccountVisualId()).thenReturn("dst");
        when(recipient.getTransferAmount()).thenReturn(BigInteger.valueOf(50));
        when(transferRequestDao.findById(1)).thenReturn(tr);
        when(tr.getSourceAccountVisualId()).thenReturn("src");
        when(tr.getRecipients()).thenReturn(recipients);
        when(accountDao.findByVisualIdOrNull("src")).thenReturn(source);
        when(accountDao.findByVisualIdOrNull("dst")).thenReturn(destination);

        sut.finalizeTransfer(1);

        verify(source, times(0)).modifyAvailable(any());
        verify(source, times(1)).modifyTotal(BigInteger.valueOf(-50));
        verify(destination, times(1)).modifyAvailable(BigInteger.valueOf(50));
        verify(destination, times(1)).modifyTotal(BigInteger.valueOf(50));
    }

    @Test(expected = TransferDeniedException.class)
    public void finalizeTransfer_confirmed_throws() throws Exception
    {
        Transfer transfer = mock(Transfer.class);
        when(transferDao.findById(1)).thenReturn(transfer);
        when(transfer.isConfirmed()).thenReturn(true);

        sut.finalizeTransfer(1);
    }
}
