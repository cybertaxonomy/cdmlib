/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.wp6.diptera;

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
	
	private static ICdmDataSource cdmDestination = CdmDestinations.cdm_tunnel_dipera_b();

	final static String epiSplitter = "(\\s+|\\[|\\]|\\(|\\))"; //( ' '+| '(' | ')'| '[' | ']' )
	static Pattern pattern = null;
	
	protected void doDistribution(CdmApplicationController app){
		pattern = Pattern.compile(epiSplitter); 
	    TransactionStatus txStatus = app.startTransaction();
		List<TaxonBase> taxa = app.getTaxonService().list(null, null, null, null, null);
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
								//app.getTaxonService().saveTaxon(taxon);
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
				if (word.contains("U.S.A")){
					logger.warn("U.S.A.");
				}
				boolean isDoubtful = false;
				if (countSkip > 0){
					countSkip--;
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
						
						if (! "".equals(word) && ! TdwgArea.isTdwgAreaLabel(word) && ! TdwgArea.isTdwgAreaAbbreviation(word) && ! isDoubleArea(word)){
							for (countSkip = 1; countSkip <= 6; countSkip++){
								word = word.trim();
								if (! TdwgArea.isTdwgAreaLabel(word) && ! TdwgArea.isTdwgAreaAbbreviation(word) && ! isDoubleArea(word)){
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
						}else if (! TdwgArea.isTdwgAreaLabel(word)  && ! TdwgArea.isTdwgAreaAbbreviation(word) &&  ! isDoubleArea(word)  ){
							if (word.contains("?")){
								logger.warn("XXX");
							}
							countNot++;
							System.out.println("   False:" + countNot + ": " + word);
							unrekognizedStrings.add(word);
							countSkip = 0;
						}else{
							if (word.equals("Netherlands")){
								if ( countSkip < 0 && words[i + 1].startsWith("Antilles")){
									word = "Netherlands Antilles";
									countSkip=2;
								}
							}
							PresenceAbsenceTermBase<?> term = PresenceTerm.PRESENT();
							if (isDoubleArea(word)){
								NamedArea[] doubleArea = getDoubleArea(word);
								for (NamedArea area : doubleArea){
									Distribution distr = Distribution.NewInstance(area, term);
									desc.addElement(distr);
								}
							}else{
								NamedArea area;
								if (TdwgArea.isTdwgAreaLabel(word)){
									area = TdwgArea.getAreaByTdwgLabel(word);
								}else{
									area = TdwgArea.getAreaByTdwgAbbreviation(word);
								}
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
			 result[0] = TdwgArea.getAreaByTdwgAbbreviation("CNY");
			 result[1] = TdwgArea.getAreaByTdwgAbbreviation("MDR");
		}else if ("southern Europe".equalsIgnoreCase(word)){
			 result[0] = TdwgArea.getAreaByTdwgAbbreviation("12");
			 result[1] = TdwgArea.getAreaByTdwgAbbreviation("13");
		}else if ("former USSR: North and Central European territory".equalsIgnoreCase(word)){
			 result[0] = TdwgArea.getAreaByTdwgAbbreviation("RUN-OO");
			 result[1] = TdwgArea.getAreaByTdwgAbbreviation("RUC-OO");
		}else{
			logger.warn("Double area not recognized");
		}
		return result;
	}
	
	
	static List<String> stopWords = new ArrayList<String>();
	static List<String> unknownAreas = new ArrayList<String>();
	static List<String> higherAreas = new ArrayList<String>();
	
	private String adaptWordsToTdwg(String word){
		word = word.replace(",", "").replace(";", "");
		if (! word.contains("U.S.A")){
			word = word.replace(",", "").replace(".", "").replace(";", "");
		}else{
			word = word.replace(",", "").replace(";", "");
		}
		
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
		word = word.replace("The Gambia", "Gambia, The");

		if (!word.contains("El Salvador")){
			word = word.replace("Salvador", "El Salvador");	
		}
		word = word.replace("Vera Cruz", "Veracruz");
		word = word.replace("Turkmenia", "Turkmenistan");
		word = word.replace("Qu\u00E9beck", "Qu\u00E9bec");
		word = word.replace("Quebeck", "Qu\u00E9bec");
		word = word.replace("Quebec", "Qu\u00E9bec");
		
		if (!word.contains("Gambia, The")){
			word = word.replace("Gambia", "Gambia, The");
		}
		word = word.replace("Mariana Is.", "Marianas");
		word = word.replace("Kenia", "Kenya");
		word = word.replace("Central Africa", "Central African Republic");
		word = word.replace("Canal Zone", "");
		//word = word.replace("Panama", "PanamÃ¡");
		word = word.replace("Panama", "Panam\u00E1");
		if (! word.contains("New South Wales")){
			word = word.replace("Wales", "Great Britain");
		}
		word = word.replace("Java", "Jawa");
		word = word.replace("former USSR: North European territory", "North European Russia");
		word = word.replace("former USSR: South European territory", "South European Russia");
		word = word.replace("former USSR: Soviet Middle Asia", "Middle Asia");
		
		word = word.replace("St Kitts-Nevis", "St.Kitts-Nevis");
		
		word = word.replace("oceanian islands", "Pacific");
		word = word.replace("Ussuri region", "Primorye");
		word = word.replace("Galapagos Is.", "Gal\u00E1pagos");
		word = word.replace("Tarapac\u00E1", "Tarapaca");
		word = word.replace("Reunion", "R\u00E9union");
		if (! word.contains("Is.")){
			word = word.replace("Galapagos", "Gal\u00E1pagos");
		}
		
		//word = word.replace("Galapagos Is.", "GalÃ¡pagos");
		if (! word.contains("Peninsular")){
			word = word.replace("Malaysia", "Peninsular Malaysia");
		}
		word = word.replace("Polynesic Is.", "South Solomons");
		
		word = word.replace("Usbek SSR", "Uzbekistan");
		word = word.replace("Mexican amber", "Mexico");
		word = word.replace("Marocco", "Morocco");
		if (! word.contains("Tobago")){
			word = word.replace("Trinidad", "Trinidad-Tobago");
		}
		if (! word.contains("Trinidad")){
			word = word.replace("Tobago", "Trinidad-Tobago");
		}
		word = word.replace("Haiti", "Haiti");  
		word = word.replace("Moluccas", "Maluku");
		word = word.replace("Belau", "Palau");
		word = word.replace("Dominican amber", "Dominican Republic");
		if (! word.contains("Russian")){
			word = word.replace("Far East", "Russian Far East");
		}
		word = word.replace("Tahiti", "Society Is.");
		word = word.replace("Iraque", "Iraq");
		word = word.replace("Wake Island", "Wake I.");
		if (! word.contains("I.")){
			word = word.replace("Johnston I", "Johnston I.");
			word = word.replace("Wake I", "Wake I.");
			word = word.replace("Clipperton I", "Clipperton I.");
		}
		if (! word.contains("Provinces")){
			word = word.replace("Cape Province", "Cape Provinces");
		}
		word = word.replace("Eastern Cape Provinces", "Eastern Cape Province");
		word = word.replace("Western Cape Provinces", "Western Cape Province");
		if (! word.contains("Barbuda")){
			word = word.replace("Antigua", "Antigua-Barbuda");
		}
		if (! word.contains("St.")){
			word = word.replace("St Vincent", "St.Vincent");
			word = word.replace("St Lucia", "St.Lucia");
			word = word.replace("St Helena", "St.Helena");
		}
		word = word.replace("Asia-tropical", "Asia-Tropical");
		word = word.replace("Society Islands", "Society Is.");
		word = word.replace("Virgin Islands", "Virgin Is.");
		word = word.replace("Canary Islands", "Canary Is.");
		word = word.replace("Rhode Island", "Rhode I.");
		
		
		word = word.replace("Rodriguez", "Rodrigues");
		word = word.replace("British Colombia", "British Columbia");
		word = word.replace("Bermudas", "Bermuda");
		word = word.replace("Tunesia", "Tunisia");
		word = word.replace("Santos S\u00E3o Paulo", "S\u00E3o Paulo");
		word = word.replace("Transvaal", "Northern Provinces");
		word = word.replace("Tucum\u00E1n", "Tucuman");
//		if (!word.contains("Netherlands")){
//			
//		}
		
//		unknownAreas.add("Baltic amber");  
//		unknownAreas.add("Arabia"); 
						
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
		stopWords.add("and");
		stopWords.add("Is");
		stopWords.add("Is.");
		stopWords.add("Islands");
		stopWords.add("Island");
		
		stopWords.add("of");
		stopWords.add("areas");
		stopWords.add("USA");
		stopWords.add("Australia"); //except for Australia only
		stopWords.add("Argentina");		

		//unknownAreas.add("Panama");
		unknownAreas.add("South Africa");
		unknownAreas.add("Chile");

		unknownAreas.add("Baltic amber");  
		unknownAreas.add("Arabia"); 

			
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
