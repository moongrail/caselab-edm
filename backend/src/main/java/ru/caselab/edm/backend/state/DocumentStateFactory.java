package ru.caselab.edm.backend.state;

public class DocumentStateFactory {
    public static DocumentState getState(DocumentStatus status) {
//        return switch (status) {
//            case DRAFT -> new DraftDocumentState();
//            case AUTHOR_SIGNED -> new AuthorSignedDocumentState();
//            default -> throw new IllegalArgumentException("Unknown state: " + status);
//        };
        return new DocumentBaseState();
    }
}