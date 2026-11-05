/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.replace;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author ben.clark
 * @since 22.12.2009
 */
@Disabled
public class ReferringObjectMetadataFactoryTest extends CdmTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@Autowired
	private ReferringObjectMetadataFactory referringObjectMetadataFactory;

	@Autowired
	private IAgentDao agentDao;

	private UUID institution1;

	private UUID institution2;

    private UUID person1;

	private UUID person2;

	private UUID person3;

	@BeforeEach
	public void setUp() throws Exception {
		institution1 = UUID.fromString("18679846-7343-4e5f-b14e-5eb56b967989");
		institution2 = UUID.fromString("28f6aaa5-e03e-4831-9ce2-71eaf56cdebe");
		person1 = UUID.fromString("ed6ac546-8c6c-48c4-9b91-40b1157c05c6");
		person2 = UUID.fromString("e4ec436a-3e8c-4166-a834-3bb84c2b5ad6");
		person3 = UUID.fromString("c62cd389-d787-47f4-99c3-b80eb12a1ef2");
	}

	@Test
	public void testReferringObjectMetadataFactory() throws Exception {
		referringObjectMetadataFactory.get(Person.class);
	}

	@Test
	@CdmDataSet
	public void testReplaceUnmapped() throws Exception {
		Institution x = (Institution)agentDao.findByUuid(institution1);
		Institution y = (Institution)agentDao.findByUuid(institution2);

		Assert.assertNotNull(x);
		Assert.assertNotNull(y);

		agentDao.replace(x,y);
	}

	@Test
	@CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml",
            "ReferringObjectMetadataFactoryTest.xml"
    })
	@CdmExpectedDataSet
	public void testReplaceToOneProperty() throws Exception {
		Person x = (Person)agentDao.findByUuid(person1);
		Person y = (Person)agentDao.findByUuid(person2);

		Assert.assertNotNull(x);
		Assert.assertNotNull(y);

		agentDao.replace(x,y);
		this.setComplete();
		this.endTransaction();
	}

	@Test
	@CdmDataSet
	//@CdmExpectedDataSet
	public void testReplaceToManyProperty() throws Exception {
		Person x = (Person)agentDao.findByUuid(person3);
		Person y = (Person)agentDao.findByUuid(person2);

		Assert.assertNotNull(x);
		Assert.assertNotNull(y);

		agentDao.replace(x,y);
	}

	@Test
	public void testIgnoreBidirectionalRelationship() {
		referringObjectMetadataFactory.get(TaxonName.class);
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}