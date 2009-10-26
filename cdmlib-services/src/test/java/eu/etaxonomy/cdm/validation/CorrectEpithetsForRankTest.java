/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * NOTE: In this test, the words "valid" and "invalid", loaded though 
 * these terms are when applied to taxonomic names, only mean "passes the
 * rules of this validator" or not and should not be confused with the strict
 * nomenclatural and taxonomic sense of these words.
 * 
 * @author ben.clark
 *
 */
public class CorrectEpithetsForRankTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(CorrectEpithetsForRankTest.class);
	
	@SpringBeanByType
	private Validator validator;
	
	private BotanicalName name;
	
	@Before
	public void setUp() {
		name = BotanicalName.NewInstance(Rank.SPECIES());
		name.setNameCache("Aus aus");
		name.setAuthorshipCache("L.");
		name.setFullTitleCache("Aus aus L.");
		name.setTitleCache("Aus aus L.");
	}
	
	
/****************** TESTS *****************************/
	
	@Test
	public void testValidSpecificName() {
		name.setGenusOrUninomial("Aus");
		name.setSpecificEpithet("aus");
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertTrue("There should be no constraint violations as this name has the correct epithets for it rank",constraintViolations.isEmpty());
	}
	
	@Test
	public void testInValidSpecificName() {
		name.setGenusOrUninomial("Aus");
		name.setSpecificEpithet(null); // at the default level, this property can be null
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertFalse("There should be a constraint violation as this name does not have a specific epithet",constraintViolations.isEmpty());
	}
	
	@Test
	public void testValidFamilyGroupName() {
		name.setGenusOrUninomial("Aus");
		name.setRank(Rank.FAMILY());
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertTrue("There should be no constraint violations as this name has the correct epithets for it rank",constraintViolations.isEmpty());
	}
	
	@Test
	public void testInValidFamilyGroupName() {
		name.setGenusOrUninomial("Aus");
		name.setRank(Rank.FAMILY()); // at the default level, this property can be null
		name.setSpecificEpithet("aus");
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertFalse("There should be a constraint violation as this name does not have a specific epithet",constraintViolations.isEmpty());
	}
	
	@Test
	public void testValidGenusGroupName() {
		name.setGenusOrUninomial("Aus");
		name.setInfraGenericEpithet("bus");
		name.setRank(Rank.SUBGENUS());
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertTrue("There should be no constraint violations as this name has the correct epithets for it rank",constraintViolations.isEmpty());
	}
	
	@Test
	public void testInValidGenusGroupName() {
		name.setGenusOrUninomial("Aus");
		name.setRank(Rank.SUBGENUS()); // at the default level, this property can be null
		name.setSpecificEpithet("aus");
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertFalse("There should be a constraint violation as this name does not have a specific epithet",constraintViolations.isEmpty());
	}
	
	@Test
	public void testValidInfraSpecificName() {
		name.setGenusOrUninomial("Aus");
		name.setSpecificEpithet("aus");
		name.setInfraSpecificEpithet("ceus");
		name.setRank(Rank.SUBSPECIES());
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertTrue("There should be no constraint violations as this name has the correct epithets for it rank",constraintViolations.isEmpty());
	}
	
	@Test
	public void testInValidInfraSpecificName() {
		name.setGenusOrUninomial("Aus");
		name.setRank(Rank.SUBGENUS()); // at the default level, this property can be null
		name.setSpecificEpithet("aus");
        Set<ConstraintViolation<BotanicalName>> constraintViolations  = validator.validate(name, Level2.class);
        assertFalse("There should be a constraint violation as this name does not have a specific epithet",constraintViolations.isEmpty());
	}
}
