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

public class TermTypeTest {

	@Test
	public void test() {
		TermType languageType = TermType.Language;
		assertEquals(TermType.getByKey("LA"), languageType);
	}
	
	@Test
	public void testKindOf(){
		assertSame(TermType.Modifier, TermType.DeterminationModifier.getKindOf());
		assertSame(TermType.Modifier, TermType.Scope.getKindOf());
		assertSame(TermType.Scope, TermType.Sex.getKindOf());
		assertSame(TermType.Scope, TermType.Stage.getKindOf());
	}
	
	@Test
	public void testGeneralisationOf(){
		assertTrue(TermType.Modifier.getGeneralizationOf().contains(TermType.DeterminationModifier));
		assertTrue(TermType.Modifier.getGeneralizationOf().contains(TermType.Scope));
		assertTrue(TermType.Scope.getGeneralizationOf().contains(TermType.Sex));
		assertTrue(TermType.Scope.getGeneralizationOf().contains(TermType.Stage));
		assertFalse("generalisationOf may contain only contain only direct children, but stage is a grandchild", TermType.Modifier.getGeneralizationOf().contains(TermType.Stage));	
	}
	
	@Test
	public void testGeneralisationOfRecursive(){
		boolean recursive = true;
		assertTrue(TermType.Modifier.getGeneralizationOf(recursive).contains(TermType.Scope));
		assertTrue(TermType.Modifier.getGeneralizationOf(! recursive).contains(TermType.Scope));
		assertTrue(TermType.Scope.getGeneralizationOf(recursive).contains(TermType.Sex));
		assertTrue(TermType.Modifier.getGeneralizationOf(recursive).contains(TermType.Stage));		
		assertFalse(TermType.Modifier.getGeneralizationOf(!recursive).contains(TermType.Stage));		
	}
	

}
