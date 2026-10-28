package ru.caselab.edm.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.caselab.edm.backend.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.dto.document.DocumentCreateDTO;
import ru.caselab.edm.backend.dto.document.DocumentOutputAllDocumentsDTO;
import ru.caselab.edm.backend.dto.document.DocumentUpdateDTO;
import ru.caselab.edm.backend.dto.file.FileDTO;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.AttributeSearch;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.entity.ReplacementManager;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.entity.UserInfoDetails;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;
import ru.caselab.edm.backend.enums.DocumentSortingType;
import ru.caselab.edm.backend.event.DocumentSignRequestEvent;
import ru.caselab.edm.backend.exceptions.ApprovementProccessItemAlreadyExistsException;
import ru.caselab.edm.backend.exceptions.DocumentForbiddenAccess;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.approvementprocessitem.ApprovementProccessItemMapper;
import ru.caselab.edm.backend.repository.ApprovementItemRepository;
import ru.caselab.edm.backend.repository.DocumentRepository;
import ru.caselab.edm.backend.repository.DocumentTypeRepository;
import ru.caselab.edm.backend.repository.DocumentVersionRepository;
import ru.caselab.edm.backend.repository.ReplacementManagerRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.service.AuthorizationService;
import ru.caselab.edm.backend.service.impl.DocumentServiceImpl;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.DOCUMENT_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.OWNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.SIGNER_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.VERSION_ID;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.approvementItem;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.attribute;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.document;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.documentType;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.user;
import static ru.caselab.edm.backend.testutils.fixtures.TestFixtures.version;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplAdditionalTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DocumentTypeRepository documentTypeRepository;
    @Mock
    private DocumentVersionRepository documentVersionRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ApprovementItemRepository approvementItemRepository;
    @Mock
    private ApprovementProccessItemMapper approvementItemMapper;
    @Mock
    private ReplacementManagerRepository replacementManagerRepository;
    @Mock
    private DocumentVersionService documentVersionService;
    @Mock
    private AttributeSearchRepository attributeSearchRepository;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private UserInfoDetails authenticatedUser;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private User owner;
    private User signer;
    private Attribute attribute;
    private DocumentType documentType;
    private Document document;
    private DocumentVersion documentVersion;
    private Page<DocumentOutputAllDocumentsDTO> emptyPage;

    @BeforeEach
    void setUp() {
        owner = user(OWNER_ID, "owner");
        signer = user(SIGNER_ID, "signer");
        attribute = attribute(100L, false);
        documentType = documentType(1L, attribute);
        document = document(DOCUMENT_ID, owner, documentType);
        documentVersion = version(VERSION_ID, document, DocumentStatus.DRAFT);
        emptyPage = new PageImpl<>(List.of());

        lenient().when(authenticatedUser.getId()).thenReturn(OWNER_ID);
    }

    @Test
    void getAllDocumentWhereUserOwnerAfterSigner_withoutSorting_delegates() {
        when(documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwnerAfterSigner(
                OWNER_ID, PageRequest.of(0, 10))).thenReturn(emptyPage);

        assertThat(documentService.getAllDocumentWhereUserOwnerAfterSigner(
                0, 10, OWNER_ID, DocumentSortingType.WITHOUT)).isSameAs(emptyPage);
    }

    @Test
    void getAllDocumentWhereUserOwnerBeforeSigner_withSorting_appliesSort() {
        Pageable pageable = PageRequest.of(0, 10)
                .withSort(org.springframework.data.jpa.domain.JpaSort.unsafe(
                        DocumentSortingType.DATE_DESC.getDirection(), DocumentSortingType.DATE_DESC.getFieldName()));
        when(documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserOwnerBeforeSigner(
                OWNER_ID, pageable)).thenReturn(emptyPage);

        assertThat(documentService.getAllDocumentWhereUserOwnerBeforeSigner(
                0, 10, OWNER_ID, DocumentSortingType.DATE_DESC)).isSameAs(emptyPage);
    }

    @Test
    void getAllDocumentWhereUserSignatoriesBeforeSigner_delegates() {
        when(documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatoriesBeforeSigner(
                OWNER_ID, PageRequest.of(1, 5))).thenReturn(emptyPage);

        assertThat(documentService.getAllDocumentWhereUserSignatoriesBeforeSigner(
                1, 5, OWNER_ID, DocumentSortingType.WITHOUT)).isSameAs(emptyPage);
    }

    @Test
    void getAllDocumentWhereUserSignatoriesAfterSigner_delegates() {
        when(documentRepository.getAllDocumentWithNameAndStatusProjectionWhereUserSignatoriesAfterSigner(
                OWNER_ID, PageRequest.of(0, 10))).thenReturn(emptyPage);

        assertThat(documentService.getAllDocumentWhereUserSignatoriesAfterSigner(
                0, 10, OWNER_ID, DocumentSortingType.WITHOUT)).isSameAs(emptyPage);
    }

    @Test
    void getLastVersionDocumentWhereUserSignatories_returnsLatestVersion() {
        DocumentVersion oldVersion = version(9L, document, DocumentStatus.APPROVED);
        oldVersion.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        documentVersion.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        when(documentRepository.getDocumentWhereUserSignatories(DOCUMENT_ID, SIGNER_ID))
                .thenReturn(Optional.of(document));
        when(documentVersionRepository.findByDocumentId(DOCUMENT_ID))
                .thenReturn(List.of(oldVersion, documentVersion));

        assertThat(documentService.getLastVersionDocumentWhereUserSignatories(DOCUMENT_ID, SIGNER_ID))
                .isSameAs(documentVersion);
    }

    @Test
    void getLastVersionDocumentWhereUserSignatories_documentNotFound_throws() {
        when(documentRepository.getDocumentWhereUserSignatories(DOCUMENT_ID, SIGNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getLastVersionDocumentWhereUserSignatories(
                DOCUMENT_ID, SIGNER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Document not found");
    }

    @Test
    void getLastVersionDocumentForUser_withoutVersions_throws() {
        Document emptyDocument = document(DOCUMENT_ID, owner, documentType);
        when(documentRepository.getDocumentForUser(DOCUMENT_ID, OWNER_ID))
                .thenReturn(Optional.of(emptyDocument));

        assertThatThrownBy(() -> documentService.getLastVersionDocumentForUser(DOCUMENT_ID, OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document version not found");
    }

    @Test
    void getAllVersionDocumentWhereUserSignatories_delegates() {
        Page<DocumentVersion> page = new PageImpl<>(List.of(documentVersion));
        when(documentRepository.getDocumentWhereUserSignatories(DOCUMENT_ID, SIGNER_ID))
                .thenReturn(Optional.of(document));
        when(documentVersionRepository.findPageByDocumentId(anyLong(), any(Pageable.class)))
                .thenReturn(page);

        assertThat(documentService.getAllVersionDocumentWhereUserSignatories(
                DOCUMENT_ID, SIGNER_ID, 0, 10)).isSameAs(page);
    }

    @Test
    void getAllVersionDocumentWhereUserSignatories_notFound_throws() {
        when(documentRepository.getDocumentWhereUserSignatories(DOCUMENT_ID, SIGNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getAllVersionDocumentWhereUserSignatories(
                DOCUMENT_ID, SIGNER_ID, 0, 10))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getArchivedDocuments_delegates() {
        when(documentRepository.getArchivedDocumentsForUser(OWNER_ID, PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        assertThat(documentService.getArchivedDocuments(0, 10, OWNER_ID)).isSameAs(emptyPage);
    }

    @Test
    void saveDocument_savesDocumentAndIndexesAttributes() {
        DocumentCreateDTO request = createRequest();
        AttributeSearch search = attributeSearch(100L);
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(documentType));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document saved = invocation.getArgument(0);
            saved.setId(55L);
            return saved;
        });
        when(attributeSearchRepository.findById(100L)).thenReturn(Optional.of(search));
        when(documentVersionService.saveDocumentVersion(any(), any(Document.class), any()))
                .thenReturn(documentVersion);

        DocumentVersion result = documentService.saveDocument(request, OWNER_ID);

        assertThat(result).isSameAs(documentVersion);
        assertThat(search.getDocuments()).contains(55L);
        verify(attributeSearchRepository).save(search);
    }

    @Test
    void saveDocument_missingDocumentType_throws() {
        DocumentCreateDTO request = createRequest();
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.saveDocument(request, OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document type not found");
    }

    @Test
    void saveDocument_missingUser_throws() {
        DocumentCreateDTO request = createRequest();
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(documentType));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.saveDocument(request, OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateDocument_savesAndIndexesAttributes() {
        DocumentUpdateDTO request = new DocumentUpdateDTO();
        request.setDocumentName("Updated");
        request.setFile(new FileDTO("data", "file.pdf"));
        AttributeSearch search = attributeSearch(100L);
        when(authorizationService.requireOwnedDocument(DOCUMENT_ID, OWNER_ID)).thenReturn(document);
        when(documentRepository.save(document)).thenReturn(document);
        when(attributeSearchRepository.findById(100L)).thenReturn(Optional.of(search));
        when(documentVersionService.updateDocumentVersion(request, document, OWNER_ID))
                .thenReturn(documentVersion);

        DocumentVersion result = documentService.updateDocument(DOCUMENT_ID, request, OWNER_ID);

        assertThat(result).isSameAs(documentVersion);
        assertThat(search.getDocuments()).contains(DOCUMENT_ID);
        verify(authorizationService).requireOwnedDocument(DOCUMENT_ID, OWNER_ID);
    }

    @Test
    void deleteDocument_archivesAndRemovesSearchReferences() {
        AttributeSearch search = attributeSearch(100L);
        search.getDocuments().add(DOCUMENT_ID);
        when(authorizationService.requireOwnedDocument(DOCUMENT_ID, OWNER_ID)).thenReturn(document);
        when(attributeSearchRepository.findById(100L)).thenReturn(Optional.of(search));

        documentService.deleteDocument(DOCUMENT_ID, OWNER_ID);

        assertThat(document.isArchived()).isTrue();
        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.DELETED);
        assertThat(search.getDocuments()).doesNotContain(DOCUMENT_ID);
        verify(attributeSearchRepository).save(search);
        verify(documentRepository).save(document);
    }

    @Test
    void deleteDocument_missingSearchReference_stillArchives() {
        when(authorizationService.requireOwnedDocument(DOCUMENT_ID, OWNER_ID)).thenReturn(document);
        when(attributeSearchRepository.findById(100L)).thenReturn(Optional.empty());

        documentService.deleteDocument(DOCUMENT_ID, OWNER_ID);

        assertThat(document.isArchived()).isTrue();
        verify(documentRepository).save(document);
    }

    @Test
    void deleteDocument_withoutVersions_throws() {
        Document emptyDocument = document(DOCUMENT_ID, owner, documentType);
        when(authorizationService.requireOwnedDocument(DOCUMENT_ID, OWNER_ID)).thenReturn(emptyDocument);

        assertThatThrownBy(() -> documentService.deleteDocument(DOCUMENT_ID, OWNER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document version not found");
    }

    @Test
    void sendForSign_createsAuthorSignRequestAndPublishesEvent() {
        when(documentRepository.getDocumentForUser(DOCUMENT_ID, OWNER_ID)).thenReturn(Optional.of(document));
        when(documentVersionRepository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(documentVersion));
        when(replacementManagerRepository.findActiveReplacementByManagerUserId(SIGNER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(signer));
        when(approvementItemRepository.existsByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
                .thenReturn(false);
        when(approvementItemRepository.save(any(ApprovementProcessItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ApprovementProcessItemDTO expected = new ApprovementProcessItemDTO();
        when(approvementItemMapper.toDTO(any(ApprovementProcessItem.class))).thenReturn(expected);

        ApprovementProcessItemDTO result = documentService.sendForSign(
                SIGNER_ID, DOCUMENT_ID, authenticatedUser);

        assertThat(result).isSameAs(expected);
        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.PENDING_AUTHOR_SIGN);
        ArgumentCaptor<ApprovementProcessItem> itemCaptor =
                ArgumentCaptor.forClass(ApprovementProcessItem.class);
        verify(approvementItemRepository).save(itemCaptor.capture());
        ApprovementProcessItem item = itemCaptor.getValue();
        assertThat(item.getUser()).isSameAs(signer);
        assertThat(item.getDocumentVersion()).isSameAs(documentVersion);
        assertThat(item.getStatus()).isEqualTo(ApprovementProcessItemStatus.PENDING_AUTHOR_SIGN);
        verify(eventPublisher).publishEvent(any(DocumentSignRequestEvent.class));
    }

    @Test
    void sendForSign_authorSignedDocument_requestsContractorSignature() {
        documentVersion.setStatus(DocumentStatus.AUTHOR_SIGNED);
        when(documentRepository.getDocumentForUser(DOCUMENT_ID, OWNER_ID)).thenReturn(Optional.of(document));
        when(documentVersionRepository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(documentVersion));
        when(replacementManagerRepository.findActiveReplacementByManagerUserId(SIGNER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(signer));
        when(approvementItemRepository.existsByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
                .thenReturn(false);
        when(approvementItemRepository.save(any(ApprovementProcessItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(approvementItemMapper.toDTO(any(ApprovementProcessItem.class)))
                .thenReturn(new ApprovementProcessItemDTO());

        documentService.sendForSign(SIGNER_ID, DOCUMENT_ID, authenticatedUser);

        ArgumentCaptor<ApprovementProcessItem> itemCaptor =
                ArgumentCaptor.forClass(ApprovementProcessItem.class);
        verify(approvementItemRepository).save(itemCaptor.capture());
        assertThat(documentVersion.getStatus()).isEqualTo(DocumentStatus.PENDING_CONTRACTOR_SIGN);
        assertThat(itemCaptor.getValue().getStatus())
                .isEqualTo(ApprovementProcessItemStatus.PENDING_CONTRACTOR_SIGN);
    }

    @Test
    void sendForSign_usesTemporaryManagerWhenReplacementIsActive() {
        User tempManager = user(UUID.randomUUID(), "temp");
        ReplacementManager replacement = ReplacementManager.builder()
                .managerUser(signer)
                .tempManagerUser(tempManager)
                .startDate(Instant.now().minusSeconds(30))
                .endDate(Instant.now().plusSeconds(30))
                .build();
        when(documentRepository.getDocumentForUser(DOCUMENT_ID, OWNER_ID)).thenReturn(Optional.of(document));
        when(documentVersionRepository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(documentVersion));
        when(replacementManagerRepository.findActiveReplacementByManagerUserId(SIGNER_ID))
                .thenReturn(Optional.of(replacement));
        when(approvementItemRepository.existsByDocumentVersionIdAndUserId(VERSION_ID, tempManager.getId()))
                .thenReturn(false);
        when(approvementItemRepository.save(any(ApprovementProcessItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(approvementItemMapper.toDTO(any(ApprovementProcessItem.class)))
                .thenReturn(new ApprovementProcessItemDTO());

        documentService.sendForSign(SIGNER_ID, DOCUMENT_ID, authenticatedUser);

        verify(userRepository, never()).findById(SIGNER_ID);
        ArgumentCaptor<ApprovementProcessItem> itemCaptor =
                ArgumentCaptor.forClass(ApprovementProcessItem.class);
        verify(approvementItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getUser()).isSameAs(tempManager);
    }

    @Test
    void sendForSign_forbiddenForNonOwner() {
        when(documentRepository.getDocumentForUser(DOCUMENT_ID, OWNER_ID)).thenReturn(Optional.of(document));
        when(documentVersionRepository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(documentVersion));
        org.mockito.Mockito.doThrow(new DocumentForbiddenAccess("forbidden"))
                .when(authorizationService).checkDocumentOwner(document, OWNER_ID);

        assertThatThrownBy(() -> documentService.sendForSign(SIGNER_ID, DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(DocumentForbiddenAccess.class);

        verify(authorizationService).checkDocumentOwner(document, OWNER_ID);
        verify(approvementItemRepository, never()).save(any());
    }

    @Test
    void sendForSign_duplicateRequest_throws() {
        when(documentRepository.getDocumentForUser(DOCUMENT_ID, OWNER_ID)).thenReturn(Optional.of(document));
        when(documentVersionRepository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(documentVersion));
        when(replacementManagerRepository.findActiveReplacementByManagerUserId(SIGNER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.of(signer));
        when(approvementItemRepository.existsByDocumentVersionIdAndUserId(VERSION_ID, SIGNER_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> documentService.sendForSign(SIGNER_ID, DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(ApprovementProccessItemAlreadyExistsException.class);

        verify(approvementItemRepository, never()).save(any());
    }

    @Test
    void sendForSign_missingTargetUser_throws() {
        when(documentRepository.getDocumentForUser(DOCUMENT_ID, OWNER_ID)).thenReturn(Optional.of(document));
        when(documentVersionRepository.findByDocumentId(DOCUMENT_ID)).thenReturn(List.of(documentVersion));
        when(replacementManagerRepository.findActiveReplacementByManagerUserId(SIGNER_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findById(SIGNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.sendForSign(SIGNER_ID, DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void sendForSign_missingDocument_throws() {
        when(documentRepository.getDocumentForUser(DOCUMENT_ID, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.sendForSign(SIGNER_ID, DOCUMENT_ID, authenticatedUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private DocumentCreateDTO createRequest() {
        DocumentCreateDTO request = new DocumentCreateDTO();
        request.setDocumentTypeId(1L);
        request.setDocumentName("Name");
        request.setFile(new FileDTO("data", "file.pdf"));
        request.setAttributeValues(new ArrayList<>());
        return request;
    }

    private AttributeSearch attributeSearch(Long id) {
        AttributeSearch search = new AttributeSearch();
        search.setId(id);
        search.setName("Attribute " + id);
        search.setDataType("STRING");
        search.setDocuments(new ArrayList<>());
        return search;
    }
}
