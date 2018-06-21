package org.janiskirsteins.accounts.api.v1.transfers;

import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import com.google.inject.Inject;

import org.janiskirsteins.accounts.api.v1.transfers.Transfer;
import org.janiskirsteins.accounts.api.v1.transfers.approval.TransferDeniedException;

public interface TransferService
{
    Transfer createTransfer(int transferRequestId, String sourceAccountVisualId) throws TransferDeniedException, InterruptedException;
    Transfer finalizeTransfer(int transferId) throws TransferDeniedException, InterruptedException;
}
