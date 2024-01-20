/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.TaxonOccurrenceRelType;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class OccurrenceDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByType
	private IOccurrenceDao dao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

//**************** TESTS ************************************************

	@Test
	public void testRebuildIndex() {
		logger.warn("testRebuildIndex not yet implemented");
	}

    @Test
    public void testCountMedia() {
        MediaSpecimen unit = MediaSpecimen.NewInstance(SpecimenOrObservationType.Media);
        SpecimenDescription desc = SpecimenDescription.NewInstance(unit);
        desc.setImageGallery(true);
        TextData textData = TextData.NewInstance(Feature.IMAGE());
        desc.addElement(textData);
        Media media1 = Media.NewInstance(URI.create("https://www.abc.de"), 5, "jpg", "jpg");
        Media media2 = Media.NewInstance(URI.create("https://www.abc.de"), 5, "jpg", "jpg");
        textData.addMedia(media1);
        textData.addMedia(media2);

        Media media3 = Media.NewInstance(URI.create("https://www.abc.de"), 5, "jpg", "jpg");
        unit.setMediaSpecimen(media3);
        dao.save(unit);

        Assert.assertEquals(3, dao.countMedia(unit));
        unit.setMediaSpecimen(media2);
        Assert.assertEquals(2, dao.countMedia(unit));
    }

    @Test
    public void testGetMedia() {
        MediaSpecimen unit = MediaSpecimen.NewInstance(SpecimenOrObservationType.Media);
        SpecimenDescription desc = SpecimenDescription.NewInstance(unit);
        desc.setImageGallery(true);
        TextData textData = TextData.NewInstance(Feature.IMAGE());
        desc.addElement(textData);
        Media media1 = Media.NewInstance(URI.create("https://www.abc.de"), 5, "jpg", "jpg");
        Media media2 = Media.NewInstance(URI.create("https://www.defg.de"), 5, "jpg", "jpg");
        textData.addMedia(media1);
        textData.addMedia(media2);

        Media media3 = Media.NewInstance(URI.create("https://www.hij.de"), 5, "jpg", "jpg");
        unit.setMediaSpecimen(media3);
        dao.save(unit);

        List<Media> media = dao.getMedia(unit, null, null, null);
        Assert.assertEquals(3, media.size());
        //test that paging works (note: the sorting is not a requirement for the method, but with current implementation it works; if implementation is changed the test may need to be adapted
        List<Integer> ids = media.stream().map(m->m.getId()).collect(Collectors.toList());
        Collections.sort(ids);
        Assert.assertEquals(ids.get(0), (Integer)media.get(0).getId());
        Assert.assertEquals(ids.get(1), (Integer)media.get(1).getId());
        Assert.assertEquals(ids.get(2), (Integer)media.get(2).getId());
        media = dao.getMedia(unit, 2, 1, null);
        Assert.assertEquals(1, media.size());
        Assert.assertEquals(ids.get(2), (Integer)media.get(0).getId());

        //test deduplication
        unit.setMediaSpecimen(media2);
        Assert.assertEquals(2, dao.countMedia(unit));
    }

	@Test
	public void testSaveOriginalLabelData(){
		DerivedUnit unit = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);
		String originalLabelInfo = StringUtils.pad("my original info", 10000, "x", false);
		Assert.assertEquals(Integer.valueOf(10000),  (Integer)originalLabelInfo.length());
		unit.setOriginalLabelInfo(originalLabelInfo);
		//test that lob is supported
		dao.save(unit);
		//assert no exception
	}

	@Test
	@DataSet
	public void testStatusWithoutTypeCanBeLoaded() {
	    //This is for testing if an occurrence status can be loaded if it has
	    //no type although OccurrenceStatus.type has a NotNull constraint.
	    //Result: it can be loaded and during update an constraint violation
	    //only takes place if the occurrence status record itself is changed.
	    //I tested with changing the specimen or adding an annotation to the
	    //occurrence status with no constraint violation observed.
	    UUID uuid = UUID.fromString("07e70de7-680b-48c1-9c94-50c1aa470a92");
	    DerivedUnit specimen = (DerivedUnit)dao.load(uuid);
	    Assert.assertNotNull(specimen);
	    OccurrenceStatus status = specimen.getStatus().iterator().next();
	    Assert.assertNotNull(status);
	    Assert.assertNull(status.getType());

	    //guarantee that a constraint still exists on OccurrenceStatus.type
	    status.setCitationMicroReference("222");
	    dao.saveOrUpdate(specimen);
	    try {
            commitAndStartNewTransaction(new String[]{/*"OccurrenceStatus","OccurrenceStatus_Annotation","Annotation","SpecimenOrObservationBase" */});
            Assert.fail("Updating status without type should throw an exception");
        } catch (ConstraintViolationException e) {
            //constraint violation expected
            Assert.assertEquals(1, e.getConstraintViolations().size());
            Assert.assertEquals("Occurrence status must have a type defined", e.getConstraintViolations().iterator().next().getMessage());
        }
	}

	@Test
	public void testListByAssociatedTaxon(){

	    //define defaults
        @SuppressWarnings("rawtypes")
        Class<SpecimenOrObservationBase> type = SpecimenOrObservationBase.class;
        boolean no_unpublished = false;
        Integer limit = null;
        Integer start = null;
        List<OrderHint> orderHints = null;
        List<String> propertyPaths = null;
        EnumSet<TaxonOccurrenceRelType> taxonOccRelType = TaxonOccurrenceRelType.All();

        //load data
	    //TODO empty
	    Taxon taxon = createListByAssociationTestData();
        this.commitAndStartNewTransaction();

        //test
        @SuppressWarnings("rawtypes")
        List<SpecimenOrObservationBase> associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, no_unpublished,
                taxonOccRelType,
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals(6, associatedUnits.size());
	}

	private Taxon createListByAssociationTestData(){

	    try {

    	    //sec (not relevant here)
            Reference sec = ReferenceFactory.newBook();
            sec.setTitleCache("Taxon sec reference", true);

            //accepted taxon
            TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Abies", "alba", null, null, null, null, null, null);
            Taxon taxon = Taxon.NewInstance(name1, sec);

            //homotypic synonym
            TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Pinus", "alba", null, null, null, null, null, null);
            Synonym homoSyn = taxon.addHomotypicSynonymName(name2);

            //heterotypic group
            TaxonName name3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Abies", "pinus", null, null, null, null, null, null);
            Synonym heteroSyn = taxon.addHeterotypicSynonymName(name2, sec, null, null);

            TaxonName name4 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Pinus", "pinus",
                    null, null, null, null, null, null);
            Synonym heteroSynBasio = taxon.addHeterotypicSynonymName(name4, sec, null, name3.getHomotypicalGroup());

            taxonDao.save(taxon);

            DerivedUnit du1 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du1.setTitleCache("Specimen1", true);
            DerivedUnit du2 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du2.setTitleCache("Specimen2", true);
            DerivedUnit du3 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du3.setTitleCache("Specimen3", true);
            DerivedUnit du4 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du4.setTitleCache("Specimen4", true);
            DerivedUnit du5 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du5.setTitleCache("Specimen5", true);
            DerivedUnit du6 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du6.setTitleCache("Specimen6", true);

            dao.save(du1);
            dao.save(du2);
            dao.save(du3);
            dao.save(du4);
            dao.save(du5);
            dao.save(du6);

            //TODO unused
            FieldUnit fu1 = DerivedUnitFacade.NewInstance(du1).getFieldUnit(true);
            FieldUnit fu2 = DerivedUnitFacade.NewInstance(du1).getFieldUnit(true);
            FieldUnit fu3 = DerivedUnitFacade.NewInstance(du1).getFieldUnit(true);
            FieldUnit fu4 = DerivedUnitFacade.NewInstance(du1).getFieldUnit(true);
            dao.save(fu1);
            dao.save(fu2);
            dao.save(fu3);
            dao.save(fu4);

            //du1 is added as indiv. association
            TaxonDescription td = TaxonDescription.NewInstance(taxon);
            IndividualsAssociation ia = IndividualsAssociation.NewInstance(du1);
            td.addElement(ia);

            //du2 is added as determination
            DeterminationEvent.NewInstance(taxon, du2);

           //du3 is added as type designation for the accepted taxon
           name1.addSpecimenTypeDesignation(du3, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);

           //du4 is added as type designation for the homotypic synonym
           name2.addSpecimenTypeDesignation(du4, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);

           //du5 is added as type designation for the heterotypic synonym
           name3.addSpecimenTypeDesignation(du5, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);

           //du6 is added as type designation for the other heterotypic synonym
           name4.addSpecimenTypeDesignation(du6, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);

            return taxon;
        } catch (Exception e) {
            Assert.fail("No exception should be thrown during data creation: " + e.getMessage());
            return null;
        }
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {


        // 1. create the entities   and save them




        //add determination

        //add type designation

        //add individuals association

        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = null;

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "TAXONBASE", "TAXONNAME",
            "TAXONRELATIONSHIP",
            "REFERENCE",
            "AGENTBASE", "HOMOTYPICALGROUP",
            "CLASSIFICATION", "TAXONNODE",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix, true );
    }
}