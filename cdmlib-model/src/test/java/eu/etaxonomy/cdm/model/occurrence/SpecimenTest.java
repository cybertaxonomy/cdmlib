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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.term.DefinedTerm;

/**
 * @author a.mueller
 * @since 28.10.2008
 */
public class SpecimenTest {

    /**
     * @author a.kohlbecker
     * @since Jan 18, 2021
     */
    private final class ByTtitleCacheComparator implements Comparator<IdentifiableEntity> {
        @Override
        public int compare(IdentifiableEntity o1, IdentifiableEntity o2) {
            return o1.getTitleCache().compareTo(o2.getTitleCache());
        }
    }

    private static final Logger logger = Logger.getLogger(SpecimenTest.class);

	private DerivedUnit specimen;

	@Before
	public void setUp() throws Exception {
		specimen = DerivedUnit.NewPreservedSpecimenInstance();
	}

	@Test
	public void testNewInstance() {
		DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
		assertNotNull(specimen);
		assertTrue(specimen.getRecordBasis().equals(SpecimenOrObservationType.PreservedSpecimen));
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
	public void testBidirectionalSpecimenDescription(){
		Assert.assertNotNull("Specimen should exist", specimen);

		SpecimenDescription desc = SpecimenDescription.NewInstance(specimen);
		Assert.assertNotNull("Description should exist.", desc);
		Assert.assertSame("Descriptions specimen should be set correctly", desc.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertTrue("Specimen should contain description", specimen.getDescriptions().contains(desc));

		SpecimenDescription desc2 = SpecimenDescription.NewInstance();
		Assert.assertNotNull("Description should exist.", desc2);
		specimen.addDescription(desc2);
		Assert.assertSame("Description2 specimen should be set correctly", desc2.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertSame("Descriptions specimen should still be set correctly", desc.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertTrue("Specimen should contain description2", specimen.getDescriptions().contains(desc2));
		Assert.assertTrue("Specimen should still contain description", specimen.getDescriptions().contains(desc));

		SpecimenDescription desc3 = SpecimenDescription.NewInstance();
		Assert.assertNotNull("Description should exist.", desc3);
		desc3.setDescribedSpecimenOrObservation(specimen);
		Assert.assertSame("Description3 specimen should be set correctly", desc3.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertSame("Descriptions2 specimen should still be set correctly", desc2.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertSame("Descriptions specimen should still be set correctly", desc.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertTrue("Specimen should contain description3", specimen.getDescriptions().contains(desc3));
		Assert.assertTrue("Specimen should still contain description2", specimen.getDescriptions().contains(desc2));
		Assert.assertTrue("Specimen should still contain description", specimen.getDescriptions().contains(desc));

		//change specimen of a given description
		DerivedUnit specimen2 = DerivedUnit.NewPreservedSpecimenInstance();
		Assert.assertNotNull("Specimen should exist.", specimen2);
		desc3.setDescribedSpecimenOrObservation(specimen2);
		Assert.assertSame("Description3 new specimen should be set correctly", desc3.getDescribedSpecimenOrObservation(),specimen2);
		Assert.assertSame("Descriptions2 specimen should still be set correctly", desc2.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertSame("Descriptions specimen should still be set correctly", desc.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertTrue("Specimen2 should contain description3", specimen2.getDescriptions().contains(desc3));
		Assert.assertEquals("Specimen2 should contain exactly 1 description", 1, specimen2.getDescriptions().size());
		Assert.assertFalse("Specimen should no longer contain description3", specimen.getDescriptions().contains(desc3));
		Assert.assertTrue("Specimen should still contain description2", specimen.getDescriptions().contains(desc2));
		Assert.assertTrue("Specimen should still contain description", specimen.getDescriptions().contains(desc));

		//remove description which is not contained
		specimen.removeDescription(desc3);
		Assert.assertSame("Nothing should have changed", desc3.getDescribedSpecimenOrObservation(),specimen2);
		Assert.assertSame("Nothing should have changed", desc2.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertSame("Nothing should have changed", desc.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertTrue("Nothing should have changed", specimen2.getDescriptions().contains(desc3));
		Assert.assertEquals("Nothing should have changed", 1, specimen2.getDescriptions().size());
		Assert.assertFalse("Nothing should have changed", specimen.getDescriptions().contains(desc3));
		Assert.assertTrue("Nothing should have changed", specimen.getDescriptions().contains(desc2));
		Assert.assertTrue("Nothing should have changed", specimen.getDescriptions().contains(desc));

		//remove description
		specimen.removeDescription(desc2);
		Assert.assertNull("Descriptions2 specimen should not exist anymore", desc2.getDescribedSpecimenOrObservation());
		Assert.assertSame("Description3 specimen should still be set correctly", desc3.getDescribedSpecimenOrObservation(),specimen2);
		Assert.assertSame("Descriptions specimen should still be set correctly", desc.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertTrue("Specimen2 should still contain description3", specimen2.getDescriptions().contains(desc3));
		Assert.assertEquals("Specimen2 should still contain exactly 1 description", 1, specimen2.getDescriptions().size());
		Assert.assertFalse("Specimen should not contain description2 anymore", specimen.getDescriptions().contains(desc2));
		Assert.assertFalse("Specimen should still no longer contain description3", specimen.getDescriptions().contains(desc3));
		Assert.assertTrue("Specimen should still contain description", specimen.getDescriptions().contains(desc));

		//remove description by setting null specimen
		desc3.setDescribedSpecimenOrObservation(null);
		Assert.assertNull("Description3 specimen should not exist anymore", desc3.getDescribedSpecimenOrObservation());
		Assert.assertNull("Descriptions2 specimen should still not exist anymore", desc2.getDescribedSpecimenOrObservation());
		Assert.assertSame("Descriptions specimen should still be set correctly", desc.getDescribedSpecimenOrObservation(),specimen);
		Assert.assertFalse("Specimen2 should not contain description3 anymore", specimen2.getDescriptions().contains(desc3));
		Assert.assertEquals("Specimen2 should contain no description now", 0, specimen2.getDescriptions().size());
		Assert.assertFalse("Specimen should still no longer contain description2", specimen.getDescriptions().contains(desc2));
		Assert.assertFalse("Specimen should still no longer contain description3", specimen.getDescriptions().contains(desc3));
		Assert.assertTrue("Specimen should still contain description", specimen.getDescriptions().contains(desc));
	}

	@Test
	public void testBidirectionalTypeDesignation(){
		SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
		SpecimenTypeDesignation desig2 = SpecimenTypeDesignation.NewInstance();
		DerivedUnit specimen2 = DerivedUnit.NewPreservedSpecimenInstance();

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

	@Test
	public void testClone() {
		logger.debug("Start testClone");

		//Null test is not full implemented, but an error is thrown if null throws
		//null pointer exception somewhere
		DerivedUnit specimenNullClone = specimen.clone();

		String accessionNumber = "accNumber";
		String catalogNumber = "catNumber";
		Collection collection = Collection.NewInstance();
		collection.setCode("code");
		DateTime created = new DateTime();
		Person createdBy = Person.NewTitledInstance("creator");
		DerivationEvent derivedFrom = DerivationEvent.NewInstance(null);
		int id = 22;
		String individualCount = "25";
		DefinedTerm lifeStage = DefinedTerm.NewStageInstance(null, null, null);
		LSID lsid = null;
		try {
			lsid = new LSID("urn:lsid:example.com:foo:1");
		} catch (MalformedLSIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		DerivedUnit nextVersion = DerivedUnit.NewPreservedSpecimenInstance();
//		DerivedUnit previousVersion = DerivedUnit.NewPreservedSpecimenInstance();
		PreservationMethod preservation = PreservationMethod.NewInstance();
		boolean protectedTitleCache = true;
		DefinedTerm sex = DefinedTerm.SEX_FEMALE();
		TaxonName storedUnder = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
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
//		Media media = Media.NewInstance();
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
//		specimen.addMedia(media);    #3597
		specimen.addRights(right);
		specimen.addSource(source);

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			//ignore
		}
		DerivedUnit specimenClone = specimen.clone();

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

		// FIXME check if descriptions and determinations were correctly cloned
//		assertEquals(description, specimenClone.getDescriptions().iterator().next());
//		// TODO ?
//		assertSame(description, specimenClone.getDescriptions().iterator().next());
//
//		assertEquals(determination, specimenClone.getDeterminations().iterator().next());
//		// TODO ?
//		assertSame(determination, specimenClone.getDeterminations().iterator().next());

		assertFalse(extension.equals(specimenClone.getExtensions().iterator().next()));
		assertEquals(extension.getValue(), specimenClone.getExtensions().iterator().next().getValue());
		assertNotSame(extension, specimenClone.getExtensions().iterator().next());
		assertEquals(1, specimen.getExtensions().size());

		assertFalse(marker.equals(specimenClone.getMarkers().iterator().next()));
		assertEquals(marker.getFlag(), specimenClone.getMarkers().iterator().next().getFlag());
		assertNotSame(marker, specimenClone.getMarkers().iterator().next());
		assertEquals(1, specimenClone.getMarkers().size());

//		assertEquals(media, specimenClone.getMedia().iterator().next());  #3597
//		assertEquals(right, specimenClone.getRights().iterator().next()); #5762
		assertTrue("Rights must contain 1 rights object", specimenClone.getRights().size() == 1);
  	    assertTrue("Rights must not be cloned", specimenClone.getRights().iterator().next().equals(right));

		assertFalse(source.equals(specimenClone.getSources().iterator().next()));
		assertEquals(source.getId(), ((OriginalSourceBase<?>)specimenClone.getSources().iterator().next()).getId());
		assertNotSame(source, specimenClone.getSources().iterator().next());
		assertEquals(1, specimenClone.getSources().size());
	}

    @Test
    public void beanTests(){
//      #5307 Test that BeanUtils does not fail
        BeanUtils.getPropertyDescriptors(DerivedUnit.class);
        BeanUtils.getPropertyDescriptors(SpecimenOrObservationBase.class);
        BeanUtils.getPropertyDescriptors(FieldUnit.class);
        BeanUtils.getPropertyDescriptors(MediaSpecimen.class);
        BeanUtils.getPropertyDescriptors(DnaSample.class);
    }

    @Test
    public void testCollectRootUnits() {

        java.util.Collection<SpecimenOrObservationBase> rootSOBs;

        DerivedUnit theSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
        theSpecimen.setTitleCache("*SP*", true);

        rootSOBs = theSpecimen.collectRootUnits(null);
        assertEquals(1, rootSOBs.size());
        assertEquals(theSpecimen, rootSOBs.iterator().next());

        rootSOBs = theSpecimen.collectRootUnits(SpecimenOrObservationBase.class);
        assertEquals(1, rootSOBs.size());
        assertEquals(theSpecimen, rootSOBs.iterator().next());

        java.util.Collection<DerivedUnit> rootDUs = theSpecimen.collectRootUnits(DerivedUnit.class);
        assertEquals(1, rootDUs.size());
        assertEquals(theSpecimen, rootDUs.iterator().next());

        java.util.Collection<FieldUnit> rootFUs = theSpecimen.collectRootUnits(FieldUnit.class);
        assertTrue(rootFUs.isEmpty());

        // -- one FieldUnit as root ----------------------------------------

        FieldUnit fieldUnit1 = FieldUnit.NewInstance();
        fieldUnit1.setTitleCache("FU1", true);
        DerivationEvent.NewSimpleInstance(fieldUnit1, theSpecimen, DerivationEventType.GATHERING_IN_SITU());

        rootSOBs = theSpecimen.collectRootUnits(null);
        assertEquals(1, rootSOBs.size());
        assertEquals(fieldUnit1, rootSOBs.iterator().next());

        rootSOBs = theSpecimen.collectRootUnits(SpecimenOrObservationBase.class);
        assertEquals(1, rootSOBs.size());
        assertEquals(fieldUnit1, rootSOBs.iterator().next());

        rootDUs = theSpecimen.collectRootUnits(DerivedUnit.class);
        assertTrue(rootDUs.isEmpty());

        rootFUs = theSpecimen.collectRootUnits(FieldUnit.class);
        assertEquals(1, rootFUs.size());
        assertEquals(fieldUnit1, rootFUs.iterator().next());

        // -- multiple roots ------------------------------------------

        DerivedUnit specimen1 = DerivedUnit.NewPreservedSpecimenInstance();
        specimen1.setTitleCache("SP1", true);
        theSpecimen.getDerivedFrom().addOriginal(specimen1);

        rootSOBs = theSpecimen.collectRootUnits(null);
        assertEquals(2, rootSOBs.size());

        rootSOBs = theSpecimen.collectRootUnits(SpecimenOrObservationBase.class);
        assertEquals(2, rootSOBs.size());
        List<SpecimenOrObservationBase> rootSOBsSorted = rootSOBs.stream().sorted(new ByTtitleCacheComparator()).collect(Collectors.toList());
        assertEquals(fieldUnit1, rootSOBsSorted.get(0));
        assertEquals(specimen1, rootSOBsSorted.get(1));

        FieldUnit fieldUnit2 = FieldUnit.NewInstance();
        fieldUnit2.setTitleCache("FU2", true);
        DerivationEvent.NewSimpleInstance(fieldUnit2, specimen1, DerivationEventType.GATHERING_IN_SITU());

        rootSOBs = theSpecimen.collectRootUnits(SpecimenOrObservationBase.class);
        assertEquals(2, rootSOBs.size());
        rootSOBsSorted = rootSOBs.stream().sorted(new ByTtitleCacheComparator()).collect(Collectors.toList());
        assertEquals(fieldUnit1, rootSOBsSorted.get(0));
        assertEquals(fieldUnit2, rootSOBsSorted.get(1));

        // -- multiple roots & cycle detection ------------------------------------------

        DerivedUnit specimen2 = DerivedUnit.NewPreservedSpecimenInstance();
        specimen2.setTitleCache("SP2", true);

        DerivedUnit specimen3 = DerivedUnit.NewPreservedSpecimenInstance();
        specimen3.setTitleCache("SP3", true);

        theSpecimen.getDerivedFrom().addOriginal(specimen2);
        DerivationEvent.NewSimpleInstance(specimen3, specimen2, DerivationEventType.GATHERING_IN_SITU());
        DerivationEvent.NewSimpleInstance(theSpecimen, specimen3, DerivationEventType.GATHERING_IN_SITU());

        rootSOBs = theSpecimen.collectRootUnits(SpecimenOrObservationBase.class);
        assertEquals(3, rootSOBs.size());
        rootSOBsSorted = rootSOBs.stream().sorted(new ByTtitleCacheComparator()).collect(Collectors.toList());
        assertEquals(fieldUnit1, rootSOBsSorted.get(0));
        assertEquals(fieldUnit2, rootSOBsSorted.get(1));
        assertEquals(specimen3, rootSOBsSorted.get(2));


    }
}
