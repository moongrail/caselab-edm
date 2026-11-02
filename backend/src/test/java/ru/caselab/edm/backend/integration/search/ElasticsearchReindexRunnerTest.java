package ru.caselab.edm.backend.integration.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.document.domain.Attribute;
import ru.caselab.edm.backend.document.infrastructure.AttributeRepository;
import ru.caselab.edm.backend.document.infrastructure.DocumentRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchReindexRunnerTest {

    @Mock
    private AttributeRepository attributeRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private AttributeSearchRepository attributeSearchRepository;

    @InjectMocks
    private ElasticsearchReindexRunner runner;

    @Test
    void reindex_rebuildsSearchDocuments() {
        Attribute attribute = Attribute.builder()
                .id(3L)
                .name("Author")
                .dataType("STRING")
                .isRequired(true)
                .build();
        when(attributeRepository.findAll()).thenReturn(List.of(attribute));
        when(documentRepository.getDocumentsWithAttribute(3L)).thenReturn(List.of(10L, 11L));

        runner.reindex();

        ArgumentCaptor<AttributeSearch> captor = ArgumentCaptor.forClass(AttributeSearch.class);
        verify(attributeSearchRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(3L);
        assertThat(captor.getValue().getName()).isEqualTo("Author");
        assertThat(captor.getValue().getDocuments()).containsExactly(10L, 11L);
    }

    @Test
    void reindex_failureIsIgnored() {
        when(attributeRepository.findAll()).thenThrow(new IllegalStateException("Elastic is down"));

        assertThatCode(runner::reindex).doesNotThrowAnyException();
        verify(attributeSearchRepository, org.mockito.Mockito.never()).save(any());
    }
}
