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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author a.mueller
 * @date 07.04.2010
 *
 */
public class DipteraCollectionImport {
	private static final Logger logger = Logger.getLogger(DipteraCollectionImport.class);

	public static final File acronymsFile = new File("src/main/resources/collections/Acronyms.tab");
	//datasource for use from local main()
	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_dipera();
	

	public boolean invoke(ICdmDataSource dataSource) {
		CdmApplicationController cdmApp = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.VALIDATE);
			
		//create collections
		TransactionStatus tx = cdmApp.startTransaction();
		Map<String, Collection> colletionMap = createCollections(cdmApp);
		
		//add collections to specimen
		addCollectionsToSpecimen(cdmApp, colletionMap);
		cdmApp.commitTransaction(tx);
		
		return true;
		
	}


	/**
	 * @param cdmApp
	 * @param colletionMap 
	 */
	private void addCollectionsToSpecimen(CdmApplicationController cdmApp, Map<String, Collection> colletionMap) {
		List<SpecimenOrObservationBase> specimens = (cdmApp.getOccurrenceService().list(Specimen.class, null, null, null, null));
		for (SpecimenOrObservationBase specOrObservBase : specimens){
			if (specOrObservBase instanceof Specimen){
				handleSingleSpecimen((Specimen)specOrObservBase, colletionMap);
			}else{
				logger.warn("There are specimenOrObservationBase objects which are not of class Specimen. This is probably an error.");
			}
		}
		cdmApp.getOccurrenceService().save(specimens);
	}


	/**
	 * @param specimen 
	 * @param colletionMap
	 */
	private void handleSingleSpecimen(Specimen specimen, Map<String, Collection> collectionMap) {
		String titleCache = specimen.getTitleCache();
		String collectionCode = getCollectionCode(titleCache);
		if (CdmUtils.isEmpty(collectionCode)){
			logger.warn("Collection code is empty for: " + titleCache);
		}else{
			Collection collection = collectionMap.get(collectionCode);
			if (collection != null){
				specimen.setCollection(collection);
			}else{
				logger.warn("Collection not found for code: " +  collectionCode + "; titleCache: " +  titleCache);
			}
		}
	}


	/**
	 * @param titleCache
	 * @return
	 */
	private String getCollectionCode(String titleCache) {
		String result = titleCache.trim();
		result = replaceBracket(result);
		result = replaceLastFullStop(result);
		result = replaceLastQuestionMark(result);
		result = parseLastUpperCase(result);
		return result;
	}


	/**
	 * @param result
	 * @return
	 */
	private String parseLastUpperCase(String string) {
		String result = "";
		String tmpString = string;
		int pos = tmpString.lastIndexOf(" ");
		if (pos>-1){
			tmpString = tmpString.substring(pos+1);
		}
		while (tmpString.length() > 0){
			int len = tmpString.length();
			char lastChar = tmpString.charAt(len-1);
			if (Character.isUpperCase( lastChar)){
				result = lastChar + result;
			}else{
				if (result.length() > 0){
					logger.warn("Collection code is not space separated: " + string);
				}
				break;
			}
			//remove last character
			tmpString = tmpString.substring(0, tmpString.length()-1);
		}
		return result;
	}



	/**
	 * @param result
	 * @return
	 */
	private String replaceLastQuestionMark(String string) {
		if (string.endsWith("?")){
			string = string.substring(0,string.length()-1).trim();
		}
		return string;
	}
	
	/**
	 * @param result
	 * @return
	 */
	private String replaceLastFullStop(String string) {
		if (string.endsWith(".")){
			string = string.substring(0,string.length()-1).trim();
		}
		return string;
	}


	/**
	 * @param result
	 * @return
	 */
	private String replaceBracket(String string) {
		if (string.endsWith("]")){
			int pos  = string.indexOf("[");
			if (pos >0){
				string = string.substring(0, pos).trim();
			}else{
				logger.warn("Closing bracket has no opening bracket in: " + string);
			}
		}
		return string;
	}


	/**
	 * @param cdmApp
	 */
	private Map<String, Collection> createCollections(CdmApplicationController cdmApp) {
		Map<String, Collection> collectionMap = new HashMap<String, Collection>(); 
		List<String[]> lines = getLines();
		for (String[] line:lines){
			Collection collection = makeLine(line);
			collectionMap.put(collection.getCode(), collection);
		}
		cdmApp.getCollectionService().save(collectionMap.values());
//			for (Collection collection: collectionMap.values()){
//				System.out.println(collection.getTitleCache());
//			}
		return collectionMap;
	}
	

	private Collection makeLine(String[] line) {
		String code = line[0];
		String instituteName = line[1];
		String lowerInstitutionName = line[2];
		String higherInstitutionName = line[3];
		String location = line[4];
		String country = line[5];
		//create objects
		Collection collection = Collection.NewInstance();
		collection.setCode(code);
		Institution institution = Institution.NewInstance();
		institution.setCode(code);
		
		institution.setName(instituteName);
		
		if (CdmUtils.isNotEmpty(lowerInstitutionName)){
			Institution lowerInstitution = Institution.NewInstance();
			lowerInstitution.setName(lowerInstitutionName);
			lowerInstitution.setIsPartOf(institution);
		}
		
		if (CdmUtils.isNotEmpty(higherInstitutionName)){
			Institution higherInstitution = Institution.NewInstance();
			higherInstitution.setName(higherInstitutionName);
			institution.setIsPartOf(higherInstitution);
		}
		
		collection.setInstitute(institution);
		String locationAndCountry = CdmUtils.concat("/", location, country);
		collection.setTownOrLocation(locationAndCountry);
		
		String titleCache = CdmUtils.concat(", ", new String[]{instituteName, lowerInstitutionName, higherInstitutionName, location, country});
		collection.setTitleCache(titleCache, true);
		
		return collection;
	}

	
	
	
	private List<String[]> getLines() {
		List<String[]> result = new ArrayList<String[]>();
		
		try {
			InputStream inStream = new FileInputStream(acronymsFile);
			InputStreamReader inputStreamReader = new InputStreamReader(inStream, "UTF8");
			CSVReader reader = new CSVReader(inputStreamReader, '\t');
			String [] nextLine = reader.readNext();
			
			
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine.length == 0){
					continue;
				}
				result.add(nextLine);
			}
			return result;
		} catch (Exception e) {
			logger.error(e + " " + e.getCause() + " " + e.getMessage());
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
			throw new RuntimeException(e);
		}
	}





	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DipteraCollectionImport collectionImport = new DipteraCollectionImport();
			collectionImport.invoke(cdmDestination);
//			String titleCache = "Peru. Mouth of Rio Pachitea. ST 2R SMT. [fig. of male abdomen]";
//			String collectionCode = collectionImport.getCollectionCode(titleCache);
//			System.out.println(collectionCode);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
