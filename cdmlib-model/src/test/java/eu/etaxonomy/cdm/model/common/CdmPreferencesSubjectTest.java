// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class CdmPreferencesSubjectTest {

	@Test
	public void test() {
		CdmPreferencesSubject databaseType = CdmPreferencesSubject.Database;
		assertEquals(CdmPreferencesSubject.getByKey("DB"), databaseType);
	}
	
	@Test
	public void testKindOf(){
		assertSame(CdmPreferencesSubject.Database, CdmPreferencesSubject.Classification.getKindOf());
		assertSame(CdmPreferencesSubject.Classification, CdmPreferencesSubject.TaxonSubTree.getKindOf());
	}
	
	@Test
	public void testGeneralisationOf(){
		assertTrue(CdmPreferencesSubject.Database.getGeneralizationOf().contains(CdmPreferencesSubject.Classification));
		assertFalse("generalisationOf may contain only contain only direct children, but taxonSubTree is a grandchild",
				CdmPreferencesSubject.Database.getGeneralizationOf().contains(CdmPreferencesSubject.TaxonSubTree));	
	}
	
	@Test
	public void testGeneralisationOfRecursive(){
		boolean recursive = true;
		assertTrue(CdmPreferencesSubject.Database.getGeneralizationOf(recursive).contains(CdmPreferencesSubject.Classification));
		assertTrue(CdmPreferencesSubject.Database.getGeneralizationOf(recursive).contains(CdmPreferencesSubject.TaxonSubTree));
		assertFalse(CdmPreferencesSubject.Database.getGeneralizationOf(!recursive).contains(CdmPreferencesSubject.TaxonSubTree));		
	}
	

}
