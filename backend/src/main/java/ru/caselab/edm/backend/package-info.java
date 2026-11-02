/**
 * Document management bounded context.
 *
 * <p>The backend is a modular monolith. Existing packages are grouped by
 * technical role for backwards compatibility, while the context map and
 * dependency rules are documented in {@code docs/DDD.md}. The security
 * bounded context is physically extracted into {@code security-module}; this
 * application consumes it through its public application services and the
 * resource-authorization SPI.</p>
 */
package ru.caselab.edm.backend;
