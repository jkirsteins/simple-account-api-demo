package org.janiskirsteins.accounts.api.v1.routes;

import org.janiskirsteins.accounts.api.App;
import org.janiskirsteins.accounts.api.accounts.Account;
import org.janiskirsteins.accounts.api.accounts.AccountDAO;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import spark.Spark;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.mashape.unirest.http.Unirest;


/**
 * Test for /api/v1/account/
 */
public class RootAccountRoutesTest
{
    private static AccountDAO mockAccountDao = Mockito.mock(AccountDAO.class);

    @BeforeClass
    public static void startServer() throws Exception
    {
        String[] args = {};
        App.mainWithOverrides(args, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(AccountDAO.class).toInstance(mockAccountDao);
            }
        });
    }

    @AfterClass
    public static void stopServer() throws Exception {
        Spark.stop();
    }

    @Before
    public void setUp() throws Exception {
        Mockito.reset(mockAccountDao);
    }

    @After
    public void tearDown() throws Exception {
    }

    private String url(String path) throws MalformedURLException {
        return new URL("http", "localhost", 4567, path).toString();
    }

    /**
     * Rigourous Test :-)
     */
    @Test
    public void testIndex_returnsAllFromDao() throws Exception
    {
        List<Account> result = new ArrayList<Account>();
        Account account = new Account("ETH:0x931D387731bBbC988B312206c74F77D004D6B84b", "ETH");
        result.add(account);

        when(mockAccountDao.all()).thenReturn(result);

        String body = Unirest.get(url("/api/v1/account/")).asString().getBody();
        assertEquals("{\"statusCode\":200,\"data\":[{\"tickerSymbol\":\"ETH\",\"visualId\":\"ETH:0x931D387731bBbC988B312206c74F77D004D6B84b\"}]}", body);
    }
}
