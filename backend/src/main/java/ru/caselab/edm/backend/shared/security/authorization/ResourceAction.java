package ru.caselab.edm.backend.shared.security.authorization;

/**
 * Actions that can be performed on a protected application resource.
 */
public enum ResourceAction {
    READ,
    WRITE,
    DELETE,
    DOWNLOAD,
    SIGN
}
