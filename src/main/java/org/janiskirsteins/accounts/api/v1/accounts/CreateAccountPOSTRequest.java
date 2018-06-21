package org.janiskirsteins.accounts.api.v1.accounts;

import java.util.Arrays;
import java.util.List;

import org.janiskirsteins.accounts.api.model_base.BaseCreateRequest;
import org.janiskirsteins.accounts.api.model_base.InvalidRequestException;

public class CreateAccountPOSTRequest extends BaseCreateRequest<Account>
{
    private String tickerSymbol;
	private String name;

	private AccountDAO accountDao;

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

	public void prepareForValidation(AccountDAO accountDao)
    {
        this.accountDao = accountDao;
    }

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

	@Override
	public Account createWithinTransaction() {
		Account result = new Account(tickerSymbol, name);
		accountDao.insert(result);
		return result;
	}
}