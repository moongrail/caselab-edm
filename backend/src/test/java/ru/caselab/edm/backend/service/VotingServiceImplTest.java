package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.ApprovementProcessRepository;
import ru.caselab.edm.backend.service.impl.VotingServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementItem;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementProcess;

@ExtendWith(MockitoExtension.class)
class VotingServiceImplTest {

    @Mock
    private ApprovementProcessRepository processRepository;

    @Mock
    private ApprovementItemRepository itemRepository;

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private VotingServiceImpl votingService;

    @Test
    void scheduleVotingJob_schedulesQuartzJobAtDeadline() throws SchedulerException {
        LocalDateTime deadline = LocalDateTime.of(2026, 12, 1, 12, 30);

        votingService.scheduleVotingJob(42L, deadline);

        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture());
        assertThat(jobCaptor.getValue().getKey().getName()).isEqualTo("job_42");
        assertThat(jobCaptor.getValue().getJobDataMap().getLong("processId")).isEqualTo(42L);
        assertThat(triggerCaptor.getValue().getKey().getName()).isEqualTo("trigger_42");
        assertThat(triggerCaptor.getValue().getStartTime().toInstant())
                .isEqualTo(deadline.toInstant(ZoneOffset.UTC));
    }

    @Test
    void scheduleVotingJob_schedulerFailure_isPropagated() throws SchedulerException {
        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class)))
                .thenThrow(new SchedulerException("scheduler down"));

        assertThatThrownBy(() -> votingService.scheduleVotingJob(42L, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to schedule voting completion")
                .hasCauseInstanceOf(SchedulerException.class);
    }

    @Test
    void collectVotingResults_approvesProcessWhenThresholdReached() {
        ApprovementProcess process = approvementProcess(
                1L, null, ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        process.setAgreementProcent(75f);
        List<ApprovementProcessItem> items = List.of(
                approvementItem(1L, null, null, process, ApprovementProcessItemStatus.APPROVED),
                approvementItem(2L, null, null, process, ApprovementProcessItemStatus.APPROVED),
                approvementItem(3L, null, null, process, ApprovementProcessItemStatus.APPROVED),
                approvementItem(4L, null, null, process, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN)
        );
        when(processRepository.findById(1L)).thenReturn(Optional.of(process));
        when(itemRepository.findAllByApprovementProcess(process)).thenReturn(items);

        votingService.collectVotingResults(1L);

        assertThat(process.getStatus()).isEqualTo(ApprovementProcessStatus.VOTING_APPROVED);
        assertThat(items.get(3).getStatus()).isEqualTo(ApprovementProcessItemStatus.ABSTAINED);
        verify(itemRepository).save(items.get(3));
        verify(processRepository).save(process);
    }

    @Test
    void collectVotingResults_alreadyCompleted_skips() {
        ApprovementProcess process = approvementProcess(
                1L, null, ApprovementProcessStatus.VOTING_APPROVED);
        when(processRepository.findById(1L)).thenReturn(Optional.of(process));

        votingService.collectVotingResults(1L);

        verify(processRepository, never()).save(process);
        verify(itemRepository, never()).findAllByApprovementProcess(process);
    }

    @Test
    void collectVotingResults_rejectsProcessBelowThreshold() {
        ApprovementProcess process = approvementProcess(
                1L, null, ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        process.setAgreementProcent(60f);
        List<ApprovementProcessItem> items = List.of(
                approvementItem(1L, null, null, process, ApprovementProcessItemStatus.APPROVED),
                approvementItem(2L, null, null, process, ApprovementProcessItemStatus.REJECTED),
                approvementItem(3L, null, null, process, ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN)
        );
        when(processRepository.findById(1L)).thenReturn(Optional.of(process));
        when(itemRepository.findAllByApprovementProcess(process)).thenReturn(items);

        votingService.collectVotingResults(1L);

        assertThat(process.getStatus()).isEqualTo(ApprovementProcessStatus.VOTING_REJECTED);
        assertThat(items.get(2).getStatus()).isEqualTo(ApprovementProcessItemStatus.ABSTAINED);
        verify(processRepository).save(process);
    }

    @Test
    void collectVotingResults_withoutItems_throwsAndDoesNotSave() {
        ApprovementProcess process = approvementProcess(
                1L, null, ApprovementProcessStatus.PUBLISHED_FOR_VOTING);
        when(processRepository.findById(1L)).thenReturn(Optional.of(process));
        when(itemRepository.findAllByApprovementProcess(process)).thenReturn(List.of());

        assertThatThrownBy(() -> votingService.collectVotingResults(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No items");

        verify(processRepository, never()).save(any());
    }

    @Test
    void collectVotingResults_missingProcess_throws() {
        when(processRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> votingService.collectVotingResults(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Approvement process not found");
    }
}
