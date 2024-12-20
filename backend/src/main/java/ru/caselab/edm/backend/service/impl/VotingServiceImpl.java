package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.job.VoteResultAggregatorJob;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.ApprovementProcessRepository;
import ru.caselab.edm.backend.service.VotingService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static ru.caselab.edm.backend.enums.ApprovementProcessStatus.VOTING_APPROVED;
import static ru.caselab.edm.backend.enums.ApprovementProcessStatus.VOTING_COMPLETED;
import static ru.caselab.edm.backend.enums.ApprovementProcessStatus.VOTING_REJECTED;

@Service
@RequiredArgsConstructor
@Slf4j
public class VotingServiceImpl implements VotingService {
    private final ApprovementProcessRepository processRepository;
    private final ApprovementItemRepository itemRepository;
    private final Scheduler scheduler;

    @Override
    public void scheduleVotingJob(Long processId, LocalDateTime deadline) {
        log.info("New job for process {} with deadline {}", processId, deadline);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("processId", processId);
        System.out.println(1);
        JobDetail jobDetail = JobBuilder.newJob(VoteResultAggregatorJob.class)
                .usingJobData(jobDataMap)
                .withIdentity("job_" + processId)
                .build();

        ZonedDateTime zonedDateTime = deadline.atZone(ZoneId.of("UTC"));

        Date triggerDate = Date.from(zonedDateTime.toInstant());

        log.info("Job name {}", "job_" + jobDetail.getKey().getName());
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + processId)
                .startAt(triggerDate)
                .build();

        log.info("Trigger name {} data {}", trigger.getKey().getName(), trigger.getStartTime());
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error(e.getMessage());
        }

    }

    @Override
    public void collectVotingResults(Long processId) {
        log.info("The job for the process {} was triggered", processId);
        ApprovementProcess process = findApprovementProcess(processId);
        process.setStatus(VOTING_COMPLETED);
        float requiredAgreementPercent = process.getAgreementProcent();
        List<ApprovementProcessItem> items = findItemsByProcess(process);
        if (items.isEmpty()) {
            log.warn("No items found for this process.");
            throw new ResourceNotFoundException("No items found for this process.");
        }
        setStatusForItems(items);
        float actualAgreementPercent = (float) calculateAgreementVoices(items) / items.size() * 100;
        if (actualAgreementPercent >= requiredAgreementPercent) {
            process.setStatus(VOTING_APPROVED);
        } else process.setStatus(VOTING_REJECTED);
        log.info("Result for approval process {}: {}", processId, process.getStatus());
        processRepository.save(process);
    }

    private void setStatusForItems(List<ApprovementProcessItem> items) {
        items.stream()
                .filter(i -> i.getStatus() == ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN)
                .forEach(i -> {
                    i.setStatus(ApprovementProcessItemStatus.ABSTAINED);
                    itemRepository.save(i);
                });
    }

    private Long calculateAgreementVoices(List<ApprovementProcessItem> items) {
        return items.stream().filter(i -> i.getStatus() == ApprovementProcessItemStatus.APPROVED).count();
    }

    private ApprovementProcess findApprovementProcess(Long processId) {
        return processRepository.findById(processId)
                .orElseThrow(() -> new ResourceNotFoundException("Approvement process not found"));
    }

    private List<ApprovementProcessItem> findItemsByProcess(ApprovementProcess process) {
        return itemRepository.findAllByApprovementProcess(process);
    }
}
