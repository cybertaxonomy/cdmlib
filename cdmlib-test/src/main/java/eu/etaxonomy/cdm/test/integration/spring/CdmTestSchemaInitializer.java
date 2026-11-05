/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Applies the H2 DDL script once (replaces Unitils dbmaintainer schema update).
 */
public class CdmTestSchemaInitializer implements InitializingBean {

    private DataSource dataSource;
    private Resource script;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setScript(Resource script) {
        this.script = script;
    }

    @Override
    public void afterPropertiesSet() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(script);
        populator.setContinueOnError(false);
        populator.setSeparator(";");
        DatabasePopulatorUtils.execute(populator, dataSource);
    }
}
