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

        PUBLISHED_FOR_VOTING,    // Опубликован на голосование
        VOTING_COMPLETED,        // Голосование завершено
        VOTING_APPROVED,         // Одобрен по итогам голосования
        VOTING_REJECTED

}
