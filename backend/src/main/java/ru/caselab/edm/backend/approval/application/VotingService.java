package ru.caselab.edm.backend.approval.application;


import java.time.LocalDateTime;

public interface VotingService {
    void collectVotingResults(Long processId);

    void scheduleVotingJob(Long processId, LocalDateTime deadline);
}
