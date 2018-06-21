// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.model_base;

import java.util.Collection;

/**
 * A unified generic interface for data access objects.
 *
 * All implementations can use the same boilerplate by extending
 * from BaseDAO, which implements this interface.
 *
 * @see BaseDAO
 * @param <T>
 */
public interface GenericDAO<T extends BaseModel>
{
    /**
     * @return all known instances of the model.
     */
    Collection<T> all();

    /**
     * Inserts a new object and sets its primary key.
     *
     * @param newObject object to insert into the data store.
     */
    void insert(T newObject);

    /**
     * Update an existing object.
     *
     * NOTE: the dummy in-memory implementations might reflect changes immediately,
     * without having to call this.
     *
     * @param object
     */
    void update(T object);

    /**
     * Finds an object by its primary key.
     *
     * @param primaryKey
     * @return result or null
     */
    T findById(int primaryKey);
}
