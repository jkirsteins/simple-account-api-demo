// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.model_base;

import java.util.Collection;
import java.util.HashMap;

/**
 * A simple generic model base class for in-memory persistance.
 *
 * It defines a primaryKey field, which is shared by all child classes, and
 * nothing else.
 */
public class BaseDAO<T extends BaseModel> implements GenericDAO<T>
{
    private int nextPrimaryKey = 1;
	private final HashMap<Integer, T> data = new HashMap<Integer, T>();

    @Override
	public Collection<T> all() {
		return data.values();
    }

	@Override
    public void insert(T newObject) throws UnsupportedOperationException
    {
        if (newObject.getPrimaryKey() != -1)
        {
            throw new UnsupportedOperationException("Primary key already exists.");
        }

        newObject.primaryKey = nextPrimaryKey++;
        data.put(newObject.primaryKey, newObject);
    }

	@Override
	public T findById(int primaryKey) {
        return data.values().stream().filter(item -> item.primaryKey == primaryKey).findAny().orElse(null);
	}

	@Override
	public void update(T object) {

	}
}
