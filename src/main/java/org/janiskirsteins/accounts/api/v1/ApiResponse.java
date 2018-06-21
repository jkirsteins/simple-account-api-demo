package org.janiskirsteins.accounts.api.v1;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.janiskirsteins.accounts.api.model_base.BaseModel;
import org.janiskirsteins.accounts.api.model_base.GenericCreateRequest;
import org.janiskirsteins.accounts.api.model_base.GenericDAO;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;
import org.janiskirsteins.accounts.api.v1.accounts.CreateAccountPOSTRequest;

import spark.Response;

public class ApiResponse
{
    private int statusCode;
    private String message;
    private JsonElement data;

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

    public static ApiResponse respondFromException(Response response, Exception e, int statusCode)
    {
        ApiResponse resultingApiResponse = null;

        resultingApiResponse = new ApiResponse(e.getLocalizedMessage(), statusCode, new Gson().toJsonTree(e.getCause()));

        response.type("application/json");
        response.status(resultingApiResponse.statusCode);
        return resultingApiResponse;
    }

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
}
