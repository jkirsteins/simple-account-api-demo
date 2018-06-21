// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.janiskirsteins.accounts.api.model_base.BaseModel;
import org.janiskirsteins.accounts.api.model_base.GenericCreateRequest;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;

import spark.Response;

/**
 * A class that provides a standardized way to respond to API requests.
 *
 * It contains statusCode (should sometimes match the HTTP code, but not necessarily. This code takes precedence)
 * Contains a message (meant purely to be informative to API consumers).
 * Contains optional data (this is either the serialized resource being accessed, or - in case of an error - additional metadata that can help API consumers debug the issue.
 */
public class ApiResponse
{
    private int statusCode;
    private String message;
    private JsonElement data;

    /**
     * Helper method that responds with either a 200 or 404 response, based on
     * the object passed in.
     *
     * @param response Spark response
     * @param result data that is being retrieved. If this is null, the response is "404 not found". Otherwise - 200
     * @return ApiResponse
     */
    public static ApiResponse respondWithResourceOrNull(Response response, Object result)
    {
        ApiResponse resultingApiResponse = null;

        if (result == null)
        {
            resultingApiResponse = new ApiResponse("Resource not found.", 404, null);
        }
        else
        {
            resultingApiResponse = new ApiResponse(null, 200, new Gson().toJsonTree(result));
        }

        response.type("application/json");
        response.status(resultingApiResponse.statusCode);
        return resultingApiResponse;
    }

    /**
     * Helper method for responding based on an exception (it shows the exception message,
     * sets the status code, and optionally shows information about the exception's cause - if any).
     *
     * @param response Spark response
     * @param e exception that triggers the response
     * @param statusCode the required status (e.g. 500 if an unexpected error, or 403 if an access exception etc.)
     * @return
     */
    public static ApiResponse respondFromException(Response response, Exception e, int statusCode)
    {
        ApiResponse resultingApiResponse = null;

        resultingApiResponse = new ApiResponse(e.getLocalizedMessage(), statusCode, new Gson().toJsonTree(e.getCause()));

        response.type("application/json");
        response.status(resultingApiResponse.statusCode);
        return resultingApiResponse;
    }

    /**
     * Helper method for processing POST requests, and responding in a unified manner.
     *
     * It starts a transaction and attempts to create the resource by invoking
     * the processing method on the request instance.
     *
     * If the resource can not be created, it returns an appropriate error response and rolls back the transaction.
     *
     * Otherwise, it commits the transaction.
     *
     * @see GenericCreateRequest#validateAndCreateWithinTransaction()
     * @param concurrencyScheduler
     * @param response
     * @param createRequest
     * @param <T>
     * @return ApiResponse
     */
    public static <T extends BaseModel> ApiResponse responseFromCreateRequestInTransaction(
            DataStoreConcurrencyScheduler concurrencyScheduler,
            Response response,
            GenericCreateRequest<T> createRequest)
    {
        try
        {
            concurrencyScheduler.startTransaction();

            T newObj = createRequest.validateAndCreateWithinTransaction();

            ApiResponse result = ApiResponse.respondWithResourceOrNull(response, newObj);
            concurrencyScheduler.commitTransaction();
            return result;
        }
        catch (InterruptedException e)
        {
            // Failed to start the transaction, so no rollback
            return ApiResponse.respondFromException(response, e, 500);
        }
        catch (Exception e)
        {
            int statusCode = 500;

            if (e instanceof InvalidRequestException)
            {
                statusCode = ((InvalidRequestException)e).getStatusCode();
            }

            concurrencyScheduler.rollbackTransaction();
            return ApiResponse.respondFromException(response, e, statusCode);
        }
    }

    /**
     * Constructor
     *
     * @param message
     * @param statusCode
     * @param data
     */
    public ApiResponse(String message, int statusCode, JsonElement data)
    {
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    /**
	 * @return the statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @return the data
	 */
	public JsonElement getData() {
		return data;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}
