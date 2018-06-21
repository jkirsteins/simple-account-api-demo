package org.janiskirsteins.accounts.api;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import org.janiskirsteins.accounts.api.v1.ProductionDependencyModule;
import org.janiskirsteins.accounts.api.v1.accounts.AccountRoutesV1;

import spark.servlet.SparkApplication;

/**
 * Hello world!
 */
public class App implements SparkApplication
{
    @Inject
    private AccountRoutesV1 rootAccountRoutes;

    public static App mainWithOverrides( String[] args, Module... overrides )
    {
        Injector injector = Guice.createInjector(Modules.override(new ProductionDependencyModule()).with(overrides));

        App app = injector.getInstance(App.class);
        app.init();

        return app;
    }

    public static void main( String[] args )
    {
        mainWithOverrides(args);
    }

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

