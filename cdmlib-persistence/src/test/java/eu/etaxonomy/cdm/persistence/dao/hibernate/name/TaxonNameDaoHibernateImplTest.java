/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonNameParts;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

@DataSet
public class TaxonNameDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonNameDao taxonNameDao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

    @SpringBeanByType
    private ICdmGenericDao genericDao;

    @SpringBeanByType
    private IHomotypicalGroupDao homotypicalGroupDao;

    private UUID cryptocoryneGriffithiiUuid;
    private UUID acherontiaUuid;
    private UUID acherontiaLachesisUuid;
    private UUID atroposUuid;

    @Before
    public void setUp() {
        cryptocoryneGriffithiiUuid = UUID.fromString("497a9955-5c5a-4f2b-b08c-2135d336d633");
        acherontiaUuid = UUID.fromString("c2cab2ad-3e3a-47b8-8aa8-d9e1c0857647");
        acherontiaLachesisUuid = UUID.fromString("7969821b-a2cf-4d01-95ec-6a5ed0ca3f69");
        // Atropos Agassiz, 1846
        atroposUuid = UUID.fromString("27004fcc-14d4-47d4-a3e1-75750fdb5b79");
    }

    @Test
    public void testGetHybridRelationships() {
        IBotanicalName cryptocoryneGriffithii = taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid);
        assert cryptocoryneGriffithii!= null : "name must exist";

        List<HybridRelationship> result = taxonNameDao.getHybridNames(cryptocoryneGriffithii, null, null, null,null,null);

        assertNotNull("getHybridNames should return a list",result);
        assertFalse("the list should not be empty", result.isEmpty());
        assertEquals("getHybridNames should return 1 HybridRelationship instance",1,result.size());
    }

    @Test
    public void testCountHybridRelationships() {
        IBotanicalName cryptocoryneGriffithii = taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid);
        assert cryptocoryneGriffithii != null : "name must exist";

        long count = taxonNameDao.countHybridNames(cryptocoryneGriffithii, null);

        assertEquals("countHybridNames should return 1",Long.valueOf(1), Long.valueOf(count));
    }

    @Test
    public void testGetNameRelationships() {
        TaxonName acherontia = taxonNameDao.findByUuid(acherontiaUuid);
        assert acherontia != null : "name must exist";

        List<NameRelationship> result = taxonNameDao.getNameRelationships(acherontia, NameRelationship.Direction.relatedFrom, null, null,null,null, null);

        assertNotNull("getRelatedNames should return a list",result);
        assertFalse("the list should not be empty", result.isEmpty());
        assertEquals("getRelatedNames should return 1 NameRelationship instance",1,result.size());

        // testing inverted direction
        TaxonName atropos = taxonNameDao.findByUuid(atroposUuid);
        assert atropos != null : "name must exist";

        result = taxonNameDao.getNameRelationships(atropos, NameRelationship.Direction.relatedTo, null, null,null,null, null);

        assertNotNull("getRelatedNames should return a list",result);
        assertFalse("the list should not be empty", result.isEmpty());
        assertEquals("getRelatedNames should return 2 NameRelationship instance",2,result.size());

        result = taxonNameDao.getNameRelationships(null, null, null, null,null,null, null);

        assertNotNull("getRelatedNames should return a list",result);
        assertFalse("the list should not be empty", result.isEmpty());
        assertEquals("getRelatedNames should return all 2 NameRelationship instance",2,result.size());
    }

    @Test
    public void testCountNameRelationships() {
        TaxonName acherontia = taxonNameDao.findByUuid(acherontiaUuid);
        assert acherontia != null : "name must exist";

        long count = taxonNameDao.countNameRelationships(acherontia, NameRelationship.Direction.relatedFrom, null);

        assertEquals("countRelatedNames should return 1",1,count);

        // testing inverted direction
        TaxonName atropos = taxonNameDao.findByUuid(atroposUuid);
        assert atropos != null : "name must exist";

        count = taxonNameDao.countNameRelationships(atropos, NameRelationship.Direction.relatedTo, null);

        assertEquals("countRelatedNames should return 2",2,count);
    }

    @Test
    public void testGetTypeDesignations() {
        TaxonName acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
        assert acherontiaLachesis != null : "name must exist";

        @SuppressWarnings("rawtypes")
        List<TypeDesignationBase> result1 = taxonNameDao.getTypeDesignations(acherontiaLachesis, null, null, null, null, null);

        assertNotNull("getTypeDesignations should return a list",result1);
        assertFalse("the list should not be empty", result1.isEmpty());
        assertEquals("getTypeDesignations should return 1 TypeDesignationBase instance",1,result1.size());

        List<SpecimenTypeDesignation> result2 = taxonNameDao.getTypeDesignations(acherontiaLachesis, SpecimenTypeDesignation.class, null, null, null, null);

        assertNotNull("getTypeDesignations should return a list",result2);
        assertFalse("the list should not be empty", result2.isEmpty());
        assertEquals("getTypeDesignations should return 1 TypeDesignationBase instance",1,result2.size());
    }

    @Test
    public void testCountTypeDesignations() {
        TaxonName acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
        assert acherontiaLachesis != null : "name must exist";

        long count = taxonNameDao.countTypeDesignations(acherontiaLachesis, null);

        assertEquals("countTypeDesignations should return 1",1,count);
    }

    @Test
    public void testSearchNames() {
        List<TaxonName> result = taxonNameDao.searchNames("Atropos", null, null, null, Rank.GENUS(), null, null, null, null);

        assertNotNull("searchNames should return a list",result);
        assertFalse("the list should not be empty", result.isEmpty());
        assertEquals("searchNames should return 3 TaxonName instances",3,result.size());
    }

    @Test
    public void testFindTaxonNameParts_genusOrUninomial() {
        Integer pageSize = 10;
        Integer pageIndex = 0;

        List<TaxonNameParts> resuls = taxonNameDao.findTaxonNameParts(
                Optional.of("Atropos"), null, null, null,
                Rank.GENUS(), null,
                pageSize, pageIndex, Arrays.asList(new OrderHint("genusOrUninomial", SortOrder.ASCENDING)));

        assertNotNull("searchNames should return a list",resuls);
        assertFalse(resuls.isEmpty());
        assertEquals(3, resuls.size());
    }

    @Test
    public void testFindTaxonNameParts_genusOrUninomial_wildcard() {
        Integer pageSize = 10;
        Integer pageIndex = 0;

        List<TaxonNameParts> results = taxonNameDao.findTaxonNameParts(
                Optional.of("Atro*"), null, null, null,
                Rank.GENUS(), null,
                pageSize, pageIndex, Arrays.asList(new OrderHint("genusOrUninomial", SortOrder.ASCENDING)));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(3, results.size());

        TaxonName n_atropos_agassiz = taxonNameDao.load(atroposUuid);
        results = taxonNameDao.findTaxonNameParts(
                Optional.of("Atro*"), null, null, null,
                Rank.GENUS(), Arrays.asList(n_atropos_agassiz.getUuid()),
                pageSize, pageIndex, Arrays.asList(new OrderHint("genusOrUninomial", SortOrder.ASCENDING)));

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());

        results = taxonNameDao.findTaxonNameParts(
                Optional.of("Atro*"), null, null, null,
                Rank.SPECIES(), null,
                pageSize, pageIndex, Arrays.asList(new OrderHint("genusOrUninomial", SortOrder.ASCENDING)));

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testFindTaxonNameParts_rankSpecies() {
        Integer pageSize = 10;
        Integer pageIndex = 0;
        // Manduca afflicta
        // Manduca chinchilla
        // Manduca bergarmatipes
        List<TaxonNameParts> results = taxonNameDao.findTaxonNameParts(
                Optional.of("Manduca"), null, Optional.of("*"), null,
                Rank.SPECIES(), null,
                pageSize, pageIndex, Arrays.asList(new OrderHint("specificEpithet", SortOrder.ASCENDING)));

        assertEquals(3, results.size());
        assertEquals("afflicta", results.get(0).getSpecificEpithet());
        assertEquals("bergarmatipes", results.get(1).getSpecificEpithet());
        assertEquals("chinchilla", results.get(2).getSpecificEpithet());

        results = taxonNameDao.findTaxonNameParts(
                Optional.of("Manduca"), null, Optional.of("chin*"), null,
                Rank.SPECIES(), null,
                pageSize, pageIndex, null);

        assertEquals(1, results.size());
        assertEquals("chinchilla", results.get(0).getSpecificEpithet());
    }

    @Test
    public void testFindTaxonNameParts_rankBelowSpecies() {

        Integer pageSize = 10;
        Integer pageIndex = 0;
        // Cryptocoryne x purpurea nothovar borneoensis
        // Cryptocoryne cordata var. zonata
        List<TaxonNameParts> results = taxonNameDao.findTaxonNameParts(
                Optional.of("Cryptocoryne"), null, null, Optional.of("borneo*"),
                Rank.VARIETY(), null,
                pageSize, pageIndex, Arrays.asList(new OrderHint("specificEpithet", SortOrder.ASCENDING)));

        assertEquals(1, results.size());

        // now also with "infraGenericEpithet is null AND specificEpithet = purpurea"
        results = taxonNameDao.findTaxonNameParts(
                Optional.of("Cryptocoryne"), Optional.empty(), Optional.of("purpurea"), Optional.of("borneo*"),
                Rank.VARIETY(), null,
                pageSize, pageIndex, Arrays.asList(new OrderHint("specificEpithet", SortOrder.ASCENDING)));

        assertEquals(1, results.size());
    }


    @Test
    public void testCountNames() {
        long count = taxonNameDao.countNames("Atropos", null, null, null, Rank.GENUS());

        assertEquals("countNames should return 3",3,count);
    }

    @Test
    public void testCountNamesByExample() {
        TaxonName zoologicalName = TaxonNameFactory.NewZoologicalInstance(Rank.GENUS());
        zoologicalName.setGenusOrUninomial("Atropos");
        Set<String> includedProperties = new HashSet<>();
        includedProperties.add("genusOrUninomial");
        includedProperties.add("specificEpithet");
        includedProperties.add("infraSpecificEpithet");
        includedProperties.add("rank");
        long count = taxonNameDao.count(zoologicalName, includedProperties);

        assertEquals("countNames should return 3",3,count);
    }

    @Test
    /**
     * This test check for a specific bug (Ticket #686) where the rank of a taxon name base
     * has no order index (=0)
     */
    public void testMissingRankOrderIndex() {
        TaxonName acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
        Rank rank = null;
        try {
			rank = Rank.getRankByLatinName(acherontiaLachesis.getRank().getLabel());
        } catch (UnknownCdmTypeException e) {
            e.printStackTrace();
        }
        assertNotNull(rank);
        assertFalse("Rank are equal, level must not be higher", rank.isHigher(acherontiaLachesis.getRank()));
        assertFalse("Rank are equal, level must not be lower", rank.isLower(acherontiaLachesis.getRank()));
    }

    @Test
    public void testDeleteTaxon(){
        TaxonName acherontiaLachesis = CdmBase.deproxy(taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid));
        @SuppressWarnings("rawtypes")
        Set<TaxonBase> taxonBases = acherontiaLachesis.getTaxonBases();
        HomotypicalGroup group = acherontiaLachesis.getHomotypicalGroup();
        UUID groupUuid = group.getUuid();
        taxonNameDao.delete(acherontiaLachesis);

        @SuppressWarnings("rawtypes")
        Iterator<TaxonBase> taxa= taxonBases.iterator();
        TaxonBase<?> taxon = taxa.next();
        UUID taxonUuid = taxon.getUuid();

        acherontiaLachesis = taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid);
        taxon = taxonDao.findByUuid(taxonUuid);
        group = CdmBase.deproxy(homotypicalGroupDao.findByUuid(groupUuid));
        assertNull("There should be no taxonName with the deleted uuid", acherontiaLachesis);
        assertNull("There should be no taxon with the deleted uuid", taxon);
        assertNull("There should be no homotypicalGroup with the deleted uuid", group);
    }

    /**
     * Test if the listener on nomenclatural source also works if the name is retrieved
     * from the database
     */
    @Test
    public void testNomenclaturalSourceListener(){
        UUID uuidAusAus = UUID.fromString("05a438d6-065f-49ef-84db-c7dc2c259975");
        TaxonName ausAus = taxonNameDao.findByUuid(uuidAusAus);
        //start condition
        assertEquals("Aus aus, Sp. Pl.", ausAus.getFullTitleCache());

        ausAus.getNomenclaturalSource().setCitationMicroReference("23");
        assertEquals("Here the full cache should show the cache as stored in the database but did not",
                "Aus aus, Sp. Pl.: 23", ausAus.getFullTitleCache());
    }

    @Test
    public void testDistinctGenusOrUninomial() {
        List<String> list = taxonNameDao.distinctGenusOrUninomial(null, null, null);
        assertEquals(6, list.size());
        assertTrue(list.contains("Aus") && list.contains("Manduca") && list.contains("Agrius") && list.contains("Acherontia")
                && list.contains("Atropos") && list.contains("Cryptocoryne"));

        list = taxonNameDao.distinctGenusOrUninomial("A*", null, null);
        assertEquals(4, list.size());
        assertTrue(list.contains("Aus") && list.contains("Agrius") && list.contains("Acherontia")
                && list.contains("Atropos"));

        list = taxonNameDao.distinctGenusOrUninomial(null, Rank.GENUS(), Rank.SPECIESAGGREGATE());
        assertEquals(4, list.size());
        assertTrue(list.contains("Manduca") && list.contains("Agrius") && list.contains("Acherontia")
                && list.contains("Atropos"));
    }

// *************** copied from removed TypeDesignationDaoHibernateImplTest ***************/

    @Test
    @DataSet(value="TypeDesignationDaoHibernateImplTest.xml")
    public void testGetAllTypeDesignations() {
        List<TypeDesignationBase<?>> typeDesignations = taxonNameDao.getAllTypeDesignations(100, 0);
        assertEquals(2, typeDesignations.size());
        SpecimenTypeDesignation specTypeDesig = null;
        for (TypeDesignationBase<?> typeDesignation : typeDesignations) {
            typeDesignation= CdmBase.deproxy(typeDesignation);
            if (typeDesignation instanceof NameTypeDesignation) {
                NameTypeDesignation ntd = (NameTypeDesignation)typeDesignation;
                NameTypeDesignationStatus status = ntd.getTypeStatus();
                assertTrue(status.isInstanceOf(NameTypeDesignationStatus.class));
            } else if (typeDesignation instanceof SpecimenTypeDesignation) {
                Assert.assertNull("There should be only 1 specimen type designation but this is already the second", specTypeDesig);
                TypeDesignationStatusBase<?> typeDesignationStatus = ((SpecimenTypeDesignation)typeDesignation).getTypeStatus();
                assertTrue(typeDesignationStatus.isInstanceOf(SpecimenTypeDesignationStatus.class));
                specTypeDesig = CdmBase.deproxy(typeDesignation,SpecimenTypeDesignation.class);
            }
        }
        @SuppressWarnings("null")
        Set<TaxonName> names = specTypeDesig.getTypifiedNames();
        Assert.assertEquals("There should be exactly 1 typified name for the the specimen type designation", 1, names.size());
        TaxonName singleName = names.iterator().next();
        Assert.assertEquals("", UUID.fromString("61b1dcae-8aa6-478a-bcd6-080cf0eb6ad7"), singleName.getUuid());
    }

    @Test
    @ExpectedDataSet("TypeDesignationDaoHibernateImplTest.testSaveTypeDesignationsWithAuditing-result.xml")
    //Auditing didn't work for SpecimenTypeDesignations. See #2396
    public void testSaveTypeDesignationsWithAuditing() {


        // creating new Typedesignation for a new Name:

        //  1. new TaxonName with UUID 8564287e-9654-4b8b-a38c-0ccdd9e885db
        TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name1.setTitleCache("Name1", true);
        name1.setUuid(UUID.fromString("8564287e-9654-4b8b-a38c-0ccdd9e885db"));
        //   2. new TypeDesignation with uuid ceca086e-e8d3-444e-abfb-c47f76835130
        SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
        desig1.setUuid(UUID.fromString("ceca086e-e8d3-444e-abfb-c47f76835130"));

        name1.addTypeDesignation(desig1, true);

        taxonNameDao.saveOrUpdate(name1);
        commitAndStartNewTransaction(new String[]{"TypeDesignationBase", "TypeDesignationBase_AUD"});

//      printDataSet(System.err, new String[]{"TaxonName","TaxonName_AUD",
//              "HomotypicalGroup","HomotypicalGroup_AUD",
//              "TypeDesignationBase","TypeDesignationBase_AUD",
//              "TaxonName_TypeDesignationBase","TaxonName_TypeDesignationBase_AUD"
//              });
    }

    @Test
    @DataSet(value="TypeDesignationDaoHibernateImplTest.xml")
    public void testGetTypeDesignationStatusInUse() {
        @SuppressWarnings("rawtypes")
        List<TypeDesignationStatusBase> statusTerms = taxonNameDao.getTypeDesignationStatusInUse();
        assertEquals(2, statusTerms.size());
    }


    @Test
//  @ExpectedDataSet
    public void testRemoveTypeDesignationsFromName() {

        TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name1.setTitleCache("Name1", true);
        name1.setUuid(UUID.fromString("2cfc05fc-138e-452d-b4ea-8798134c7410"));

        TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name2.setTitleCache("Name2", true);
        name2.setUuid(UUID.fromString("7a12057d-2e99-471e-ac7e-633f1d0b5686"));

        SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
        desig1.setUuid(UUID.fromString("fe9f7711-de4a-4789-8045-86b2cb5c4358"));
        name1.addTypeDesignation(desig1, true);
        name2.addTypeDesignation(desig1, true);

        SpecimenTypeDesignation desig2 = SpecimenTypeDesignation.NewInstance();
        desig2.setUuid(UUID.fromString("bf357711-e752-44e9-bd3d-aef0a0bb5b91"));
        name1.addTypeDesignation(desig2, true);

        taxonNameDao.save(name1);
        taxonNameDao.save(name2);

        this.setComplete();
        this.endTransaction();
        this.startNewTransaction();

        name1 = taxonNameDao.load(name1.getUuid());
        Assert.assertNotNull(name1);
        Assert.assertEquals("Name1 should have 2 type designations", 2, name1.getTypeDesignations().size());

        desig1 = genericDao.find(SpecimenTypeDesignation.class, desig1.getUuid());
        name1.removeTypeDesignation(desig1);

        this.setComplete();
        this.endTransaction();
        this.startNewTransaction();

        name1 = taxonNameDao.load(name1.getUuid());
        Assert.assertNotNull(name1);
        Assert.assertEquals("Name1 should have 1 type designation", 1, name1.getTypeDesignations().size());

        desig2 = genericDao.find(SpecimenTypeDesignation.class, desig2.getUuid());
        Assert.assertNotNull(desig2);
        name1.removeTypeDesignation(desig2);

        this.setComplete();
        this.endTransaction();
        this.startNewTransaction();

        name1 = taxonNameDao.load(name1.getUuid());
        Assert.assertNotNull(name1);
        Assert.assertEquals("Name1 should have no type designations", 0, name1.getTypeDesignations().size());

        name2 = taxonNameDao.load(name2.getUuid());
        Assert.assertNotNull(name1);
        Assert.assertEquals("Name2 should have 1 type designation", 1, name2.getTypeDesignations().size());
        SpecimenTypeDesignation desig1New = (SpecimenTypeDesignation)name2.getTypeDesignations().iterator().next();
        desig1 = genericDao.find(SpecimenTypeDesignation.class, desig1.getUuid());
        Assert.assertSame("Desig1New should be same as desig1", desig1, desig1New);

        try{
            genericDao.delete(desig1);
            this.setComplete();
            this.endTransaction();
            Assert.fail("desig1 should not be deletable as it is still connected to name2");
        }catch (Exception e){
            //this.setComplete();
            this.endTransaction();
            this.startNewTransaction();
        }
        name2 = taxonNameDao.load(name2.getUuid());
        Assert.assertNotNull(name1);
        desig1 = genericDao.find(SpecimenTypeDesignation.class, desig1.getUuid());
        name2.removeTypeDesignation(desig1);

        genericDao.delete(desig1);  //now it can be deleted

        this.setComplete();
        this.endTransaction();
        this.startNewTransaction();

        desig2 = genericDao.find(SpecimenTypeDesignation.class, desig2.getUuid());
        genericDao.delete(desig2); //desig2 is already orphaned and therefore can be deleted

        this.setComplete();
        this.endTransaction();

//      printDataSet(System.out, new String[]{"TaxonName","TaxonName_AUD","TypeDesignationBase","TypeDesignationBase_AUD",
//              "TaxonName_TypeDesignationBase","TaxonName_TypeDesignationBase_AUD",
//              "SpecimenOrObservationBase","SpecimenOrObservationBase_AUD",
//              "HomotypicalGroup","HomotypicalGroup_AUD"});
    }


    @Test
    @DataSet(value="TypeDesignationDaoHibernateImplTest.xml")
    @ExpectedDataSet("TypeDesignationDaoHibernateImplTest.testSaveTypeDesignations-result.xml")  //not yet necessary with current test
    public void testSaveTypeDesignations() {

        List<TypeDesignationBase<?>> typeDesignations = taxonNameDao.getAllTypeDesignations(100, 0);
        assertEquals(typeDesignations.size(), 2);
        SpecimenTypeDesignation specTypeDesig = null;
        for (TypeDesignationBase<?> typeDesignation : typeDesignations) {
            if (typeDesignation.isInstanceOf(SpecimenTypeDesignation.class)) {
                specTypeDesig = CdmBase.deproxy(typeDesignation,SpecimenTypeDesignation.class);
            }
        }

        TaxonName newName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        newName.setUuid(UUID.fromString("c16c3bc5-d3d0-4676-91a1-848ebf011e7c"));
        newName.setTitleCache("Name used as typified name", true);
        newName.addTypeDesignation(specTypeDesig, false);

        taxonNameDao.saveOrUpdate(newName);

        commitAndStartNewTransaction(null);
        specTypeDesig = genericDao.find(SpecimenTypeDesignation.class, specTypeDesig.getUuid());
        Assert.assertNotNull("specimen type designation should exists in db", specTypeDesig);
        specTypeDesig.getTypifiedNames().size();
        Set<TaxonName> typifiedNames = specTypeDesig.getTypifiedNames();
        Assert.assertEquals("There should be 2 typified names for this type designation now", 2, typifiedNames.size());

//      printDataSet(System.out, new String[]{"TaxonName","TaxonName_AUD",
//              "HomotypicalGroup","HomotypicalGroup_AUD",
//              "TypeDesignationBase","TypeDesignationBase_AUD",
//              "TaxonName_TypeDesignationBase", "TaxonName_TypeDesignationBase_AUD"
//              });

    }

// ******** END copied from removed TypeDesignationDaoHibernateImplTest ***************/

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}