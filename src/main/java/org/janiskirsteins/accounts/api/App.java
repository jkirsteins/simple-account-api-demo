package org.janiskirsteins.accounts.api;

import static spark.Spark.*;

import spark.servlet.SparkApplication;

/**
 * Hello world!
 */
public class App implements SparkApplication
{
    public static void main( String[] args )
    {
        new App().init();
    }

	@Override
	public void init() {
		path("/api/v1", () -> {
            get("/hello", (a,b) -> "Hello API v1");
        });
	}
}

