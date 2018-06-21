package org.janiskirsteins.accounts.api.model_base;

public interface GenericCreateRequest<T extends BaseModel>
{
	T validateAndCreateWithinTransaction() throws InvalidRequestException;
}