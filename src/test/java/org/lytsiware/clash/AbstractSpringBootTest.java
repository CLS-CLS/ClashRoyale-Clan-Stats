package org.lytsiware.clash;


import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.lytsiware.clash.service.integration.clashapi.ProxyAndBearerHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.net.Proxy;

@RunWith(SpringRunner.class)
@AutoConfigureTestEntityManager
@SpringBootTest
@Transactional
@ActiveProfiles(profiles = {"statsRoyale", "test"})
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

    @Configuration
    static class TestConfiguration {

        @Bean
        public ProxyAndBearerHolder proxyAndBearerHolder(){
            return new ProxyAndBearerHolder() {
                @Override
                public String getBearer() {
                    return null;
                }

                @Override
                public Proxy getProxy() {
                    return null;
                }
            };
        }

    }

}