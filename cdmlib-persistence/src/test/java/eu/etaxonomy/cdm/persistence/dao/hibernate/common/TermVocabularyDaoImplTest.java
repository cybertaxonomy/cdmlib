/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 10.02.2009
 * @version 1.0
 */
public class TermVocabularyDaoImplTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TermVocabularyDaoImplTest.class);

	@SpringBeanByType
	private ITermVocabularyDao dao;

	@Before
	public void setUp() {
	}
	
	@Test
	public void testListVocabularyByClass() {
		List<TermVocabulary<Rank>> rankVocabularies = dao.listByTermClass(Rank.class, null, null, null, null);
		
		assertFalse("There should be at least one vocabulary containing terms of class Rank",rankVocabularies.isEmpty());
		assertEquals("There should be only one vocabulary containing terms of class Rank",1,rankVocabularies.size());
	}
	
}
