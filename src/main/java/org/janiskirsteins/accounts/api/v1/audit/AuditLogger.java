// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.audit;

/**
 * Simple interface to demonstrate the need for audit logging.
 */
public interface AuditLogger
{
    /**
     * Logs an event for later auditing.
     *
     * This could invoke an auditing API, or do more-elaborate logging than a regular logfile (e.g.
     * it could write to a log where each entry is cryptographically linked with the previous
     * entry. To avoid tampering).
     *
     * @param type type of event
     * @param message message to log
     */
    void logAuditEvent(EventType type, String message);

    /**
     * Audit log event types.
     */
    enum EventType
    {
        /**
         * An event which can be used when a more specific event type is not defined.
         */
        Generic
    }
}
