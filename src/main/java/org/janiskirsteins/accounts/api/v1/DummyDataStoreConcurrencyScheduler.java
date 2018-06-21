// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1;

/**
 * Dummy (no op) implementation of DataStoreConcurrencyScheduler
 * @see DataStoreConcurrencyScheduler
 */
public class DummyDataStoreConcurrencyScheduler implements DataStoreConcurrencyScheduler
{
    public void startTransaction() {

    }
    public void rollbackTransaction()
    {

    }
    public void commitTransaction()
    {

    }
}