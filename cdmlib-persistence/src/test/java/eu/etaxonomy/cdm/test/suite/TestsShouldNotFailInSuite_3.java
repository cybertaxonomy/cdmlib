/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBaseTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionDaoHibernateImplTest;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImplTest;


@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
            //MethodCacheImplTest.class,
            //GroupDaoHibernateImplTest.class,
            //OccurrenceDaoHibernateImplTest.class,
            CdmEntityDaoBaseTest.class,
            /*
             * FIXME:
             * DescriptionDaoHibernateImplTest.class Fails with "java.sql.BatchUpdateException: failed batch" on rollback after test,
             * this can be circumvented by manually editing org/unitils/dbunit/DbUnitModule.java in order to disable batched statements:
             * LINE 461: config.setProperty(FEATURE_BATCHED_STATEMENTS, "false");
             *
             * This reveals other errors during org.unitils.database.DatabaseModule.rollbackTransaction()
             * of test method testSaveClonedDescription():
             *
             *  - in hsqldb:
             *      Caused by: java.sql.SQLException: Violation of unique constraint SYS_PK_1209:
             *      duplicate value(s) for column(s) ID in statement
             *      [insert into TaxonBase (created, createdby_id, uuid, updated, updatedby_id, lsid_authority, lsid_lsid, lsid_namespace, lsid_object, lsid_revision, protectedtitlecache, titleCache, appendedphrase, doubtful, name_id, sec_id, usenamecache, excluded, taxonstatusunknown, unplaced, DTYPE, id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Taxon', ?)]
             *		at org.hsqldb.jdbc.Util.throwError(Util.java:58)
             *
             *  - in h2:
             * 		Caused by: org.h2.jdbc.JdbcSQLException: Unique index or primary key violation:
             *      PRIMARY_KEY_C0 ON PUBLIC.TAXONBASE(ID); SQL statement:
             * 		insert into TaxonBase (created, createdby_id, uuid, updated, updatedby_id, lsid_authority, lsid_lsid, lsid_namespace, lsid_object, lsid_revision, protectedtitlecache, titleCache, appendedphrase, doubtful, name_id, sec_id, usenamecache, excluded, taxonstatusunknown, unplaced, DTYPE, id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Taxon', ?) [23001-115]
             *
             */
            DescriptionDaoHibernateImplTest.class,
            TaxonDaoHibernateImplTest.class

        }
    )
public class TestsShouldNotFailInSuite_3 {

    // the class remains completely empty,
    // being used only as a holder for the above annotations

}
