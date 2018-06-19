package org.janiskirsteins.accounts.api.accounts;

public class Account
{
    private String tickerSymbol;
    private String visualId;

    public Account(String visualId, String tickerSymbol)
    {
        this.visualId = visualId;
        this.tickerSymbol = tickerSymbol;
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
		return visualId;
	}

	/**
	 * @param visualId the visualId to set
	 */
	public void setVisualId(String visualId) {
		this.visualId = visualId;
	}

	/**
	 * @param tickerSymbol the tickerSymbol to set
	 */
	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}
}
