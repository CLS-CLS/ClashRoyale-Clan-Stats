package org.lytsiware.clash;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.autoconfigure.flyway.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcOperationsDependsOnPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.jdbc.SchemaManagement;
import org.springframework.boot.jdbc.SchemaManagementProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.*;
import java.util.function.Supplier;

/**
 * Custom flyway configuration to avoid using spring's {@link org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration}
 * as the latter is not compatible with flyway 4.2.0.
 * Flyway at version 5+ uses FluentBuilder to create a flyway instance which spring 2.5.0 uses in FlywayAutoConfiguration class to
 * create a flyway instance bean. Older flyway version do not havethis API, causing spring's  flyway auto-configuration to
 * throw {@link javax.el.MethodNotFoundException} at runtime
 *
 * <p>Disclaimer</p>
 * This class is a copy-paste of the FlywayAutoConfiguration class of spring-boot 2.0.3 with a slight modification
 */

@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true")
@AutoConfigureAfter({DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigureBefore(FlywayAutoConfiguration.class)
public class OldFlywaySupportFlywayAutoConfiguration {

    @Bean
    @ConfigurationPropertiesBinding
    public OldFlywaySupportFlywayAutoConfiguration.StringOrNumberToMigrationVersionConverter stringOrNumberMigrationVersionConverter() {
        return new OldFlywaySupportFlywayAutoConfiguration.StringOrNumberToMigrationVersionConverter();
    }

    @Bean
    public SchemaManagementProvider flywayDefaultDdlModeProvider(ObjectProvider<List<Flyway>> flyways) {
        return dataSource -> {
            for (Flyway flywayInstance : flyways.getIfAvailable(Collections::emptyList)) {
                if (dataSource.equals(flywayInstance.getDataSource())) {
                    return SchemaManagement.MANAGED;
                }
            }
            return SchemaManagement.UNMANAGED;
        };
    }

    @Configuration
    @ConditionalOnMissingBean(Flyway.class)
    @EnableConfigurationProperties({DataSourceProperties.class, FlywayProperties.class})
    public static class FlywayConfiguration {

        private final FlywayProperties properties;

        private final DataSourceProperties dataSourceProperties;

        private final ResourceLoader resourceLoader;

        private final DataSource dataSource;

        private final DataSource flywayDataSource;

        private final FlywayMigrationStrategy migrationStrategy;

        private final List<FlywayCallback> flywayCallbacks;

        public FlywayConfiguration(FlywayProperties properties,
                                   DataSourceProperties dataSourceProperties, ResourceLoader resourceLoader,
                                   ObjectProvider<DataSource> dataSource,
                                   @FlywayDataSource ObjectProvider<DataSource> flywayDataSource,
                                   ObjectProvider<FlywayMigrationStrategy> migrationStrategy,
                                   ObjectProvider<List<FlywayCallback>> flywayCallbacks) {
            this.properties = properties;
            this.dataSourceProperties = dataSourceProperties;
            this.resourceLoader = resourceLoader;
            this.dataSource = dataSource.getIfUnique();
            this.flywayDataSource = flywayDataSource.getIfAvailable();
            this.migrationStrategy = migrationStrategy.getIfAvailable();
            this.flywayCallbacks = flywayCallbacks.getIfAvailable(Collections::emptyList);
        }

        @Bean
        @ConfigurationProperties(prefix = "spring.flyway")
        public Flyway flyway() {
            Flyway flyway = new OldFlywaySupportFlywayAutoConfiguration.SpringBootFlyway();
            if (this.properties.isCreateDataSource()) {
                String url = getProperty(this.properties::getUrl,
                        this.dataSourceProperties::getUrl);
                String user = getProperty(this.properties::getUser,
                        this.dataSourceProperties::getUsername);
                String password = getProperty(this.properties::getPassword,
                        this.dataSourceProperties::getPassword);
                flyway.setDataSource(url, user, password,
                        StringUtils.toStringArray(this.properties.getInitSqls()));
            } else if (this.flywayDataSource != null) {
                flyway.setDataSource(this.flywayDataSource);
            } else {
                flyway.setDataSource(this.dataSource);
            }
            flyway.setCallbacks(this.flywayCallbacks.toArray(new FlywayCallback[0]));
            String[] locations = new OldFlywaySupportFlywayAutoConfiguration.LocationResolver(flyway.getDataSource())
                    .resolveLocations(this.properties.getLocations());
            checkLocationExists(locations);
            flyway.setLocations(locations);
            return flyway;
        }

        private String getProperty(Supplier<String> property,
                                   Supplier<String> defaultValue) {
            String value = property.get();
            return (value != null) ? value : defaultValue.get();
        }

        private void checkLocationExists(String... locations) {
            if (this.properties.isCheckLocation()) {
                Assert.state(locations.length != 0,
                        "Migration script locations not configured");
                boolean exists = hasAtLeastOneLocation(locations);
                Assert.state(exists, () -> "Cannot find migrations location in: "
                        + Arrays.asList(locations)
                        + " (please add migrations or check your Flyway configuration)");
            }
        }

        private boolean hasAtLeastOneLocation(String... locations) {
            for (String location : locations) {
                if (this.resourceLoader.getResource(normalizePrefix(location)).exists()) {
                    return true;
                }
            }
            return false;
        }

        private String normalizePrefix(String location) {
            return location.replace("filesystem:", "file:");
        }

        @Bean
        @ConditionalOnMissingBean
        public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
            return new FlywayMigrationInitializer(flyway, this.migrationStrategy);
        }

        /**
         * Additional configuration to ensure that {@link EntityManagerFactory} beans
         * depend on the {@code flywayInitializer} bean.
         */
        @Configuration
        @ConditionalOnClass(LocalContainerEntityManagerFactoryBean.class)
        @ConditionalOnBean(AbstractEntityManagerFactoryBean.class)
        protected static class FlywayInitializerJpaDependencyConfiguration
                extends EntityManagerFactoryDependsOnPostProcessor {

            public FlywayInitializerJpaDependencyConfiguration() {
                super("flywayInitializer");
            }

        }

        /**
         * Additional configuration to ensure that {@link JdbcOperations} beans depend on
         * the {@code flywayInitializer} bean.
         */
        @Configuration
        @ConditionalOnClass(JdbcOperations.class)
        @ConditionalOnBean(JdbcOperations.class)
        protected static class FlywayInitializerJdbcOperationsDependencyConfiguration
                extends JdbcOperationsDependsOnPostProcessor {

            public FlywayInitializerJdbcOperationsDependencyConfiguration() {
                super("flywayInitializer");
            }

        }

    }

    /**
     * Additional configuration to ensure that {@link EntityManagerFactory} beans depend
     * on the {@code flyway} bean.
     */
    @Configuration
    @ConditionalOnClass(LocalContainerEntityManagerFactoryBean.class)
    @ConditionalOnBean(AbstractEntityManagerFactoryBean.class)
    protected static class FlywayJpaDependencyConfiguration
            extends EntityManagerFactoryDependsOnPostProcessor {

        public FlywayJpaDependencyConfiguration() {
            super("flyway");
        }

    }

    /**
     * Additional configuration to ensure that {@link JdbcOperations} beans depend on the
     * {@code flyway} bean.
     */
    @Configuration
    @ConditionalOnClass(JdbcOperations.class)
    @ConditionalOnBean(JdbcOperations.class)
    protected static class FlywayJdbcOperationsDependencyConfiguration
            extends JdbcOperationsDependsOnPostProcessor {

        public FlywayJdbcOperationsDependencyConfiguration() {
            super("flyway");
        }

    }

    private static class SpringBootFlyway extends Flyway {

        @Override
        public void setLocations(String... locations) {
            super.setLocations(
                    new OldFlywaySupportFlywayAutoConfiguration.LocationResolver(getDataSource()).resolveLocations(locations));
        }

    }

    private static class LocationResolver {

        private static final String VENDOR_PLACEHOLDER = "{vendor}";

        private final DataSource dataSource;

        LocationResolver(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public String[] resolveLocations(Collection<String> locations) {
            return resolveLocations(StringUtils.toStringArray(locations));
        }

        public String[] resolveLocations(String[] locations) {
            if (usesVendorLocation(locations)) {
                DatabaseDriver databaseDriver = getDatabaseDriver();
                return replaceVendorLocations(locations, databaseDriver);
            }
            return locations;
        }

        private String[] replaceVendorLocations(String[] locations,
                                                DatabaseDriver databaseDriver) {
            if (databaseDriver == DatabaseDriver.UNKNOWN) {
                return locations;
            }
            String vendor = databaseDriver.getId();
            return Arrays.stream(locations)
                    .map((location) -> location.replace(VENDOR_PLACEHOLDER, vendor))
                    .toArray(String[]::new);
        }

        private DatabaseDriver getDatabaseDriver() {
            try {
                String url = JdbcUtils.extractDatabaseMetaData(this.dataSource, "getURL");
                return DatabaseDriver.fromJdbcUrl(url);
            } catch (MetaDataAccessException ex) {
                throw new IllegalStateException(ex);
            }

        }

        private boolean usesVendorLocation(String... locations) {
            for (String location : locations) {
                if (location.contains(VENDOR_PLACEHOLDER)) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Convert a String or Number to a {@link MigrationVersion}.
     */
    private static class StringOrNumberToMigrationVersionConverter
            implements GenericConverter {

        private static final Set<ConvertiblePair> CONVERTIBLE_TYPES;

        static {
            Set<ConvertiblePair> types = new HashSet<>(2);
            types.add(new ConvertiblePair(String.class, MigrationVersion.class));
            types.add(new ConvertiblePair(Number.class, MigrationVersion.class));
            CONVERTIBLE_TYPES = Collections.unmodifiableSet(types);
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return CONVERTIBLE_TYPES;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType,
                              TypeDescriptor targetType) {
            String value = ObjectUtils.nullSafeToString(source);
            return MigrationVersion.fromVersion(value);
        }

    }

}
