// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.model_base;

/**
 * A base implementation of GenericCreateRequest<T>, which allows
 * child classes to override separate validation and creation methods.
 *
 * @see GenericCreateRequest
 * @param <T>
 */
public abstract class BaseCreateRequest<T extends BaseModel> implements GenericCreateRequest<T>
{
    /**
     * Validates the request and creates a resource. Assumes this is called within a transaction.
     *
     * The resulting value should be persisted in the data store.
     *
     * @return The newly created resource instance.
     * @throws InvalidRequestException if the request did not pass validation.
     */
    @Override
    public T validateAndCreateWithinTransaction() throws InvalidRequestException
    {
        validateWithinTransaction();
        try
        {
			return createWithinTransaction();
        }
        catch (Exception e)
        {
			throw new InvalidRequestException("Creation of a resource failed (validation was inadequate)", 500, e);
		}
    }

    protected abstract void validateWithinTransaction() throws InvalidRequestException;
    protected abstract T createWithinTransaction() throws Exception;
}