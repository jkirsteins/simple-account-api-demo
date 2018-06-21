// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.accounts;

import java.util.Arrays;
import java.util.List;

import org.janiskirsteins.accounts.api.model_base.BaseCreateRequest;
import org.janiskirsteins.accounts.api.model_base.GenericCreateRequest;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;
import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import spark.Response;

/**
 * POST request for creating accounts.
 *
 * When a POST request is sent to e.g. an account creation route (/account/)
 * then the request contents are deserialized from JSON To this object.
 *
 * Then it is passed to ApiResponse for processing and generating a response, which
 * is returned to the API client.
 *
 * @see org.janiskirsteins.accounts.api.v1.ApiResponse#responseFromCreateRequestInTransaction(DataStoreConcurrencyScheduler, Response, GenericCreateRequest)
 */
public class CreateAccountPOSTRequest extends BaseCreateRequest<Account>
{
    private String tickerSymbol;
	private String name;

	private AccountDAO accountDao;

    /**
     * Constructor
     *
     * @param name
     * @param tickerSymbol
     */
    public CreateAccountPOSTRequest(String name, String tickerSymbol)
    {
        this.tickerSymbol = tickerSymbol;
        this.name = name;
    }

	/**
	 * @return the tickerSymbol
	 */
	public String getTickerSymbol() {
		return tickerSymbol;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

    /**
     * This function allows for dependency injection, which can not happen
     * through the constructor, because instances of this class are deserialized
     * automatically from JSON.
     *
     * @param accountDao
     */
	public void prepareForValidation(AccountDAO accountDao)
    {
        this.accountDao = accountDao;
    }

    /**
     * This should not be invoked directly. It assumes invocation
     * within a transaction.
     *
     * @see BaseCreateRequest#validateWithinTransaction()
     * @throws InvalidRequestException when accountDao is missing
     * @throws InvalidRequestException when name is null
     * @throws InvalidRequestException when the specified ticker is not one of [ETH, GBP, EUR, USD]
     */
	@Override
	public void validateWithinTransaction() throws InvalidRequestException
	{
		if (accountDao == null)
		{
			throw new InvalidRequestException("#prepareForValidation not caled with valid parameters.", 500);
		}

		if (name == null)
		{
			throw new InvalidRequestException("Missing name");
		}

		List<String> allowedTickers = Arrays.asList("ETH", "GBP", "EUR", "USD");
		if (!allowedTickers.contains(tickerSymbol))
		{
			throw new InvalidRequestException("Missing or invalid tickerSymbol");
		}
	}

    /**
     * This should not be invoked directly. It assumes invocation
     * within a transaction.
     *
     * @see BaseCreateRequest#createWithinTransaction()
     * @return
     */
	@Override
	public Account createWithinTransaction() {
		Account result = new Account(tickerSymbol, name);
		accountDao.insert(result);
		return result;
	}
}