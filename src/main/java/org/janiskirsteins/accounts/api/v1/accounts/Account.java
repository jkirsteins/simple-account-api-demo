// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.accounts;

import java.math.BigInteger;

import org.janiskirsteins.accounts.api.model_base.BaseModel;

/**
 * Simple account model.
 *
 * It features a denominatino (tickerSymbol), a name, and
 * has two types of balances - available/total.
 *
 * The balances should be governed by the TransferService, and this model
 * only exposes methods for changing them (but does not enforce any rules).
 */
public class Account extends BaseModel
{
	private String tickerSymbol;
	private String name;
	private BigInteger availableBalance;
	private BigInteger totalBalance;

	public Account(String tickerSymbol, String name)
	{
		this(tickerSymbol, name, BigInteger.valueOf(0), BigInteger.valueOf(0));
	}

	public Account(String tickerSymbol, String name, BigInteger totalBalance)
	{
		this(tickerSymbol, name, totalBalance, totalBalance);
	}

    public Account(String tickerSymbol, String name, BigInteger availableBalance, BigInteger totalBalance)
    {
		this.name = name;
		this.tickerSymbol = tickerSymbol;
		this.availableBalance = availableBalance;
		this.totalBalance = totalBalance;
    }

	public Account(CreateAccountPOSTRequest createRequest)
	{
		this(createRequest.getName(), createRequest.getTickerSymbol());
	}

	/**
	 * @return the totalBalance
	 */
	public BigInteger getTotalBalance() {
		return totalBalance;
	}

	/**
	 * @return the availableBalance
	 */
	public BigInteger getAvailableBalance() {
		return availableBalance;
	}

	/**
	 * @return the tickerSymbol
	 */
	public String getTickerSymbol() {
		return tickerSymbol;
    }

	/**
	 * @return the visualId
	 */
	public String getVisualId() {
		return String.format("%s:%s", tickerSymbol, name);
	}

	/**
	 * @param tickerSymbol the tickerSymbol to set
	 */
	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	public void modifyAvailable(BigInteger difference)
	{
		this.availableBalance.add(difference);
	}

	public void modifyTotal(BigInteger difference)
	{
		this.totalBalance.add(difference);
	}
}
