package ru.caselab.edm.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @ManyToOne
    @JoinColumn(name = "document_version_id")
    private DocumentVersion documentVersion;

}
