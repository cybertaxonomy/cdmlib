/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.berlinModelImport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 17.10.2008
 * @version 1.0
 */
public class DipteraDistributionParser {
	private static final Logger logger = Logger.getLogger(DipteraDistributionParser.class);
	
	final static String epiSplitter = "(\\s+|\\[|\\]|\\(|\\))"; //( ' '+| '(' | ')'| '[' | ']' )
	static Pattern pattern = null;
	
	protected void doDistribution(CdmApplicationController app){
		pattern = Pattern.compile(epiSplitter); 
	    TransactionStatus txStatus = app.startTransaction();
		List<TaxonBase> taxa = app.getTaxonService().getAllTaxonBases(1000000, 0);
		for (TaxonBase taxon: taxa ){
			if (taxon instanceof Taxon){
		//		unlazyDescription(app, (Taxon)taxon);
				Set<TaxonDescription> descriptions = ((Taxon) taxon).getDescriptions();
				for (DescriptionBase description: descriptions){
					Set<DescriptionElementBase> descElements = new HashSet<DescriptionElementBase>();
					descElements.addAll(description.getElements());
					
					for (DescriptionElementBase descEl: descElements){
						if (descEl.getFeature().equals(Feature.OCCURRENCE())){
							if (descEl instanceof TextData){
								String occString = ((TextData)descEl).getText(Language.ENGLISH());
								parseOccurenceString(occString, description);
							}
						}
					}
				}
			}
		}
		System.out.println("Unknowns: ");
		for (String unknown: unrekognizedStrings){
			System.out.println(unknown);
		}
		System.out.println("Distributions not recognized: " + countNot);
		System.out.println("Distributions created: " + countYes);
		app.commitTransaction(txStatus);
	}
	
	static Set<String> unrekognizedStrings = new HashSet<String>();
	static int countNot = 0;
	static int countYes = 0;
	
	private void parseOccurenceString(String occString, DescriptionBase desc){
		System.out.println(occString);
		if (occString != null){
			String[] words = pattern.split(occString);
			int i = 0;
			int countSkip = 0;
			for (String word: words){
				boolean isDoubtful = false;
				if (countSkip > 0){
					countSkip--;
				}else if(word.contains("widesp") || word.equals("in")) {
					//skip
				}else if(word.trim().length() == 0){
					//skip
				}else{
					if (word.endsWith(":") && word.length()<=4){
						//Higher area
						//TODO
					}else{
						word = word.trim();
						if (word.contains("?")){
							isDoubtful = true;
							word = word.replace("?", "");
						}
						word = adaptWordsToTdwg(word);
						
						if (! "".equals(word) && ! TdwgArea.isTdwgAreaLabel(word) && ! isDoubleArea(word)){
							for (countSkip = 1; countSkip <= 6; countSkip++){
								word = word.trim();
								if (! TdwgArea.isTdwgAreaLabel(word) && ! isDoubleArea(word)){
									if (words.length > i + countSkip){
										word = word + " " + words[i + countSkip];
									}
									if (word.contains("?")){
										isDoubtful = true;
										word = word.replace("?", "");
									}
									word = adaptWordsToTdwg(word);
									if ("".equals(word)){
										break;
									}
								}else{
									break;
								}
							}
						}
						if ("".equals(word)){
							//countSkip = countSkip;
						}else if (! TdwgArea.isTdwgAreaLabel(word) && ! isDoubleArea(word)  ){
							if (word.contains("?")){
								logger.warn("XXX");
							}
							countNot++;
							System.out.println("   False:" + countNot + ": " + word);
							unrekognizedStrings.add(word);
							countSkip = 0;
						}else{
							PresenceAbsenceTermBase<?> term = PresenceTerm.PRESENT();
							if (isDoubleArea(word)){
								NamedArea[] doubleArea = getDoubleArea(word);
								for (NamedArea area : doubleArea){
									Distribution distr = Distribution.NewInstance(area, term);
									desc.addElement(distr);
								}
							}else{
								NamedArea area = TdwgArea.getAreaByTdwgLabel(word);
								if (isDoubtful){
									term = PresenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE();
								}
								Distribution distr = Distribution.NewInstance(area, term);
								desc.addElement(distr);
							}
							countYes++;
							System.out.println("      True:" + countYes + ": " + word);
							countSkip--;
						}
					}
				}
				i++;
			}
		}
	}
	
	private boolean isDoubleArea(String word){
		if ("Canary and Madeira Is.".equalsIgnoreCase(word) || 
				"southern Europe".equalsIgnoreCase(word) ||
				"former USSR: North and Central European territory".equalsIgnoreCase(word)
				){
			return true;
		}else{
			return false;
		}
	}
	
	private NamedArea[] getDoubleArea(String word){
		NamedArea[] result = new NamedArea[2];
		if ("Canary and Madeira Is.".equalsIgnoreCase(word)){
			 result[0] = TdwgArea.getAreaByTdwgAbbreviation("");
			 result[1] = TdwgArea.getAreaByTdwgAbbreviation("");
		}else if ("southern Europe".equalsIgnoreCase(word)){
			 result[0] = TdwgArea.getAreaByTdwgAbbreviation("");
			 result[1] = TdwgArea.getAreaByTdwgAbbreviation("");
		}else if ("former USSR: North and Central European territory".equalsIgnoreCase(word)){
			 result[0] = TdwgArea.getAreaByTdwgAbbreviation("");
			 result[1] = TdwgArea.getAreaByTdwgAbbreviation("");
		}else{
			logger.warn("Double area not recognized");
		}
		return result;
	}
	
	
	static List<String> stopWords = new ArrayList<String>();
	static List<String> unknownAreas = new ArrayList<String>();
	static List<String> higherAreas = new ArrayList<String>();
	
	private String adaptWordsToTdwg(String word){
		word = word.replace(",", "").replace(".", "").replace(";", "");
		word = word.replace("Caronlina", "Carolina");
		
		word = word.trim();
		if (word.endsWith("Is")){
			word = word + ".";
		}
		if (stopWords.size() == 0){
			initStopWords();
		}
		
		word = word.replace("Russia [North European territory]", "North European Russia");
		word = word.replace("Russia North European territory", "North European Russia");
		word = word.replace("Russia: North European territory", "North European Russia");
		word = word.replace("Russia: North European territory", "North European Russia");
				
		word = word.replace("Amber", "amber");
		
		
		word = word.replace("Prince Edward Is.", "Marion-Prince Edward Is.");
		//or word = word.replace("Prince Edward Is.", "Prince Edward I.");
		word = word.replace("Bahama Is.", "Bahamas");
		word = word.replace("Comores Is.", "Comoros");
		word = word.replace("former Yugoslavia", "Yugoslavia");
		word = word.replace("former Czechoslovakia", "Czechoslovakia");
		word = word.replace("Rhodesia", "Zimbabwe");
		if (!word.contains("El Salvador")){
			word = word.replace("Salvador", "El Salvador");	
		}
		word = word.replace("Vera Cruz", "Veracruz");
		word = word.replace("Turkmenia", "Turkmenistan");
		word = word.replace("Quebec", "Québec");
		word = word.replace("Gambia", "Gambia, The");
		word = word.replace("Mariana Is.", "Marianas");
		word = word.replace("Kenia", "Kenya");
		word = word.replace("Central Africa", "Central African Republic");
		word = word.replace("Panama", "Panamá");
		word = word.replace("Wales", "Great Britain");  //?? Problem mit New South Wales??
		word = word.replace("Java", "Jawa");
		word = word.replace("former USSR: North European territory", "North European Russia");
		word = word.replace("former USSR: South European territory", "South European Russia");
		word = word.replace("former USSR: Soviet Middle Asia", "Middle Asia");
		
		word = word.replace("oceanian islands", "Pacific");
		word = word.replace("Primorye", "Ussuri region");
		word = word.replace("Galapagos Is.", "Galápagos");
		word = word.replace("Malaysia", "Peninsular Malaysia");
		word = word.replace("Canal Zone", "Panamá");
		word = word.replace("Polynesic Is.", "South Solomons");

		word = word.replace("Usbek SSR", "Uzbekistan");
		word = word.replace("Mexican amber", "Mexico");
		word = word.replace("southern Europe", "Ussuri region");
		word = word.replace("Marocco", "Morocco");
		word = word.replace("Trinidad", "Trinidad-Tobago");
		word = word.replace("Haiti", "Haiti");  //??
		word = word.replace("Moluccas", "Maluku");
		word = word.replace("Belau", "Palau");
		word = word.replace("Dominican amber", "Dominican Republic");
		word = word.replace("Far East", "Russian Far East");
		word = word.replace("Tahiti", "Society Is.");

		
		
		unknownAreas.add("Baltic amber");  
		unknownAreas.add("Arabia"); 

		
		
		
						
		for (String stopWord : stopWords){
			if (stopWord.equals(word)){
				System.out.println("         STOP: " + word);
				return "";
			}
		}
		for (String unknownArea : unknownAreas){
			if (unknownArea.equals(word)){
				System.out.println("         UNKNOWN: " + word);
				return "";
			}
		}
		for (String higherArea : higherAreas){
			if (higherArea.equals(word)){
				return "";
			}
		}
		
		//higher regions
		
		return word;
	}
	
	private void initStopWords(){
		stopWords.add("to");
		stopWords.add("also");
		stopWords.add("almost");
		stopWords.add("and");
		stopWords.add("cosmopolitan");
		stopWords.add("s");
		stopWords.add("Is");
		stopWords.add("Is.");
		stopWords.add("of");
		stopWords.add("bordering areas");
		stopWords.add("areas");
		stopWords.add("USA");
		stopWords.add("Australia"); // except for "widesp. in Australia" !!
		stopWords.add("&");
		stopWords.add("part");
		stopWords.add("excl");
//		stopWords.add("European territory");  //part of Russian distributions
		stopWords.add("northern part");
		stopWords.add("Distr:");
		
		unknownAreas.add("Argentina");
		//unknownAreas.add("Panama");
		unknownAreas.add("South Africa");
		unknownAreas.add("Indonesia");
		unknownAreas.add("Chile");
//		unknownAreas.add("Wales");
//		unknownAreas.add("Java");
//		unknownAreas.add("former USSR: North European territory");
//		unknownAreas.add("former USSR: South European territory");
//		unknownAreas.add("former USSR: Soviet Middle Asia");
		unknownAreas.add("former USSR: North and Central European territory");
//		unknownAreas.add("oceanian islands");
//		unknownAreas.add("Ussuri region");
//		unknownAreas.add("Galapagos Is.");
//		unknownAreas.add("Malaysia");  // Malaysia Peninsular exists (level 4)
		unknownAreas.add("West Indies");  //-> as a whole
//		unknownAreas.add("Canal Zone");  
//		unknownAreas.add("Polynesic Is.");  
//		unknownAreas.add("Usbek SSR");  
//		unknownAreas.add("Mexican amber");  
//		unknownAreas.add("southern Europe");  // ->Southeastern Europe, Southwestern Europe
//		unknownAreas.add("Marocco");  
//		unknownAreas.add("Trinidad");  //-> Trinidad-Tobago
//		unknownAreas.add("Haiti");  
//		unknownAreas.add("Moluccas");  //-> Indonesia  
//		unknownAreas.add("Belau");  
		unknownAreas.add("Baltic amber");  
		unknownAreas.add("Arabia"); 
//		unknownAreas.add("Dominican amber"); 
//		unknownAreas.add("Canary and Madeira Is.");  //-> Canary Is. / Madeira 
//		unknownAreas.add("Dominican amber"); 
//		unknownAreas.add("Far East"); 
//		unknownAreas.add("Tahiti"); 
			
		higherAreas.add("AF");
		higherAreas.add("OR");
		higherAreas.add("PA");
		higherAreas.add("AU");
		higherAreas.add("NE");
		
		higherAreas.add("NT");
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ICdmDataSource cdmDestination = CdmDestinations.localH2();
		CdmApplicationController app = null;
		try {
			DbSchemaValidation val = DbSchemaValidation.UPDATE;
			app = CdmApplicationController.NewInstance(cdmDestination, val);
		} catch (DataSourceNotFoundException e) {
			e.printStackTrace();
		} catch (TermNotFoundException e) {
			e.printStackTrace();
		}
		DipteraDistributionParser dipDist = new DipteraDistributionParser();
		if (app != null){
			dipDist.doDistribution(app);
		}else{
			logger.warn("No Application Context");
		}
	}
}
