/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

@DataSet
public class DescriptionDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    IDescriptionDao descriptionDao;

    @SpringBeanByType
    IDefinedTermDao definedTermDao;

    @SpringBeanByType
    ITaxonDao taxonDao;

    private Set<NamedArea> namedAreas;
    private Set<Feature> features;

    private UUID northernAmericaUuid;
    private UUID southernAmericaUuid;
    private UUID antarcticaUuid;

    private UUID uuid;

    private UUID taxonSphingidaeUuid;

    @SuppressWarnings("unused")
    private static final String[] TABLE_NAMES = new String[] {"DESCRIPTIONBASE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING", "HOMOTYPICALGROUP","LANGUAGESTRING"
            , "ORIGINALSOURCEBASE", "REFERENCE", "TAXONBASE", "TAXONNAME", "HIBERNATE_SEQUENCES" };

    @Before
    public void setUp() {
        uuid = UUID.fromString("5f3265ed-68ad-4ec3-826f-0d29d25986b9");

        namedAreas = new HashSet<NamedArea>();
        northernAmericaUuid = UUID.fromString("2757e726-d897-4546-93bd-7951d203bf6f");
        southernAmericaUuid = UUID.fromString("6310b3ba-96f4-4855-bb5b-326e7af188ea");
        antarcticaUuid = UUID.fromString("791b3aa0-54dd-4bed-9b68-56b4680aad0c");
        taxonSphingidaeUuid = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");

        features = new HashSet<Feature>();

//		loadDataSet(getClass().getClassLoader().getResourceAsStream("eu/etaxonomy/cdm/persistence/dao/hibernate/description/DescriptionDaoHibernateImplTest.xml"));
//		printDataSet(System.err, TABLE_NAMES);
    }


    @Test
    public void testCountByDistribution() {
//		printDataSet(System.err, TABLE_NAMES);
        NamedArea northernAmerica = (NamedArea)definedTermDao.findByUuid(northernAmericaUuid);
        NamedArea southernAmerica = (NamedArea)definedTermDao.findByUuid(southernAmericaUuid);
        NamedArea antarctica = (NamedArea)definedTermDao.findByUuid(antarcticaUuid);

        assert northernAmerica != null : "term must exist";
        assert southernAmerica != null : "term must exist";
        assert antarctica != null : "term must exist";

        namedAreas.add(northernAmerica);
        namedAreas.add(southernAmerica);
        namedAreas.add(antarctica);

        int numberOfDescriptions = descriptionDao.countDescriptionByDistribution(namedAreas, null);
        assertEquals("countDescriptionsByDistribution should return 23",23,numberOfDescriptions);
    }

    @Test
    public void testCountByDistributionWithStatus() {
        NamedArea northernAmerica = (NamedArea)definedTermDao.findByUuid(northernAmericaUuid);
        NamedArea southernAmerica = (NamedArea)definedTermDao.findByUuid(southernAmericaUuid);
        NamedArea antarctica = (NamedArea)definedTermDao.findByUuid(antarcticaUuid);

        assert northernAmerica != null : "term must exist";
        assert southernAmerica != null : "term must exist";
        assert antarctica != null : "term must exist";

        namedAreas.add(northernAmerica);
        namedAreas.add(southernAmerica);
        namedAreas.add(antarctica);

        int numberOfDescriptions = descriptionDao.countDescriptionByDistribution(namedAreas, PresenceAbsenceTerm.PRESENT());
        assertEquals("countDescriptionsByDistribution should return 20",20,numberOfDescriptions);
    }

    @Test
    public void testGetByDistribution() {
        NamedArea northernAmerica = (NamedArea)definedTermDao.findByUuid(northernAmericaUuid);
        NamedArea southernAmerica = (NamedArea)definedTermDao.findByUuid(southernAmericaUuid);
        NamedArea antarctica = (NamedArea)definedTermDao.findByUuid(antarcticaUuid);

        assert northernAmerica != null : "term must exist";
        assert southernAmerica != null : "term must exist";
        assert antarctica != null : "term must exist";

        namedAreas.add(northernAmerica);
        namedAreas.add(southernAmerica);
        namedAreas.add(antarctica);

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("taxon");

        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("titleCache",SortOrder.ASCENDING));

        List<TaxonDescription> descriptions = descriptionDao.searchDescriptionByDistribution(namedAreas, null, 10,2,orderHints,propertyPaths);
        assertNotNull("searchDescriptionByDistribution should return a List",descriptions);
        assertFalse("searchDescriptionsByDistribution should not be empty",descriptions.isEmpty());
        assertEquals("searchDescriptionsByDistribution should return 3 elements",3,descriptions.size());
        assertTrue("TaxonDescription.taxon should be initialized",Hibernate.isInitialized(descriptions.get(0).getTaxon()));
        assertEquals("Sphingidae Linnaeus, 1758 sec. cate-sphingidae.org should come first","Sphingidae Linnaeus, 1758 sec. cate-sphingidae.org",descriptions.get(0).getTitleCache());
        assertEquals("Sphinx Linnaeus, 1758 sec. cate-sphingidae.org should come last","Sphinx Linnaeus, 1758 sec. cate-sphingidae.org",descriptions.get(2).getTitleCache());
    }

    @Test
    public void testGetByDistributionWithStatus() {
        NamedArea northernAmerica = (NamedArea)definedTermDao.findByUuid(northernAmericaUuid);
        NamedArea southernAmerica = (NamedArea)definedTermDao.findByUuid(southernAmericaUuid);
        NamedArea antarctica = (NamedArea)definedTermDao.findByUuid(antarcticaUuid);

        assert northernAmerica != null : "term must exist";
        assert southernAmerica != null : "term must exist";
        assert antarctica != null : "term must exist";

        namedAreas.add(northernAmerica);
        namedAreas.add(southernAmerica);
        namedAreas.add(antarctica);

        List<TaxonDescription> descriptions = descriptionDao.searchDescriptionByDistribution(namedAreas, PresenceAbsenceTerm.ABSENT(), 10,0,null,null);
        assertNotNull("searchDescriptionByDistribution should return a List",descriptions);
        assertFalse("searchDescriptionsByDistribution should not be empty",descriptions.isEmpty());
        assertEquals("searchDescriptionsByDistribution should return 3 elements",3,descriptions.size());
    }

    @Test
    public void testCountDescriptionsWithText() {
        int numberOfDescriptions = descriptionDao.countDescriptions(TaxonDescription.class, null, true, null);

        assertNotNull("countDescriptions should return a 2",numberOfDescriptions);
    }

    @Test
    public void testListDescriptionsWithText() {
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("titleCache",SortOrder.ASCENDING));
        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("taxon");
        List<DescriptionBase> descriptions = descriptionDao.listDescriptions(TaxonDescription.class, null, true, null,null,null,orderHints,propertyPaths);

        assertNotNull("listDescriptions should return a List",descriptions);
        assertFalse("listDescriptions should not be empty", descriptions.isEmpty());
        assertEquals("listDescriptions should return 2 descriptions",2,descriptions.size());
    }

    @Test
    public void testCountDescriptionsWithTextAndFeatures() {
        features.add(Feature.ECOLOGY());
        int numberOfDescriptions = descriptionDao.countDescriptions(TaxonDescription.class, null, true, features);

        assertNotNull("countDescriptions should return a 1",numberOfDescriptions);
    }

    @Test
    public void testListDescriptionsWithTextAndFeatures() {
        assert Feature.ECOLOGY() != null;
        features.add(Feature.ECOLOGY());

        List<DescriptionBase> descriptions = descriptionDao.listDescriptions(TaxonDescription.class, null, true, features, null, null, null, null);

        assertNotNull("listDescriptions should return a List",descriptions);
        assertFalse("listDescriptions should not be empty", descriptions.isEmpty());
        assertEquals("listDescriptions should return 1 descriptions",1,descriptions.size());
    }

    @Test
    public void testCountDescriptionElements() {

        int numberOfDescriptionElements = descriptionDao.countDescriptionElements(null, null, null, null);
        assertEquals("expecting 37 description elements in total", 37, numberOfDescriptionElements);

        numberOfDescriptionElements = descriptionDao.countDescriptionElements(null, null, null, TextData.class);
        assertEquals("expecting 4 description elements of type TextData", 4, numberOfDescriptionElements);

        numberOfDescriptionElements = descriptionDao.countDescriptionElements(null, TaxonDescription.class, null, TextData.class);
        assertEquals("expecting 3 description elements of type TextData", 3, numberOfDescriptionElements);

        DescriptionBase<?> description = descriptionDao.findByUuid(uuid);
        assert description != null : "description must exist";

        numberOfDescriptionElements = descriptionDao.countDescriptionElements(description, null, null, TextData.class);

        assertEquals("expecting 2 description elements of type TextData in specific description", 2, numberOfDescriptionElements);
    }

    @Test
    public void testGetDescriptionElements() {

        List<TextData> elements = descriptionDao.getDescriptionElements(null, null, null, null, null, null, null);
        assertEquals("expecting 37 description elements in total", 37, elements.size());

        elements = descriptionDao.getDescriptionElements(null, null, null, TextData.class, null, null, null);
        assertEquals("expecting 4 description elements of type TextData", 4, elements.size());

        elements = descriptionDao.getDescriptionElements(null, TaxonDescription.class, null, TextData.class, null, null, null);
        assertEquals("expecting 3 description elements of type TextData", 3, elements.size());

        elements = descriptionDao.getDescriptionElements(null, SpecimenDescription.class, null, TextData.class, null, null, null);
        assertEquals("expecting 1 description elements of type TextData", 1, elements.size());

        DescriptionBase<?> description = descriptionDao.findByUuid(uuid);
        assert description != null : "description must exist";

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("multilanguageText");
        propertyPaths.add("media");
        propertyPaths.add("feature");
        propertyPaths.add("sources.citation");

        elements = descriptionDao.getDescriptionElements(description, null, null, TextData.class, null, null, propertyPaths);

        for (DescriptionElementBase descElB: elements){
            if (descElB instanceof TextData){
                Map<Language, LanguageString> multiLanguage = ((TextData)descElB).getMultilanguageText();
                LanguageString defaultString = multiLanguage.get(Language.DEFAULT());

            }
        }
        Iterator<DescriptionElementBase> elements2 = description.getElements().iterator();
        while(elements2.hasNext()){
            DescriptionElementBase element = elements2.next();
            if (element instanceof TextData){
                Map<Language, LanguageString> multiLanguage = ((TextData)element).getMultilanguageText();
                LanguageString defaultString = multiLanguage.get(Language.DEFAULT());
                defaultString.setText("blablub");
            }
        }
        elements = descriptionDao.getDescriptionElements(description, null, null, TextData.class, null, null, propertyPaths);

        DescriptionElementBase element34 = null;
        for (DescriptionElementBase descElB: elements){
            if (descElB instanceof TextData){
                Map<Language, LanguageString> multiLanguage = ((TextData)descElB).getMultilanguageText();
                LanguageString defaultString = multiLanguage.get(Language.DEFAULT());
                //???
            }
            if (descElB.getId() == 34){
                element34 = descElB;
            }
        }

        assertNotNull("getDescriptionElements should return a List", elements);
        assertEquals("getDescriptionElement should return 2 elements",2,elements.size());
        assertNotNull("Description Element with ID 34 should be part of the list",element34);
        assertTrue("DescriptionElement.sources should be initialized",Hibernate.isInitialized(element34.getSources()));
        assertTrue("DescriptionElement.sources should have elements",element34.getSources().size() > 0);
        assertTrue("ReferencedEntityBase.citation should be initialized",Hibernate.isInitialized(element34.getSources().iterator().next().getCitation()));
        assertTrue("DescriptionElementBase.feature should be initialized",Hibernate.isInitialized(element34.getFeature()));
        assertTrue("DescriptionElementBase.media should be initialized",Hibernate.isInitialized(element34.getMedia()));
        assertTrue("TextData.multilanguageText should be initialized",Hibernate.isInitialized(((TextData)element34).getMultilanguageText()));
    }

    @Test
    public void testCountDescriptionElementsFeature() {
        features.add(Feature.ECOLOGY());
        DescriptionBase<?> description = descriptionDao.findByUuid(uuid);
        assert description != null : "description must exist";

        int numberOfDescriptionElements = descriptionDao.countDescriptionElements(description, features, TextData.class);

        assertEquals("countDescriptionElements should return 1", 1, numberOfDescriptionElements);
    }

    @Test
    public void testGetDescriptionElementsByFeature() {

        // 2. search for one Feature: ECOLOGY
        features.add(Feature.ECOLOGY());
        DescriptionBase<?> description = descriptionDao.findByUuid(uuid);
        assert description != null : "description must exist";

        List<TextData> elements = descriptionDao.getDescriptionElements(null, null, features, TextData.class, null, null,null);
        assertNotNull("getDescriptionElements should return a List", elements);
        assertEquals("getDescriptionElement should return 2 elements", 2, elements.size());

        elements = descriptionDao.getDescriptionElements(description, null, features, TextData.class, null, null,null);
        assertEquals("getDescriptionElement should return 1 elements", 1, elements.size());

        elements = descriptionDao.getDescriptionElements(null, SpecimenDescription.class, features, TextData.class, null, null,null);
        assertEquals("getDescriptionElement should return 1 elements", 1, elements.size());

        // 2. search for more Features: ECOLOGY & DESCRIPTION
        features.add(Feature.DESCRIPTION());
        elements = descriptionDao.getDescriptionElements(null, null, features, TextData.class, null, null,null);
        assertEquals("getDescriptionElement should return 4 elements", 4, elements.size());

        elements = descriptionDao.getDescriptionElements(null, TaxonDescription.class, features, TextData.class, null, null,null);
        assertEquals("getDescriptionElement should return 3 elements", 3, elements.size());
    }

    @Test
    public void testGetDescriptionElementForTaxon() {

        Taxon taxonSphingidae = (Taxon) taxonDao.load(taxonSphingidaeUuid);
        assert taxonSphingidae != null : "taxon must exist";

        // 1.

        List<DescriptionElementBase> elements1 = descriptionDao.getDescriptionElementForTaxon(
                taxonSphingidae.getUuid() , null, null, null, 0, null);

        assertNotNull("getDescriptionElementForTaxon should return a List", elements1);
        assertEquals("getDescriptionElementForTaxon should return 1 elements",1,elements1.size());

        // 2.

        List<DescriptionElementBase> elements2 = descriptionDao.getDescriptionElementForTaxon(
                taxonSphingidae.getUuid() , null, DescriptionElementBase.class, null, 0, null);

        assertNotNull("getDescriptionElementForTaxon should return a List", elements2);
        assertTrue("getDescriptionElementForTaxon should be empty",elements2.isEmpty());

        // 3.

        List<Distribution> elements3 = descriptionDao.getDescriptionElementForTaxon(
                taxonSphingidae.getUuid() , null, Distribution.class, null, 0, null);

        assertNotNull("getDescriptionElementForTaxon should return a List", elements3);
        assertEquals("getDescriptionElementForTaxon should return 1 elements",1,elements3.size());
    }

    //see #2234
    @Test
    public void testSaveClonedDescription() {

//		printDataSet(System.err, new String[]{"TAXONBASE"});

        Taxon taxon = Taxon.NewInstance(null, null);
        taxon.setTitleCache("##### created in testSaveClonedDescription()", true);
        taxonDao.save(taxon);
        commitAndStartNewTransaction(null);

        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        this.descriptionDao.saveOrUpdate(description);
//		TaxonDescription clonedDescription = (TaxonDescription)description.clone();
//		this.descriptionDao.saveOrUpdate(clonedDescription);
//		printDataSet(System.err, new String[]{"TAXONBASE"});

        assertTrue(true);

    }

    //see #2592
    @Test
    public void testSaveScope(){
        int n1 = this.descriptionDao.count();
        Taxon taxon = Taxon.NewInstance(null, null);
        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        this.taxonDao.save(taxon);
        int n2 = this.descriptionDao.count();
        Assert.assertEquals(1, n2-n1);

        DefinedTerm scope = DefinedTerm.SEX_FEMALE();
        description.addScope(scope);

        this.descriptionDao.saveOrUpdate(description);

    }

    @Test
    public void testListTaxonDescriptionWithMarker(){
        Taxon taxon = (Taxon)this.taxonDao.findByUuid(UUID.fromString("b04cc9cb-2b4a-4cc4-a94a-3c93a2158b06"));
        Set<DefinedTerm> scopes = null;
        Set<NamedArea> geographicalScope = null;
        Integer pageSize = null;
        Integer pageNumber = null;
        List<String> propertyPaths = null;

        //complete
        MarkerType completeMarkerType = (MarkerType)this.definedTermDao.findByUuid(UUID.fromString("b4b1b2ab-89a8-4ce6-8110-d60b8b1bc433")); //Marker "complete"
        Assert.assertNotNull("MarkerType for 'complete' should exist", completeMarkerType);
        Set<MarkerType> markerTypes = new HashSet<MarkerType>();
        markerTypes.add(completeMarkerType);
        int n1 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes);
        Assert.assertEquals("There should be 1 description marked 'complete'", 1, n1);
        List<TaxonDescription> descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("There should be 1 description marked 'complete'", 1, descriptions.size());

        //doubtful
        MarkerType isDoubtfulMarkerType = (MarkerType)this.definedTermDao.findByUuid(UUID.fromString("b51325c8-05fe-421a-832b-d86fc249ef6e")); //Marker "doubtful"
        Assert.assertNotNull("MarkerType for 'doubtful' should exist", isDoubtfulMarkerType);
        markerTypes = new HashSet<MarkerType>();  //reset
        markerTypes.add(isDoubtfulMarkerType);
        int n2 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes);
        Assert.assertEquals("There should be no description marked 'doubtful'", 0, n2);
        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("There should be 0 description marked 'doubtful'", 0, descriptions.size());

        //imported = false
        UUID uuidImported = UUID.fromString("96878790-4ceb-42a2-9738-a2242079b679");
        MarkerType importedMarkerType = (MarkerType)this.definedTermDao.findByUuid(uuidImported);
        Assert.assertNotNull("MarkerType for 'imported' should exist", completeMarkerType);
        markerTypes = new HashSet<MarkerType>();
        markerTypes.add(importedMarkerType);
        int n3 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes);
        Assert.assertEquals("There should be no description marked 'imported' as true", 0, n3);
        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("There should be no description marked 'imported' as true", 0, descriptions.size());
        markerTypes = null;
        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("There should be 1 description", 1, descriptions.size());
        TaxonDescription desc = descriptions.iterator().next();
        boolean hasMarkerImportedAsFalse = desc.hasMarker(importedMarkerType, false);
        Assert.assertTrue("The only description should have a negative marker on 'imported'", hasMarkerImportedAsFalse);

    }

//
//        //complete
//        MarkerType completeMarkerType = (MarkerType)this.definedTermDao.findByUuid(UUID.fromString("b4b1b2ab-89a8-4ce6-8110-d60b8b1bc433")); //Marker "complete"
//
//        Assert.assertNotNull("MarkerType for 'complete' should exist", completeMarkerType);
//        Set<MarkerType> markerTypes = new HashSet<MarkerType>();
//        markerTypes.add(completeMarkerType);
//        int n1 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes);
//        Assert.assertEquals("There should be 1 description marked 'complete'", 1, n1);
//        List<TaxonDescription> descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
//        Assert.assertEquals("There should be 1 description marked 'complete'", 1, descriptions.size());
//
//        //doubtful
//        MarkerType isDoubtfulMarkerType = (MarkerType)this.definedTermDao.findByUuid(UUID.fromString("b51325c8-05fe-421a-832b-d86fc249ef6e")); //Marker "doubtful"
//        Assert.assertNotNull("MarkerType for 'doubtful' should exist", isDoubtfulMarkerType);
//        markerTypes = new HashSet<MarkerType>();  //reset
//        markerTypes.add(isDoubtfulMarkerType);
//        int n2 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes);
//        Assert.assertEquals("There should be no description marked 'doubtful'", 0, n2);
//        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
//        Assert.assertEquals("There should be 0 description marked 'doubtful'", 0, descriptions.size());
//
//        //imported = false
//        UUID uuidImported = UUID.fromString("96878790-4ceb-42a2-9738-a2242079b679");
//        MarkerType importedMarkerType = (MarkerType)this.definedTermDao.findByUuid(uuidImported);
//        Assert.assertNotNull("MarkerType for 'imported' should exist", completeMarkerType);
//        markerTypes = new HashSet<MarkerType>();
//        markerTypes.add(importedMarkerType);
//        int n3 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes);
//        Assert.assertEquals("There should be no description marked 'imported' as true", 0, n3);
//        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
//        Assert.assertEquals("There should be no description marked 'imported' as true", 0, descriptions.size());
//        markerTypes = null;
//        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
//        Assert.assertEquals("There should be 1 description", 1, descriptions.size());
//        TaxonDescription desc = descriptions.iterator().next();
//        boolean hasMarkerImportedAsFalse = desc.hasMarker(importedMarkerType, false);
//        Assert.assertTrue("The only description should have a negative marker on 'imported'", hasMarkerImportedAsFalse);
//
//    }


    @Test
    @DataSet("DescriptionDaoHibernateImplTest.testListTaxonDescriptionMedia.xml")
    public void testListTaxonDescriptionMedia(){
        //init
        Taxon firstTaxon = (Taxon)this.taxonDao.findByUuid(UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2"));
        Taxon secondTaxon = (Taxon)this.taxonDao.findByUuid(UUID.fromString("822d98dc-9ef7-44b7-a870-94573a3bcb46"));
        Taxon thirdTaxon = (Taxon)this.taxonDao.findByUuid(UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9"));
        MarkerType markerType890 = (MarkerType)this.definedTermDao.findById(890);
        MarkerType markerType892 = (MarkerType)this.definedTermDao.findById(892);

        Assert.assertNotNull("First taxon should exist", firstTaxon);
        Assert.assertNotNull("Second taxon should exist", secondTaxon);
        Assert.assertNotNull("Third taxon should exist", thirdTaxon);
        Assert.assertEquals("There should be 6 descriptions in the database", 6, this.descriptionDao.count());


        //set parameter
        Set<MarkerType> markerTypes = null;
        Integer pageSize = null;
        Integer pageNumber = null;
        List<String> propertyPaths = null;

        //test
        //first Taxon gallery filter
        List<Media> mediaList = this.descriptionDao.listTaxonDescriptionMedia(firstTaxon.getUuid(), true, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("media list size for first taxon and filter on image galleries should be 1", 1, mediaList.size());

        //first taxon all descriptions
        mediaList = this.descriptionDao.listTaxonDescriptionMedia(firstTaxon.getUuid(), false, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("media list size for first taxon without filter on image galleries should be 2", 2, mediaList.size());

        //second taxon
        mediaList = this.descriptionDao.listTaxonDescriptionMedia(secondTaxon.getUuid(), true, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("media list size for second taxon and filter on image galleries should be 2", 2, mediaList.size());

        //all taxa
        mediaList = this.descriptionDao.listTaxonDescriptionMedia(null, true, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("media list size for any taxon and filter on image galleries should be 3", 3, mediaList.size());
        //with marker
        markerTypes = new HashSet<MarkerType>();
        mediaList = this.descriptionDao.listTaxonDescriptionMedia(null, true, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("Empty marker type set should give same result as before: 3", 3, mediaList.size());
        markerTypes.add(markerType890);
        mediaList = this.descriptionDao.listTaxonDescriptionMedia(null, true, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("markerType890 should only give 1 result", 1, mediaList.size());
        markerTypes.add(markerType892);
        mediaList = this.descriptionDao.listTaxonDescriptionMedia(null, true, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("markerType892 should not give additional result as the markers value is false", 1, mediaList.size());
        markerTypes = null;

        //check deduplication
        mediaList = this.descriptionDao.listTaxonDescriptionMedia(null, false, markerTypes, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("media list size for any taxon without filter on image galleries should be 3", 3, mediaList.size());

//        System.out.println(mediaList);
    }

    @Test
    @DataSet("DescriptionDaoHibernateImplTest.testListTaxonDescriptionMedia.xml")
    public void testcountTaxonDescriptionMedia(){
        //init
        Taxon firstTaxon = (Taxon)this.taxonDao.findByUuid(UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2"));
        Taxon secondTaxon = (Taxon)this.taxonDao.findByUuid(UUID.fromString("822d98dc-9ef7-44b7-a870-94573a3bcb46"));
        Taxon thirdTaxon = (Taxon)this.taxonDao.findByUuid(UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9"));
        MarkerType markerType890 = (MarkerType)this.definedTermDao.findById(890);
        MarkerType markerType892 = (MarkerType)this.definedTermDao.findById(892);

        Assert.assertNotNull("First taxon should exist", firstTaxon);
        Assert.assertNotNull("Second taxon should exist", secondTaxon);
        Assert.assertNotNull("Third taxon should exist", thirdTaxon);
        Assert.assertEquals("There should be 6 descriptions in the database", 6, this.descriptionDao.count());

        //set parameter
        Set<MarkerType> markerTypes = null;

        //test
        //first Taxon gallery filter
        int mediaCount = this.descriptionDao.countTaxonDescriptionMedia(firstTaxon.getUuid(), true, markerTypes);
        Assert.assertEquals("media list size for first taxon and filter on image galleries should be 1", 1, mediaCount);
        mediaCount = this.descriptionDao.countTaxonDescriptionMedia(firstTaxon.getUuid(), false, markerTypes);
        Assert.assertEquals("media list size for first taxon without filter on image galleries should be 2", 2, mediaCount);

        //second taxon
        mediaCount = this.descriptionDao.countTaxonDescriptionMedia(secondTaxon.getUuid(), true, markerTypes);
        Assert.assertEquals("media list size for second taxon and filter on image galleries should be 2", 2, mediaCount);

        //all taxa
        mediaCount = this.descriptionDao.countTaxonDescriptionMedia(null, true, markerTypes);
        Assert.assertEquals("media list size for any taxon and filter on image galleries should be 3", 3, mediaCount);
        //with marker
        markerTypes = new HashSet<MarkerType>();
        mediaCount = this.descriptionDao.countTaxonDescriptionMedia(null, true, markerTypes);
        Assert.assertEquals("Empty marker type set should give same result as before: 3", 3, mediaCount);
        markerTypes.add(markerType890);
        mediaCount = this.descriptionDao.countTaxonDescriptionMedia(null, true, markerTypes);
        Assert.assertEquals("markerType890 should only give 1 result", 1, mediaCount);
        markerTypes.add(markerType892);
        mediaCount = this.descriptionDao.countTaxonDescriptionMedia(null, true, markerTypes);
        Assert.assertEquals("markerType892 should not give additional result as the markers value is false", 1, mediaCount);
        markerTypes = null;

        //check deduplication
        mediaCount = this.descriptionDao.countTaxonDescriptionMedia(null, false, markerTypes);
        Assert.assertEquals("media list size for any taxon without filter on image galleries should be 3", 3, mediaCount);

    }

    @Test
//    @DataSet
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="DescriptionDaoHibernateImplTest.xml")
    })
    public void testListNamedAreasInUse(){

        Collection<TermDto> list = null;

        DefinedTermBase<?> antarctica = definedTermDao.load(antarcticaUuid);
        antarctica.getRepresentations().add(Representation.NewInstance("Antarktis", "Antarktis", "An", Language.GERMAN()));
        definedTermDao.saveOrUpdate(antarctica);
        commitAndStartNewTransaction(null);

        list = descriptionDao.listNamedAreasInUse(false, null, null);
        Assert.assertEquals(3, list.size());

    }

    @Test
    @DataSet
    // @Ignore // the first query in listNamedAreasInUse is for some reason not working with h2
    public void testListNamedAreasInUseWithParents(){

        Collection<TermDto> list = null;

        list = descriptionDao.listNamedAreasInUse(true, null, null);
        Assert.assertEquals(3, list.size());

    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {}


}
