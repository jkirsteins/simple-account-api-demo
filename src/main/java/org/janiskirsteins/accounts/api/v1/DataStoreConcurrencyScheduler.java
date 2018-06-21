package org.janiskirsteins.accounts.api.v1;

public interface DataStoreConcurrencyScheduler
{
    void startTransaction() throws InterruptedException;
    void rollbackTransaction();
    void commitTransaction();
}