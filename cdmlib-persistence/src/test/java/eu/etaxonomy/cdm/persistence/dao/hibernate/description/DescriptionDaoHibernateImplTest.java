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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
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

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

@DataSet
public class DescriptionDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private IDescriptionDao descriptionDao;

    @SpringBeanByType
    private IDefinedTermDao definedTermDao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

    private Set<Feature> features;

    private UUID antarcticaUuid;

    private UUID uuidTaxonDescription1;

    private UUID taxonSphingidaeUuid;

    @Before
    public void setUp() {
        uuidTaxonDescription1 = UUID.fromString("5f3265ed-68ad-4ec3-826f-0d29d25986b9");

        antarcticaUuid = UUID.fromString("791b3aa0-54dd-4bed-9b68-56b4680aad0c");
        taxonSphingidaeUuid = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");

        features = new HashSet<>();
    }

    @Test
    public void testCountDescriptionElements() {

        boolean includeUnpublished = false;
        long numberOfDescriptionElements = descriptionDao.countDescriptionElements(null, null, null, null, includeUnpublished);
        assertEquals("expecting 35 description elements in total", 35, numberOfDescriptionElements);

        numberOfDescriptionElements = descriptionDao.countDescriptionElements(null, null, null, TextData.class, includeUnpublished);
        assertEquals("expecting 3 published description elements of type TextData", 3, numberOfDescriptionElements);

        numberOfDescriptionElements = descriptionDao.countDescriptionElements(null, TaxonDescription.class, null, TextData.class, includeUnpublished);
        assertEquals("expecting 2 published description elements of type TextData", 2, numberOfDescriptionElements);

        DescriptionBase<?> description = descriptionDao.findByUuid(uuidTaxonDescription1);
        assert description != null : "description must exist";

        numberOfDescriptionElements = descriptionDao.countDescriptionElements(description, null, null, TextData.class, includeUnpublished);

        assertEquals("expecting 2 description elements of type TextData in specific description", 2, numberOfDescriptionElements);
    }

    @Test
    public void testGetDescriptionElements() {

        boolean includeUnpublished = false;

        List<TextData> elements = descriptionDao.listDescriptionElements(null, null, null, null, includeUnpublished, null, null, null);
        assertEquals("expecting 35 description elements in total without unpublished", 35, elements.size());

        elements = descriptionDao.listDescriptionElements(null, null, null, TextData.class, includeUnpublished, null, null, null);
        assertEquals("expecting 4 published description elements of type TextData", 3, elements.size());

        elements = descriptionDao.listDescriptionElements(null, TaxonDescription.class, null, TextData.class, includeUnpublished, null, null, null);
        assertEquals("expecting 3 published description elements of type TextData", 2, elements.size());

        elements = descriptionDao.listDescriptionElements(null, SpecimenDescription.class, null, TextData.class, includeUnpublished, null, null, null);
        assertEquals("expecting 1 description elements of type TextData", 1, elements.size());

        DescriptionBase<?> description = descriptionDao.findByUuid(uuidTaxonDescription1);
        assert description != null : "description must exist";

        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("multilanguageText");
        propertyPaths.add("media");
        propertyPaths.add("feature");
        propertyPaths.add("sources.citation");

        elements = descriptionDao.listDescriptionElements(description, null, null, TextData.class, includeUnpublished, null, null, propertyPaths);

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
        elements = descriptionDao.listDescriptionElements(description, null, null, TextData.class, includeUnpublished, null, null, propertyPaths);

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
        assertTrue("OriginalSourceBase.citation should be initialized",Hibernate.isInitialized(element34.getSources().iterator().next().getCitation()));
        assertTrue("DescriptionElementBase.feature should be initialized",Hibernate.isInitialized(element34.getFeature()));
        assertTrue("DescriptionElementBase.media should be initialized",Hibernate.isInitialized(element34.getMedia()));
        assertTrue("TextData.multilanguageText should be initialized",Hibernate.isInitialized(((TextData)element34).getMultilanguageText()));
    }

    @Test
    public void testCountDescriptionElementsFeature() {
        features.add(Feature.ECOLOGY());
        DescriptionBase<?> description = descriptionDao.findByUuid(uuidTaxonDescription1);
        assert description != null : "description must exist";

        long numberOfDescriptionElements = descriptionDao.countDescriptionElements(description, null, features, TextData.class, true);

        assertEquals("countDescriptionElements should return 1", 1, numberOfDescriptionElements);
    }

    @Test
    public void testGetDescriptionElementsByFeature() {

        boolean includeUnpublished = false;

        // 2. search for one Feature: ECOLOGY
        features.add(Feature.ECOLOGY());
        DescriptionBase<?> description = descriptionDao.findByUuid(uuidTaxonDescription1);
        assert description != null : "description must exist";

        List<TextData> elements = descriptionDao.listDescriptionElements(null, null, features, TextData.class, includeUnpublished, null, null,null);
        assertNotNull("getDescriptionElements should return a List", elements);
        assertEquals("getDescriptionElement should return 2 elements", 2, elements.size());

        elements = descriptionDao.listDescriptionElements(description, null, features, TextData.class, includeUnpublished, null, null,null);
        assertEquals("getDescriptionElement should return 1 elements", 1, elements.size());

        elements = descriptionDao.listDescriptionElements(null, SpecimenDescription.class, features, TextData.class, includeUnpublished, null, null,null);
        assertEquals("getDescriptionElement should return 1 elements", 1, elements.size());

        // 2. search for more Features: ECOLOGY & DESCRIPTION
        features.add(Feature.DESCRIPTION());
        elements = descriptionDao.listDescriptionElements(null, null, features, TextData.class, includeUnpublished, null, null,null);
        assertEquals("getDescriptionElement should return 3 elements", 3, elements.size());

        elements = descriptionDao.listDescriptionElements(null, TaxonDescription.class, features, TextData.class, includeUnpublished, null, null,null);
        assertEquals("getDescriptionElement should return 2 published elements", 2, elements.size());
    }

    @Test
    public void testGetDescriptionElementForTaxon() {

        Taxon taxonSphingidae = (Taxon) taxonDao.load(taxonSphingidaeUuid);
        assert taxonSphingidae != null : "taxon must exist";

        // 1.
        List<DescriptionElementBase> elements1 = descriptionDao.getDescriptionElementForTaxon(
                taxonSphingidae.getUuid(), null, null, false, null, 0, null);

        assertNotNull("getDescriptionElementForTaxon should return a List", elements1);
        assertEquals("getDescriptionElementForTaxon should return 1 elements",1,elements1.size());

        // 2.
        List<DescriptionElementBase> elements2 = descriptionDao.getDescriptionElementForTaxon(
                taxonSphingidae.getUuid(), null, DescriptionElementBase.class, false, null, 0, null);

        assertNotNull("getDescriptionElementForTaxon should return a List", elements2);
        assertTrue("getDescriptionElementForTaxon should be empty",elements2.isEmpty());

        // 3.
        List<Distribution> elements3 = descriptionDao.getDescriptionElementForTaxon(
                taxonSphingidae.getUuid() , null, Distribution.class, false, null, 0, null);

        assertNotNull("getDescriptionElementForTaxon should return a List", elements3);
        assertEquals("getDescriptionElementForTaxon should return 1 elements",1,elements3.size());
    }

    //see #2234
    @Test
    public void testSaveClonedDescription() {

        Taxon taxon = Taxon.NewInstance(null, null);
        taxon.setTitleCache("##### created in testSaveClonedDescription()", true);
        taxonDao.save(taxon);
        commitAndStartNewTransaction(null);

        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        this.descriptionDao.saveOrUpdate(description);

        assertTrue(true);
    }

    //see #2592
    @Test
    public void testSaveScope(){

        long n1 = this.descriptionDao.count();
        Taxon taxon = Taxon.NewInstance(null, null);
        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        this.taxonDao.save(taxon);
        descriptionDao.save(description);
        long n2 = this.descriptionDao.count();
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
        Set<MarkerType> markerTypes = new HashSet<>();
        markerTypes.add(completeMarkerType);
        long n1 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, null);
        Assert.assertEquals("There should be 1 description marked 'complete'", 1, n1);
        List<TaxonDescription> descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, null, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("There should be 1 description marked 'complete'", 1, descriptions.size());

        //doubtful
        MarkerType isDoubtfulMarkerType = (MarkerType)this.definedTermDao.findByUuid(UUID.fromString("b51325c8-05fe-421a-832b-d86fc249ef6e")); //Marker "doubtful"
        Assert.assertNotNull("MarkerType for 'doubtful' should exist", isDoubtfulMarkerType);
        markerTypes = new HashSet<>();  //reset
        markerTypes.add(isDoubtfulMarkerType);
        long n2 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, null);
        Assert.assertEquals("There should be no description marked 'doubtful'", 0, n2);
        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, null, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("There should be 0 description marked 'doubtful'", 0, descriptions.size());

        //imported = false
        UUID uuidImported = UUID.fromString("96878790-4ceb-42a2-9738-a2242079b679");
        MarkerType importedMarkerType = (MarkerType)this.definedTermDao.findByUuid(uuidImported);
        Assert.assertNotNull("MarkerType for 'imported' should exist", completeMarkerType);
        markerTypes = new HashSet<MarkerType>();
        markerTypes.add(importedMarkerType);
        long n3 = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, null);
        Assert.assertEquals("There should be no description marked 'imported' as true", 0, n3);
        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, null, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("There should be no description marked 'imported' as true", 0, descriptions.size());

        markerTypes = null;
        descriptions = this.descriptionDao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, null, pageSize, pageNumber, propertyPaths);
        Assert.assertEquals("There should be 1 description", 1, descriptions.size());
        TaxonDescription desc = descriptions.iterator().next();
        boolean hasMarkerImportedAsFalse = desc.hasMarker(importedMarkerType, false);
        Assert.assertTrue("The only description should have a negative marker on 'imported'", hasMarkerImportedAsFalse);
    }

    @Test
    public void testListTaxonDescriptionWithTypes(){

        Taxon taxon = (Taxon)this.taxonDao.findByUuid(UUID.fromString("b04cc9cb-2b4a-4cc4-a94a-3c93a2158b06"));
        Set<DefinedTerm> scopes = null;
        Set<NamedArea> geographicalScope = null;
        Set<MarkerType> markerTypes = null;
        Integer pageSize = null;
        Integer pageNumber = null;
        List<String> propertyPaths = null;

        //types
        Set<DescriptionType> types = EnumSet.noneOf(DescriptionType.class);
        long n = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, types);
        Assert.assertEquals("There should be 1 description for the given taxon", 1, n);

        types = EnumSet.of(DescriptionType.AGGREGATED);
        n = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, types);
        Assert.assertEquals("There should be 1 description of type 'aggregated' for the given taxon", 1, n);

        types = EnumSet.of(DescriptionType.COMPUTED);
        n = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, types);
        Assert.assertEquals("There should be 0 descriptions of type 'computed' for the given taxon", 0, n);

        types = EnumSet.of(DescriptionType.AGGREGATED, DescriptionType.COMPUTED);
        n = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, types);
        Assert.assertEquals("There should be 0 descriptions of type 'aggregated' "
                + "and type 'computed' for the given taxon", 0, n);

        types = EnumSet.of(DescriptionType.AGGREGATED, DescriptionType.SECONDARY_DATA);
        n = this.descriptionDao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, types);
        Assert.assertEquals("There should be 1 descriptions of type 'aggregated' "
                + "and type 'secondary data' for the given taxon", 1, n);

    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"),
        @DataSet(value="DescriptionDaoHibernateImplTest.xml")
    })
    public void testListNamedAreasInUse(){

        DefinedTermBase<?> antarctica = definedTermDao.load(antarcticaUuid);
        antarctica.getRepresentations().add(Representation.NewInstance("Antarktis", "Antarktis", "An", Language.GERMAN()));
        definedTermDao.saveOrUpdate(antarctica);
        commitAndStartNewTransaction(null);

        Collection<TermDto> list = descriptionDao.listNamedAreasInUse(false, null, null);
        Assert.assertEquals(3, list.size());
    }

    @Test
    @DataSet
    public void testListNamedAreasInUseWithParents(){

        Collection<TermDto> list = descriptionDao.listNamedAreasInUse(true, null, null);
        Assert.assertEquals(3, list.size());
    }


    @Test
    @DataSet
    public void testGetNodeOfIndividualAssociationForSpecimen() {
    	List<SortableTaxonNodeQueryResult> list = descriptionDao.getNodeOfIndividualAssociationForSpecimen(UUID.fromString("4c3231a9-336e-4b21-acf2-129683627de4"), null);
    	Assert.assertEquals(1, list.size());

    }
    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
