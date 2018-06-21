// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers.approval;

/**
 * The possible states of a transfer request.
 */
public enum ApprovalStatus
{
    PendingResolution,
    ApprovalFine_CanProceed,
    ApprovalDenied_WillNotProceed
}
