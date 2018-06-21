// © 2018 Janis Kirsteins. Licensed under MIT (see LICENSE.md)
package org.janiskirsteins.accounts.api.v1.transfers.approval;

import org.janiskirsteins.accounts.api.model_base.BaseModel;

/**
 * This class represents a single item of transaction approval requirements.
 *
 * While an internal API could avoid elaborate approval schemes, it could be beneficial
 * to have an approval mechanism that requires outside input for a number of cases. For example:
 *
 * - a non-custodial cryptocurrency account could require a signed transaction from the key holder, to approve
 *   the transaction
 * - a multi-sig account could require multiple users to approve a transaction, before it is allowed through
 * - different approval requirements could be combined. E.g. a transaction goes through if two managers approve
 *   it (multi-sig), OR if the CFO of a company approves it.
 * - multi-factor authentication might require validating a secret in posession of the user
 * - for auditing purposes, certain transfers might require a digital signature by a key held in a hardware security
 *   module
 *
 * And so on.
 *
 * Since approval mechanisms could be pretty complex (and require a lot of work to enable them to be defined and
 * managed), these objects should not be accessed directly, but rather used through an ApprovalService implementation.
 *
 * Each approval requirement has a type, an optional challenge, and an optional response. Challenge/response
 * are optional free-form fields, and the type can help the ApprovalService determine how to validate them.
 *
 * @see ApprovalService
 * @see DummyApprovalService
 */
public class ApprovalRequirement extends BaseModel
{
    RequiredApprovalType requiredApprovalType;
    int transferRequestId;
    String challenge;
    String response;

    /**
     * Constructor for dependency injection.
     *
     * @param transferRequestId
     * @param requiredApprovalType
     * @param challenge
     * @param response
     */
    public ApprovalRequirement(
        int transferRequestId,
        RequiredApprovalType requiredApprovalType,
        String challenge,
        String response)
    {
        this.transferRequestId = transferRequestId;
        this.requiredApprovalType = requiredApprovalType;
        this.challenge = challenge;
        this.response = response;
    }

    public ApprovalRequirement(int transferRequestId, RequiredApprovalType requiredApprovalType)
    {
        this(transferRequestId, requiredApprovalType, null, null);
    }

    public RequiredApprovalType getRequiredApprovalType() {
		return this.requiredApprovalType;
    }

    /**
     * Get the response (submitted by the API client)
     * @return
     */
    public String getResponse() {
		return this.response;
    }

    /**
     * Type of required approval.
     *
     * This value can be used to determine how to process the requirement (by the ApprovalService)
     */
	public enum RequiredApprovalType
    {
        Debug_PutAPPROVEDInResponse,
        AND_AllFromChallengeMustBeApproved,
        OR_OneFromChallengeMustBeApproved,
        SignedEthereumTransaction,
        TOTP_RFC6238
    }
}
