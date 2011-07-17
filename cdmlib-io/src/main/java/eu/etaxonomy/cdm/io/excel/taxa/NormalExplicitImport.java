/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.taxa;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase.SourceDataHolder;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */

@Component
public class NormalExplicitImport extends TaxonExcelImporterBase {
	private static final Logger logger = Logger.getLogger(NormalExplicitImport.class);
	
	public static Set<String> validMarkers = new HashSet<String>(Arrays.asList(new String[]{"", "valid", "accepted", "a", "v", "t"}));
	public static Set<String> synonymMarkers = new HashSet<String>(Arrays.asList(new String[]{"", "invalid", "synonym", "s", "i"}));
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase#analyzeSingleValue(eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase.KeyValue, eu.etaxonomy.cdm.io.excel.common.ExcelImportState)
	 */
	@Override
	protected boolean analyzeSingleValue(KeyValue keyValue, TaxonExcelImportState state) {
		boolean success = true;
		
		NormalExplicitRow normalExplicitRow = state.getCurrentRow();
		String key = keyValue.key;
		String value = keyValue.value;
		Integer index = keyValue.index;
		if (key.equalsIgnoreCase(ID_COLUMN)) {
			int ivalue = floatString2IntValue(value);
			normalExplicitRow.setId(ivalue);
			
		} else if(key.equalsIgnoreCase(PARENT_ID_COLUMN)) {
			int ivalue = floatString2IntValue(value);
			normalExplicitRow.setParentId(ivalue);
			
		} else if(key.equalsIgnoreCase(RANK_COLUMN)) {
			normalExplicitRow.setRank(value);
			
		} else if(key.equalsIgnoreCase(SCIENTIFIC_NAME_COLUMN)) {
			normalExplicitRow.setScientificName(value);
			
		} else if(key.equalsIgnoreCase(AUTHOR_COLUMN)) {
			normalExplicitRow.setAuthor(value);
			
		} else if(key.equalsIgnoreCase(NAME_STATUS_COLUMN)) {
			normalExplicitRow.setNameStatus(value);
			
		} else if(key.equalsIgnoreCase(VERNACULAR_NAME_COLUMN)) {
			normalExplicitRow.setCommonName(value);
			
		} else if(key.equalsIgnoreCase(LANGUAGE_COLUMN)) {
			normalExplicitRow.setLanguage(value);
		
		} else if(key.equalsIgnoreCase(TDWG_COLUMN)) {
			value = value.replace(".0", "");
			normalExplicitRow.putDistribution(index, value);
		
		} else if(key.equalsIgnoreCase(PROTOLOGUE_COLUMN)) {
			normalExplicitRow.putProtologue(index, value);
			
		} else if(key.equalsIgnoreCase(IMAGE_COLUMN)) {
			normalExplicitRow.putImage(index, value);
			
		} else {
			if (handleFeatures(state, keyValue)){
				//ok
			}else{
				String message = "Unexpected column header " + key;
				fireWarningEvent(message, state, 10);
				success = false;
				logger.error(message);
			}
		}
		return success;
	}


	private boolean handleFeatures(TaxonExcelImportState state, KeyValue keyValue) {
		String key = keyValue.key;
		Pager<DefinedTermBase> features = getTermService().findByTitle(Feature.class, key, null, null, null, null, null, null);
		if (features.getCount() > 1){
			String message = "More than one feature found matching key " + key;
			fireWarningEvent(message, state, 4);
			return false;
		}else if (features.getCount() == 0){
			return false;
		}else{
			Feature feature = CdmBase.deproxy(features.getRecords().get(0), Feature.class);
			NormalExplicitRow row = state.getCurrentRow();
			if ( keyValue.isKeyData()){
				row.putFeature(feature.getUuid(), keyValue.index, keyValue.value);
			}else{
				row.putFeatureSource(feature.getUuid(), keyValue.index, keyValue.refType, keyValue.value, keyValue.refIndex);
			}
			return true;
		}
	}


	/** 
	 *  Stores taxa records in DB
	 */
	@Override
    protected boolean firstPass(TaxonExcelImportState state) {
		boolean success = true;
		Rank rank = null;
		NormalExplicitRow taxonDataHolder = state.getCurrentRow();
		
		String rankStr = taxonDataHolder.getRank();
		String taxonNameStr = taxonDataHolder.getScientificName();
		String authorStr = taxonDataHolder.getAuthor();
		String nameStatus = taxonDataHolder.getNameStatus();
		Integer id = taxonDataHolder.getId();
			
		TaxonBase taxonBase = null;
		if (taxonDataHolder.getCdmUuid() != null){
			taxonBase = getTaxonService().find(taxonDataHolder.getCdmUuid());
		}else{
			if (CdmUtils.isNotEmpty(taxonNameStr)) {

				// Rank
				try {
					rank = Rank.getRankByNameOrAbbreviation(rankStr);
				} catch (UnknownCdmTypeException ex) {
					try {
						rank = Rank.getRankByEnglishName(rankStr, state.getConfig().getNomenclaturalCode(), false);
					} catch (UnknownCdmTypeException e) {
						success = false;
						logger.error(rankStr + " is not a valid rank.");
					}
				}
				
	            //taxon
				taxonBase = createTaxon(state, rank, taxonNameStr, authorStr, nameStatus);
			}else{
				return true;
			}
		}
		if (taxonBase == null){
			String message = "Taxon could not be created. Record will not be handled";
			fireWarningEvent(message, "Record: " + state.getCurrentLine(), 6);
			logger.warn(message);
			return false;
		}
		
		//protologue
		for (String protologue : taxonDataHolder.getProtologues()){
			TextData textData = TextData.NewInstance(Feature.PROTOLOGUE());
			this.getNameDescription(taxonBase.getName()).addElement(textData);
			URI uri;
			try {
				uri = new URI(protologue);
				textData.addMedia(Media.NewInstance(uri, null, null, null));
			} catch (URISyntaxException e) {
				String warning = "URISyntaxException when trying to convert to URI: " + protologue;
				logger.error(warning);
			}	
		}

		//media
		for (String imageUrl : taxonDataHolder.getImages()){
			//TODO
			Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
			TaxonDescription td = taxon.getImageGallery(true);
			DescriptionElementBase mediaHolder;
			if (td.getElements().size() != 0){
				mediaHolder = td.getElements().iterator().next();
			}else{
				mediaHolder = TextData.NewInstance(Feature.IMAGE());
				td.addElement(mediaHolder);
			}
			try {
				Media media = getImageMedia(imageUrl, true);
				mediaHolder.addMedia(media);
			} catch (MalformedURLException e) {
				logger.warn("Can't add media: " + e.getMessage());
			}
		}

		//tdwg label
		for (String tdwg : taxonDataHolder.getDistributions()){
			//TODO
			Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
			TaxonDescription td = this.getTaxonDescription(taxon, false, true);
			NamedArea area = TdwgArea.getAreaByTdwgAbbreviation(tdwg);
			if (area == null){
				area = TdwgArea.getAreaByTdwgLabel(tdwg);
			}
			if (area != null){
				Distribution distribution = Distribution.NewInstance(area, PresenceTerm.PRESENT());
				td.addElement(distribution);
			}else{
				String message = "TDWG area could not be recognized: " + tdwg;
				logger.warn(message);
			}
			
		}
		
		//feature
		for (UUID featureUuid : taxonDataHolder.getFeatures()){
			Feature feature = CdmBase.deproxy(getTermService().find(featureUuid), Feature.class);
			List<String> textList = taxonDataHolder.getFeatureTexts(featureUuid);
			
			
			for (int i = 0; i < textList.size(); i++){
				String featureText = textList.get(i);
				//TODO
				Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				TaxonDescription td = this.getTaxonDescription(taxon, false, true);
				TextData textData = TextData.NewInstance(feature);
				textData.putText(Language.DEFAULT(), featureText);
				td.addElement(textData);
				
				SourceDataHolder sourceDataHolder = taxonDataHolder.getFeatureTextReferences(featureUuid, i);
				List<Map<SourceType, String>> sourceList = sourceDataHolder.getSources();
				for (Map<SourceType, String> sourceMap : sourceList){
				
					DescriptionElementSource source = DescriptionElementSource.NewInstance();
					//ref
					Reference<?> ref = ReferenceFactory.newGeneric();
					boolean refExists = false; //in case none of the ref fields exists, the ref should not be added
					for (SourceType type : sourceMap.keySet()){
						String value = sourceMap.get(type);
						if (type.equals(SourceType.Author)){
							Team team = Team.NewInstance();
							team.setTitleCache(value, true);
							ref.setAuthorTeam(team);
						}else if (type.equals(SourceType.Title)) {
							ref.setTitle(value);
						}else if (type.equals(SourceType.Year)) {
							ref.setDatePublished(TimePeriod.parseString(value));
						}
						refExists = true;
					}
					if (refExists){
						source.setCitation(ref);
					}
					textData.addSource(source);
				}				
			}
		}
		
		state.putTaxon(id, taxonBase);
		getTaxonService().save(taxonBase);

		return success;
    }

	/**
	 * @param state
	 * @param rank
	 * @param taxonNameStr
	 * @param authorStr
	 * @param nameStatus
	 * @return
	 */
	private TaxonBase createTaxon(TaxonExcelImportState state, Rank rank,
			String taxonNameStr, String authorStr, String nameStatus) {
		// Create the taxon name object depending on the setting of the nomenclatural code 
		// in the configurator (botanical code, zoological code, etc.) 
		if (StringUtils.isBlank(taxonNameStr)){
			return null;
		}
		NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();
		
		TaxonBase taxonBase = null;
		
		String titleCache = CdmUtils.concat(" ", taxonNameStr, authorStr);
		if (! synonymMarkers.contains(nameStatus)  && state.getConfig().isDoMatchTaxa()){
			titleCache = CdmUtils.concat(" ", taxonNameStr, authorStr);
			taxonBase = getTaxonService().findBestMatchingTaxon(titleCache);
		}else{
			taxonBase = getTaxonService().findBestMatchingSynonym(titleCache);
			if (taxonBase != null){
				logger.info("Matching taxon/synonym found for " + titleCache);
			}
		}
		if (taxonBase != null){
			logger.info("Matching taxon/synonym found for " + titleCache);
		}else {
			taxonBase = createTaxon(state, rank, taxonNameStr, authorStr, nameStatus, nc);
		}
		return taxonBase;
	}




	/** 
	 *  Stores parent-child, synonym and common name relationships
	 */
	@Override
    protected boolean secondPass(TaxonExcelImportState state) {
		boolean success = true;
		try {
			String taxonNameStr = state.getCurrentRow().getScientificName();
			String nameStatus = state.getCurrentRow().getNameStatus();
			String commonNameStr = state.getCurrentRow().getCommonName();
			Integer parentId = state.getCurrentRow().getParentId();
			Integer childId = state.getCurrentRow().getId();
			
			Taxon parentTaxon = (Taxon)state.getTaxonBase(parentId);
			if (CdmUtils.isNotEmpty(taxonNameStr)) {
				nameStatus = CdmUtils.Nz(nameStatus).trim().toLowerCase();
				if (validMarkers.contains(nameStatus)){
					Taxon taxon = (Taxon)state.getTaxonBase(childId);
					// Add the parent relationship
					if (state.getCurrentRow().getParentId() != 0) {
						if (parentTaxon != null) {
							//Taxon taxon = (Taxon)state.getTaxonBase(childId);
							
							Reference citation = state.getConfig().getSourceReference();
							String microCitation = null;
							Taxon childTaxon = taxon;
							success &= makeParent(state, parentTaxon, childTaxon, citation, microCitation);
							getTaxonService().saveOrUpdate(parentTaxon);
						} else {
							String message = "Taxonomic parent not found for " + taxonNameStr;
							logger.warn(message);
							fireWarningEvent(message, state, 6);
							success = false;
						}
					}else{
						//do nothing (parent == 0) no parent exists
					}
				}else if (synonymMarkers.contains(nameStatus)){
					//add synonym relationship
					try {
						TaxonBase taxonBase = state.getTaxonBase(childId);
						Synonym synonym = CdmBase.deproxy(taxonBase,Synonym.class);
						parentTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
						getTaxonService().saveOrUpdate(parentTaxon);
					} catch (Exception e) {
						logger.warn("Child id = " + childId);
						e.printStackTrace();
					}
				}
			} 
			if (CdmUtils.isNotEmpty(commonNameStr)){			// add common name to taxon
				handleCommonName(state, taxonNameStr, commonNameStr, parentId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}



	/**
	 * @param state
	 * @param taxonNameStr
	 * @param commonNameStr
	 * @param parentId
	 */
	private void handleCommonName(TaxonExcelImportState state,
			String taxonNameStr, String commonNameStr, Integer parentId) {
		Language language = getTermService().getLanguageByIso(state.getCurrentRow().getLanguage());
		if (language == null && CdmUtils.isNotEmpty(state.getCurrentRow().getLanguage())  ){
			String error ="Language is null but shouldn't"; 
			logger.error(error);
			throw new IllegalArgumentException(error);
		}
		CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance(commonNameStr, language);
		try {
			Taxon taxon = (Taxon)state.getTaxonBase(parentId);
			TaxonDescription taxonDescription = getTaxonDescription(taxon, false, true);
			taxonDescription.addElement(commonTaxonName);
			logger.info("Common name " + commonNameStr + " added to " + taxon.getTitleCache());
		} catch (ClassCastException ex) {
			logger.error(taxonNameStr + " is not a taxon instance.");
		}
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
	private TaxonBase createTaxon(TaxonExcelImportState state, Rank rank, String taxonNameStr, 
			String authorStr, String nameStatus, NomenclaturalCode nc) {
		TaxonBase taxonBase;
		NonViralName taxonNameBase = null;
		if (nc == NomenclaturalCode.ICVCN){
			logger.warn("ICVCN not yet supported");
			
		}else{
			taxonNameBase =(NonViralName) nc.getNewTaxonNameInstance(rank);
			//NonViralName nonViralName = (NonViralName)taxonNameBase;
			NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
			taxonNameBase = parser.parseFullName(taxonNameStr, nc, rank);
			
			taxonNameBase.setNameCache(taxonNameStr);
			
			// Create the author
			if (CdmUtils.isNotEmpty(authorStr)) {
				try {
					parser.parseAuthors(taxonNameBase, authorStr);
				} catch (StringNotParsableException e) {
					taxonNameBase.setAuthorshipCache(authorStr);
 				}
			}
		}

		//Create the taxon
		Reference sec = state.getConfig().getSourceReference();
		// Create the status
		nameStatus = CdmUtils.Nz(nameStatus).trim().toLowerCase();
		if (validMarkers.contains(nameStatus)){
			taxonBase = Taxon.NewInstance(taxonNameBase, sec);
		}else if (synonymMarkers.contains(nameStatus)){
			taxonBase = Synonym.NewInstance(taxonNameBase, sec);
		}else {
			Taxon taxon = Taxon.NewInstance(taxonNameBase, sec);
			taxon.setTaxonStatusUnknown(true);
			taxonBase = taxon;
		}
		return taxonBase;
	}
	
	/**
	 * @param taxon
	 * @return
	 */
	//TODO implementation must be improved when matching of taxon names with existing names is implemented
	//=> the assumption that the only description is the description added by this import
	//is wrong then
	private TaxonNameDescription getNameDescription(TaxonNameBase name) {
		Set<TaxonNameDescription> descriptions = name.getDescriptions();
		if (descriptions.size()>1){
			throw new IllegalStateException("Implementation does not yet support names with multiple descriptions");
		}else if (descriptions.size()==1){
			return descriptions.iterator().next();
		}else{
			return TaxonNameDescription.NewInstance(name);
		}
	}

	private boolean makeParent(TaxonExcelImportState state, Taxon parentTaxon, Taxon childTaxon, Reference citation, String microCitation){
		boolean success = true;
		Reference sec = state.getConfig().getSourceReference();
		
//		Reference sec = parentTaxon.getSec();
		Classification tree = state.getTree(sec);
		if (tree == null){
			tree = makeTree(state, sec);
		}
		if (sec.equals(childTaxon.getSec())){
			success &=  (null !=  tree.addParentChild(parentTaxon, childTaxon, citation, microCitation));
		}else{
			logger.warn("No relationship added for child " + childTaxon.getTitleCache());
		}
		return success;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase#createDataHolderRow()
	 */
	@Override
	protected NormalExplicitRow createDataHolderRow() {
		return new NormalExplicitRow();
	}

	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(TaxonExcelImportState state) {
		logger.warn("DoCheck not yet implemented for NormalExplicitImport");
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(TaxonExcelImportState state) {
		return false;
	}
	

	
}
