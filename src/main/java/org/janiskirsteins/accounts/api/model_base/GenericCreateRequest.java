package org.janiskirsteins.accounts.api.model_base;

import org.janiskirsteins.accounts.api.v1.DataStoreConcurrencyScheduler;
import spark.Response;

/**
 * POST requests are deserialized to child classes of this abstract class.
 *
 * Then ApiResponse can use this interface to perform resource creation in a unified
 * manner.
 *
 * @see org.janiskirsteins.accounts.api.v1.ApiResponse#responseFromCreateRequestInTransaction(DataStoreConcurrencyScheduler, Response, GenericCreateRequest)
 * @param <T> the type of resource that will be created
 */
public interface GenericCreateRequest<T extends BaseModel>
{
	T validateAndCreateWithinTransaction() throws InvalidRequestException;
}
