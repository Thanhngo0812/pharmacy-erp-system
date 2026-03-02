package com.ct08.PharmacyManagement.modules.hr.scheduler;

import com.ct08.PharmacyManagement.modules.hr.entity.CareerChanges;
import com.ct08.PharmacyManagement.modules.hr.repository.CareerChangesRepository;
import com.ct08.PharmacyManagement.modules.hr.repository.EmployeesRepository;
import com.ct08.PharmacyManagement.modules.hr.service.CareerChangesService;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class CareerChangeScheduler {

    @Autowired
    private CareerChangesRepository careerChangesRepository;

    @Autowired
    private CareerChangesService careerChangesService;

    @Autowired
    private EmployeesRepository employeesRepository;

    /**
     * Chạy định kỳ vào 00:00:00 mỗi ngày.
     * Quét các yêu cầu thay đổi đã duyệt (Approved), chưa áp dụng (isApplied =
     * false),
     * và có ngày hiệu lực (effectiveDate) <= ngày hiện tại để áp dụng.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(fixedDelay = 10000)
    @Transactional
    public void applyScheduledCareerChanges() {
        log.info("Starting scheduled job: applyScheduledCareerChanges",
                StructuredArguments.kv("TYPE_LOG", "SCHEDULER"));
        LocalDate today = LocalDate.now();

        List<CareerChanges> pendingChanges = careerChangesRepository
                .findByStatusAndIsAppliedFalseAndEffectiveDateLessThanEqual(
                        CareerChanges.ApprovalStatus.Approved, today);

        if (pendingChanges.isEmpty()) {
            log.info("No scheduled career changes to apply today.",
                    StructuredArguments.kv("TYPE_LOG", "SCHEDULER"));
            return;
        }

        log.info("Found {} career changes to apply.", pendingChanges.size(),
                StructuredArguments.kv("TYPE_LOG", "SCHEDULER"));

        for (CareerChanges change : pendingChanges) {
            try {
                log.info("Applying career change ID: {} for employee ID: {}", change.getId(),
                        change.getEmployee().getId(),
                        StructuredArguments.kv("TYPE_LOG", "SCHEDULER"));

                careerChangesService.applyCareerChange(change.getEmployee(), change);
                change.setIsApplied(true);

                careerChangesRepository.save(change);
                employeesRepository.save(change.getEmployee());

                log.info("Successfully applied career change ID: {}", change.getId(),
                        StructuredArguments.kv("TYPE_LOG", "SCHEDULER"));
            } catch (Exception e) {
                log.error("Failed to apply career change ID: {}. Error: {}", change.getId(), e.getMessage(),
                        StructuredArguments.kv("TYPE_LOG", "SCHEDULER"), e);
            }
        }

        log.info("Finished scheduled job: applyScheduledCareerChanges",
                StructuredArguments.kv("TYPE_LOG", "SCHEDULER"));
    }
}
