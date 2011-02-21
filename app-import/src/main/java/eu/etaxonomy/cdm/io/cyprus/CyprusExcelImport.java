/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cyprus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */

@Component
public class CyprusExcelImport extends ExcelImporterBase<CyprusImportState> {
	private static final Logger logger = Logger.getLogger(CyprusExcelImport.class);
	
	public static Set<String> validMarkers = new HashSet<String>(Arrays.asList(new String[]{"", "valid", "accepted", "a", "v", "t"}));
	public static Set<String> synonymMarkers = new HashSet<String>(Arrays.asList(new String[]{"", "invalid", "synonym", "s", "i"}));
	
	
	@Override
	protected boolean isIgnore(CyprusImportState state) {
		return ! state.getConfig().isDoTaxa();
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(CyprusImportState state) {
		logger.warn("DoCheck not yet implemented for CyprusExcelImport");
		return true;
	}

//	protected static final String ID_COLUMN = "Id";
	protected static final String SPECIES_COLUMN = "species";
	protected static final String SUBSPECIES_COLUMN = "subspecies";
	protected static final String GENUS_COLUMN = "genus";
	protected static final String FAMILY_COLUMN = "family";
	protected static final String DIVISION_COLUMN = "division";
	protected static final String HOMOTYPIC_SYNONYM_COLUMN = "homotypic synonyms";
	protected static final String HETEROTYPIC_SYNONYMS_COLUMN = "heterotypic synonyms";
	protected static final String ENDEMISM_COLUMN = "endemism";

	protected static final String STATUS_COLUMN = "status";
	protected static final String RED_DATA_BOOK_CATEGORY_COLUMN = "red data book category";
	protected static final String SYSTEMATICS_COLUMN = "systematics";
	
	
	
	// TODO: This enum is for future use (perhaps).
	protected enum Columns { 
//		Id("Id"), 
		Species("species"), 
		Subspecies("subspecies"),
		Genus("genus"),
		Family("family"),
		Division("division"),
		HomotypicSynonyms("homotypic synonyms"),
		HeterotypicSynonyms("heterotypic synonyms"),
		Status("status"),
		Endemism("endemism");
		
		private String head;
		private String value;
	
		private Columns(String head) {
			this.head = head;
		}
		
		public String head() {
			return this.head;
		}
	
		public String value() {
			return this.value;
		}
	}
	
	
	@Override
    protected boolean analyzeRecord(HashMap<String, String> record, CyprusImportState state) {
		
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	CyprusRow cyprusRow = new CyprusRow();
    	state.setCyprusRow(cyprusRow);
    	
    	for (String originalKey: keys) {
    		Integer index = 0;
    		String indexedKey = CdmUtils.removeDuplicateWhitespace(originalKey.trim()).toString();
    		String[] split = indexedKey.split("_");
    		String key = split[0];
    		if (split.length > 1){
    			String indexString = split[1];
    			try {
					index = Integer.valueOf(indexString);
				} catch (NumberFormatException e) {
					String message = "Index must be integer";
					logger.error(message);
					continue;
				}
    		}
    		
    		String value = (String) record.get(indexedKey);
    		if (! StringUtils.isBlank(value)) {
    			if (logger.isDebugEnabled()) { logger.debug(key + ": " + value); }
        		value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
    		}else{
    			continue;
    		}
    		
    		
    		if (key.equalsIgnoreCase(SPECIES_COLUMN)) {
//    			int ivalue = floatString2IntValue(value);
    			cyprusRow.setSpecies(value);
    			
			} else if(key.equalsIgnoreCase(SUBSPECIES_COLUMN)) {
				cyprusRow.setSubspecies(value);
				
			} else if(key.equalsIgnoreCase(HOMOTYPIC_SYNONYM_COLUMN)) {
				cyprusRow.setHomotypicSynonyms(value);
    			
			} else if(key.equalsIgnoreCase(HETEROTYPIC_SYNONYMS_COLUMN)) {
				cyprusRow.setHeterotypicSynonyms(value);
    			
			} else if(key.equalsIgnoreCase(ENDEMISM_COLUMN)) {
				cyprusRow.setEndemism(value);
   			
			} else if(key.equalsIgnoreCase(STATUS_COLUMN)) {
				cyprusRow.setStatus(value);
    			
			} else if(key.equalsIgnoreCase(RED_DATA_BOOK_CATEGORY_COLUMN)) {
				cyprusRow.setRedDataBookCategory(value);
    			
			} else if(key.equalsIgnoreCase(SYSTEMATICS_COLUMN)) {
				cyprusRow.setSystematics(value);
			
			} else if(key.equalsIgnoreCase(GENUS_COLUMN)) {
				cyprusRow.setGenus(value);
			
			} else if(key.equalsIgnoreCase(FAMILY_COLUMN)) {
				cyprusRow.setFamily(value);
    			
			} else if(key.equalsIgnoreCase(DIVISION_COLUMN)) {
				cyprusRow.setDivision(value);
    			
			} else {
				success = false;
				logger.error("Unexpected column header " + key);
			}
    	}
    	return success;
    }
	
	private static INonViralNameParser nameParser = NonViralNameParserImpl.NewInstance();
	private static NomenclaturalCode nc = NomenclaturalCode.ICBN;
	private Feature redBookCategory;
	private Feature endemism;
	private PresenceTerm indigenous;
	private PresenceTerm indigenousDoubtful;
	private PresenceTerm cultivatedDoubtful;
	
	private PresenceTerm casual;
	private PresenceTerm casualDoubtful;
	private PresenceTerm nonInvasive;
	private PresenceTerm nonInvasiveDoubtful;
	private PresenceTerm invasive;
	private PresenceTerm invasiveDoubtful;
	private PresenceTerm questionable;
	private PresenceTerm questionableDoubtful;
	
	private boolean termsCreated = false;
	
	private boolean makeTerms(CyprusImportState state) {
		if (termsCreated == false){
			IInputTransformer transformer = state.getTransformer();
			
			try {
				//feature
				UUID redBookUuid = transformer.getFeatureUuid("Red book");
				redBookCategory = this.getFeature(state, redBookUuid, "Red book category", "Red data book category", "Red book");
				getTermService().save(redBookCategory);
				
				UUID endemismUuid = transformer.getFeatureUuid("Endemism");
				endemism = this.getFeature(state, endemismUuid, "Endemism", "Endemism", "Endemism");
				getTermService().save(endemism);
				
				//status
				
				UUID indigenousUuid = transformer.getPresenceTermUuid("IN");
				indigenous = this.getPresenceTerm(state, indigenousUuid, "Indigenous", "Indigenous", "IN");
				getTermService().save(indigenous);
				UUID indigenousDoubtfulUuid = transformer.getPresenceTermUuid("IN?");
				indigenousDoubtful = this.getPresenceTerm(state, indigenousDoubtfulUuid, "Indigenous?", "Indigenous?", "IN?");
				getTermService().save(indigenousDoubtful);

				UUID cultivatedDoubtfulUuid = transformer.getPresenceTermUuid("CU?");
				cultivatedDoubtful = this.getPresenceTerm(state, cultivatedDoubtfulUuid, "Cultivated?", "Cultivated?", "CU?");
				getTermService().save(cultivatedDoubtful);
				
				
				UUID casualUuid = transformer.getPresenceTermUuid("CA");
				casual = this.getPresenceTerm(state, casualUuid, "Casual", "Casual", "CA");
				getTermService().save(casual);
				UUID casualDoubtfulUuid = transformer.getPresenceTermUuid("CA?");
				casualDoubtful = this.getPresenceTerm(state, casualDoubtfulUuid, "Casual?", "Casual?", "CA?");
				getTermService().save(casualDoubtful);

				
				UUID nonInvasiveUuid = transformer.getPresenceTermUuid("NN");
				nonInvasive = this.getPresenceTerm(state, nonInvasiveUuid, "Naturalized  non-invasive", "Naturalized  non-invasive", "NN");
				getTermService().save(nonInvasive);
				UUID nonInvasiveDoubtfulUuid = transformer.getPresenceTermUuid("NN?");
				nonInvasiveDoubtful = this.getPresenceTerm(state, nonInvasiveDoubtfulUuid, "Naturalized  non-invasive?", "Naturalized  non-invasive?", "NN?");
				getTermService().save(nonInvasiveDoubtful);
	
				UUID invasiveUuid = transformer.getPresenceTermUuid("NA");
				invasive = this.getPresenceTerm(state, invasiveUuid, "Naturalized  invasive", "Naturalized  invasive", "NA");
				getTermService().save(invasive);
				UUID invasiveDoubtfulUuid = transformer.getPresenceTermUuid("NA?");
				invasiveDoubtful = this.getPresenceTerm(state, invasiveDoubtfulUuid, "Naturalized  invasive?", "Naturalized  invasive?", "NA?");
				getTermService().save(invasiveDoubtful);
	
				UUID questionableUuid = transformer.getPresenceTermUuid("Q");
				questionable = this.getPresenceTerm(state, questionableUuid, "Questionable", "Questionable", "Q");
				getTermService().save(questionable);
				UUID questionableDoubtfulUuid = transformer.getPresenceTermUuid("Q?");
				questionableDoubtful = this.getPresenceTerm(state, questionableDoubtfulUuid, "Questionable?", "Questionable?", "Q?");
				getTermService().save(questionableDoubtful);
				
				termsCreated = true;
				
				return true;
			} catch (UndefinedTransformerMethodException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
		
	}
	
	/** 
	 *  Stores taxa records in DB
	 */
	@Override
    protected boolean firstPass(CyprusImportState state) {
		
		boolean success = true;
		makeTerms(state);
		CyprusRow taxonLight = state.getCyprusRow();
		Reference citation = null;
		String microCitation = null;
		
		//species name
		String speciesStr = taxonLight.getSpecies();
		String subSpeciesStr = taxonLight.getSubspecies();
		String homotypicSynonymsString = taxonLight.getHomotypicSynonyms();
		List<String> homotypicSynonymList = Arrays.asList(homotypicSynonymsString.split(";"));
		String heterotypicSynonymsString = taxonLight.getHeterotypicSynonyms();
		List<String> heterotypicSynonymList = Arrays.asList(heterotypicSynonymsString.split(";"));
		
		String systematicsString = taxonLight.getSystematics();
		String endemismString = taxonLight.getEndemism();
		String statusString = taxonLight.getStatus();
		String redBookCategory = taxonLight.getRedDataBookCategory();
		
		if (StringUtils.isNotBlank(speciesStr)) {
			boolean speciesIsExisting = false;
			Taxon mainTaxon = null;
			//species
			Taxon speciesTaxon = (Taxon)createTaxon(state, Rank.SPECIES(), speciesStr, Taxon.class, nc);
			mainTaxon = speciesTaxon;
			
			//subspecies
			if (StringUtils.isNotBlank(subSpeciesStr)){
				Taxon existingSpecies = state.getHigherTaxon(speciesStr);
				if (existingSpecies != null){
					speciesIsExisting = true;
					speciesTaxon = existingSpecies;
				}
				
				Taxon subSpeciesTaxon = (Taxon)createTaxon(state, Rank.SUBSPECIES(), subSpeciesStr, Taxon.class, nc);
				
				if (subSpeciesTaxon != null){
					makeParent(state, speciesTaxon, subSpeciesTaxon, citation, microCitation);
				}
				mainTaxon = subSpeciesTaxon;
				state.putHigherTaxon(speciesStr, speciesTaxon);
			}
			
			if (! speciesIsExisting){
				makeHigherTaxa(state, taxonLight, speciesTaxon, citation, microCitation);
			}
			makeHomotypicSynonyms(state, citation, microCitation, homotypicSynonymList, mainTaxon);			
			makeHeterotypicSynonyms(state, citation, microCitation, heterotypicSynonymList, mainTaxon);			
			makeSystematics(systematicsString, mainTaxon);
			makeEndemism(endemismString, mainTaxon);
			makeStatus(statusString, mainTaxon);
			makeRedBookCategory(redBookCategory, mainTaxon);
			
//			state.putHigherTaxon(higherName, uuid);//(speciesStr, mainTaxon);
			getTaxonService().save(mainTaxon);
		}
		return success;
    }


	private void makeHigherTaxa(CyprusImportState state, CyprusRow taxonLight, Taxon speciesTaxon, Reference citation, String microCitation) {
		String divisionStr = taxonLight.getDivision();
		String genusStr = taxonLight.getGenus();
		String familyStr = taxonLight.getFamily();
		
		Taxon division = getTaxon(state, divisionStr, Rank.DIVISION(), null, citation, microCitation);
		Taxon family = getTaxon(state, familyStr, Rank.FAMILY(), division, citation, microCitation);
		Taxon genus = getTaxon(state, genusStr, Rank.GENUS(), family, citation, microCitation);
		makeParent(state, genus, speciesTaxon, citation, microCitation)	;
	}


	private Taxon getTaxon(CyprusImportState state, String taxonNameStr, Rank rank, Taxon parent, Reference citation, String microCitation) {
		Taxon result;
		if (state.containsHigherTaxon(taxonNameStr)){
			result = state.getHigherTaxon(taxonNameStr);
		}else{
			result = (Taxon)createTaxon(state, rank, taxonNameStr, Taxon.class, nc);
			state.putHigherTaxon(taxonNameStr, result);
			if (parent == null){
				makeParent(state, null,result, citation, microCitation);
			}else{
				makeParent(state, parent, result, citation, microCitation);
			}
			
		}
		return result;
	}


	private void makeHomotypicSynonyms(CyprusImportState state,
			Reference citation, String microCitation, List<String> homotypicSynonymList, Taxon mainTaxon) {
		for (String homotypicSynonym: homotypicSynonymList){
			if (StringUtils.isNotBlank(homotypicSynonym)){
				Synonym synonym = (Synonym)createTaxon(state, null, homotypicSynonym, Synonym.class, nc);
				mainTaxon.addHomotypicSynonym(synonym, citation, microCitation);
			}
		}
	}


	private void makeHeterotypicSynonyms(CyprusImportState state, Reference citation, String microCitation, List<String> heterotypicSynonymList, Taxon mainTaxon) {
		for (String heterotypicSynonym: heterotypicSynonymList){
			if (StringUtils.isNotBlank(heterotypicSynonym)){
				Synonym synonym = (Synonym)createTaxon(state, null, heterotypicSynonym, Synonym.class, nc);
				mainTaxon.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), citation, microCitation);
			}
		}
	}


	private void makeSystematics(String systematicsString, Taxon mainTaxon) {
		//Systematics
		if (StringUtils.isNotBlank(systematicsString)){
			TaxonDescription td = this.getTaxonDescription(mainTaxon, false, true);
			TextData textData = TextData.NewInstance(Feature.SYSTEMATICS());
			textData.putText(Language.UNDETERMINED(), systematicsString);
			td.addElement(textData);
		}
	}


	private void makeEndemism(String endemismString, Taxon mainTaxon) {
		//endemism
		if (StringUtils.isNotBlank(endemismString)){
			//OLD - not wanted as marker
//			boolean flag;
//			if (endemismString.trim().equalsIgnoreCase("not endemic") || endemismString.trim().equalsIgnoreCase("ne?")){
//				flag = false;
//			}else if (endemismString.trim().equalsIgnoreCase("endemic")){
//				flag = true;
//			}else{
//				throw new RuntimeException(endemismString + " is not a valid value for endemism");
//			}
//			Marker marker = Marker.NewInstance(MarkerType.ENDEMIC(), flag);
//			mainTaxon.addMarker(marker);
			//text data
			TaxonDescription td = this.getTaxonDescription(mainTaxon, false, true);
			TextData textData = TextData.NewInstance(endemism);
			textData.putText(Language.ENGLISH(), endemismString);
			td.addElement(textData);
		}
	}


	private void makeStatus(String statusString, Taxon mainTaxon) {
		//status
		if (StringUtils.isNotBlank(statusString)){
			PresenceTerm status = null;
			if (statusString.contains("Indigenous?")){
				status = indigenousDoubtful;
			}else if (statusString.contains("Indigenous")){
				status = indigenous;
			}else if (statusString.contains("Casual?") || statusString.contains("Causal?")){
				status = casualDoubtful;
			}else if (statusString.contains("Casual")){
				status = casual;
			}else if (statusString.contains("Cultivated?")){
				status = cultivatedDoubtful;
			}else if (statusString.contains("Cultivated")){
				status = PresenceTerm.CULTIVATED();
			}else if (statusString.contains("non-invasive?")){
				status = nonInvasiveDoubtful;
			}else if (statusString.contains("non-invasive")){
				status = nonInvasive;
			}else if (statusString.contains("invasive?")){
				status = invasiveDoubtful;
			}else if (statusString.contains("invasive")){
				status = invasive;
			}else if (statusString.contains("Questionable?")){
				status = questionableDoubtful;
			}else if (statusString.contains("Questionable")){
				status = questionable;
			}else if (statusString.startsWith("F")){
				status = null;
			}else if (statusString.equals("##")){
				status = null;
			}else{
				logger.warn("Unknown status: " + statusString);
				status = null;
			}
			TaxonDescription td = this.getTaxonDescription(mainTaxon, false, true);
			NamedArea area = TdwgArea.getAreaByTdwgAbbreviation("CYP");
			Distribution distribution = Distribution.NewInstance(area, status);
			td.addElement(distribution);
			
			//text data
			TextData textData = TextData.NewInstance(Feature.STATUS());
			textData.putText(Language.ENGLISH(), statusString);
			td.addElement(textData);
		}
	}


	private void makeRedBookCategory(String redBookCategory, Taxon mainTaxon) {
		//red data book category
		if (StringUtils.isNotBlank(redBookCategory)){
			TaxonDescription td = this.getTaxonDescription(mainTaxon, false, true);
			TextData textData = TextData.NewInstance(this.redBookCategory);
			textData.putText(Language.ENGLISH(), redBookCategory);
			td.addElement(textData);
		}
	}




	/** 
	 *  Stores parent-child, synonym and common name relationships
	 */
	@Override
    protected boolean secondPass(CyprusImportState state) {
		boolean success = true;
//		CyprusRow cyprusRow = state.getCyprusRow();

		return success;
	}



	/**
	 * @param state
	 * @param rank
	 * @param taxonNameStr
	 * @param authorStr
	 * @param nameStatus
	 * @param nc
	 * @return
	 */
	private TaxonBase createTaxon(CyprusImportState state, Rank rank, String taxonNameStr, 
			Class statusClass, NomenclaturalCode nc) {
		TaxonBase taxonBase;
		NonViralName taxonNameBase = null;
		if (nc == NomenclaturalCode.ICVCN){
			logger.warn("ICVCN not yet supported");
			
		}else{
			taxonNameBase =(NonViralName) nc.getNewTaxonNameInstance(rank);
			//NonViralName nonViralName = (NonViralName)taxonNameBase;
			INonViralNameParser parser = nameParser;//NonViralNameParserImpl.NewInstance();
			taxonNameBase = (NonViralName<BotanicalName>)parser.parseFullName(taxonNameStr, nc, rank);
			
			//taxonNameBase.setNameCache(taxonNameStr);
			
		}

		//Create the taxon
		Reference sec = state.getConfig().getSourceReference();
		// Create the status
		if (statusClass.equals(Taxon.class)){
			taxonBase = Taxon.NewInstance(taxonNameBase, sec);
		}else if (statusClass.equals(Synonym.class)){
			taxonBase = Synonym.NewInstance(taxonNameBase, sec);
		}else {
			Taxon taxon = Taxon.NewInstance(taxonNameBase, sec);
			taxon.setTaxonStatusUnknown(true);
			taxonBase = taxon;
		}
		return taxonBase;
	}

	private boolean makeParent(CyprusImportState state, Taxon parentTaxon, Taxon childTaxon, Reference citation, String microCitation){
		boolean success = true;
		Reference sec = state.getConfig().getSourceReference();
		
//		Reference sec = parentTaxon.getSec();
		Classification tree = state.getTree(sec);
		if (tree == null){
			tree = makeTree(state, sec);
			tree.setTitleCache(state.getConfig().getSourceReferenceTitle());
		}
		if (sec.equals(childTaxon.getSec())){
			success &=  (null !=  tree.addParentChild(parentTaxon, childTaxon, citation, microCitation));
		}else{
			logger.warn("No relationship added for child " + childTaxon.getTitleCache());
		}
		return success;
	}
	

	
}
