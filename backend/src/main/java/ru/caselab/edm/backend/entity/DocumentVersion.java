package ru.caselab.edm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.caselab.edm.backend.state.DocumentState;
import ru.caselab.edm.backend.state.DocumentStateFactory;
import ru.caselab.edm.backend.state.DocumentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_versions")
@Builder
public class DocumentVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "content_url", columnDefinition = "TEXT")
    private String contentUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documents_id")
    private Document document;

    @OneToMany(mappedBy = "documentVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentAttributeValue> documentAttributeValue = new ArrayList<>();

    @OneToMany(mappedBy = "documentVersion", fetch = FetchType.LAZY)
    private List<ApprovementProcessItem> approvementProcessItems;

    @OneToMany(mappedBy = "documentVersion", fetch = FetchType.LAZY)
    private List<ApprovementProcess> approvementProcesses;

    @Enumerated(EnumType.STRING)
    private DocumentStatus state;

    public DocumentState getState() {
        return DocumentStateFactory.getState(state);
    }
}
