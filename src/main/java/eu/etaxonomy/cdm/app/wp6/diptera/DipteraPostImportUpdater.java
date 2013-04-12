// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.diptera;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 01.10.2009
 * @version 1.0
 */
public class DipteraPostImportUpdater {
	private static final Logger logger = Logger.getLogger(DipteraPostImportUpdater.class);

	static final ICdmDataSource cdmDestination = CdmDestinations.localH2Palmae();
	
	/**
	 * This method updateds the citation text by deleting <code>OriginalName</code> tags and 
	 * adding the original name to the source either as a link to an existing taxon name
	 * or as a string. The later becomes true if there is not exactly one matching name
	 * @param dataSource
	 * @return
	 */
	public boolean updateCitations(ICdmDataSource dataSource) {
		try{
			logger.warn("start updating citations");
			boolean result = true;
			CdmApplicationController cdmApp = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.VALIDATE);
			Set<DescriptionElementBase> citationsToSave = new HashSet<DescriptionElementBase>();
			TransactionStatus tx = cdmApp.startTransaction();

			logger.warn("start updating citations ... application context started");
			int modCount = 100;
			int page = 0;
			int count = cdmApp.getTaxonService().count(Taxon.class);
			List<TaxonBase> taxonList = cdmApp.getTaxonService().list(Taxon.class, 100000, page, null, null);
			List<TaxonNameBase> nameList = cdmApp.getNameService().list(null, 100000, page, null, null);
			Map<String, TaxonNameBase> nameMap = new HashMap<String, TaxonNameBase>();
			Map<String, TaxonNameBase> nameDuplicateMap = new HashMap<String, TaxonNameBase>();
			fillNameMaps(nameList, nameMap, nameDuplicateMap);
			
			int i = 0;
			
			Taxon taxon;
			for (TaxonBase taxonBase : taxonList){
				if ((i++ % modCount) == 0){ logger.warn("taxa handled: " + (i-1));}
				
				if (taxonBase.isInstanceOf(Taxon.class)){
					taxon = CdmBase.deproxy(taxonBase, Taxon.class);
					Set<TextData> citations = getCitations(taxon);
					for (TextData citation : citations){
						Language language = Language.DEFAULT();
						String text = citation.getText(language);
						String originalNameString = parseOriginalNameString(text);
						String newText = parseNewText(text);
						citation.removeText(language);
						citation.putText(language, newText);
						TaxonNameBase scientificName = getScientificName(originalNameString, nameMap, nameDuplicateMap);
						
						Set<DescriptionElementSource> sources = citation.getSources();
						if (sources.size() > 1){
							logger.warn("There are more then 1 sources for a description");
						}else if (sources.size() == 0){
							DescriptionElementSource source = DescriptionElementSource.NewInstance();
							citation.addSource(source);
							sources = citation.getSources();
						}
						for (DescriptionElementSource source : sources){
							if (scientificName != null){
								source.setNameUsedInSource(scientificName);
							}else{
								source.setOriginalNameString(originalNameString);
							}
						}
						
						citationsToSave.add(citation);
					}
				}
			}
				
			cdmApp.getDescriptionService().saveDescriptionElement(citationsToSave);
			//commit
			cdmApp.commitTransaction(tx);
			logger.warn("Citations updated!");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR in citation update");
			return false;
		}
		
	}
	
	public boolean updateCollections(ICdmDataSource dataSource){
		DipteraCollectionImport collectionImport = new DipteraCollectionImport();
		return collectionImport.invoke(dataSource);
	}


	private void fillNameMaps(List<TaxonNameBase> nameList, Map<String, TaxonNameBase> nameMap, Map<String, TaxonNameBase> duplicateMap) {
		for (TaxonNameBase name : nameList){
			NonViralName nvn = name.deproxy(name, NonViralName.class);
			String nameCache = nvn.getNameCache();
			if (nameMap.containsKey(nameCache)){
				duplicateMap.put(nameCache, nvn);
			}else{
				nameMap.put(nameCache, nvn);
			}
		}
	}
	
	
	private TaxonNameBase getScientificName(String originalNameString, Map<String, TaxonNameBase> nameMap, Map<String, TaxonNameBase> nameDuplicateMap) {
		originalNameString = originalNameString.trim();
		TaxonNameBase result = nameMap.get(originalNameString);
		if (nameDuplicateMap.containsKey(originalNameString)){
			result = null;
		}
		return result;
	}

	private TaxonNameBase getScientificName(String originalNameString, INameService nameService) {
		Pager<TaxonNameBase> names = nameService.findByName(null, originalNameString, null, null, null, null, null, null);
		if (names.getCount() != 1){
			return null;
		}else{
			return names.getRecords().get(0);
		}
	}

	private String parseOriginalNameString(String text) {
		String originalName = "<OriginalName>";
		int start = text.indexOf(originalName);
		int end = text.indexOf("</OriginalName>");
		if (start >-1 ){
			text = text.substring(start + originalName.length(), end);
		}
		text = text.trim();
		return text;
	}

	private String parseNewText(String text) {
		int start = text.indexOf("</OriginalName>");
		text = text.substring(start + "</OriginalName>".length());
		text = text.trim();
		if (text.startsWith(":")){
			text = text.substring(1);
		}
		text = text.trim();
		return text;
	}

	private Set<TextData> getCitations(Taxon taxon) {
		Set<TextData> result = new HashSet<TextData>();
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		for (DescriptionBase description : descriptions){
			Set<DescriptionElementBase> elements = description.getElements();
			for (DescriptionElementBase element : elements){
				Feature feature = element.getFeature();
				if (feature.equals(Feature.CITATION())){
					if (! element.isInstanceOf(TextData.class)){
						logger.warn("Citation is not of class TextData but " + element.getClass().getSimpleName());
					}else{
						TextData textData = element.deproxy(element, TextData.class);
						result.add(textData);
					}
				}
			}
		}
		return result;
	}



	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DipteraPostImportUpdater updater = new DipteraPostImportUpdater();
		try {
			updater.updateCitations(cdmDestination);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR in feature tree update");
		}
	}

}
