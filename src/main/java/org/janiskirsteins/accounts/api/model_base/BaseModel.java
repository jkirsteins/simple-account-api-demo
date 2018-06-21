// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.model_base;

/**
 * A simple generic model base class for in-memory persistance.
 *
 * It defines a primaryKey field, which is shared by all child classes, and
 * nothing else.
 */
public abstract class BaseModel
{
    int primaryKey = -1;

    public int getPrimaryKey() {
        return primaryKey;
    }
}
