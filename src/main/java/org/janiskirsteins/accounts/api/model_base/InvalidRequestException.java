package org.janiskirsteins.accounts.api.model_base;

public class InvalidRequestException extends Exception
{
	private static final long serialVersionUID = -5233349331872400231L;
	private int statusCode;

	public InvalidRequestException(String message, int statusCode)
	{
		super(message);
		this.statusCode = statusCode;
	}

	public InvalidRequestException(String message, int statusCode, Exception cause)
	{
		super(message, cause);
		this.statusCode = statusCode;
	}

	public InvalidRequestException(String message)
	{
		this(message, 400);
	}

	public int getStatusCode() {
		return this.statusCode;
	}
}