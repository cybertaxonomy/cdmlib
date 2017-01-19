/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.etaxonomy.cdm.model.metadata.PreferenceSubjectEnum;

public class PreferenceSubjectEnumTest {

	@Test
	public void test() {
		PreferenceSubjectEnum databaseType = PreferenceSubjectEnum.Database;
		assertEquals(PreferenceSubjectEnum.getByKey("DB"), databaseType);
	}
	
	@Test
	public void testKindOf(){
		assertSame(PreferenceSubjectEnum.Database, PreferenceSubjectEnum.Classification.getKindOf());
		assertSame(PreferenceSubjectEnum.Classification, PreferenceSubjectEnum.TaxonSubTree.getKindOf());
	}
	
	@Test
	public void testGeneralisationOf(){
		assertTrue(PreferenceSubjectEnum.Database.getGeneralizationOf().contains(PreferenceSubjectEnum.Classification));
		assertFalse("generalisationOf may contain only contain only direct children, but taxonSubTree is a grandchild",
				PreferenceSubjectEnum.Database.getGeneralizationOf().contains(PreferenceSubjectEnum.TaxonSubTree));	
	}
	
	@Test
	public void testGeneralisationOfRecursive(){
		boolean recursive = true;
		assertTrue(PreferenceSubjectEnum.Database.getGeneralizationOf(recursive).contains(PreferenceSubjectEnum.Classification));
		assertTrue(PreferenceSubjectEnum.Database.getGeneralizationOf(recursive).contains(PreferenceSubjectEnum.TaxonSubTree));
		assertFalse(PreferenceSubjectEnum.Database.getGeneralizationOf(!recursive).contains(PreferenceSubjectEnum.TaxonSubTree));		
	}
	

}
