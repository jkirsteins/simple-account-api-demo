package org.janiskirsteins.accounts.api.model_base;

import java.util.Collection;

public interface GenericDAO<T extends BaseModel>
{
    Collection<T> all();
    void insert(T newObject);
    void update(T object);
    T findById(int primaryKey);
}
