package org.lytsiware;


import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.lytsiware.clash.Application;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
@TestPropertySource(locations = "classpath:test.properties")
@ActiveProfiles("statsRoyale")
public class AbstractSpringRunnerTest {

    @PersistenceContext
    protected EntityManager em;

    @Before
    public void initDb() {
        EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) em.getEntityManagerFactory();
        DataSource datasource = info.getDataSource();
        Flyway flyway = new Flyway();
        flyway.setDataSource(datasource);
        flyway.clean();
        flyway.migrate();
        createData();
    }

    protected void createData(){

    }
}
