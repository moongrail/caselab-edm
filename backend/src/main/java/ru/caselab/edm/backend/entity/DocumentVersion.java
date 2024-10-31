package ru.caselab.edm.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @ManyToOne
    @JoinColumn(name = "documents_id")
    private Document document;

    @OneToMany(mappedBy = "documentVersion", cascade = CascadeType.ALL)
    private List<DocumentAttributeValue> documentAttributeValue= new ArrayList<>();

    @OneToMany(mappedBy = "documentVersion")
    private List<ApprovementProcessItem> approvementProcessItems;

    @OneToMany(mappedBy = "documentVersion")
    private List<ApprovementProcess> approvementProcesses;
}
