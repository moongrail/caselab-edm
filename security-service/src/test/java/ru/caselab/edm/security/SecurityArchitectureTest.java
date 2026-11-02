package ru.caselab.edm.security;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architectural guards for the security bounded context.
 */
@AnalyzeClasses(packages = "ru.caselab.edm.security",
        importOptions = ImportOption.DoNotIncludeTests.class)
class SecurityArchitectureTest {

    @ArchTest
    static final ArchRule moduleDoesNotDependOnBackend =
            noClasses().should().dependOnClassesThat()
                    .resideInAPackage("ru.caselab.edm.backend..");

    @ArchTest
    static final ArchRule domainDoesNotDependOnAdapters =
            noClasses().that().resideInAPackage("ru.caselab.edm.security.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "ru.caselab.edm.security.service..",
                            "ru.caselab.edm.security.api..",
                            "ru.caselab.edm.security.autoconfigure..",
                            "ru.caselab.edm.security.persistence..",
                            "ru.caselab.edm.security.jwt..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnDeliveryOrConfiguration =
            noClasses().that().resideInAPackage("ru.caselab.edm.security.service..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "ru.caselab.edm.security.api.rest..",
                            "ru.caselab.edm.security.autoconfigure..");

    @ArchTest
    static final ArchRule controllersDoNotUseRepositoriesDirectly =
            noClasses().that().resideInAPackage("ru.caselab.edm.security.api.rest..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("ru.caselab.edm.security.persistence..");
}
