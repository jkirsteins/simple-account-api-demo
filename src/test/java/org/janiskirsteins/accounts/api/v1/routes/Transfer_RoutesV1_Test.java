package org.janiskirsteins.accounts.api.v1.routes;

import static org.junit.Assert.assertEquals;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.core.Option.*;

import com.google.inject.Binder;
import com.mashape.unirest.http.Unirest;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.ArrayList;

import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.accounts.InMemoryAccountDAO;
import org.janiskirsteins.accounts.api.v1.transfers.InMemoryTransferDAO;
import org.janiskirsteins.accounts.api.v1.transfers.InMemoryTransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.Transfer;
import org.janiskirsteins.accounts.api.v1.transfers.TransferDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRecipient;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequest;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalRequirement;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.DummyApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.TransferDeniedException;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalRequirement.RequiredApprovalType;
import org.json.JSONObject;
import org.junit.Test;

import spark.Spark;


/**
 * Test for /api/v1/account/:account_id/transfer/
 */
public class Transfer_RoutesV1_Test extends AbstractSparkTest
{
    AccountDAO accountDao;
    TransferRequestDAO transferRequestDao;
    TransferService transferService;
    ApprovalService approvalService;
	TransferDAO transferDao;

    @Override
    protected void bindDependencies(Binder binder)
    {
        transferRequestDao = new InMemoryTransferRequestDAO();
        accountDao = new InMemoryAccountDAO();
        transferDao = new InMemoryTransferDAO();
        transferService = mock(TransferService.class);
        approvalService = mock(ApprovalService.class);

        binder.bind(TransferDAO.class).toInstance(transferDao);
        binder.bind(AccountDAO.class).toInstance(accountDao);
        binder.bind(TransferRequestDAO.class).toInstance(transferRequestDao);
        binder.bind(ApprovalService.class).toInstance(approvalService);
        binder.bind(TransferService.class).toInstance(transferService);
    }

    @Test
    public void testCreate_createTransfer_verifiesApproved_invokesApprovalServiceCreate() throws Exception
    {
        accountDao.insert(new Account("ETH", "0x1234"));

        Unirest.post(url("/api/v1/account/ETH:0x1234/transfer/")).body("{transferRequestId: 1}").asString().getBody();

        verify(approvalService, times(1)).requireApprovedOrThrow(1, "ETH:0x1234");
        verify(transferService, times(1)).createTransfer(1, "ETH:0x1234");
    }

    @Test
    public void testCreate_createTransfer_throwsUnapproved_doesNotInvokeApprovalServiceCreate() throws Exception
    {
        accountDao.insert(new Account("ETH", "0x1234"));
        doThrow(TransferDeniedException.class).when(approvalService).requireApprovedOrThrow(1, "ETH:0x1234");

        Unirest.post(url("/api/v1/account/ETH:0x1234/transfer_request/")).body("{recipients: [{targetAccountVisualId:\"ETH:0x1234\", transferAmount:0}]}").asJson().getBody().getObject();
        String realResponse = Unirest.post(url("/api/v1/account/ETH:0x1234/transfer/")).body("{transferRequestId: 1}").asString().getBody();

        verify(approvalService, times(1)).requireApprovedOrThrow(1, "ETH:0x1234");
        verify(transferService, times(0)).createTransfer(1, "ETH:0x1234");
        assertJsonEquals("{\"statusCode\":400}", realResponse, net.javacrumbs.jsonunit.JsonAssert.when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    public void testShow_getNonExistant_returns404() throws Exception
    {
        accountDao.insert(new Account("ETH", "0x1234"));

        String realResponse = Unirest.get(url("/api/v1/account/ETH:0x1234/transfer/1")).asString().getBody();
        assertJsonEquals("{\"statusCode\":404}", realResponse, net.javacrumbs.jsonunit.JsonAssert.when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    public void testConfirm_putEmptyBody_invokesFinalize() throws Exception
    {
        String realResponse = Unirest.put(url("/api/v1/account/ETH:0x1234/transfer/1")).body("{isConfirmed: true}").asString().getBody();

        verify(transferService, times(1)).finalizeTransfer(1);
        assertJsonEquals("{\"statusCode\":200}", realResponse, net.javacrumbs.jsonunit.JsonAssert.when(IGNORING_EXTRA_FIELDS));
    }

}
