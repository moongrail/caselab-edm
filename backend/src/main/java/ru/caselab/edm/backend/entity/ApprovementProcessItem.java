package ru.caselab.edm.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.caselab.edm.backend.enums.ApprovementProcessItemStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "approvment_process_item")
public class ApprovementProcessItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "app_procc_id")
    private ApprovementProcess approvementProcess;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApprovementProcessItemStatus status;

    @ManyToOne
    @JoinColumn(name = "document_version_id")
    private DocumentVersion documentVersion;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "approvementProcessItem")
    private Signature signature;
}
