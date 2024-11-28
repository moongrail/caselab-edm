package ru.caselab.edm.backend.testutils.annotations;


import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.testutils.facade.TestDatabaseFacade;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@DataJpaTest
@Import({TestDatabaseFacade.Config.class})
public @interface DatabaseTest {


    @AliasFor(annotation = DataJpaTest.class, attribute = "properties")
    String[] properties() default {};

}
