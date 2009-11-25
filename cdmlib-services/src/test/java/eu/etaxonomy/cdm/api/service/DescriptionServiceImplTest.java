/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 09.02.2009
 * @version 1.0
 */
public class DescriptionServiceImplTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DescriptionServiceImplTest.class);
	
	@SpringBeanByType
	private IDescriptionService service;
	
	

	@Test
	public void testGetDefaultFeatureVocabulary() {
		
		service.getDefaultFeatureVocabulary();
	}
	
	@Test
	@DataSet("CommonServiceImplTest.xml")
	public void testChangeDescriptionElement(){
		DescriptionBase descBase = service.find(UUID.fromString("eb17b80a-9be6-4642-a6a8-b19a318925e6"));
		Set<DescriptionElementBase> elements = descBase.getElements();
		Iterator iterator = elements.iterator();
		while (iterator.hasNext()){
			DescriptionElementBase base = (DescriptionElementBase) iterator.next();
			if (base instanceof TextData){
				TextData textdata = (TextData) base;
				Set <Entry<Language,LanguageString>> entries = textdata.getMultilanguageText().entrySet();
				Iterator entryIterator = entries.iterator();
				while (entryIterator.hasNext()){
					Entry <Language, LanguageString> entry = (Entry<Language, LanguageString>) entryIterator.next();
					LanguageString langString = entry.getValue();
					System.out.println(langString);
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
			Iterator entryIterator = entries.iterator();
			while (entryIterator.hasNext()){
				Entry <Language, LanguageString> entry = (Entry<Language, LanguageString>) entryIterator.next();
				LanguageString langString = entry.getValue();
				System.out.println(langString);
			}
		}
	}
	
}
