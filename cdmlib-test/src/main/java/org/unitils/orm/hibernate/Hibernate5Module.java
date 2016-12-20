/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.unitils.orm.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.unitils.orm.common.util.OrmConfig;
import org.unitils.orm.common.util.OrmPersistenceUnitLoader;
import org.unitils.orm.hibernate.util.Hibernate5SessionFactoryLoader;

/**
 * @author a.mueller
 * @date 03.11.2015
 *
 */
public class Hibernate5Module extends HibernateModule {

    @Override
    protected OrmPersistenceUnitLoader<SessionFactory, Configuration, OrmConfig> createOrmPersistenceUnitLoader() {
        return new Hibernate5SessionFactoryLoader(databaseName);
    }

}
