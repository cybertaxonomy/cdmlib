/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.permission;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.permission.CdmAuthority;
import eu.etaxonomy.cdm.model.permission.Operation;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.persistence.dao.permission.ICdmAuthorityDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class CdmAuthorityDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	private ICdmAuthorityDao authorityDao;

	private UUID uuidAuthority1 = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");



	@Before
	public void setUp() {

	}

	@Test
	public void testSave() {
	    CdmAuthority authority = CdmAuthority.NewInstance(PermissionClass.AGENTBASE,
	            "property", Operation.CREATE, uuidAuthority1);
		authorityDao.save(authority);
	}

    @Override
    @Test
    public void createTestDataSet() throws FileNotFoundException {
        CdmAuthority authority = CdmAuthority.NewInstance(PermissionClass.AGENTBASE,
                "property", Operation.CREATE, uuidAuthority1);
        authorityDao.save(authority);

        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = null;

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "CDMAUTHORITY" // IMPORTANT!!!
            },
            fileNameAppendix, true );
    }


}
