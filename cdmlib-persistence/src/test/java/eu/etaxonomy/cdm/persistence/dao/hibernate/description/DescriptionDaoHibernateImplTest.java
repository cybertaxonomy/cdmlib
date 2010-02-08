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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class DescriptionDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	IDescriptionDao descriptionDao;
	
	@SpringBeanByType
	IDefinedTermDao definedTermDao;
	
	private Set<NamedArea> namedAreas;
	private Set<Feature> features;
	
	private UUID northernAmericaUuid;
	private UUID southernAmericaUuid;
	private UUID antarcticaUuid;
	
	private UUID uuid;
	
	@Before
	public void setUp() {
		uuid = UUID.fromString("5f3265ed-68ad-4ec3-826f-0d29d25986b9");
		
		namedAreas = new HashSet<NamedArea>();
		northernAmericaUuid = UUID.fromString("2757e726-d897-4546-93bd-7951d203bf6f");
		southernAmericaUuid = UUID.fromString("6310b3ba-96f4-4855-bb5b-326e7af188ea");
		antarcticaUuid = UUID.fromString("791b3aa0-54dd-4bed-9b68-56b4680aad0c");
		
		features = new HashSet<Feature>();
	}
	
	
	@Test
	public void testCountByDistribution() {
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
		
		int numberOfDescriptions = descriptionDao.countDescriptionByDistribution(namedAreas, PresenceTerm.PRESENT());
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
		
		List<TaxonDescription> descriptions = descriptionDao.searchDescriptionByDistribution(namedAreas, AbsenceTerm.ABSENT(), 10,0,null,null);
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
		List<DescriptionBase> descriptions = descriptionDao.listDescriptions(TaxonDescription.class, null, true, features,null,null,null,null);
		
		assertNotNull("listDescriptions should return a List",descriptions);
		assertFalse("listDescriptions should not be empty", descriptions.isEmpty());
		assertEquals("listDescriptions should return 1 descriptions",1,descriptions.size());
	}
	
	@Test
	public void testCountDescriptionElements() {
		DescriptionBase description = descriptionDao.findByUuid(uuid);
		assert description != null : "description must exist";
		
		int numberOfDescriptionElements = descriptionDao.countDescriptionElements(description, null, TextData.class);
		
		assertEquals("countDescriptionElements should return 2",2,numberOfDescriptionElements);
	}
	
	@Test
	public void testGetDescriptionElements() {
		DescriptionBase description = descriptionDao.findByUuid(uuid);
		assert description != null : "description must exist";
		
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("multilanguageText");
		propertyPaths.add("media");
		propertyPaths.add("citation");
		propertyPaths.add("feature");
		
		List<DescriptionElementBase> elements = descriptionDao.getDescriptionElements(description, null, TextData.class, null, null,propertyPaths);
		
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
		elements = descriptionDao.getDescriptionElements(description, null, TextData.class, null, null,propertyPaths);
		
		for (DescriptionElementBase descElB: elements){
			if (descElB instanceof TextData){
				Map<Language, LanguageString> multiLanguage = ((TextData)descElB).getMultilanguageText();
				LanguageString defaultString = multiLanguage.get(Language.DEFAULT());
				
			}
		}
		
		assertNotNull("getDescriptionElements should return a List");
		assertFalse("getDescriptionElements should not be empty",elements.isEmpty());
		assertEquals("getDescriptionElement should return 2 elements",2,elements.size());
		assertTrue("ReferencedEntityBase.citation should be initialized",Hibernate.isInitialized(elements.get(0).getCitation()));
		assertTrue("DescriptionElementBase.feature should be initialized",Hibernate.isInitialized(elements.get(0).getFeature()));
		assertTrue("DescriptionElementBase.media should be initialized",Hibernate.isInitialized(elements.get(0).getMedia()));
		assertTrue("TextData.multilanguageText should be initialized",Hibernate.isInitialized(((TextData)elements.get(0)).getMultilanguageText()));
	}
	
	@Test
	public void testCountDescriptionElementsFeature() {
		features.add(Feature.ECOLOGY());
		DescriptionBase description = descriptionDao.findByUuid(uuid);
		assert description != null : "description must exist";
		
		int numberOfDescriptionElements = descriptionDao.countDescriptionElements(description, features, TextData.class);
		
		assertEquals("countDescriptionElements should return 1",1,numberOfDescriptionElements);
	}
	
	@Test
	public void testGetDescriptionElementsByFeature() {
		features.add(Feature.ECOLOGY());
		DescriptionBase description = descriptionDao.findByUuid(uuid);
		assert description != null : "description must exist";
		
		List<DescriptionElementBase> elements = descriptionDao.getDescriptionElements(description, features, TextData.class, null, null,null);
		
		assertNotNull("getDescriptionElements should return a List");
		assertFalse("getDescriptionElements should not be empty",elements.isEmpty());
		assertEquals("getDescriptionElement should return 1 elements",1,elements.size());
	}
	
}
