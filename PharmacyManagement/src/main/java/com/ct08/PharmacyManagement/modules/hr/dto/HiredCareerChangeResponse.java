package com.ct08.PharmacyManagement.modules.hr.dto;

import com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HiredCareerChangeResponse {
    private Integer id;
    private Integer employeeId;
    private String employeeName;
    private String positionName;
    private LocalDate effectiveDate;
    private BigDecimal newSalary;
    private String status;
    private String reason;
    private String proposedByName;
    private LocalDateTime createdAt;

    public static HiredCareerChangeResponse fromEntity(CareerChanges careerChange) {
        HiredCareerChangeResponse response = new HiredCareerChangeResponse();
        response.setId(careerChange.getId());

        if (careerChange.getEmployee() != null) {
            response.setEmployeeId(careerChange.getEmployee().getId());
            response.setEmployeeName(
                    careerChange.getEmployee().getLastName() + " " + careerChange.getEmployee().getFirstName());
        }

        if (careerChange.getNewPosition() != null) {
            response.setPositionName(careerChange.getNewPosition().getPositionName());
        }

        response.setEffectiveDate(careerChange.getEffectiveDate());
        response.setNewSalary(careerChange.getNewSalary());
        response.setStatus(careerChange.getStatus() != null ? careerChange.getStatus().name() : null);
        response.setReason(careerChange.getReason());

        if (careerChange.getProposedBy() != null) {
            response.setProposedByName(careerChange.getProposedBy().getEmployee() != null
                    ? careerChange.getProposedBy().getEmployee().getLastName() + " "
                            + careerChange.getProposedBy().getEmployee().getFirstName()
                    : careerChange.getProposedBy().getUsername());
        }

        response.setCreatedAt(careerChange.getCreatedAt());

        return response;
    }
}
