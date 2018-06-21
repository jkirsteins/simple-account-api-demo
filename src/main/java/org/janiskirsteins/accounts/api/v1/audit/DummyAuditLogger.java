// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.audit;

/**
 * Simple AuditLogger implementation, which prints to STDOUT.
 *
 * @see AuditLogger
 */
public class DummyAuditLogger implements AuditLogger
{
    @Override
    public void logAuditEvent(EventType type, String message) {
        System.out.println(String.format("Auditing event %s: %s", type, message));
    }
}
