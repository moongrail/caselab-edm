package ru.caselab.edm.backend.state;

public enum DocumentStatus
{
        DRAFT,
        AUTHOR_SIGNED,
        PENDING_AUTHOR_SIGN,
        PENDING_CONTRACTOR_SIGN,
        DELETED,
        REJECTED,
        APPROVED,
        REWORK_REQUIRED,
        IN_VOTING

}
