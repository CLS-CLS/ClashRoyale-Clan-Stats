package org.lytsiware;


import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.lytsiware.clash.Application;
import org.lytsiware.clash.service.integration.ClashStatsSiteServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
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
public abstract class AbstractSpringBootTest {
    Logger logger = LoggerFactory.getLogger(AbstractSpringBootTest.class);

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
    }

}
