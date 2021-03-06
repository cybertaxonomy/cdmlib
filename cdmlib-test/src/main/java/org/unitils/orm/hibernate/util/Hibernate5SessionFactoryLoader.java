/*
 * Copyright 2008,  Unitils.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitils.orm.hibernate.util;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.unitils.core.Unitils;
import org.unitils.core.UnitilsException;
import org.unitils.database.DatabaseModule;
import org.unitils.orm.common.util.ConfiguredOrmPersistenceUnit;
import org.unitils.orm.common.util.OrmConfig;
import org.unitils.orm.common.util.OrmPersistenceUnitLoader;
import org.unitils.orm.hibernate.Hibernate5Module;


//todo javadoc
public class Hibernate5SessionFactoryLoader implements OrmPersistenceUnitLoader<SessionFactory, Configuration, OrmConfig> {

    protected String databaseName;
    /**
     * @param databaseName
     */
    public Hibernate5SessionFactoryLoader(String databaseName) {
        this.databaseName = databaseName;
    }


    @Override
    public ConfiguredOrmPersistenceUnit<SessionFactory, Configuration> getConfiguredOrmPersistenceUnit(Object testObject, OrmConfig entityManagerConfig) {
        LocalSessionFactoryBean factoryBean = createSessionFactoryBean(testObject, entityManagerConfig);
        SessionFactory entityManagerFactory = factoryBean.getObject();
        Configuration hibernateConfiguration = factoryBean.getConfiguration();
        return new ConfiguredOrmPersistenceUnit<SessionFactory, Configuration>(entityManagerFactory, hibernateConfiguration);
    }


    protected LocalSessionFactoryBean createSessionFactoryBean(Object testObject, OrmConfig entityManagerConfig) {
        // A custom subclass of spring's LocalSessionFactoryBean is used, to enable calling a custom config method
        UnitilsHibernate5LocalSessionFactoryBean factoryBean = new UnitilsHibernate5LocalSessionFactoryBean();
        factoryBean.setDataSource(getDataSource());
//        factoryBean.set .setConfigurationClass(getConfigurationObjectClass());
        Resource[] hibernateConfigFiles = new Resource[entityManagerConfig.getConfigFiles().size()];
        int index = 0;
        for (String configFileName : entityManagerConfig.getConfigFiles()) {
            hibernateConfigFiles[index++] = new ClassPathResource(configFileName);
        }
        factoryBean.setConfigLocations(hibernateConfigFiles);

        // Enable invocation of custom config method
        factoryBean.setTestObject(testObject);
        factoryBean.setCustomConfigMethod(entityManagerConfig.getConfigMethod());

        // Build SessionFactory
        try {
            factoryBean.afterPropertiesSet();
        } catch (Exception e) {
            throw new UnitilsException("Error while processing " + LocalSessionFactoryBean.class.getSimpleName() + " configuration", e);
        }

        return factoryBean;
    }


    protected Class<? extends Configuration> getConfigurationObjectClass() {
        return getHibernateModule().getConfigurationObjectClass();
    }


    protected DataSource getDataSource() {
        return getDatabaseModule().getWrapper(databaseName).getDataSourceAndActivateTransactionIfNeeded();
    }


    protected DatabaseModule getDatabaseModule() {
        return Unitils.getInstance().getModulesRepository().getModuleOfType(DatabaseModule.class);
    }


    protected Hibernate5Module getHibernateModule() {
        return Unitils.getInstance().getModulesRepository().getModuleOfType(Hibernate5Module.class);
    }
}
