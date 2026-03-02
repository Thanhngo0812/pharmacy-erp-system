package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.hr.entity.Positions;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PositionResponse {
    private Integer id;
    private String positionName;
    private String status;
    private String reason;
    private String approvalReason;
    private Integer proposedById;
    private String proposedByName;
    private Integer approvedById;
    private String approvedByName;

    public PositionResponse(Positions position) {
        this.id = position.getId();
        this.positionName = position.getPositionName();
        this.status = position.getStatus() != null ? position.getStatus().name() : null;
        this.reason = position.getReason();
        this.approvalReason = position.getApprovalReason();

        if (position.getProposedBy() != null) {
            this.proposedById = position.getProposedBy().getId();
            this.proposedByName = position.getProposedBy().getEmployee() != null
                    ? position.getProposedBy().getEmployee().getLastName() + " "
                            + position.getProposedBy().getEmployee().getFirstName()
                    : position.getProposedBy().getUsername();
        }

        if (position.getApprovedBy() != null) {
            this.approvedById = position.getApprovedBy().getId();
            this.approvedByName = position.getApprovedBy().getEmployee() != null
                    ? position.getApprovedBy().getEmployee().getLastName() + " "
                            + position.getApprovedBy().getEmployee().getFirstName()
                    : position.getApprovedBy().getUsername();
        }
    }
}
