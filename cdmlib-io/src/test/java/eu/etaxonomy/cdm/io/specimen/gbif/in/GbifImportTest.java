// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.gbif.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author k.luther
 * @date 27.07.2016
 *
 */
public class GbifImportTest extends CdmTransactionalIntegrationTest {
    @SpringBeanByName
    private CdmApplicationAwareDefaultImport<?> defaultImport;
    @SpringBeanByType
    private IOccurrenceService occurrenceService;
    @SpringBeanByType
    private ITaxonService taxonService;



    /**
     * Tests import of gbif data
     * @throws ParseException
     */
    @Test
    @DataSet( value="/eu/etaxonomy/cdm/database/BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testImportAssociatedSpecimenSameIndividual() {
        TaxonName abies = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        abies.setSpecificEpithet("alba");
        abies.setGenusOrUninomial("Abies");
        Person author = Person.NewInstance();
        author.setTitleCache("Mill.", true);
        abies.setCombinationAuthorship(author);
        Taxon taxon = Taxon.NewInstance(abies, null);
        taxonService.save(taxon);
        GbifImportConfigurator importConfigurator = null;
        String[] tripleId = new String[]{"039141","KTU","KTU Pinophyta"};
        Set<String[]> tripleIds = new HashSet<String[]>();
        tripleIds.add(tripleId);
        OccurenceQuery query = new OccurenceQuery(tripleIds);

        importConfigurator = GbifImportConfigurator.newInstance(query);

        assertNotNull("Configurator could not be created", importConfigurator);
        importConfigurator.setIgnoreImportOfExistingSpecimen(true);
        boolean result = defaultImport.invoke(importConfigurator).isSuccess();
        int count = occurrenceService.count(DerivedUnit.class);
        List<DerivedUnit> list = occurrenceService.list(DerivedUnit.class, 100, 0, null, null);
        for (DerivedUnit unit:list){
            System.out.println(unit.getTitleCache());
        }

        List<Taxon> listtaxa = taxonService.list(Taxon.class, 100, 0, null, null);
        for (Taxon tax:listtaxa){
            System.out.println(taxon.getTitleCache());
        }
        tripleId = new String[]{"32288", "SMNG", "GLM"};
        tripleIds = new HashSet<String[]>();
        tripleIds.add(tripleId);
        query = new OccurenceQuery(tripleIds);

        importConfigurator = GbifImportConfigurator.newInstance(query);

        assertNotNull("Configurator could not be created", importConfigurator);
        importConfigurator.setIgnoreImportOfExistingSpecimen(true);
        result = defaultImport.invoke(importConfigurator).isSuccess();
        count = occurrenceService.count(DerivedUnit.class);
        list = occurrenceService.list(DerivedUnit.class, 100, 0, null, null);
        for (DerivedUnit unit:list){
            System.out.println(unit.getTitleCache());
        }

        listtaxa = taxonService.list(Taxon.class, 100, 0, null, null);
        for (Taxon tax:listtaxa){
            System.out.println(taxon.getTitleCache());
        }
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        derivedUnit.setAccessionNumber("B 10 0066577");
        derivedUnit.setTitleCache("testUnit1", true);

        derivedUnit.setUuid(derivedUnit1Uuid );

        occurrenceService.save(derivedUnit);

        commitAndStartNewTransaction(null);

        setComplete();
        endTransaction();


        try {
            writeDbUnitDataSetFile(new String[] {
                    "SpecimenOrObservationBase",
            }, "testAttachDnaSampleToDerivedUnit");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DataSet( value="/eu/etaxonomy/cdm/database/BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testImportSpecimenWithImages() {
        TaxonName dianthus = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        dianthus.setSpecificEpithet("deltoides");
        dianthus.setGenusOrUninomial("Dianthus ");
        Person author = Person.NewInstance();
        author.setTitleCache("L..", true);
        dianthus.setCombinationAuthorship(author);
        Taxon taxon = Taxon.NewInstance(dianthus, null);
        taxonService.save(taxon);
        GbifImportConfigurator importConfigurator = null;
        String[] tripleId = new String[]{"B -W 08537 -00 1","B","Herbarium Berolinense"};
        Set<String[]> tripleIds = new HashSet<String[]>();
        tripleIds.add(tripleId);
        OccurenceQuery query = new OccurenceQuery(tripleIds);

        importConfigurator = GbifImportConfigurator.newInstance(query);

        assertNotNull("Configurator could not be created", importConfigurator);
        importConfigurator.setIgnoreImportOfExistingSpecimen(true);
        boolean result = defaultImport.invoke(importConfigurator).isSuccess();
        int count = occurrenceService.count(DerivedUnit.class);
        List<DerivedUnit> list = occurrenceService.list(DerivedUnit.class, 100, 0, null, null);
        assertEquals(list.size(), 1);
        DerivedUnit unit = list.get(0);
        assertNotNull("There should be an image attached to the specimen", unit.getDescriptions());

        List<Taxon> listtaxa = taxonService.list(Taxon.class, 100, 0, null, null);
        for (Taxon tax:listtaxa){
            System.out.println(taxon.getTitleCache());
        }
        tripleId = new String[]{"32288", "SMNG", "GLM"};
        tripleIds = new HashSet<String[]>();
        tripleIds.add(tripleId);
        query = new OccurenceQuery(tripleIds);

        importConfigurator = GbifImportConfigurator.newInstance(query);

        assertNotNull("Configurator could not be created", importConfigurator);
        importConfigurator.setIgnoreImportOfExistingSpecimen(true);
        result = defaultImport.invoke(importConfigurator).isSuccess();
        count = occurrenceService.count(DerivedUnit.class);
        list = occurrenceService.list(DerivedUnit.class, 100, 0, null, null);
//        for (DerivedUnit unit:list){
//            System.out.println(unit.getTitleCache());
//        }

        listtaxa = taxonService.list(Taxon.class, 100, 0, null, null);
        for (Taxon tax:listtaxa){
            System.out.println(taxon.getTitleCache());
        }
    }

}
