/**
 * Security bounded context.
 *
 * <p>The module owns authentication, authorization, users, departments, roles
 * and refresh tokens. Internally it follows a hexagonal/DDD package layout:</p>
 *
 * <ul>
 *   <li>{@code domain} — entities and ubiquitous-language types;</li>
 *   <li>{@code service} — application services and use cases;</li>
 *   <li>{@code persistence} — repository ports implemented by Spring Data;</li>
 *   <li>{@code api} — HTTP DTOs and REST adapters;</li>
 *   <li>{@code jwt} — token issuing, validation and the servlet filter;</li>
 *   <li>{@code authorization} — resource-level policy contract and ACL;</li>
 *   <li>{@code autoconfigure} — Spring Boot auto-configuration and the module's
 *       integration contract.</li>
 * </ul>
 *
 * <p>The module never depends on the document bounded context. Resource-level
 * decisions are inverted through {@link ru.caselab.edm.security.authorization.ResourceAccessPolicy},
 * which consuming applications implement.</p>
 */
package ru.caselab.edm.security;
