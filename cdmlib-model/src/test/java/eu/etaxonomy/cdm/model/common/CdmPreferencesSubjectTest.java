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

import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;

public class CdmPreferencesSubjectTest {

	@Test
	public void test() {
		PreferenceSubject databaseType = PreferenceSubject.Database;
		assertEquals(PreferenceSubject.getByKey("DB"), databaseType);
	}
	
	@Test
	public void testKindOf(){
		assertSame(PreferenceSubject.Database, PreferenceSubject.Classification.getKindOf());
		assertSame(PreferenceSubject.Classification, PreferenceSubject.TaxonSubTree.getKindOf());
	}
	
	@Test
	public void testGeneralisationOf(){
		assertTrue(PreferenceSubject.Database.getGeneralizationOf().contains(PreferenceSubject.Classification));
		assertFalse("generalisationOf may contain only contain only direct children, but taxonSubTree is a grandchild",
				PreferenceSubject.Database.getGeneralizationOf().contains(PreferenceSubject.TaxonSubTree));	
	}
	
	@Test
	public void testGeneralisationOfRecursive(){
		boolean recursive = true;
		assertTrue(PreferenceSubject.Database.getGeneralizationOf(recursive).contains(PreferenceSubject.Classification));
		assertTrue(PreferenceSubject.Database.getGeneralizationOf(recursive).contains(PreferenceSubject.TaxonSubTree));
		assertFalse(PreferenceSubject.Database.getGeneralizationOf(!recursive).contains(PreferenceSubject.TaxonSubTree));		
	}
	

}
