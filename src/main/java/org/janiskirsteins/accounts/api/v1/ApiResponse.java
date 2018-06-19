package org.janiskirsteins.accounts.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class ApiResponse
{
    private int statusCode;
    private String message;
    private JsonElement data;

    public static ApiResponse fromResult(Object result)
    {
        if (result == null)
        {
            return new ApiResponse("Resource not found.", 404, null);
        }

        return new ApiResponse(null, 200, new Gson().toJsonTree(result));
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
}
