package org.janiskirsteins.accounts.api.accounts;

public class Account
{
    private int ownerId;
    private String tickerSymbol;

    /**
	 * @return the tickerSymbol
	 */
	public String getTickerSymbol() {
		return tickerSymbol;
    }

	/**
	 * @param tickerSymbol the tickerSymbol to set
	 */
	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}
}
