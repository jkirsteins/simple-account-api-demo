package org.janiskirsteins.accounts.api;

import static spark.Spark.*;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import org.janiskirsteins.accounts.api.v1.routes.RootAccountRoutes;

import spark.servlet.SparkApplication;

/**
 * Hello world!
 */
public class App implements SparkApplication
{
    @Inject
    private RootAccountRoutes rootAccountRoutes;

    public static void mainWithOverrides( String[] args, Module... overrides )
    {
        Injector injector = Guice.createInjector(Modules.override(new ProductionDependencyModule()).with(overrides));

        App app = injector.getInstance(App.class);
        app.init();
    }

    public static void main( String[] args )
    {
        mainWithOverrides(args);
    }

    @Override
	public void init() {
        path("/api/v1", () -> {
            path(rootAccountRoutes.ROOT, rootAccountRoutes::populatePath);
            get("/test", (r,re)->"testing2");
        });
	}
}

