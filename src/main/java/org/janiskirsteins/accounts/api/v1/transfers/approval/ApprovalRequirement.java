package org.janiskirsteins.accounts.api.v1.transfers.approval;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.lang.model.util.ElementScanner6;

import org.janiskirsteins.accounts.api.model_base.BaseModel;

public class ApprovalRequirement extends BaseModel
{
    RequiredApprovalType requiredApprovalType;
    int transferRequestId;
    String challenge;
    String response;

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

    public String getResponse() {
		return this.response;
    }

	public enum RequiredApprovalType
    {
        Debug_PutAPPROVEDInResponse,
        AND_AllFromChallengeMustBeApproved,
        OR_OneFromChallengeMustBeApproved,
        SignedEthereumTransaction,
        TOTP_RFC6238
    }
}


// abstract class ApprovalRequirement extends BaseModel
// {
//     RequiredApprovalType requiredApprovalType;
//     ApprovalRequestType approvalRequestType;
//     Collection<ApprovalRequirement> childRequirements;

//     public ApprovalRequirement(
//         RequiredApprovalType requiredApprovalType,
//         ApprovalRequestType approvalRequestType,
//         Collection<ApprovalRequirement> childRequirements)
//     {
//         this.requiredApprovalType = requiredApprovalType;
//         this.approvalRequestType = approvalRequestType;
//         this.childRequirements = childRequirements;
//     }

//     public ApprovalStatus calculateAggregateStatus()
//     {
//         if (this.approvalRequestType == ApprovalRequestType.Individual)
//         {
//             return this.calculateSelfStatus();
//         }
//         else
//         {
//             if (this.requiredApprovalType != RequiredApprovalType.None)
//             {
//                 throw new UnsupportedOperationException("Invalid state. Approval requirement groups must require .None as their individual approval type.");
//             }

//             if (this.childRequirements == null || this.childRequirements.isEmpty())
//             {
//                 return ApprovalStatus.ApprovalFine_CanProceed;
//             }

//             Stream<ApprovalStatus> childAggregateStatuses = childRequirements.stream().map(child -> child.calculateAggregateStatus()).distinct();

//             boolean permanentlyDenied = childAggregateStatuses.anyMatch(item -> item == ApprovalStatus.ApprovalDenied_WillNotProceed);
//             boolean allFine = childAggregateStatuses.allMatch(item -> item == ApprovalStatus.ApprovalFine_CanProceed);

//             if (permanentlyDenied)
//             {
//                 return ApprovalStatus.ApprovalDenied_WillNotProceed;
//             }
//             else if (allFine)
//             {
//                 return ApprovalStatus.ApprovalFine_CanProceed;
//             }

//             return ApprovalStatus.PendingResolution;
//         }
//     }

//     public abstract ApprovalStatus calculateSelfStatus();

//     public abstract ApprovalStatus processPayload(byte[] payload);

//     public enum ApprovalStatus
//     {
//         PendingResolution,
//         ApprovalFine_CanProceed,
//         ApprovalDenied_WillNotProceed
//     }

//     public enum RequiredApprovalType
//     {
//         None, // for groups
//         SimpleApproval,
//         TOTP_RFC6238
//     }

//     public enum ApprovalRequestType
//     {
//         GroupAnd,
//         GroupOr,
//         Individual
//     }
// }
