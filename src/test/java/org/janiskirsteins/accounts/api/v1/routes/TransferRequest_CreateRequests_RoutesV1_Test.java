package org.janiskirsteins.accounts.api.v1.routes;

import org.janiskirsteins.accounts.api.App;
import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.accounts.InMemoryAccountDAO;
import org.janiskirsteins.accounts.api.v1.transfers.TransferRequestDAO;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import net.javacrumbs.jsonunit.core.Configuration;

import static net.javacrumbs.jsonunit.JsonAssert.*;
import static net.javacrumbs.jsonunit.core.Option.*;

import static org.mockito.Mockito.*;

import spark.Spark;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.mashape.unirest.http.Unirest;


/**
 * Test for /api/v1/account/:account_id/transfer_request
 */
public class TransferRequest_CreateRequests_RoutesV1_Test extends AbstractSparkTest
{
    @Mock
    AccountDAO mockAccountDao;

    @Mock
    TransferRequestDAO transferRequestDao;

    @Override
    protected void bindDependencies(Binder binder)
    {
        MockitoAnnotations.initMocks(this);

        binder.bind(AccountDAO.class).toInstance(mockAccountDao);
        binder.bind(TransferRequestDAO.class).toInstance(transferRequestDao);
    }

    @Test
    public void testCreate_postTransferToInvalidAccount_receive404() throws Exception
    {
        String response = Unirest.post(url("/api/v1/account/invalid_account/transfer_request/")).body("{}").asString().getBody();

        assertJsonEquals("{\"statusCode\":404,\"message\":\"Invalid source account ID\"}", response);
    }

    @Test
    public void testCreate_postTransfer_missingRecipientAccounts_receive400() throws Exception
    {
        Account mockAccount = mock(Account.class);
        when(mockAccountDao.findByVisualIdOrNull("abc")).thenReturn(mockAccount);

        String response = Unirest.post(url("/api/v1/account/abc/transfer_request/")).body("{}").asString().getBody();

        assertJsonEquals("{\"statusCode\":400,\"message\":\"Missing or empty transfer recipient list\"}", response);
    }

    @Test
    public void testCreate_postTransfer_invalidRecipientAccount_receive400() throws Exception
    {
        Account mockAccount = mock(Account.class);
        when(mockAccountDao.findByVisualIdOrNull("abc")).thenReturn(mockAccount);

        String response = Unirest.post(url("/api/v1/account/abc/transfer_request/")).body("{recipients:[{transferAmount: 0, targetAccountVisualId: \"abc2\"}]}").asString().getBody();

        assertJsonEquals("{\"statusCode\":400,\"message\":\"Invalid transfer recipient abc2\"}", response);
    }

    @Test
    public void testCreate_postTransfer_twoRecipientsExceedingAvailableAmount_receive400() throws Exception
    {
        Account mockAccount = mock(Account.class);
        when(mockAccountDao.findByVisualIdOrNull("abc")).thenReturn(mockAccount);
        when(mockAccount.getAvailableBalance()).thenReturn(BigInteger.valueOf(50));

        String response = Unirest.post(url("/api/v1/account/abc/transfer_request/")).body("{recipients:[{transferAmount: 1, targetAccountVisualId: \"abc\"}, {transferAmount: 50, targetAccountVisualId: \"abc\"}]}").asString().getBody();

        assertJsonEquals("{\"statusCode\":400,\"message\":\"Total transfer amount (51) exceeds the available balance (50).\"}", response);
    }

    @Test
    public void testCreate_postTransfer_twoRecipientsExceedingAvailableAmount_allowOverdraft_receive200() throws Exception
    {
        Account mockAccount = mock(Account.class);
        when(mockAccountDao.findByVisualIdOrNull("abc")).thenReturn(mockAccount);
        when(mockAccount.getAvailableBalance()).thenReturn(BigInteger.valueOf(50));

        String response = Unirest.post(url("/api/v1/account/abc/transfer_request/")).body("{allowOverdraft: true, recipients:[{transferAmount: 1, targetAccountVisualId: \"abc\"}, {transferAmount: 50, targetAccountVisualId: \"abc\"}]}").asString().getBody();

        assertJsonEquals("{\"statusCode\":200}", response, net.javacrumbs.jsonunit.JsonAssert.when(IGNORING_EXTRA_FIELDS));
    }
}
