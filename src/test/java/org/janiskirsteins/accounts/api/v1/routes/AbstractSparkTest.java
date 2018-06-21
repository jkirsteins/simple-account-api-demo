package org.janiskirsteins.accounts.api.v1.routes;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.janiskirsteins.accounts.api.App;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import spark.Spark;

public abstract class AbstractSparkTest
{
    App app = null;

    @Before
    public void startServer() throws Exception
    {
        String[] args = {};
        app = App.mainWithOverrides(args, new Module() {
            @Override
            public void configure(Binder binder) {
                bindDependencies(binder);
            }
        });
        Spark.init();
        Spark.awaitInitialization();
    }

    @After
    public void stopServer() throws InterruptedException
    {
        /* A proper solution is to use the (as yet unreleased) version with with: https://github.com/perwendel/spark/pull/730
           and then simply use awaitStop()

           The server is stopped in a background thread, so there is no great way to know when it is stopped (so it can be restarted).*/
        Spark.stop();
        Thread.sleep(2000);

        app.destroy();
        app = null;
    }

    protected String url(String path) throws MalformedURLException {
        return new URL("http", "localhost", 4567, path).toString();
    }

	abstract protected void bindDependencies(Binder binder);
}