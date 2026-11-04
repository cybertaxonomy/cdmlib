/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

/**
 * JPA bootstrap ({@link LocalContainerEntityManagerFactoryBean}) for CDM
 * <ul>
 *   <li>Entity / package list from {@code hibernate.cfg.xml} (same source as
 *       native Configuration; avoids {@code packagesToScan} which would
 *       double-register nested converters from
 *       {@link eu.etaxonomy.cdm.hibernate.CdmEnumConverterContributor})</li>
 *   <li>{@link ImplicitNamingStrategyComponentPathImpl}</li>
 * </ul>
 * {@link #getObjectType()} reports {@link SessionFactory} so a single Spring bean
 * satisfies {@code @Autowired SessionFactory}. DAOs should obtain the Session via
 * {@link CdmSessionUtils} (EntityManagerHolder from JpaTransactionManager).
 */
public class CdmLocalContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    private static final String DEFAULT_HIBERNATE_CFG = "eu/etaxonomy/cdm/hibernate.cfg.xml";
    private static final String PERSISTENCE_UNIT = "cdm";

    private Interceptor entityInterceptor;
    private Resource hibernateConfigLocation = new ClassPathResource(DEFAULT_HIBERNATE_CFG);
    private boolean packagesToScanSet;
    private boolean jpaVendorAdapterSet;
    private boolean persistenceUnitNameSet;

    @Override
    public Class<? extends EntityManagerFactory> getObjectType() {
        // H6 SessionFactory implements EMF; expose SF so autowiring is unambiguous
        return SessionFactory.class;
    }

    /**
     * Return the native Hibernate {@link SessionFactory} (not Spring's EMF proxy).
     * Same instance must be used as JpaTransactionManager's {@code entityManagerFactory}
     * and as injected {@code SessionFactory}, so {@link CdmSessionUtils} finds the
     * {@link org.springframework.orm.jpa.EntityManagerHolder} bound under that key.
     */
    @Override
    public EntityManagerFactory getObject() {
        EntityManagerFactory nativeEmf = getNativeEntityManagerFactory();
        if (nativeEmf == null) {
            return null;
        }
        return nativeEmf.unwrap(SessionFactory.class);
    }

    public void setEntityInterceptor(Interceptor entityInterceptor) {
        this.entityInterceptor = entityInterceptor;
    }

    /**
     * Hibernate {@code hibernate.cfg.xml} used to resolve {@code <mapping class>} /
     * {@code <mapping package>} when {@code packagesToScan} is not set.
     */
    public void setHibernateConfigLocation(Resource hibernateConfigLocation) {
        this.hibernateConfigLocation = hibernateConfigLocation;
    }

    @Override
    public void setPackagesToScan(String... packagesToScan) {
        this.packagesToScanSet = true;
        super.setPackagesToScan(packagesToScan);
    }

    @Override
    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitNameSet = true;
        super.setPersistenceUnitName(persistenceUnitName);
    }

    @Override
    public void setJpaVendorAdapter(JpaVendorAdapter jpaVendorAdapter) {
        this.jpaVendorAdapterSet = true;
        super.setJpaVendorAdapter(jpaVendorAdapter);
    }

    @Override
    public void afterPropertiesSet() throws PersistenceException {
        if (!jpaVendorAdapterSet) {
            super.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        }
        if (!persistenceUnitNameSet) {
            super.setPersistenceUnitName(PERSISTENCE_UNIT);
        }
        if (!packagesToScanSet) {
            HibernateCfgMappings mappings = readHibernateCfgMappings();
            super.setPersistenceUnitPostProcessors(new HibernateCfgPersistenceUnitPostProcessor(mappings));
        }

        Map<String, Object> map = getJpaPropertyMap();
        map.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY,
                ImplicitNamingStrategyComponentPathImpl.class.getName());
        // Do NOT set SpringSessionContext here: with JpaTransactionManager it can open a
        // non-transactional Session + SpringSessionSynchronization, breaking Envers flush.
        // Sessions come from EntityManagerHolder via CdmSessionUtils.
        if (entityInterceptor != null) {
            map.put(AvailableSettings.INTERCEPTOR, entityInterceptor);
        }
        // HS 7 Jandex JAR scan: FileSystemAlreadyExistsException → HSEARCH900016 under Tomcat
        map.putIfAbsent("hibernate.search.mapping.configurer",
                "eu.etaxonomy.cdm.persistence.hibernate.search.CdmHibernateSearchMappingConfigurer");

        super.afterPropertiesSet();
    }

    private HibernateCfgMappings readHibernateCfgMappings() {
        try (InputStream in = hibernateConfigLocation.getInputStream()) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            Document doc = dbf.newDocumentBuilder().parse(in);
            List<String> classNames = new ArrayList<>();
            List<String> packages = new ArrayList<>();
            NodeList mappings = doc.getElementsByTagName("mapping");
            for (int i = 0; i < mappings.getLength(); i++) {
                Element el = (Element) mappings.item(i);
                if (el.hasAttribute("class")) {
                    classNames.add(el.getAttribute("class"));
                }
                if (el.hasAttribute("package")) {
                    packages.add(el.getAttribute("package"));
                }
            }
            return new HibernateCfgMappings(classNames, packages);
        } catch (Exception e) {
            throw new PersistenceException(
                    "Failed to read Hibernate mappings from " + hibernateConfigLocation, e);
        }
    }

    private static final class HibernateCfgMappings {
        private final List<String> classNames;
        private final List<String> packages;

        private HibernateCfgMappings(List<String> classNames, List<String> packages) {
            this.classNames = classNames;
            this.packages = packages;
        }
    }

    private static final class HibernateCfgPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

        private final HibernateCfgMappings mappings;

        private HibernateCfgPersistenceUnitPostProcessor(HibernateCfgMappings mappings) {
            this.mappings = mappings;
        }

        @Override
        public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
            for (String className : mappings.classNames) {
                pui.addManagedClassName(className);
            }
            for (String pkg : mappings.packages) {
                pui.addManagedPackage(pkg);
            }
        }
    }
}
