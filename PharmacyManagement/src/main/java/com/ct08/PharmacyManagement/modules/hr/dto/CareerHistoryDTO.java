package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareerHistoryDTO {
    private Integer id;
    private Integer employeeId;
    private String employeeName;
    private CareerChanges.ChangeType changeType;
    private BigDecimal oldSalary;
    private BigDecimal newSalary;
    private String oldPositionName;
    private String newPositionName;
    private LocalDate effectiveDate;
    private CareerChanges.ApprovalStatus status;
    private String reason;
    private Integer proposedById;
    private String proposedByName;
    private Integer approvedById;
    private String approvedByName;
    private String approvalReason;

    public static CareerHistoryDTO fromEntity(CareerChanges entity) {
        CareerHistoryDTO dto = new CareerHistoryDTO();
        dto.setId(entity.getId());

        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
            dto.setEmployeeName(entity.getEmployee().getLastName() + " " + entity.getEmployee().getFirstName());
        }

        dto.setChangeType(entity.getChangeType());
        dto.setOldSalary(entity.getOldSalary());
        dto.setNewSalary(entity.getNewSalary());
        dto.setOldPositionName(entity.getOldPosition() != null ? entity.getOldPosition().getPositionName() : null);
        dto.setNewPositionName(entity.getNewPosition() != null ? entity.getNewPosition().getPositionName() : null);
        dto.setEffectiveDate(entity.getEffectiveDate());
        dto.setStatus(entity.getStatus());
        dto.setReason(entity.getReason());

        if (entity.getProposedBy() != null) {
            dto.setProposedById(entity.getProposedBy().getId());
            dto.setProposedByName(entity.getProposedBy().getEmployee() != null
                    ? entity.getProposedBy().getEmployee().getLastName() + " "
                            + entity.getProposedBy().getEmployee().getFirstName()
                    : entity.getProposedBy().getUsername());
        }

        if (entity.getApprovedBy() != null) {
            dto.setApprovedById(entity.getApprovedBy().getId());
            dto.setApprovedByName(entity.getApprovedBy().getEmployee() != null
                    ? entity.getApprovedBy().getEmployee().getLastName() + " "
                            + entity.getApprovedBy().getEmployee().getFirstName()
                    : entity.getApprovedBy().getUsername());
        }

        dto.setApprovalReason(entity.getApprovalReason());

        return dto;
    }
}
