package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.ContentTypeDetectionException;
import ru.caselab.edm.backend.job.VoteResultAggregatorJob;
import ru.caselab.edm.backend.listener.DocumentSignRequestListener;
import ru.caselab.edm.backend.service.helpers.TikaInputStreamContentTypeDetector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InfrastructureComponentsTest {

    @Test
    void tikaDetector_detectsKnownContentType() {
        TikaInputStreamContentTypeDetector detector = new TikaInputStreamContentTypeDetector();

        String contentType = detector.detect("document.txt",
                new ByteArrayInputStream("plain text".getBytes(StandardCharsets.UTF_8)));

        assertThat(contentType).isEqualTo("text/plain");
    }

    @Test
    void tikaDetector_wrapsIoException() {
        TikaInputStreamContentTypeDetector detector = new TikaInputStreamContentTypeDetector();
        InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("broken stream");
            }
        };

        assertThatThrownBy(() -> detector.detect("file.bin", brokenStream))
                .isInstanceOf(ContentTypeDetectionException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void voteAggregatorJob_passesProcessIdToVotingService() {
        VotingService votingService = mock(VotingService.class);
        JobExecutionContext context = mock(JobExecutionContext.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("processId", 42L);
        when(context.getMergedJobDataMap()).thenReturn(dataMap);

        new VoteResultAggregatorJob(votingService).execute(context);

        verify(votingService).collectVotingResults(42L);
    }

    @Test
    void signRequestListener_delegatesToEmailService() {
        EmailService emailService = mock(EmailService.class);
        DocumentSignRequestListener listener = new DocumentSignRequestListener(emailService);
        DocumentSignRequestEvent event = new DocumentSignRequestEvent(
                this, ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementItem(
                        1L, null, null, null,
                        ru.caselab.edm.backend.enums.ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN));

        listener.onDocumentSentForSign(event);

        verify(emailService).sendEmailForSign(event.getApprovementProcessItem());
    }
}
