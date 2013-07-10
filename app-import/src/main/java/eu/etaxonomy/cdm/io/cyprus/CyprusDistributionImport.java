/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cyprus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */

@Component
public class CyprusDistributionImport extends ExcelImporterBase<CyprusImportState> {
	private static final Logger logger = Logger.getLogger(CyprusDistributionImport.class);
	
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
	

	private Reference<?> refMeikle1977 = ReferenceFactory.newGeneric();
	private Reference<?> refMeikle1985 = ReferenceFactory.newGeneric();
	
	private Map<String, Taxon> taxonWithAuthorStore = new HashMap<String, Taxon>(); 
	private Map<String, Taxon> taxonNameOnlyStore = new HashMap<String, Taxon>();


	private boolean areasCreated = false;
	private Map<String, NamedArea> divisions = new HashMap<String, NamedArea>();
	
	private void makeAreasAndReference(CyprusImportState state) {
		if (areasCreated == false){
			IInputTransformer transformer = state.getTransformer();
			try {
				//divisions
				makeNewDivisions(state, transformer);
				
				loadReferences();
				loadStatus();
				loadTaxa();
				
				areasCreated = true;
				return;
			} catch (Exception e) {
				e.printStackTrace();
				state.setUnsuccessfull();
				return;
			}
		}
	}

	/** 
	 *  Stores taxa records in DB
	 */
	@Override
    protected void firstPass(CyprusImportState state) {
		
		makeAreasAndReference(state);
		
		CyprusDistributionRow taxonLight = state.getCyprusDistributionRow();
		//species name
		String taxonStr = taxonLight.getSpecies();
		if ("#entfällt#".equalsIgnoreCase(taxonStr)){
			logger.warn("entfällt");
			return;
		}
		Taxon taxon = getTaxon(state, taxonStr);
		Reference<?> ref = getReference(taxonLight.getReference());
		if (taxon != null){
			makeDistribution(state, taxon, taxonLight.getDistribution(), ref);
			getTaxonService().save(taxon);
		}	
			
		return;
    }

	
	protected static final boolean CREATE = true;
	protected static final boolean CREATE_NOT = false;
	protected static final boolean NO_IMAGE_GALLERY = false;
	protected static final boolean IMAGE_GALLERY = false;
	
	private void makeDistribution(CyprusImportState state, Taxon taxon, String distributionStr, Reference<?> ref) {
		
//		TaxonDescription description = getTaxonDescription(taxon, NO_IMAGE_GALLERY, CREATE);
		TaxonDescription description = getNewDescription(state, taxon);
		PresenceAbsenceTermBase<?> status = getStatus(taxon);
		status = removeDoubtfulStatus(status);
		removeDistributions(taxon);
		
		for (int i = 1; i <= 8; i++){
			if (distributionStr.contains(String.valueOf(i))){
				NamedArea area = this.divisions.get(String.valueOf(i));
				Distribution distribution = Distribution.NewInstance(area, status);
				distribution.addSource(null, null, ref, null);
				description.addElement(distribution);
			}
		}
	}
	
	private TaxonDescription getNewDescription(CyprusImportState state, Taxon taxon) {
		Reference<?> excelRef = state.getConfig().getSourceReference();
		TaxonDescription desc = TaxonDescription.NewInstance(taxon, false);
		desc.setTitleCache(excelRef.getTitleCache() + " for " + taxon.getTitleCache(), true);
		desc.addSource(null, null, excelRef, null);
		return desc;
	}

	private PresenceAbsenceTermBase<?> indigenousStatus;
	private PresenceAbsenceTermBase<?> casualStatus;
	private PresenceAbsenceTermBase<?> nonInvasiveStatus;
	private PresenceAbsenceTermBase<?> invasiveStatus;
	private PresenceAbsenceTermBase<?> questionableStatus;
	
	private PresenceAbsenceTermBase<?> removeDoubtfulStatus(PresenceAbsenceTermBase<?> status) {
		if (status == null){
			return null;
		}
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
	private void removeDistributions(Taxon taxon) {
		Set<Distribution> toRemove = new HashSet<Distribution>();
		for (TaxonDescription desc : taxon.getDescriptions()){
			if (desc.isImageGallery() == NO_IMAGE_GALLERY ){
				Iterator<DescriptionElementBase> iterator = desc.getElements().iterator();
				while (iterator.hasNext()){
					DescriptionElementBase element = iterator.next();
					if (element.isInstanceOf(Distribution.class)){
						toRemove.add(CdmBase.deproxy(element, Distribution.class));
//						iterator.remove();
					}
				}
			}
		}
		for (Distribution distribution : toRemove){
			DescriptionBase<?> desc = distribution.getInDescription();
			desc.removeElement(distribution);
			getDescriptionService().saveOrUpdate(desc);
		}
		return;
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
	
	private Reference<?> getReference(String referenceStr) {
		Reference<?> result;
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

	private Taxon getTaxon(CyprusImportState state, String taxonStr) {
		Taxon result;

		if (taxonWithAuthorStore.get(taxonStr) != null){
			result = taxonWithAuthorStore.get(taxonStr);
		}else if(taxonNameOnlyStore.get(taxonStr) != null){
			result = taxonNameOnlyStore.get(taxonStr);
		}else {
//			result = getTaxonService().findBestMatchingTaxon(taxonStr);
//			TaxonNameBase name = BotanicalName.NewInstance(Rank.SPECIES());
//			name.setTitleCache(taxonStr, true);
//			
//			result = Taxon.NewInstance(name, null);
//			if (result == null){
				logger.warn("Taxon not found: " +  taxonStr);
//			}else{
//				taxonStore.put(taxonStr, result);
//			}
			result = null;
		}
		return result;
	}

	

	/**
	 * 
	 */
	private void loadTaxa() {
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("*.name");
		List<Taxon> taxonList = (List)getTaxonService().list(Taxon.class, null, null, null, propertyPaths);
		for (Taxon taxon: taxonList){
			if (taxon.getTaxonNodes().size() == 0){
				continue;
			}
			String nameTitle = taxon.getName().getTitleCache();
			String nameCache = CdmBase.deproxy(taxon.getName(), BotanicalName.class).getNameCache();
			Taxon returnValue = taxonWithAuthorStore.put(nameTitle, taxon);
			if (returnValue != null){
				logger.warn("Duplicate titleCache entry for taxon: " + nameTitle);
			}
			returnValue = taxonNameOnlyStore.put(nameCache, taxon);
			if (returnValue != null){
				logger.warn("Duplicate nameCache entry for taxon: " + nameCache);
			}
		}
	}

	/**
	 * @param meikle1977List
	 */
	private void loadReferences() {
		Pager<Reference> meikle1977List = getReferenceService().findByTitle(Reference.class, "R. D. Meikle, Flora of Cyprus 1. 1977", null, null, null, null, null, null);
		
		if (meikle1977List.getCount() != 1){
			logger.error("There is not exactly 1 Meikle 1977 reference");
		}else{
			refMeikle1977 = meikle1977List.getRecords().iterator().next();
		}
		
		Pager<Reference> meikle1985List = getReferenceService().findByTitle(Reference.class, "R. D. Meikle, Flora of Cyprus 2. 1985", null, null, null, null, null, null);
		if (meikle1985List.getCount() != 1){
			logger.error("There is not exactly 1 Meikle 1985 reference");
		}else{
			refMeikle1985 = meikle1977List.getRecords().iterator().next();
		}
	}

	/**
	 * 
	 */
	private void loadStatus() {
		indigenousStatus = (PresenceTerm)getTermService().find(CyprusTransformer.indigenousUuid);
		casualStatus = (PresenceTerm)getTermService().find(CyprusTransformer.casualUuid);
		nonInvasiveStatus = (PresenceTerm)getTermService().find(CyprusTransformer.nonInvasiveUuid);
		invasiveStatus = (PresenceTerm)getTermService().find(CyprusTransformer.invasiveUuid);
		questionableStatus = (PresenceTerm)getTermService().find(CyprusTransformer.questionableUuid);
	}

	/**
	 * @param state
	 * @param transformer
	 * @throws UndefinedTransformerMethodException
	 */
	private void makeNewDivisions(CyprusImportState state,
			IInputTransformer transformer)
			throws UndefinedTransformerMethodException {
		NamedAreaType areaType = NamedAreaType.NATURAL_AREA();
		NamedAreaLevel areaLevel = (NamedAreaLevel)getTermService().find(CyprusTransformer.uuidCyprusDivisionsAreaLevel);
		if (areaLevel == null){
			areaLevel = NamedAreaLevel.NewInstance("Cyprus Division", "Cyprus Division", null);
			getTermService().save(areaLevel);
		}
		
		TermVocabulary<NamedArea> areaVocabulary = getVocabulary(CyprusTransformer.uuidCyprusDivisionsVocabulary, "Cyprus devisions", "Cyprus divisions", null, null, true, NamedArea.NewInstance());
		TdwgArea tdwg4Cyprus = (TdwgArea)getTermService().find(UUID.fromString("9d447b51-e363-4dde-ae40-84c55679983c"));
		WaterbodyOrCountry isoCountryCyprus = (WaterbodyOrCountry)getTermService().find(UUID.fromString("4b13d6b8-7eca-4d42-8172-f2018051ca19"));
		
		for(int i = 1; i <= 8; i++){
			UUID divisionUuid = transformer.getNamedAreaUuid(String.valueOf(i));
			NamedArea division = this.getNamedArea(state, divisionUuid, "Division " + i, "Cyprus: Division " + i, String.valueOf(i), areaType, areaLevel, areaVocabulary, null);
			divisions.put(String.valueOf(i), division);
			tdwg4Cyprus.addIncludes(division);
			isoCountryCyprus.addIncludes(division);
			getTermService().save(division);
		}
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase#analyzeRecord(java.util.HashMap, eu.etaxonomy.cdm.io.excel.common.ExcelImportState)
	 */
	@Override
    protected void analyzeRecord(HashMap<String, String> record, CyprusImportState state) {
		
    	Set<String> keys = record.keySet();
    	CyprusDistributionRow cyprusDistributionRow = new CyprusDistributionRow();
    	state.setCyprusDistributionRow(cyprusDistributionRow);
    	
    	for (String originalKey: keys) {
    		String indexedKey = CdmUtils.removeDuplicateWhitespace(originalKey.trim()).toString();
    		String[] split = indexedKey.split("_");
    		String key = split[0];
    		if (split.length > 1){
    			String indexString = split[1];
    			try {
					Integer.valueOf(indexString);
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
				state.setUnsuccessfull();
				logger.error("Unexpected column header " + key);
			}
    	}
    	return;
    }
	
	
	/** 
	 *  
	 */
	@Override
    protected void secondPass(CyprusImportState state) {
		//no second pass for this import
		return;
	}
	
	@Override
	protected boolean isIgnore(CyprusImportState state) {
		return ! state.getConfig().isDoDistribution();
	}
	
}
