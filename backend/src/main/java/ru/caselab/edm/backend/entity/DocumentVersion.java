package ru.caselab.edm.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.caselab.edm.backend.state.DocumentBaseState;
import ru.caselab.edm.backend.state.DocumentState;
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

    @OneToMany(mappedBy = "documentVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApprovementProcessItem> approvementProcessItems;

    @OneToMany(mappedBy = "documentVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApprovementProcess> approvementProcesses;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private DocumentStatus state;

    public DocumentStatus getStatus() {
        return state;
    }

    public DocumentState getState() {
        return new DocumentBaseState();
    }
}
