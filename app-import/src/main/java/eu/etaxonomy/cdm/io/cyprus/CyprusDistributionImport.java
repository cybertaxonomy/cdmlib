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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */

@Component
public class CyprusDistributionImport extends ExcelImporterBase<CyprusImportState> {
	private static final Logger logger = Logger.getLogger(CyprusDistributionImport.class);
	
	public static Set<String> validMarkers = new HashSet<String>(Arrays.asList(new String[]{"", "valid", "accepted", "a", "v", "t"}));
	public static Set<String> synonymMarkers = new HashSet<String>(Arrays.asList(new String[]{"", "invalid", "synonym", "s", "i"}));
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(CyprusImportState state) {
		logger.warn("DoCheck not yet implemented for CyprusExcelImport");
		return true;
	}

//	protected static final String ID_COLUMN = "Id";
	protected static final String SPECIES_COLUMN = "Taxon";
	protected static final String DISTRIBUTION_COLUMN = "Distribution";
	protected static final String REFERENCE_COLUMN = "source";
	

	private Reference refMeikle1977 = ReferenceFactory.newGeneric();
	private Reference refMeikle1985 = ReferenceFactory.newGeneric();

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase#analyzeRecord(java.util.HashMap, eu.etaxonomy.cdm.io.excel.common.ExcelImportState)
	 */
	@Override
    protected boolean analyzeRecord(HashMap<String, String> record, CyprusImportState state) {
		
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	CyprusDistributionRow cyprusDistributionRow = new CyprusDistributionRow();
    	state.setCyprusDistributionRow(cyprusDistributionRow);
    	
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
    			cyprusDistributionRow.setSpecies(value);
    			
			} else if(key.equalsIgnoreCase(DISTRIBUTION_COLUMN)) {
				cyprusDistributionRow.setDistribution(value);
				
			} else if(key.equalsIgnoreCase(REFERENCE_COLUMN)) {
				cyprusDistributionRow.setReference(value);
    			
			} else {
				success = false;
				logger.error("Unexpected column header " + key);
			}
    	}
    	return success;
    }
	
	private static INonViralNameParser nameParser = NonViralNameParserImpl.NewInstance();
	
	private boolean areasCreated = false;
	private Map<String, NamedArea> divisions = new HashMap<String, NamedArea>();
	
	private boolean makeAreasAndReference(CyprusImportState state) {
		if (areasCreated == false){
			IInputTransformer transformer = state.getTransformer();
			
			try {
				//divisions
				
				
				NamedAreaType areaType = NamedAreaType.NATURAL_AREA();
				NamedAreaLevel areaLevel = NamedAreaLevel.NewInstance("Cyprus Division", "Cyprus Division", null);
				getTermService().save(areaLevel);
				
				TermVocabulary areaVocabulary = TermVocabulary.NewInstance("Cyprus devisions", "Cyprus divisions", null, null);
				getVocabularyService().save(areaVocabulary);
				
				for(int i = 1; i <= 8; i++){
					UUID divisionUuid = transformer.getNamedAreaUuid(String.valueOf(i));
					NamedArea division = this.getNamedArea(state, divisionUuid, "Division " + i, "Cyprus: Division " + i, "1", areaType, areaLevel, areaVocabulary);
					divisions.put(String.valueOf(i), division);
					getTermService().save(division);
				}
				
//				refMeikle1977 = getReferenceService().findByTitle(Reference.class, "Meikle 1977", null, null, null, null, null, null);
//				refMeikle1985 = getReferenceService().findByTitle(Reference.class, "Meikle 1977", null, null, null, null, null, null);

				areasCreated = true;
				indigenousStatus = (PresenceTerm)getTermService().find(CyprusTransformer.indigenousUuid);
				casualStatus = (PresenceTerm)getTermService().find(CyprusTransformer.casualUuid);
				nonInvasiveStatus = (PresenceTerm)getTermService().find(CyprusTransformer.nonInvasiveUuid);
				invasiveStatus = (PresenceTerm)getTermService().find(CyprusTransformer.invasiveUuid);
				questionableStatus = (PresenceTerm)getTermService().find(CyprusTransformer.questionableUuid);
				
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
		makeAreasAndReference(state);
		CyprusDistributionRow taxonLight = state.getCyprusDistributionRow();
		
		//species name
		String taxonStr = taxonLight.getSpecies();
		Taxon taxon = getTaxon(state, taxonStr);
		Reference ref = getReference(taxonLight.getReference());
		makeDistribution(state, taxon, taxonLight.getDistribution(), ref);
			
			
		getTaxonService().save(taxon);
		return success;
    }

	
	protected static final boolean CREATE = true;
	protected static final boolean CREATE_NOT = false;
	protected static final boolean NO_IMAGE_GALLERY = false;
	protected static final boolean IMAGE_GALLERY = false;
	
	private void makeDistribution(CyprusImportState state, Taxon taxon, String distributionStr, Reference ref) {
		TaxonDescription description = getTaxonDescription(taxon, NO_IMAGE_GALLERY, CREATE);
		for (int i = 1; i <= 8; i++){
			if (distributionStr.contains(String.valueOf(i))){
				NamedArea area = this.divisions.get(String.valueOf(i));
				PresenceAbsenceTermBase<?> status = getStatus(taxon);
				status = removeDoubtfulStatus(status);
				removeDistributions(taxon);
				Distribution distribution = Distribution.NewInstance(area, status);
				distribution.addSource(null, null, ref, null);
				description.addElement(distribution);
			}
		}
	}

	private PresenceAbsenceTermBase<?> indigenousStatus;
	private PresenceAbsenceTermBase<?> casualStatus;
	private PresenceAbsenceTermBase<?> nonInvasiveStatus;
	private PresenceAbsenceTermBase<?> invasiveStatus;
	private PresenceAbsenceTermBase<?> questionableStatus;
	
	private PresenceAbsenceTermBase<?> removeDoubtfulStatus(PresenceAbsenceTermBase<?> status) {
		if (status.getUuid().equals(CyprusTransformer.indigenousDoubtfulUuid)){
			status = indigenousStatus;
		}else if (status.getUuid().equals(CyprusTransformer.casualDoubtfulUuid)){
			status = casualStatus;
		}else if (status.getUuid().equals(CyprusTransformer.nonInvasiveDoubtfulUuid)){
			status = nonInvasiveStatus;
		}else if (status.getUuid().equals(CyprusTransformer.invasiveDoubtfulUuid)){
			status = invasiveStatus;
		}else if (status.getUuid().equals(CyprusTransformer.questionableDoubtfulUuid)){
			status = questionableStatus;
		}else if (status.getUuid().equals(CyprusTransformer.cultivatedDoubtfulUuid)){
			status = PresenceTerm.CULTIVATED();
		}
		
		return status;
	}

	private PresenceAbsenceTermBase<?> getStatus(Taxon taxon) {
		Set<PresenceAbsenceTermBase<?>> statusSet = new HashSet<PresenceAbsenceTermBase<?>>();
		Set<Distribution> existingDistributions = getExistingDistributions(taxon);
		if (existingDistributions.size() > 1){
			logger.warn("There is more than 1 distribution: " + taxon.getTitleCache());
		}
		for (Distribution distribution: existingDistributions){
			PresenceAbsenceTermBase<?> status = distribution.getStatus();
			statusSet.add(status);
		}
		
		if (statusSet.size() == 0){
			logger.warn("No status found for: " +  taxon.getTitleCache());
			return null;
		}else if (statusSet.size() == 1){
			return statusSet.iterator().next();
		}else{
			logger.warn("More than 1 status found. Return first: " +  taxon.getTitleCache());
			return statusSet.iterator().next();
		}
	}

	/**
	 * @param taxon
	 * @param statusSet
	 */
	private Set<Distribution> removeDistributions(Taxon taxon) {
		Set<Distribution> result = new HashSet<Distribution>();
		for (TaxonDescription desc : taxon.getDescriptions()){
			if (desc.isImageGallery() == NO_IMAGE_GALLERY ){
				Iterator<DescriptionElementBase> iterator = desc.getElements().iterator();
				while (iterator.hasNext()){
					DescriptionElementBase element = iterator.next();
					if (element.isInstanceOf(Distribution.class)){
						iterator.remove();
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * @param taxon
	 * @param statusSet
	 */
	private Set<Distribution> getExistingDistributions(Taxon taxon) {
		Set<Distribution> result = new HashSet<Distribution>();
		for (TaxonDescription desc : taxon.getDescriptions()){
			if (desc.isImageGallery() == NO_IMAGE_GALLERY ){
				for (DescriptionElementBase element : desc.getElements()){
					if (element.isInstanceOf(Distribution.class)){
						Distribution distribution = CdmBase.deproxy(element, Distribution.class);
						result.add(distribution);
					}
				}
			}
		}
		return result;
	}
	
	private Reference getReference(String referenceStr) {
		Reference result;
		if ("Meikle 1977".equals(referenceStr)){
			result = refMeikle1977;
		}else if("Meikle 1985".equals(referenceStr)){
			result = refMeikle1985;
		}else{
			logger.warn("Reference not recognized: " + referenceStr);
			result = null;
		}
		return result;
	}

	Map<String, Taxon> taxonStore = new HashMap<String, Taxon>();


	private Taxon getTaxon(CyprusImportState state, String taxonStr) {
		Taxon result;

		if (taxonStore.get(taxonStr) != null){
			result = taxonStore.get(taxonStr);
		}else{
//			result = getTaxonService().findBestMatchingTaxon(taxonStr);
			TaxonNameBase name = BotanicalName.NewInstance(Rank.SPECIES());
			name.setTitleCache(taxonStr, true);
			
			result = Taxon.NewInstance(name, null);
			if (result == null){
				logger.warn("Taxon not found: " +  taxonStr);
			}else{
				taxonStore.put(taxonStr, result);
			}
		}
		return result;
	}


	/** 
	 *  
	 */
	@Override
    protected boolean secondPass(CyprusImportState state) {
		boolean success = true;
		return success;
	}
	
	@Override
	protected boolean isIgnore(CyprusImportState state) {
		return ! state.getConfig().isDoDistribution();
	}
	
}
