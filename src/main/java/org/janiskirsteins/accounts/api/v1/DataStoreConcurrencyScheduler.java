package org.janiskirsteins.accounts.api.v1;

public interface DataStoreConcurrencyScheduler
{
    public void startTransaction() throws InterruptedException;
    public void rollbackTransaction();
    public void commitTransaction();
}