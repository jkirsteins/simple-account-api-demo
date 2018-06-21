package org.janiskirsteins.accounts.api.v1.routes;

import static org.junit.Assert.assertEquals;
import static net.javacrumbs.jsonunit.JsonAssert.*;
import static net.javacrumbs.jsonunit.core.Option.*;

import com.google.inject.Binder;
import com.mashape.unirest.http.Unirest;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.accounts.InMemoryAccountDAO;
import org.janiskirsteins.accounts.api.v1.transfers.InMemoryTransferDAO;
import org.janiskirsteins.accounts.api.v1.transfers.InMemoryTransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestDAO;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalRequirement;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.DummyApprovalService;
import org.janiskirsteins.accounts.api.v1.transfers.approval.ApprovalRequirement.RequiredApprovalType;
import org.json.JSONObject;
import org.junit.Test;

import spark.Spark;


/**
 * Test for /api/v1/account/:account_id/transfer_request/ (related to approval)
 */
public class TransferRequest_Approval_RoutesV1_Test extends AbstractSparkTest
{
    AccountDAO accountDao;
    TransferRequestDAO transferRequestDao;
    ApprovalService approvalService;
	InMemoryTransferDAO transferDao;

    @Override
    protected void bindDependencies(Binder binder)
    {
        transferRequestDao = new InMemoryTransferRequestDAO();
        accountDao = new InMemoryAccountDAO();
        transferDao = new InMemoryTransferDAO();
        approvalService = new DummyApprovalService(transferDao, transferRequestDao);

        binder.bind(AccountDAO.class).toInstance(accountDao);
        binder.bind(TransferRequestDAO.class).toInstance(transferRequestDao);
        binder.bind(ApprovalService.class).toInstance(approvalService);
    }

    @Test
    public void testCreate_postNewTransferRequest_pendingApproval() throws Exception
    {
        accountDao.insert(new Account("ETH", "0x1234"));

        Unirest.post(url("/api/v1/account/ETH:0x1234/transfer_request/")).body("{recipients: [{targetAccountVisualId:\"ETH:0x1234\", transferAmount:0}]}").asJson().getBody().getObject();
        String realResponse = Unirest.get(url("/api/v1/account/ETH:0x1234/transfer_request/1/status")).asString().getBody();

        assertJsonEquals("{\"statusCode\":200,\"data\":\"PendingResolution\"}", realResponse);
    }

    @Test
    public void testCreate_postNewTransferRequest_showsApprovalRequirementsFromApprovalService() throws Exception
    {
        accountDao.insert(new Account("ETH", "0x1234"));

        Unirest.post(url("/api/v1/account/ETH:0x1234/transfer_request/")).body("{recipients: [{targetAccountVisualId:\"ETH:0x1234\", transferAmount:0}]}").asJson().getBody().getObject();
        String realResponse = Unirest.get(url("/api/v1/account/ETH:0x1234/transfer_request/1/approval_requirement/")).asString().getBody();

        assertJsonEquals("{\"statusCode\":200,\"data\":[{\"requiredApprovalType\":\"Debug_PutAPPROVEDInResponse\",\"transferRequestId\":1,\"primaryKey\":1}]}", realResponse);
    }

    @Test
    public void testCreate_approveApprovalRequirement_transferRequestStatusApproved() throws Exception
    {
        accountDao.insert(new Account("ETH", "0x1234"));

        Unirest.post(url("/api/v1/account/ETH:0x1234/transfer_request/")).body("{recipients: [{targetAccountVisualId:\"ETH:0x1234\", transferAmount:0}]}").asJson().getBody().getObject();
        Unirest.put(url("/api/v1/account/ETH:0x1234/transfer_request/1/approval_requirement/1")).body("{response: \"APPROVED\"}").asJson().getBody().getObject();

        String realResponse = Unirest.get(url("/api/v1/account/ETH:0x1234/transfer_request/1/status")).asString().getBody();

        assertJsonEquals("{\"statusCode\":200,\"data\":\"ApprovalFine_CanProceed\"}", realResponse);
    }

    @Test
    public void testCreate_failApprovalRequirement_transferRequestStatusDenied() throws Exception
    {
        accountDao.insert(new Account("ETH", "0x1234"));

        Unirest.post(url("/api/v1/account/ETH:0x1234/transfer_request/")).body("{recipients: [{targetAccountVisualId:\"ETH:0x1234\", transferAmount:0}]}").asJson().getBody().getObject();
        Unirest.put(url("/api/v1/account/ETH:0x1234/transfer_request/1/approval_requirement/1")).body("{response: \"NOT_APPROVED\"}").asJson().getBody().getObject();

        String realResponse = Unirest.get(url("/api/v1/account/ETH:0x1234/transfer_request/1/status")).asString().getBody();

        assertJsonEquals("{\"statusCode\":200,\"data\":\"ApprovalDenied_WillNotProceed\"}", realResponse);
    }
}
