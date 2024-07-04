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

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonNameParts;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class TaxonNameDaoHibernateImplTest extends CdmIntegrationTest {

    @SpringBeanByType
    private ITaxonNameDao taxonNameDao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

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

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
    }