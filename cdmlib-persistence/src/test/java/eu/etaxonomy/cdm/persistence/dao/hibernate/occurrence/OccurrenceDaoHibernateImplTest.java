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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
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

import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
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
        final Class<SpecimenOrObservationBase> type = SpecimenOrObservationBase.class;
        final boolean included_unpublished = true;
        final boolean no_unpublished = false;
        final Integer limit = null;
        final Integer start = null;
        final List<OrderHint> orderHints = null;
        final List<String> propertyPaths = null;
        final EnumSet<TaxonOccurrenceRelationType> taxonOccRelType = TaxonOccurrenceRelationType.All();

        //load data
	    //TODO make DB empty
	    Taxon taxon = createListByAssociationTestData();

        this.commitAndStartNewTransaction();

        //test
        @SuppressWarnings("rawtypes")
        List<SpecimenOrObservationBase> associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, included_unpublished, taxonOccRelType,
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals("All directly associated specimen should be attached", 7, associatedUnits.size());

        //test published only
        associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, no_unpublished, taxonOccRelType,
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals("All published directly associated specimen should be attached", 6, associatedUnits.size());

        //test include individual associations
        associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, included_unpublished, TaxonOccurrenceRelationType.IndividualsAssociations(),
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals("Only specimen associated via individuals association should be attached", 1, associatedUnits.size());

        //test include determinations
        associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, included_unpublished, TaxonOccurrenceRelationType.Determinations(),
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals("Only specimen associated via determination should be attached", 2, associatedUnits.size());

        //test include current determinations only
        associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, included_unpublished, TaxonOccurrenceRelationType.CurrentDeterminations(),
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals("Only specimen associated via determination should be attached", 1, associatedUnits.size());

        //test include type specimens
        associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, included_unpublished, TaxonOccurrenceRelationType.TypeDesignations(),
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals("Only specimen associated via type designation should be attached", 4, associatedUnits.size());

        //test orderHints
        List<OrderHint> titleCacheOrder = new ArrayList<>();
        titleCacheOrder.add(OrderHint.ORDER_BY_TITLE_CACHE);
        associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, included_unpublished, taxonOccRelType,
                limit, start, titleCacheOrder, propertyPaths);
        @SuppressWarnings("rawtypes")
        Iterator<SpecimenOrObservationBase> iterator = associatedUnits.iterator();
        Assert.assertEquals("FieldUnit 3", iterator.next().getTitleCache());
        Assert.assertEquals("Specimen Accepted Name Type", iterator.next().getTitleCache());
        Assert.assertEquals("Specimen Determination", iterator.next().getTitleCache());
        Assert.assertEquals("Specimen Heterotypic Name Type 1", iterator.next().getTitleCache());
        Assert.assertEquals("Specimen Heterotypic Name Type 2", iterator.next().getTitleCache());
        Assert.assertEquals("Specimen Homotypic Name Type", iterator.next().getTitleCache());
        Assert.assertEquals("Specimen Individual Association", iterator.next().getTitleCache());

        //test limit + start
        int myLimit = 3;
        int myStart = 2;
        associatedUnits = dao.listByAssociatedTaxon(
                type, taxon, included_unpublished, taxonOccRelType,
                myLimit, myStart, titleCacheOrder, propertyPaths);
        Assert.assertEquals(myLimit, associatedUnits.size());
        iterator = associatedUnits.iterator();
        //this is a subset of the above test
        Assert.assertEquals("Specimen Determination", iterator.next().getTitleCache());
        Assert.assertEquals("Specimen Heterotypic Name Type 1", iterator.next().getTitleCache());
        Assert.assertEquals("Specimen Heterotypic Name Type 2", iterator.next().getTitleCache());

        //test type
        //... type all classes (default)
        Taxon fieldUnitTaxon = (Taxon)taxonDao.load(uuid_fieldUnitTaxon);
        associatedUnits = dao.listByAssociatedTaxon(
                type, fieldUnitTaxon, included_unpublished, taxonOccRelType,
                limit, start, orderHints, propertyPaths);
        //TODO do we really want to have the type of the homotypic synonym here
        //     though it is not explicitly mentioned in the synonymy of field unit taxon?
        Assert.assertEquals("Only field unit 1 (added via ind. ass.) and the 2 derived units "
                + " associated to the name and its homotypic group via type designation "
                + " should be returned for field unit taxon",
                3, associatedUnits.size());

        //... test type = FieldUnit.class
        List<FieldUnit> fieldUnits = dao.listByAssociatedTaxon(
                FieldUnit.class, fieldUnitTaxon, included_unpublished, taxonOccRelType,
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals("Only the field unit 1 should be attached to field unit taxon", 1, fieldUnits.size());

        //... test type = DerivedUnit.class
        List<DerivedUnit> derivedUnits = dao.listByAssociatedTaxon(
                DerivedUnit.class, fieldUnitTaxon, included_unpublished, taxonOccRelType,
                limit, start, orderHints, propertyPaths);
        Assert.assertEquals("Only the 2 derived unit associated via type designation should "
                + " be attached to field unit taxon", 2, derivedUnits.size());

        //test synonyms
        //TODO not yet available in listByAssociatedTaxon()
//        Synonym heteroSyn_1 = (Synonym)taxonDao.load(uuid_hetero_syn_1);
//        associatedUnits = dao.listByAssociatedTaxon(
//                type, heteroSyn_1, included_unpublished, taxonOccRelType,
//                limit, start, orderHints, propertyPaths);

	}

	private UUID uuid_du_indAss = UUID.randomUUID();
	private UUID uuid_du_determination = UUID.randomUUID();
	private UUID uuid_du_accType = UUID.randomUUID();
	private UUID uuid_fieldUnitTaxon = UUID.randomUUID();
	private UUID uuid_hetero_syn_1 = UUID.randomUUID();

	private Taxon createListByAssociationTestData(){

	    try {

    	    //sec (not relevant here)
            Reference sec = ReferenceFactory.newBook();
            sec.setTitleCache("Taxon sec reference", true);

            //accepted taxon
            TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Abies", null, "alba", null, null, null, null, null);
            Taxon taxon = Taxon.NewInstance(name1, sec);
            Taxon fieldUnitTaxon = Taxon.NewInstance(name1, sec);
            fieldUnitTaxon.setUuid(uuid_fieldUnitTaxon);

            //homotypic synonym
            TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Pinus", null, "alba", null, null, null, null, null);
            taxon.addHomotypicSynonymName(name2);

            //heterotypic group
            TaxonName name3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Abies", null, "pinus", null, null, null, null, null);
            Synonym heteroSyn1 = taxon.addHeterotypicSynonymName(name2, sec, null, null);
            heteroSyn1.setUuid(uuid_hetero_syn_1);

            TaxonName name4 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Pinus", null, "pinus", null, null, null, null, null);
            taxon.addHeterotypicSynonymName(name4, sec, null, name3.getHomotypicalGroup());

            taxonDao.save(taxon);
            taxonDao.save(fieldUnitTaxon);

            //create associated derived units
            DerivedUnit du_indAss = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du_indAss.setTitleCache("Specimen Individual Association", true);
            du_indAss.setUuid(uuid_du_indAss);

            DerivedUnit du_determination = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du_determination.setTitleCache("Specimen Determination", true);
            du_determination.setUuid(uuid_du_determination);

            DerivedUnit du_accType = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du_accType.setTitleCache("Specimen Accepted Name Type", true);
            du_accType.setUuid(uuid_du_accType);

            DerivedUnit du_homonymType = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du_homonymType.setTitleCache("Specimen Homotypic Name Type", true);

            DerivedUnit du_heteroType1 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du_heteroType1.setTitleCache("Specimen Heterotypic Name Type 1", true);

            DerivedUnit du_heteroType2 = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du_heteroType2.setTitleCache("Specimen Heterotypic Name Type 2", true);
            du_heteroType2.setPublish(false);

            //not associated derived unit
            DerivedUnit du_notAssociated = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            du_notAssociated.setTitleCache("Specimen Not Associated", true);

            dao.save(du_indAss);
            dao.save(du_determination);
            dao.save(du_accType);
            dao.save(du_homonymType);
            dao.save(du_heteroType1);
            dao.save(du_heteroType2);

            //TODO unused
            FieldUnit fu1 = DerivedUnitFacade.NewInstance(du_indAss).getFieldUnit(true);
            fu1.setTitleCache("FieldUnit 1", true);
            FieldUnit fu2 = DerivedUnitFacade.NewInstance(du_determination).getFieldUnit(true);
            FieldUnit fu3 = DerivedUnitFacade.NewInstance(du_accType).getFieldUnit(true);
            fu3.setTitleCache("FieldUnit 3", true);
            FieldUnit fu4 = DerivedUnitFacade.NewInstance(du_homonymType).getFieldUnit(true);
            dao.save(fu1);
            dao.save(fu2);
            dao.save(fu3);
            dao.save(fu4);

            //*** Add assoziations ****

            //du1 is added as indiv. association
            TaxonDescription td = TaxonDescription.NewInstance(taxon);
            IndividualsAssociation ia = IndividualsAssociation.NewInstance(du_indAss);
            td.addElement(ia);

            //du_determination is assoziated as determination
            DeterminationEvent.NewInstance(taxon, du_determination);

            //fu3 is assoziated with name3 (first heterotypic synonym) as current determination
            DeterminationEvent de = DeterminationEvent.NewInstance(heteroSyn1.getName(), fu3);
            de.setPreferredFlag(true);

            //du_accType is added as type designation for the accepted taxon
            name1.addSpecimenTypeDesignation(du_accType, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);

            //du_homonymType is added as type designation for the homotypic synonym
            name2.addSpecimenTypeDesignation(du_homonymType, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);

            //du_heteroType1 is added as type designation for the heterotypic synonym
            name3.addSpecimenTypeDesignation(du_heteroType1, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);

            //du_heteroType2 is added as type designation for the other heterotypic synonym
            name4.addSpecimenTypeDesignation(du_heteroType2, SpecimenTypeDesignationStatus.HOLOTYPE(), null, null, null, false, false);


            TaxonDescription fieldUnitDescription = TaxonDescription.NewInstance(fieldUnitTaxon);
            IndividualsAssociation fieldUnitIndAss = IndividualsAssociation.NewInstance(fu1);
            fieldUnitDescription.addElement(fieldUnitIndAss);

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