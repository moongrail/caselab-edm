package ru.caselab.edm.platform.search;

/**
 * Contract for a domain-specific search index rebuild.
 */
@FunctionalInterface
public interface SearchReindexer {

    void reindex();
}
