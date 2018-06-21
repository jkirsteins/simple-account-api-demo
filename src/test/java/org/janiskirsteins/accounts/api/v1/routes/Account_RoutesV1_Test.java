package org.janiskirsteins.accounts.api.v1.routes;

import org.janiskirsteins.accounts.api.App;
import org.janiskirsteins.accounts.api.v1.accounts.Account;
import org.janiskirsteins.accounts.api.v1.accounts.AccountDAO;
import org.janiskirsteins.accounts.api.v1.accounts.InMemoryAccountDAO;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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
 * Test for /api/v1/account/
 */
public class Account_RoutesV1_Test extends AbstractSparkTest
{
    @Mock
    AccountDAO mockAccountDao;

    @Override
    protected void bindDependencies(Binder binder)
    {
        MockitoAnnotations.initMocks(this);

        binder.bind(AccountDAO.class).toInstance(mockAccountDao);
	}

    @Test
    public void testCreate_postAccount_createsAccount() throws Exception
    {
        Unirest.post(url("/api/v1/account/")).body("{tickerSymbol: \"ETH\", name: \"0x0000000000000000000000000000000000000000\"}").asString().getBody();

        ArgumentCaptor<Account> argumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(mockAccountDao, times(1)).insert(argumentCaptor.capture());
        Account capturedArgument = argumentCaptor.getValue();
        assertEquals("ETH", capturedArgument.getTickerSymbol());
        assertEquals("0x0000000000000000000000000000000000000000", capturedArgument.getName());
        assertEquals(BigInteger.valueOf(0), capturedArgument.getAvailableBalance());
        assertEquals(BigInteger.valueOf(0), capturedArgument.getTotalBalance());
    }


    @Test
    public void testCreate_getAccountByVisualId_returnsAccountJson() throws Exception
    {
        Account mockAccount = new Account("ETH", "testName");
        when(mockAccountDao.findByVisualIdOrNull("my_visual_id")).thenReturn(mockAccount);

        String responseBody = Unirest.get(url("/api/v1/account/my_visual_id")).asString().getBody();

        verify(mockAccountDao, times(1)).findByVisualIdOrNull("my_visual_id");
        assertJsonEquals("{\"statusCode\":200,\"data\":{\"tickerSymbol\":\"ETH\",\"name\":\"testName\",\"availableBalance\":0,\"totalBalance\":0,\"primaryKey\":-1}}", responseBody);
    }
}
