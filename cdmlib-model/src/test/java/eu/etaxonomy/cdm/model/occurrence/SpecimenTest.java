/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author a.mueller
 * @created 28.10.2008
 * @version 1.0
 */
public class SpecimenTest {
	private static final Logger logger = Logger.getLogger(SpecimenTest.class);

	private Specimen specimen;
	
	@Before
	public void setUp() throws Exception {
		specimen = Specimen.NewInstance();
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.occurrence.Specimen#NewInstance()}.
	 */
	@Test
	public void testNewInstance() {
		Specimen specimen = Specimen.NewInstance();
		assertNotNull(specimen);
		assertTrue(specimen instanceof Specimen);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.occurrence.Specimen#Specimen()}.
	 */
	@Test
	public void testSpecimen() {
		Specimen specimen = new Specimen();
		assertNotNull(specimen);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.occurrence.Specimen#getPreservation()} and 
	 * {@link eu.etaxonomy.cdm.model.occurrence.Specimen#setPreservation(eu.etaxonomy.cdm.model.occurrence.PreservationMethod)}.
	 */
	@Test
	public void testGetSetPreservation() {
		PreservationMethod preservation = PreservationMethod.NewInstance();
		specimen.setPreservation(preservation);
		assertSame(preservation, specimen.getPreservation());
		specimen.setPreservation(null);
	}
	
	@Test
	public void testBidirectionalTypeDesignation(){
		SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
		SpecimenTypeDesignation desig2 = SpecimenTypeDesignation.NewInstance();
		Specimen specimen2 = Specimen.NewInstance();
		
		specimen.addSpecimenTypeDesignation(desig1);
		Assert.assertEquals("Specimen1 should be the designations specimen", specimen, desig1.getTypeSpecimen());
		Assert.assertEquals("specimen1 should have exactly 1 designation", 1, specimen.getSpecimenTypeDesignations().size());
		Assert.assertEquals("specimen1's designation should be desig1", desig1, specimen.getSpecimenTypeDesignations().iterator().next());
		
		specimen.addSpecimenTypeDesignation(desig2);
		Assert.assertEquals("Specimen1 should be the desig2's specimen", specimen, desig2.getTypeSpecimen());
		Assert.assertEquals("specimen1 should have exactly 2 designation", 2, specimen.getSpecimenTypeDesignations().size());
		
		specimen2.addSpecimenTypeDesignation(desig2);
		Assert.assertEquals("Specimen2 should have replaced specimen1 as desig2's specimen", specimen2, desig2.getTypeSpecimen());
		Assert.assertEquals("Specimen2 should have exactly 1 designation", 1, specimen2.getSpecimenTypeDesignations().size());
		Assert.assertEquals("Specimen1's designation should be desig2", desig2, specimen2.getSpecimenTypeDesignations().iterator().next());
		Assert.assertEquals("specimen1 should have exactly 1 designation", 1, specimen.getSpecimenTypeDesignations().size());
		
		specimen2.removeSpecimenTypeDesignation(desig2);
		Assert.assertEquals("Desig2 should not have a specimen anymore", null, desig2.getTypeSpecimen());
		Assert.assertEquals("Specimen2 should have no designation", 0, specimen2.getSpecimenTypeDesignations().size());
		Assert.assertEquals("specimen1 should have exactly 1 designation", 1, specimen.getSpecimenTypeDesignations().size());
		
		specimen.addSpecimenTypeDesignation(desig2);
		Assert.assertEquals("Specimen1 should be the desig2's specimen", specimen, desig2.getTypeSpecimen());
		Assert.assertEquals("specimen1 should have exactly 2 designation", 2, specimen.getSpecimenTypeDesignations().size());
		
		desig1.setTypeSpecimen(null);
		Assert.assertEquals("Desig1 should not have a specimen anymore", null, desig1.getTypeSpecimen());
		Assert.assertEquals("Specimen1 should have 1 designation", 1, specimen.getSpecimenTypeDesignations().size());
		Assert.assertEquals("Specimen1's designation should be desig2", desig2, specimen.getSpecimenTypeDesignations().iterator().next());
		
		desig1.setTypeSpecimen(specimen);
		Assert.assertEquals("Desig1 should have specimen1 as specimen again", specimen, desig1.getTypeSpecimen());
		Assert.assertEquals("Specimen1 should have 2 designation", 2, specimen.getSpecimenTypeDesignations().size());
		
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.occurrence.Specimen#clone()}.
	 */
	@Test
	public void testClone() {
		logger.debug("Start testClone");
		
		//Null test is not full implemented, but an error is thrown if null throws 
		//null pointer exception somewhere
		Specimen specimenNullClone = (Specimen)specimen.clone();
		
		String accessionNumber = "accNumber";
		String catalogNumber = "catNumber";
		Collection collection = Collection.NewInstance();
		collection.setCode("code");
		DateTime created = new DateTime();
		Person createdBy = Person.NewTitledInstance("creator");
		DerivationEvent derivedFrom = DerivationEvent.NewInstance(null);
		int id = 22;
		int individualCount = 25;
		Stage lifeStage = Stage.NewInstance();
		LSID lsid = null;
		try {
			lsid = new LSID("urn:lsid:example.com:foo:1");
		} catch (MalformedLSIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Specimen nextVersion = Specimen.NewInstance();
		Specimen previousVersion = Specimen.NewInstance();
		PreservationMethod preservation = PreservationMethod.NewInstance();
		boolean protectedTitleCache = true;
		Sex sex = Sex.FEMALE();
		TaxonNameBase<?, ?> storedUnder = BotanicalName.NewInstance(Rank.GENUS());
		String titleCache = "title";
		Calendar updated = Calendar.getInstance();
		Person updatedBy = Person.NewTitledInstance("updatedPerson");
		UUID uuid = UUID.randomUUID();
		
		Annotation annotation = Annotation.NewDefaultLanguageInstance("annotation");
		String definition = "definition";
		//TODO
		DerivationEvent derivationEvent = DerivationEvent.NewInstance(null);
		SpecimenDescription description = SpecimenDescription.NewInstance();
		DeterminationEvent determination = DeterminationEvent.NewInstance();
		Extension extension = Extension.NewInstance();
		extension.setValue("extension");
		Marker marker = Marker.NewInstance(MarkerType.COMPLETE(), false);
		Rights right = Rights.NewInstance("right", Language.DEFAULT());
		Media media = Media.NewInstance();
		IdentifiableSource source = IdentifiableSource.NewDataImportInstance("12", "idNamespace");
		
		specimen.setAccessionNumber(accessionNumber);
		specimen.setCatalogNumber(catalogNumber);
		specimen.setCollection(collection);
		specimen.setCreated(created);
//		specimen.setCreatedBy(createdBy);
		specimen.setDerivedFrom(derivedFrom);
		specimen.setId(id);
		specimen.setIndividualCount(individualCount);
		specimen.setLifeStage(lifeStage);
		specimen.setLsid(lsid);
		specimen.setPreservation(preservation);
		specimen.setProtectedTitleCache(protectedTitleCache);
		specimen.setSex(sex);
		specimen.setStoredUnder(storedUnder);
		specimen.setTitleCache(titleCache, protectedTitleCache);
//		specimen.setUpdated(updated);
//		specimen.setUpdatedBy(updatedBy);
		specimen.setUuid(uuid);
		
		specimen.addAnnotation(annotation);
		specimen.putDefinition(Language.DEFAULT(), definition);
		specimen.addDerivationEvent(derivationEvent);
		specimen.addDescription(description);
		specimen.addDetermination(determination);
		specimen.addExtension(extension);
		specimen.addMarker(marker);
		specimen.addMedia(media);
		specimen.addRights(right);
		specimen.addSource(source);

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			//ignore
		}
		Specimen specimenClone = (Specimen)specimen.clone();
		
		assertFalse(id == specimenClone.getId());
		assertFalse(created.equals(specimenClone.getCreated()));
		assertFalse(createdBy.equals(specimenClone.getCreatedBy()));
		assertFalse(updated.equals(specimenClone.getUpdated()));
		assertFalse(updatedBy.equals(specimenClone.getUpdatedBy()));
		assertNull(specimenClone.getUpdatedBy());
		assertNull(specimenClone.getCreatedBy());
		assertFalse(uuid.equals(specimenClone.getUuid()));
		
		
		assertEquals(accessionNumber, specimenClone.getAccessionNumber());
		assertEquals(catalogNumber, specimenClone.getCatalogNumber());
		assertEquals(collection, specimenClone.getCollection());
		assertEquals(derivedFrom, specimenClone.getDerivedFrom());
		assertEquals(lifeStage, specimenClone.getLifeStage());
		assertEquals(lsid, specimenClone.getLsid());
		assertEquals(preservation, specimenClone.getPreservation());
		assertEquals(protectedTitleCache, specimenClone.isProtectedTitleCache());
		assertEquals(storedUnder, specimenClone.getStoredUnder());
		assertEquals(sex, specimenClone.getSex());
		assertEquals(titleCache, specimenClone.getTitleCache());
		
		Annotation clonedAnnotation = specimenClone.getAnnotations().iterator().next();
		assertFalse(annotation.equals(clonedAnnotation));
		assertEquals(annotation.getText(), ((LanguageStringBase)specimenClone.getAnnotations().iterator().next()).getText() );
		assertNotSame(annotation, specimenClone.getAnnotations().iterator().next() );
		
		assertEquals(definition, specimenClone.getDefinition().get(Language.DEFAULT()).getText());
//TODO	
//		assertNotSame(definition, specimenClone.getDefinition().getText(Language.DEFAULT()));
		
		assertEquals(derivationEvent, specimenClone.getDerivationEvents().iterator().next());
		assertSame(derivationEvent, specimenClone.getDerivationEvents().iterator().next());
		
		assertEquals(description, specimenClone.getDescriptions().iterator().next());
		// TODO ?
		assertSame(description, specimenClone.getDescriptions().iterator().next());
		
		assertEquals(determination, specimenClone.getDeterminations().iterator().next());
		// TODO ?
		assertSame(determination, specimenClone.getDeterminations().iterator().next());

		assertFalse(extension.equals(specimenClone.getExtensions().iterator().next()));
		assertEquals(extension.getValue(), ((Extension)specimenClone.getExtensions().iterator().next()).getValue());
		assertNotSame(extension, specimenClone.getExtensions().iterator().next());
		assertEquals(1, specimen.getExtensions().size());
		
		assertFalse(marker.equals(specimenClone.getMarkers().iterator().next()));
		assertEquals(marker.getFlag(), ((Marker)specimenClone.getMarkers().iterator().next()).getFlag());
		assertNotSame(marker, specimenClone.getMarkers().iterator().next());
		assertEquals(1, specimenClone.getMarkers().size());
		
		assertEquals(media, specimenClone.getMedia().iterator().next());
		assertEquals(right, specimenClone.getRights().iterator().next());
		
		assertFalse(source.equals(specimenClone.getSources().iterator().next()));
		assertEquals(source.getId(), ((OriginalSourceBase)specimenClone.getSources().iterator().next()).getId());
		assertNotSame(source, specimenClone.getSources().iterator().next());
		assertEquals(1, specimenClone.getSources().size());
	}
}
