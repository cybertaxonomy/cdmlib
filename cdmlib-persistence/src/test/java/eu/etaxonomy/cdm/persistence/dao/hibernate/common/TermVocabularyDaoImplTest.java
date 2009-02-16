/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 10.02.2009
 * @version 1.0
 */
@Ignore
public class TermVocabularyDaoImplTest extends CdmIntegrationTest {
	private static Logger logger = Logger
			.getLogger(TermVocabularyDaoImplTest.class);

	@SpringBeanByType
	private ITermVocabularyDao dao;

	@Before
	public void setUp() {
	}
	
	}
