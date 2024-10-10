package ru.caselab.edm.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime creationDate;

    @Column(name = "name")
    private String name;

    @Column(name = "updated_at")
    private LocalDateTime updateDate;

    @Column(name = "data", nullable = false)
    private byte[] data;

    @OneToMany(mappedBy = "document")
    private List<AttributeValue> documentAttributeValues;

}
