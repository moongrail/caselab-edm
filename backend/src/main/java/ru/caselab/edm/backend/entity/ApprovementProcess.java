package ru.caselab.edm.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.caselab.edm.backend.enums.ApprovementProcessStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "approvement_process")
public class ApprovementProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersion documentVersion;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private ApprovementProcessStatus status;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "agreement_procent")
    private float agreementProcent;

    @OneToMany(mappedBy = "approvementProcess")
    private List<ApprovementProcessItem> approvementProcessItems = new ArrayList<>();
}
