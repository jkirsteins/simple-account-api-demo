// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api;

import static spark.Spark.*;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import org.janiskirsteins.accounts.api.v1.ProductionDependencyModule;
import org.janiskirsteins.accounts.api.v1.accounts.routes.v1.AccountRoutesV1;

import spark.servlet.SparkApplication;

/**
 * A JavaSpark application, which can be run standalone, or in a servlet.
 *
 * It exposes a simple API for transferring funds between accounts.
 */
public class App implements SparkApplication
{
    @Inject
    private AccountRoutesV1 rootAccountRoutes;

    /**
     * This entrypoint allows overriding Guice modules, before the application
     * is started.
     *
     * It does nothing when run normally, but it can be used in e.g. tests, to inject
     * mocked dependencies.
     *
     * @param args application arguments
     * @param overrides Guice dependency modules, which will be used as overrides to the default modules
     * @return Instantiated application instance
     */
    public static App mainWithOverrides( String[] args, Module... overrides )
    {
        Injector injector = Guice.createInjector(Modules.override(new ProductionDependencyModule()).with(overrides));

        App app = injector.getInstance(App.class);
        app.init();

        return app;
    }

    /**
     * Application entrypoint
     *
     * @param args application arguments
     */
    public static void main( String[] args )
    {
        mainWithOverrides(args);
    }

    /**
     * The Spark route initializer.
     */
    @Override
	public void init() {
        path("/api/v1", () -> {
            path(rootAccountRoutes.ROOT, rootAccountRoutes::populatePath);
        });

        // testing splat and params
        path("/test/:value_a", () -> {
            get("/second/:value_b", (req,resp) -> {
                return String.format("A: %s; B: %s", req.params(":value_a"), req.params(":value_b"));
            });
        });
	}
}

