package ru.caselab.edm.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "document_attribute_value")
public class AttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id",nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id",nullable = false)
    private Attribute attribute;

    private String value;
}
