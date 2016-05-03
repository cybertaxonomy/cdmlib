/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.NameDeletionConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 *
 */
public class NameServiceImplTest extends CdmTransactionalIntegrationTest {
    private static final Logger logger = Logger.getLogger(NameServiceImplTest.class);

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private IOccurrenceService occurrenceService;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private ITermService termService;


/* ******************** TESTS ********************************************/

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao)}.
     */
    @Test
    public void testSetDao() {
//		Assert.assertNotNull(((NameServiceImpl)nameService).dao);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setVocabularyDao(eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao)}.
     */
    @Test
    public void testSetVocabularyDao() {
//		Assert.assertNotNull(( (NameServiceImpl)nameService).vocabularyDao);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getNamesByName(java.lang.String)}.
     */
    @Test
    public void testGetNamesByName() {
        logger.warn("Not yet implemented");
    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getAllNames(int, int)}.
     */
    @Test
    public void testGetAllNames() {
        logger.warn("Not yet implemented");
    }



    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
    public void testGenerateTitleCache() {
        logger.warn("Not yet implemented");
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDeleteTaxonNameBaseWithNameRelations() {

        final String[] tableNames = new String[]{"USERACCOUNT", "TaxonNameBase","NameRelationship","HybridRelationship","DescriptionBase","NomenclaturalStatus","TaxonBase","SpecimenOrObservationBase","OriginalSourceBase","DescriptionElementBase"};
//        printDataSetWithNull(System.err, true, null);

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);
        TaxonNameBase<?,?> nameWithBasionym = BotanicalName.NewInstance(getSpeciesRank());
        nameWithBasionym.setTitleCache("nameWithBasionym", true);

        NameRelationshipType nameRelType = (NameRelationshipType)termService.find(NameRelationshipType.BASIONYM().getUuid());
        name1.addRelationshipToName(nameWithBasionym,nameRelType , null, null, null);
//		nameWithBasionym.addBasionym(name1);
        nameService.save(name1);
        commitAndStartNewTransaction(tableNames);


        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        DeleteResult result = nameService.delete(name1);
        if (!result.isOk()){
        	Exception e = result.getExceptions().iterator().next();
        	Assert.assertEquals("The Ecxeption should be a ReferencedObjectException because it is a basionym", "Name can't be deleted as it is a basionym.", e.getMessage());
        } else{
        	Assert.fail();
        }
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNotNull("Name should still be in database",name1);
        nameWithBasionym = name1.getNameRelations().iterator().next().getToName();
        nameWithBasionym.removeBasionyms();

        result = nameService.delete(name1); //should throw now exception



        commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should not be in database anymore",name1);

    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDeleteTaxonNameBaseConfiguratorWithNameRelations() {
        final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship"};

//        printDataSet(System.err, new String[]{"TaxonNameBase","NameRelationship","HybridRelationship","DescriptionBase","NomenclaturalStatus","TaxonBase","SpecimenOrObservationBase","OriginalSourceBase",
//                "DescriptionElementBase",
//                "AGENTBASE", "USERACCOUNT", "PERMISSIONGROUP", "USERACCOUNT_PERMISSIONGROUP", "USERACCOUNT_GRANTEDAUTHORITYIMPL", "GRANTEDAUTHORITYIMPL"});

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);
        TaxonNameBase<?,?> nameWithBasionym = BotanicalName.NewInstance(getSpeciesRank());
        nameWithBasionym.setTitleCache("nameWithBasionym", true);

        NameRelationshipType nameRelType = (NameRelationshipType)termService.find(NameRelationshipType.BASIONYM().getUuid());
        name1.addRelationshipToName(nameWithBasionym,nameRelType , null, null, null);
        nameService.save(name1);
        commitAndStartNewTransaction(tableNames);
        NameDeletionConfigurator config = new NameDeletionConfigurator();

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        DeleteResult result = nameService.delete(name1.getUuid(), config);
        if (result.isOk()){
        	Assert.fail("This should throw an error as long as name relationships exist.");
        }


        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNotNull("Name should still be in database",name1);

        //ignore is basionym for
        config.setIgnoreIsBasionymFor(true);

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        nameService.delete(name1.getUuid(),  config);
        commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should not be in database anymore",name1);

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
    public void testDeleteTaxonNameBaseConfiguratorWithNameRelationsAll() {
        final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship"};

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);
        TaxonNameBase<?,?> nameWithBasionym = BotanicalName.NewInstance(getSpeciesRank());
        nameWithBasionym.setTitleCache("nameWithBasionym", true);

        NameRelationshipType nameRelType = (NameRelationshipType)termService.find(NameRelationshipType.BASIONYM().getUuid());
        name1.addRelationshipToName(nameWithBasionym,nameRelType , null, null, null);
        nameService.save(name1);
        commitAndStartNewTransaction(tableNames);
        NameDeletionConfigurator config = new NameDeletionConfigurator();

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        DeleteResult result = nameService.delete(name1.getUuid(), config);
        if (result.isOk()){
    	   Assert.fail("Delete should throw an error as long as name relationships exist.");
        }

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNotNull("Name should still be in database",name1);

        //ignore all name relationships
        config.setRemoveAllNameRelationships(true);

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        result = nameService.delete(name1.getUuid(), config);
        logger.debug(result);
        commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should not be in database anymore",name1);

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
    public void testDeleteTaxonNameBaseConfiguratorWithHasBasionym() {
        final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship"};

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);
        TaxonNameBase<?,?> basionym = BotanicalName.NewInstance(getSpeciesRank());
        basionym.setTitleCache("basionym", true);

        NameRelationshipType nameRelType = (NameRelationshipType)termService.find(NameRelationshipType.BASIONYM().getUuid());
        basionym.addRelationshipToName(name1,nameRelType , null, null, null);
        nameService.save(name1);
        commitAndStartNewTransaction(tableNames);
        NameDeletionConfigurator config = new NameDeletionConfigurator();
        config.setIgnoreHasBasionym(false);

       name1 = (NonViralName<?>)nameService.find(name1.getUuid());
       DeleteResult result = nameService.delete(name1.getUuid(), config);
       if (result.isOk()){
    	  Assert.fail("Delete should throw an error as long as name relationships exist.");
        }


        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNotNull("Name should still be in database",name1);

        //ignore has basionym
        config.setIgnoreHasBasionym(true);
        try {
            name1 = (NonViralName<?>)nameService.find(name1.getUuid());
            result = nameService.delete(name1.getUuid(), config);
            logger.debug(result);
            commitAndStartNewTransaction(tableNames);
            name1 = (NonViralName<?>)nameService.find(name1.getUuid());
            Assert.assertNull("Name should not be in database anymore",name1);
        } catch (Exception e) {
            Assert.fail("Delete should not throw an error for .");
        }
    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
   // @Ignore //currently does not run in suite
    public void testDeleteTaxonNameBaseWithHybridRelations() {
        final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship"};

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);
        NonViralName<?> parent = BotanicalName.NewInstance(getSpeciesRank());
        parent.setTitleCache("parent", true);
        NonViralName<?> child = BotanicalName.NewInstance(getSpeciesRank());
        child.setTitleCache("child", true);

        HybridRelationshipType relType = (HybridRelationshipType)termService.find(HybridRelationshipType.FIRST_PARENT().getUuid());
        name1.addHybridParent(parent, relType, null);
        nameService.save(name1);
        commitAndStartNewTransaction(tableNames); //otherwise first save is rolled back with following failing delete
        Assert.assertEquals("'Parent' should be a parent in a hybrid relation.", 1,parent.getHybridParentRelations().size());
//		printDataSet(System.out, tableNames);

        //parent

         name1 = (NonViralName<?>)nameService.find(name1.getUuid());
         DeleteResult result = nameService.delete(name1);
         if (result.isError()){
            Assert.fail("Delete should throw NO exception when deleting a hybrid child: " + result.getExceptions().iterator().next().getMessage());
        }
        commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should not be in database anymore",name1);
        parent = (NonViralName<?>)nameService.find(parent.getUuid());
        Assert.assertEquals("'Parent' should not be a parent anymore.", 0,parent.getHybridParentRelations().size());

        //child
        name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.addHybridChild(child, relType, null);
        nameService.save(name1);
        commitAndStartNewTransaction(tableNames);


        result = nameService.delete(name1);
        if (result.isOk()){
            Assert.fail("Delete should throw an error as long as hybrid child exist.");
        }
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNotNull("Name should still be in database",name1);
        name1.removeHybridChild(child);

        result = nameService.delete(name1); //should throw now exception
        if (result.isError()){
        	Assert.fail("Delete should throw NO exception when deleting a hybrid child: " +result.getExceptions().iterator().next().getMessage());
        }

        commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should not be in database anymore",name1);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDeleteTaxonNameBaseInConcept() {
        final String[] tableNames = new String[]{"TaxonNameBase","TaxonBase"};

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);
        TaxonNameBase<?,?> basionym = BotanicalName.NewInstance(getSpeciesRank());
        basionym.setTitleCache("basionym", true);

        Taxon taxon = Taxon.NewInstance(name1, null);
        nameService.save(name1);
        taxonService.save(taxon);
        commitAndStartNewTransaction(tableNames);


        DeleteResult result = nameService.delete(name1);

        if (result.isOk()){
        	Assert.fail("Delete should throw an error as long as name is used in a concept.");
        }
        TaxonNameBase<?,?> nameBase =nameService.find(name1.getUuid());
        Assert.assertNotNull("Name should still be in database",nameBase);
        TaxonBase<?> taxonBase = taxonService.find(taxon.getUuid());
        Assert.assertNotNull("Taxon should still be in database",taxonBase);
        taxon = (Taxon)taxonBase;
        taxon.setName(basionym);
        taxonService.save(taxon);
        nameBase =nameService.find(name1.getUuid());




        result = nameService.delete(nameBase); //should throw no exception
        if (result.isError()){
        	Assert.fail("Delete should throw NO error ");
        }
        commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should still be in database",name1);
        taxon = (Taxon)taxonService.find(taxon.getUuid());
        Assert.assertNotNull("Taxon should still be in database",taxon);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
    public void testDeleteTaxonNameBaseAsStoredUnder() {
        final String[] tableNames = new String[]{"TaxonNameBase","SpecimenOrObservationBase"};

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);
        DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
        specimen.setStoredUnder(name1);

        occurrenceService.save(specimen);
        UUID uuidName1 = nameService.save(name1).getUuid();

        commitAndStartNewTransaction(tableNames);
        DeleteResult result = nameService.delete(name1);
        if (result.isOk()){
    	   Assert.fail("This should throw an error because name is used for specimen#storedUnder.");
        }
         commitAndStartNewTransaction(tableNames);

        name1 = (NonViralName<?>)nameService.find(uuidName1);
        Assert.assertNotNull("Name should still be in database",name1);
        specimen = (DerivedUnit)occurrenceService.find(specimen.getUuid());
        Assert.assertNotNull("Specimen should still be in database",name1);
        specimen.setStoredUnder(null);
        occurrenceService.saveOrUpdate(specimen);

        nameService.delete(name1); //should throw no exception


        name1 = (NonViralName<?>)nameService.find(uuidName1);
        Assert.assertNull("Name should not be in database anymore",name1);
        specimen = (DerivedUnit)occurrenceService.find(specimen.getUuid());
        Assert.assertNotNull("Specimen should still be in database",specimen);

        	occurrenceService.delete(specimen); //this is to better run this test in the test suit

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
    @Ignore //currently does not run in suite
    public void testDeleteTaxonNameBaseInSource() {
        final String[] tableNames = new String[]{"TaxonNameBase","DescriptionBase","TaxonBase","OriginalSourceBase","DescriptionElementBase"};

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);
        TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(getSpeciesRank());
        taxonName.setTitleCache("taxonName", true);
        Taxon taxon = Taxon.NewInstance(taxonName, null);

        TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
        Feature feature = (Feature)termService.find(Feature.DESCRIPTION().getUuid());
        Language lang = (Language)termService.find(Language.DEFAULT().getUuid());
        TextData textData = TextData.NewInstance("Any text", lang, null);
        textData.setFeature(feature);
        taxonDescription.addElement(textData);
        DescriptionElementSource source = DescriptionElementSource.NewPrimarySourceInstance(null, null, name1, "");
        textData.addSource(source);
        taxonService.saveOrUpdate(taxon);
        nameService.save(name1);
        try {
            commitAndStartNewTransaction(tableNames);
            name1 = (NonViralName<?>)nameService.find(name1.getUuid());
            nameService.delete(name1);
            Assert.fail("Delete should throw an error as long as name is used in a source.");
        } catch (Exception e) {
            if (e.getMessage().contains("Name can't be deleted as it is used as descriptionElementSource#nameUsedInSource")){
                //ok
                endTransaction();  //exception rolls back transaction!
                startNewTransaction();
            }else{
                Assert.fail("Unexpected error occurred when trying to delete taxon name: " + e.getMessage());
            }
        }
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNotNull("Name should still be in database",name1);
        taxon = (Taxon)taxonService.find(taxon.getUuid());
        Assert.assertNotNull("Taxon should still be in database",name1);
        source = taxon.getDescriptions().iterator().next().getElements().iterator().next().getSources().iterator().next();
        source.setNameUsedInSource(null);
        taxonService.saveOrUpdate(taxon);

        nameService.delete(name1);  //should throw now exception

        commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should not be in database anymore",name1);
        taxon = (Taxon)taxonService.find(taxon.getUuid());
        Assert.assertNotNull("Taxon should still be in database",taxon);
        source = taxon.getDescriptions().iterator().next().getElements().iterator().next().getSources().iterator().next();
        Assert.assertNull("Source should not have a nameUsedInSource anymore",source.getNameUsedInSource());
    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test

    public void testDeleteTaxonNameBaseAsType() {
        final String[] tableNames = new String[]{"TaxonNameBase","TypeDesignationBase","TaxonNameBase_TypeDesignationBase"};

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name used as type", true);

        NonViralName<?> higherName = BotanicalName.NewInstance(getGenusRank());
        higherName.setTitleCache("genus name", true);
        NameTypeDesignationStatus typeStatus = (NameTypeDesignationStatus)termService.find(NameTypeDesignationStatus.AUTOMATIC().getUuid());
        boolean addToAllHomotypicNames = true;
        higherName.addNameTypeDesignation(name1, null, null, null, typeStatus, addToAllHomotypicNames);
        nameService.save(higherName);

       commitAndStartNewTransaction(tableNames);
       name1 = (NonViralName<?>)nameService.find(name1.getUuid());
       DeleteResult result = nameService.delete(name1);
       if (result.isOk()){
    	   Assert.fail("This should throw an error because name is used in a type designation.");
        }

        commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNotNull("Name should still be in database",name1);
        higherName = (NonViralName<?>)nameService.find(higherName.getUuid());
        higherName.getNameTypeDesignations().iterator().next().removeType();  //keeps the designation but removes the name from it
//		nameService.deleteTypeDesignation(higherName,commitAndStartNewTransaction(tableNames) );  //deletes the complete designation  //both options can be used

    	nameService.delete(name1);  //should throw now exception

    	commitAndStartNewTransaction(tableNames);
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should not be in database anymore",name1);
        higherName = (NonViralName<?>)nameService.find(higherName.getUuid());
        Assert.assertNotNull("Higher name should still exist in database",higherName);
        Assert.assertEquals("Higher name should not have type designations anymore",1, higherName.getTypeDesignations().size());
    }



    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test

    public void testDeleteTaxonNameBase() {
        final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship","DescriptionBase","NomenclaturalStatus","TaxonBase","SpecimenOrObservationBase","OriginalSourceBase","DescriptionElementBase","TypeDesignationBase","TaxonNameBase_TypeDesignationBase"};

        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1", true);

        //TaxonNameDescription
        name1 = BotanicalName.NewInstance(getSpeciesRank());
        TaxonNameDescription.NewInstance(name1);
        nameService.saveOrUpdate(name1);
        commitAndStartNewTransaction(tableNames);

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        DeleteResult result = nameService.delete(name1);//should throw now exception

        setComplete();
        endTransaction();
        name1 = (NonViralName<?>)nameService.find(name1.getUuid());
        Assert.assertNull("Name should not be in database anymore",name1);

//		printDataSet(System.out, tableNames);


        //NomenclaturalStatus
        name1 = BotanicalName.NewInstance(getSpeciesRank());
        NomenclaturalStatusType nomStatusType = (NomenclaturalStatusType)termService.find(NomenclaturalStatusType.ILLEGITIMATE().getUuid());
        NomenclaturalStatus status = NomenclaturalStatus.NewInstance(nomStatusType);
        name1.addStatus(status);
        nameService.saveOrUpdate(name1);
        commitAndStartNewTransaction(tableNames);

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());

        nameService.delete(name1);  //should throw now exception
        if (!result.isOk()){
        	Assert.fail();
        }
        setComplete();
        endTransaction();
//		printDataSet(System.out, tableNames);


        //Type Designations
        name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name with type designation", true);
        SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
        SpecimenTypeDesignationStatus typeStatus = (SpecimenTypeDesignationStatus)termService.find(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid());
        typeDesignation.setTypeStatus(typeStatus);
        DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
        specimen.setTitleCache("Type specimen", true);
        occurrenceService.save(specimen);
        typeDesignation.setTypeSpecimen(specimen);

        name1.addTypeDesignation(typeDesignation, true);
        nameService.save(name1);
        commitAndStartNewTransaction(tableNames);
//		printDataSet(System.out, tableNames);

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());

        result = nameService.delete(name1);  //should throw now exception
        if (!result.isOk()){
        	Assert.fail();
        }
        setComplete();
        endTransaction();
//		printDataSet(System.out, tableNames);

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
     */
    @Test
    public void testDeleteTaxonNameBaseWithTypeInHomotypicalGroup() {
        final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship","DescriptionBase","NomenclaturalStatus","TaxonBase","SpecimenOrObservationBase","OriginalSourceBase","DescriptionElementBase","TypeDesignationBase","TaxonNameBase_TypeDesignationBase"};

        //Type Designations for homotypical group with > 1 names
        NonViralName<?> name1 = BotanicalName.NewInstance(getSpeciesRank());
        name1.setTitleCache("Name1 with type designation", true);
        NonViralName<?> name2 = BotanicalName.NewInstance(getSpeciesRank());
        name2.setTitleCache("Name2 with type designation", true);
        name2.setHomotypicalGroup(name1.getHomotypicalGroup());

        DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
        specimen.setTitleCache("Type specimen 2", true);
        occurrenceService.save(specimen);
        SpecimenTypeDesignationStatus typeStatus = (SpecimenTypeDesignationStatus)termService.find(SpecimenTypeDesignationStatus.HOLOTYPE().getUuid());

        SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
        typeDesignation.setTypeStatus(typeStatus);
        typeDesignation.setTypeSpecimen(specimen);

        boolean addToAllNames = true;
        name1.addTypeDesignation(typeDesignation, addToAllNames);
        nameService.saveOrUpdate(name1);
        commitAndStartNewTransaction(tableNames);

        name1 = (NonViralName<?>)nameService.find(name1.getUuid());

        	nameService.delete(name1);  //should throw now exception

        setComplete();
        endTransaction();
//		printDataSet(System.out, tableNames);

    }

    @Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDeleteTypeDesignation() {
        final String[] tableNames = new String[]{
                "TaxonNameBase","TypeDesignationBase","TaxonNameBase_TypeDesignationBase",
                "SpecimenOrObservationBase"};

//		BotanicalName name1 = BotanicalName.NewInstance(getSpeciesRank());
//		name1.setTitleCache("Name1");
//		name1.setUuid(UUID.fromString("6dbd41d1-fe13-4d9c-bb58-31f051c2c384"));
//
//		BotanicalName name2 = BotanicalName.NewInstance(getSpeciesRank());
//		name2.setTitleCache("Name2");
//		name2.setUuid(UUID.fromString("f9e9c13f-5fa5-48d3-88cf-712c921a099e"));
//
//		BotanicalName name3 = BotanicalName.NewInstance(getGenusRank());
//		name3.setTitleCache("Name3");
//		name3.setUuid(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
//
//
//		SpecimenTypeDesignation desig1 = SpecimenTypeDesignation.NewInstance();
//		desig1.setUuid(UUID.fromString("1357c307-00c3-499c-8e20-0849d4706125"));
//		name1.addTypeDesignation(desig1, true);
//		name2.addTypeDesignation(desig1, true);
//
//		SpecimenTypeDesignation desig2 = SpecimenTypeDesignation.NewInstance();
//		desig2.setUuid(UUID.fromString("9bbda70b-7272-4e65-a807-852a3f2eba63"));
//		name1.addTypeDesignation(desig2, true);
//
//		Specimen specimen1 = Specimen.NewInstance();
//		Fossil specimen2 = Fossil.NewInstance();
//
//		desig1.setTypeSpecimen(specimen1);
//		desig2.setTypeSpecimen(specimen2);
//
//		NameTypeDesignation nameDesig = NameTypeDesignation.NewInstance();
//		nameDesig.setTypeName(name1);
//		name3.addTypeDesignation(nameDesig, true);
//
//		nameService.save(name1);
//		nameService.save(name2);
//		nameService.save(name3);
//
//		commitAndStartNewTransaction(tableNames);
//
//		printDataSet(System.out, tableNames);
//


        TaxonNameBase<?,?> name1 =  this.nameService.load(UUID.fromString("6dbd41d1-fe13-4d9c-bb58-31f051c2c384"));
        TaxonNameBase<?,?> name2 = this.nameService.load(UUID.fromString("f9e9c13f-5fa5-48d3-88cf-712c921a099e"));
        TaxonNameBase<?,?> name3 = this.nameService.load(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
        DerivedUnit specimen1 = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("0d19a9ca-21a7-4adb-8640-8d6719e15eea")), DerivedUnit.class);
        DerivedUnit fossil = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("4c48b7c8-4c8d-4e48-b083-0837fe51a0a9")), DerivedUnit.class);

        Set<TypeDesignationBase> desigs1 = name1.getTypeDesignations();
        Set<TypeDesignationBase> desigs2 = name2.getTypeDesignations();
        Set<TypeDesignationBase> desigs3 = name3.getTypeDesignations();

        Assert.assertEquals("name1 should have 2 type designations", 2, desigs1.size());
        Assert.assertEquals("name2 should have 1 type designations", 1, desigs2.size());
        Assert.assertEquals("name3 should have 1 type designations", 1, desigs3.size());
        Assert.assertEquals("Specimen1 should be used in 1 type designation", 1, specimen1.getSpecimenTypeDesignations().size());
        Assert.assertEquals("Fossil should be used in 1 type designation", 1, fossil.getSpecimenTypeDesignations().size());

        nameService.deleteTypeDesignation((TaxonNameBase)null, null);

        commitAndStartNewTransaction(tableNames);

        name1 =  this.nameService.load(UUID.fromString("6dbd41d1-fe13-4d9c-bb58-31f051c2c384"));
        name2 = this.nameService.load(UUID.fromString("f9e9c13f-5fa5-48d3-88cf-712c921a099e"));
        name3 = this.nameService.load(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
        specimen1 = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("0d19a9ca-21a7-4adb-8640-8d6719e15eea")), DerivedUnit.class);
        fossil = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("4c48b7c8-4c8d-4e48-b083-0837fe51a0a9")), DerivedUnit.class);

        desigs1 = name1.getTypeDesignations();
        desigs2 = name2.getTypeDesignations();
        desigs3 = name3.getTypeDesignations();
        //nothing should be deleted
        Assert.assertEquals("name1 should have 2 type designations", 2, desigs1.size());
        Assert.assertEquals("name2 should have 1 type designations", 1, desigs2.size());
        Assert.assertEquals("name3 should have 1 type designations", 1, desigs3.size());
        Assert.assertEquals("Specimen1 should be used in 1 type designation", 1, specimen1.getSpecimenTypeDesignations().size());
        Assert.assertEquals("Fossil should be used in 1 type designation", 1, fossil.getSpecimenTypeDesignations().size());

        nameService.deleteTypeDesignation(name1, null);

        commitAndStartNewTransaction(tableNames);

        name1 =  this.nameService.load(UUID.fromString("6dbd41d1-fe13-4d9c-bb58-31f051c2c384"));
        name2 = this.nameService.load(UUID.fromString("f9e9c13f-5fa5-48d3-88cf-712c921a099e"));
        name3 = this.nameService.load(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
        specimen1 = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("0d19a9ca-21a7-4adb-8640-8d6719e15eea")), DerivedUnit.class);
        fossil = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("4c48b7c8-4c8d-4e48-b083-0837fe51a0a9")), DerivedUnit.class);

        desigs1 = name1.getTypeDesignations();
        desigs2 = name2.getTypeDesignations();
        desigs3 = name3.getTypeDesignations();
        //only the types of name1 should be deleted
        Assert.assertEquals("name1 should have 0 type designations", 0, desigs1.size());
        Assert.assertEquals("name2 should have 1 type designations", 1, desigs2.size());
        Assert.assertEquals("name3 should have 1 type designations", 1, desigs3.size());
        Assert.assertEquals("Specimen1 should be used in 1 type designation", 1, specimen1.getSpecimenTypeDesignations().size());
        Assert.assertEquals("Fossil should be used in 0 type designation", 0, fossil.getSpecimenTypeDesignations().size());

        SpecimenTypeDesignation desig2 = (SpecimenTypeDesignation)name2.getTypeDesignations().iterator().next();
        nameService.deleteTypeDesignation(name2, desig2);

        commitAndStartNewTransaction(tableNames);

        name1 =  this.nameService.load(UUID.fromString("6dbd41d1-fe13-4d9c-bb58-31f051c2c384"));
        name2 = this.nameService.load(UUID.fromString("f9e9c13f-5fa5-48d3-88cf-712c921a099e"));
        name3 = this.nameService.load(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
        specimen1 = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("0d19a9ca-21a7-4adb-8640-8d6719e15eea")), DerivedUnit.class);
        fossil = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("4c48b7c8-4c8d-4e48-b083-0837fe51a0a9")), DerivedUnit.class);

        desigs1 = name1.getTypeDesignations();
        desigs2 = name2.getTypeDesignations();
        desigs3 = name3.getTypeDesignations();

        Assert.assertEquals("name1 should have 0 type designations", 0, desigs1.size());
        Assert.assertEquals("name2 should have 0 type designations", 0, desigs2.size());
        Assert.assertEquals("name3 should have 1 type designations", 1, desigs3.size());
        Assert.assertEquals("Specimen1 should be used in 0 type designation", 0, specimen1.getSpecimenTypeDesignations().size());
        Assert.assertEquals("Fossil should be used in 0 type designation", 0, fossil.getSpecimenTypeDesignations().size());

        NameTypeDesignation desig3 = (NameTypeDesignation)name3.getTypeDesignations().iterator().next();
        name3.addTypeDesignation(SpecimenTypeDesignation.NewInstance(), false);
        this.nameService.update(name3);

        this.nameService.load(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
        desigs3 = name3.getTypeDesignations();
        Assert.assertEquals("name3 should have 2 type designations", 2, desigs3.size());

        nameService.deleteTypeDesignation(name3, desig3);
        commitAndStartNewTransaction(tableNames);


        name1 =  this.nameService.load(UUID.fromString("6dbd41d1-fe13-4d9c-bb58-31f051c2c384"));
        name2 = this.nameService.load(UUID.fromString("f9e9c13f-5fa5-48d3-88cf-712c921a099e"));
        name3 = this.nameService.load(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
        specimen1 = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("0d19a9ca-21a7-4adb-8640-8d6719e15eea")), DerivedUnit.class);
        fossil = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("4c48b7c8-4c8d-4e48-b083-0837fe51a0a9")), DerivedUnit.class);

        desigs1 = name1.getTypeDesignations();
        desigs2 = name2.getTypeDesignations();
        desigs3 = name3.getTypeDesignations();

        Assert.assertEquals("name1 should have 0 type designations", 0, desigs1.size());
        Assert.assertEquals("name2 should have 0 type designations", 0, desigs2.size());
        Assert.assertEquals("name3 should have 0 type designations", 1, desigs3.size());
        Assert.assertEquals("Specimen1 should be used in 0 type designation", 0, specimen1.getSpecimenTypeDesignations().size());
        Assert.assertEquals("Fossil should be used in 0 type designation", 0, fossil.getSpecimenTypeDesignations().size());

    }

    @Test
    @DataSet
    public void testDeleteTypeDesignationAllNames() {
        final String[] tableNames = new String[]{
                "TaxonNameBase","TypeDesignationBase",
                "TaxonNameBase_TypeDesignationBase","SpecimenOrObservationBase"};


        TaxonNameBase<?,?> name1 =  this.nameService.load(UUID.fromString("6dbd41d1-fe13-4d9c-bb58-31f051c2c384"));
        TaxonNameBase<?,?> name2 = this.nameService.load(UUID.fromString("f9e9c13f-5fa5-48d3-88cf-712c921a099e"));
        TaxonNameBase<?,?> name3 = this.nameService.load(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
        DerivedUnit specimen1 = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("0d19a9ca-21a7-4adb-8640-8d6719e15eea")), DerivedUnit.class);
        DerivedUnit fossil = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("4c48b7c8-4c8d-4e48-b083-0837fe51a0a9")), DerivedUnit.class);

        Set<TypeDesignationBase> desigs1 = name1.getTypeDesignations();
        Set<TypeDesignationBase> desigs2 = name2.getTypeDesignations();
        Set<TypeDesignationBase> desigs3 = name3.getTypeDesignations();

        Assert.assertEquals("name1 should have 2 type designations", 2, desigs1.size());
        Assert.assertEquals("name2 should have 1 type designations", 1, desigs2.size());
        Assert.assertEquals("name3 should have 1 type designations", 1, desigs3.size());
        Assert.assertEquals("Specimen1 should be used in 1 type designation", 1, specimen1.getSpecimenTypeDesignations().size());
        Assert.assertEquals("Fossil should be used in 1 type designation", 1, fossil.getSpecimenTypeDesignations().size());

        SpecimenTypeDesignation desig2 = (SpecimenTypeDesignation)name2.getTypeDesignations().iterator().next();

        nameService.deleteTypeDesignation(null, desig2);

        commitAndStartNewTransaction(tableNames);

        name1 =  this.nameService.load(UUID.fromString("6dbd41d1-fe13-4d9c-bb58-31f051c2c384"));
        name2 = this.nameService.load(UUID.fromString("f9e9c13f-5fa5-48d3-88cf-712c921a099e"));
        name3 = this.nameService.load(UUID.fromString("e1e66264-f16a-4df9-80fd-6ab5028a3c28"));
        specimen1 = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("0d19a9ca-21a7-4adb-8640-8d6719e15eea")), DerivedUnit.class);
        fossil = CdmBase.deproxy(this.occurrenceService.load(UUID.fromString("4c48b7c8-4c8d-4e48-b083-0837fe51a0a9")), DerivedUnit.class);

        desigs1 = name1.getTypeDesignations();
        desigs2 = name2.getTypeDesignations();
        desigs3 = name3.getTypeDesignations();

        Assert.assertEquals("name1 should have 1 type designations", 1, desigs1.size());
        Assert.assertEquals("name2 should have 0 type designations", 0, desigs2.size());
        Assert.assertEquals("name3 should have 1 type designations", 1, desigs3.size());
        Assert.assertEquals("Specimen1 should be used in 0 type designation", 0, specimen1.getSpecimenTypeDesignations().size());
        Assert.assertEquals("Fossil should be used in 1 type designation", 1, fossil.getSpecimenTypeDesignations().size());

    }

    /**
     * @return
     */
    private Rank getSpeciesRank() {
        return (Rank)termService.find(Rank.uuidSpecies);
    }

    /**
     * @return
     */
    private Rank getGenusRank() {
        return (Rank)termService.find(Rank.uuidGenus);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }


}
