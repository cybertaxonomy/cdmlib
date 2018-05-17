/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;

/**
 * @author a.babadshanjan
 * @since 19.05.2009
 */
public class SpecimenTypeDesignationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenTypeDesignationTest.class);

	private SpecimenTypeDesignation term1 = null;
	private SpecimenTypeDesignation term2 = null;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	@Before
	public void setUp() {
		term1 = new SpecimenTypeDesignation();
		term2 = new SpecimenTypeDesignation(DerivedUnit.NewPreservedSpecimenInstance(),
				SpecimenTypeDesignationStatus.ISOTYPE(), null, null, null, false);
	}

	@Test
	public void testSpecimenTypeDesignation() {
		assertNotNull(term1);
		assertNotNull(term2);
	}

	@Test
	public void testGetTypeStatus() {
		SpecimenTypeDesignationStatus std = term1.getTypeStatus();
		std = term2.getTypeStatus();
		term1.setTypeStatus(SpecimenTypeDesignationStatus.EPITYPE());
		assertEquals(term1.getTypeStatus(), SpecimenTypeDesignationStatus.EPITYPE());
		assertTrue(term1.getTypeStatus().isInstanceOf(SpecimenTypeDesignationStatus.class));
	}

	@Test
	public void testBidirectionalRelationship(){
		term2 = new SpecimenTypeDesignation(DerivedUnit.NewPreservedSpecimenInstance(), SpecimenTypeDesignationStatus.ISOTYPE(),
				null, null, null, false);
		DerivedUnit specimen = term2.getTypeSpecimen();
		Set<SpecimenTypeDesignation> set = specimen.getSpecimenTypeDesignations();
		boolean contains = false;
		for (SpecimenTypeDesignation spec: set){
			if (spec.equals(term2)){
				contains = true;
				break;
			}
		}
		assertTrue(contains);
	}

    @Test
    public void beanTests(){
//      #5307 Test that BeanUtils does not fail
        BeanUtils.getPropertyDescriptors(SpecimenTypeDesignation.class);
        BeanUtils.getPropertyDescriptors(NameTypeDesignation.class);
        BeanUtils.getPropertyDescriptors(HomotypicalGroup.class);
        BeanUtils.getPropertyDescriptors(NomenclaturalStatusType.class);
        BeanUtils.getPropertyDescriptors(Rank.class);
    }


}
