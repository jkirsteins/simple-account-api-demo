package org.janiskirsteins.accounts.api.model_base;

public abstract class BaseCreateRequest<T extends BaseModel> implements GenericCreateRequest<T>
{
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