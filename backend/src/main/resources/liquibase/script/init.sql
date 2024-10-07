--author=Maevgal
DROP TABLE IF EXISTS document_type;

CREATE TABLE document_type
(
    id           bigint     PRIMARY KEY,
    name         VARCHAR (255)  not null,
    description  TEXT           not null,
    created_at   TIMESTAMP      not null,
    updated_at   TIMESTAMP      not null,
    attribute_id bigint         not null,
    CONSTRAINT attribute_foreign_key FOREIGN KEY (attribute_id) REFERENCES document_attribute (id)
);