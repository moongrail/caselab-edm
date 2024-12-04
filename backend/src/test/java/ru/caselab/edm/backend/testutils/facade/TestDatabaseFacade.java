package ru.caselab.edm.backend.testutils.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

public class TestDatabaseFacade {

    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public <T> T find(Object id, Class<T> entityClass) {
        return transactionTemplate.execute(status -> testEntityManager.find(entityClass, id));
    }


    public <T> T save(T entity) {
        transactionTemplate.execute(status -> {
            testEntityManager.persistAndFlush(entity);
            return entity;
        });

        return entity;
    }

    public <T> void saveAll(T... entities) {
        transactionTemplate.execute(status -> {
            for (T entity : entities) {
                testEntityManager.persistAndFlush(entity);
            }
            return null;
        });
    }

    public void cleanDatabase() {
        transactionTemplate.execute(status -> {
            JdbcTestUtils.deleteFromTables(jdbcTemplate, getTablesToClean());
            return null;
        });
    }

    private String[] getTablesToClean() {
        return new String[] {
            "document_types", "documents", "users"
        };
    }

    @TestConfiguration
    public static class Config {

        @Bean
        public TestDatabaseFacade testDBFacade() {
            return new TestDatabaseFacade();
        }
    }

}
