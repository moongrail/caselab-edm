package ru.caselab.edm.backend.job;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.caselab.edm.backend.service.VotingService;

@Component
public class VoteResultAggregatorJob implements Job {

    private final VotingService votingService;

    @Autowired
    public VoteResultAggregatorJob(VotingService votingService) {
        this.votingService = votingService;
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        Long processId = jobExecutionContext.getMergedJobDataMap().getLong("processId");
        votingService.collectVotingResults(processId);
    }
}
