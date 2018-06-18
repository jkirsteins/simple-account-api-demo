package org.janiskirsteins.accounts.api;

import org.junit.*;
import spark.Spark;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import com.mashape.unirest.http.Unirest;



/**
 * Unit test for simple App.
 */
public class AppTest
{
    @BeforeClass
    public static void startServer() {
        String[] args = {};
        App.main(args);
    }

    @AfterClass
    public static void stopServer() {
        Spark.stop();
    }

    @Before
    public void setUp() throws Exception {

    }

    private String url(String path) throws MalformedURLException {
        return new URL("http", "localhost", 4567, path).toString();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Rigourous Test :-)
     */
    @Test
    public void testApp() throws Exception
    {
        String body = Unirest.get(url("/hello")).asString().getBody();
        assertEquals("Hello World", body);
    }
}
