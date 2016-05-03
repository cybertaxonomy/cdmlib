/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 09.02.2009
 * @version 1.0
 */

public class DescriptionServiceImplTest extends CdmTransactionalIntegrationTest {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(DescriptionServiceImplTest.class);

    @SpringBeanByType
    private IDescriptionService service;

    @SpringBeanByType
    private ITermService termService;



    @Test
    public void testGetDefaultFeatureVocabulary() {
        service.getDefaultFeatureVocabulary();
    }

    @Test
    @DataSet("CommonServiceImplTest.xml")
    public void testChangeDescriptionElement(){
        DescriptionBase<?> descBase = service.find(UUID.fromString("eb17b80a-9be6-4642-a6a8-b19a318925e6"));
        Set<DescriptionElementBase> elements = descBase.getElements();
        Iterator<?> iterator = elements.iterator();
        while (iterator.hasNext()){
            DescriptionElementBase base = (DescriptionElementBase) iterator.next();
            if (base instanceof TextData){
                TextData textdata = (TextData) base;
                Set <Entry<Language,LanguageString>> entries = textdata.getMultilanguageText().entrySet();
                Iterator<?> entryIterator = entries.iterator();
                while (entryIterator.hasNext()){
                    Entry <Language, LanguageString> entry = (Entry<Language, LanguageString>) entryIterator.next();
                    LanguageString langString = entry.getValue();
//					System.out.println(langString);
                    langString.setText("blablubber");
                }
            }

        }
        service.saveOrUpdate(descBase);
        Pager<DescriptionElementBase> allElements = service.getDescriptionElements(null, null, null, null, null, null);
        Assert.assertEquals(1, allElements.getCount().intValue());
        DescriptionElementBase test = allElements.getRecords().get(0);
        if (test instanceof TextData){

            Set <Entry<Language,LanguageString>> entries = ((TextData) test).getMultilanguageText().entrySet();
            Iterator<Entry<Language,LanguageString>> entryIterator = entries.iterator();
            while (entryIterator.hasNext()){
                Entry <Language, LanguageString> entry = entryIterator.next();
                LanguageString langString = entry.getValue();
//				System.out.println(langString);
            }
        }
    }


    @Test
    public void testMoveDescriptionElement(){
        //Create data
        Taxon taxon = Taxon.NewInstance(null, null);
        TaxonDescription desc1 = TaxonDescription.NewInstance(taxon);
        TextData textData1 = TextData.NewInstance(Feature.HABITAT(), "My habitat", Language.GERMAN(), null);
        desc1.addElement(textData1);
        service.saveOrUpdate(desc1);

        TaxonDescription desc2 = TaxonDescription.NewInstance(taxon);
        TextData textData2 = TextData.NewInstance(Feature.HABITAT(), "My habitat2", Language.GERMAN(), null);
        desc2.addElement(textData2);
        service.saveOrUpdate(desc2);
        commitAndStartNewTransaction(null);


        DescriptionBase<?> descLoaded1 = service.find(desc1.getUuid());
        DescriptionBase<?> descLoaded2 = service.find(desc2.getUuid());

        DescriptionElementBase textDataLoaded = descLoaded1.getElements().iterator().next();
        Set<DescriptionElementBase> tmpSet = new HashSet<DescriptionElementBase>(descLoaded1.getElements());

        //test for #4806
        service.moveDescriptionElementsToDescription(tmpSet, descLoaded2, false);
        try {
            commitAndStartNewTransaction(null);
        } catch (Exception e) {
            Assert.fail("Moving description element should not throw an exception. Exception is " + e.getMessage());
        }


    }

    @Test
    @Ignore
    public void testMoveDescriptionElementsToTaxon(){
        //Create data
        UUID commonNameFeatureUuid = Feature.COMMON_NAME().getUuid();
        Feature commonNameFeatureData = (Feature)termService.find(commonNameFeatureUuid);

        TaxonDescription sourceDescriptionData = TaxonDescription.NewInstance();
        TextData elementData = TextData.NewInstance();
        elementData.setFeature(commonNameFeatureData);
        sourceDescriptionData.addElement(elementData);

        TextData element2 = TextData.NewInstance();
        element2.setFeature(commonNameFeatureData);
        sourceDescriptionData.addElement(element2);

        TextData element3Data = TextData.NewInstance();
        element3Data.setFeature(commonNameFeatureData);
        sourceDescriptionData.addElement(element3Data);
        Assert.assertEquals(3, sourceDescriptionData.getElements().size());
        TaxonDescription targetDescriptionData = TaxonDescription.NewInstance();
        this.service.save(sourceDescriptionData);
        this.service.save(targetDescriptionData);

        commitAndStartNewTransaction(null);

        TaxonDescription sourceDescription = (TaxonDescription)this.service.find(sourceDescriptionData.getId());
        Assert.assertEquals(3, sourceDescription.getElements().size());

        TaxonDescription targetDescription = (TaxonDescription)this.service.find(targetDescriptionData.getId());


        Collection<DescriptionElementBase> sourceCollection = new HashSet<DescriptionElementBase>();
        sourceCollection.addAll(sourceDescription.getElements());
        sourceCollection.remove(element3Data);  //should work as it works on equal
        Assert.assertEquals(2, sourceCollection.size());

        service.moveDescriptionElementsToDescription(sourceCollection, targetDescription, false);

        Assert.assertEquals("Source description should have 1 element left", 1, sourceDescription.getElements().size());
        Assert.assertEquals("Target description should have 2 new elements", 2, targetDescription.getElements().size());
        //the following tests are not valid anymore as elements are cloned now even if isCopy is false
        //		Assert.assertTrue("The moved element should be in the new description", targetDescription.getElements().contains(element));
        //		Assert.assertTrue("The moved element2 should be in the new description", targetDescription.getElements().contains(element2));
        //		Assert.assertFalse("Element3 should not be in the new description", targetDescription.getElements().contains(element3));

        Assert.assertTrue("Element3 should remain in the old description", sourceDescription.getElements().contains(element3Data));
        sourceDescription = (TaxonDescription) this.service.find(sourceDescription.getUuid());
        targetDescription = (TaxonDescription) this.service.find(targetDescription.getUuid());
        assertNotNull(sourceDescription);
        assertNotNull(targetDescription);
        try {
            service.moveDescriptionElementsToDescription(targetDescription.getElements(), sourceDescription, false);
        } catch (Exception e) {
            //asserting that no ConcurrentModificationException is thrown when the elements collection is passed as a parameter
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertEquals("Source description should have 3 elements again", 3, sourceDescription.getElements().size());
        Assert.assertEquals("Destination description should have no elements again", 0, targetDescription.getElements().size());
        sourceDescription = (TaxonDescription) this.service.find(sourceDescription.getUuid());
        targetDescription = (TaxonDescription) this.service.find(targetDescription.getUuid());
        assertNotNull(sourceDescription);
        assertNull(targetDescription);




    }

    @Test
    public void testMoveDescriptionElementsToTaxonAndResaveDeletedDescription(){

      //Create data
        UUID commonNameFeatureUuid = Feature.COMMON_NAME().getUuid();
        Feature commonNameFeatureData = (Feature)termService.find(commonNameFeatureUuid);

        TaxonDescription sourceDescriptionData = TaxonDescription.NewInstance();
        TextData elementData = TextData.NewInstance();
        elementData.setFeature(commonNameFeatureData);
        sourceDescriptionData.addElement(elementData);

        TextData element2 = TextData.NewInstance();
        element2.setFeature(commonNameFeatureData);
        sourceDescriptionData.addElement(element2);

        TextData element3Data = TextData.NewInstance();
        element3Data.setFeature(commonNameFeatureData);
        sourceDescriptionData.addElement(element3Data);
        Assert.assertEquals(3, sourceDescriptionData.getElements().size());
        TaxonDescription targetDescriptionData = TaxonDescription.NewInstance();
        this.service.save(sourceDescriptionData);
        this.service.save(targetDescriptionData);

        commitAndStartNewTransaction(null);

        TaxonDescription sourceDescription = (TaxonDescription)this.service.find(sourceDescriptionData.getId());
        Assert.assertEquals(3, sourceDescription.getElements().size());

        TaxonDescription targetDescription = (TaxonDescription)this.service.find(targetDescriptionData.getId());
        service.moveDescriptionElementsToDescription(sourceDescription.getElements(), targetDescription, false);
        TaxonDescription removedDescription = (TaxonDescription) this.service.find(sourceDescription.getUuid());
        assertNull(removedDescription);
        this.service.save(targetDescription);

        removedDescription = (TaxonDescription) this.service.find(targetDescription.getUuid());
        assertNotNull(removedDescription);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {};
    }
