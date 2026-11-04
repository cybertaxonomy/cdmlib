/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.resource.jdbc.spi.PhysicalConnectionHandlingMode;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate5.SpringSessionContext;

/**
 * Native SessionFactory bootstrap via Hibernate {@link Configuration}
 * (fallback when not using JPA {@code LocalContainerEntityManagerFactoryBean}).
 * <p>
 * Prefer {@link CdmLocalContainerEntityManagerFactoryBean} +
 * {@link SessionFactoryFromEntityManagerFactoryBean} with
 * {@code HibernateTransactionManager} (hybrid JPA bootstrap). This class
 * remains for native {@code hibernate.cfg.xml} Configuration bootstrap.
 * <p>
 * Avoids Spring's {@code LocalSessionFactoryBean}, which is binary-incompatible
 * with Hibernate 6.2+ ({@code setImplicitNamingStrategy} changed from
 * {@code void} to fluent {@code Configuration} return type).
 * <p>
 * Applies the same Spring integration defaults as
 * {@code LocalSessionFactoryBuilder} ({@link SpringSessionContext},
 * {@link PhysicalConnectionHandlingMode#DELAYED_ACQUISITION_AND_HOLD}).
 */
public class CdmHibernateSessionFactoryBean
        implements FactoryBean<SessionFactory>, InitializingBean, DisposableBean {

    private Resource configLocation;
    private DataSource dataSource;
    private Properties hibernateProperties;
    private Interceptor entityInterceptor;
    private ImplicitNamingStrategy implicitNamingStrategy = new ImplicitNamingStrategyComponentPathImpl();

    private SessionFactory sessionFactory;

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setHibernateProperties(Properties hibernateProperties) {
        this.hibernateProperties = hibernateProperties;
    }

    public void setEntityInterceptor(Interceptor entityInterceptor) {
        this.entityInterceptor = entityInterceptor;
    }

    public void setImplicitNamingStrategy(ImplicitNamingStrategy implicitNamingStrategy) {
        this.implicitNamingStrategy = implicitNamingStrategy;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        Configuration configuration = new Configuration();
        if (configLocation != null) {
            configuration.configure(configLocation.getURL());
        }
        if (hibernateProperties != null) {
            configuration.addProperties(hibernateProperties);
        }
        if (!configuration.getProperties().containsKey("hibernate.search.mapping.configurer")) {
            configuration.setProperty("hibernate.search.mapping.configurer",
                    "eu.etaxonomy.cdm.persistence.hibernate.search.CdmHibernateSearchMappingConfigurer");
        }
        // Spring LocalSessionFactoryBuilder defaults
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS,
                SpringSessionContext.class.getName());
        configuration.setProperty(JdbcSettings.CONNECTION_HANDLING,
                PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_HOLD.name());
        if (dataSource != null) {
            configuration.getProperties().put(JdbcSettings.DATASOURCE, dataSource);
        }
        if (implicitNamingStrategy != null) {
            configuration.setImplicitNamingStrategy(implicitNamingStrategy);
        }
        if (entityInterceptor != null) {
            configuration.setInterceptor(entityInterceptor);
        }

        // Use Configuration's own registry builder so mappings from configure() are kept
        StandardServiceRegistryBuilder registryBuilder = configuration.getStandardServiceRegistryBuilder();
        registryBuilder.applySettings(configuration.getProperties());
        this.sessionFactory = configuration.buildSessionFactory(registryBuilder.build());
    }

    @Override
    public SessionFactory getObject() {
        return sessionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return SessionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
