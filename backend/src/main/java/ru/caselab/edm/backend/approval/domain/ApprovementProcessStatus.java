package ru.caselab.edm.backend.approval.domain;

public enum ApprovementProcessStatus {
    DRAFT,
    PUBLISHED_FOR_VOTING,
    VOTING_COMPLETED,
    VOTING_APPROVED,
    VOTING_REJECTED
}
