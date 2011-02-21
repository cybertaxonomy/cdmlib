/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.wp6;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 * @version 1.0
 */

@Component
public class CommonNameExcelImport extends ExcelImporterBase<CichorieaeCommonNameImportState> {
	private static final Logger logger = Logger.getLogger(CommonNameExcelImport.class);

	private static NomenclaturalCode nc = NomenclaturalCode.ICBN;
	private static INonViralNameParser nameParser = NonViralNameParserImpl.NewInstance();
	
	protected static final String SPECIES_COLUMN = "Art";
	protected static final String COMMON_NAME_COLUMN = "common name";
	protected static final String REFERENCE_COLUMN = "Literaturnummer";
	protected static final String DISTIRBUTION_COLUMN = "Verbreitung";
	protected static final String AREA_COLUMN = "Vorschlag Bezeichnung Länder/Regionen";

	private Map<String, NamedArea> areaStore = new HashMap<String, NamedArea>();
	private Map<String, Language> languageStore = new HashMap<String, Language>();

	
	@Override
	protected boolean isIgnore(CichorieaeCommonNameImportState state) {
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(CichorieaeCommonNameImportState state) {
		logger.warn("DoCheck not yet implemented for CommonNameExcelImport");
		return true;
	}

	
	@Override
    protected boolean analyzeRecord(HashMap<String, String> record, CichorieaeCommonNameImportState state) {
		
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	CommonNameRow row = new CommonNameRow();
    	state.setCommonNameRow(row);
    	
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
    			row.parseSpecies(value);
    			
			} else if(key.equalsIgnoreCase(COMMON_NAME_COLUMN)) {
				row.setCommonNames(value);
				
			} else if(key.equalsIgnoreCase(REFERENCE_COLUMN)) {
				row.setReference(value);
			} else if(key.equalsIgnoreCase(DISTIRBUTION_COLUMN)) {
				//do nothing
    			
			} else if(key.equalsIgnoreCase(AREA_COLUMN)) {
				row.setArea(value);
 			} else {
				success = false;
				logger.error("Unexpected column header " + key);
			}
    	}
    	return success;
    }
	
	
	/** 
	 *  Stores taxa records in DB
	 */
	@Override
    protected boolean firstPass(CichorieaeCommonNameImportState state) {
		
		boolean success = true;
		CommonNameRow taxonLight = state.getCommonNameRow();

		//species name
		String speciesStr = taxonLight.getSpecies();
		Taxon taxon = getTaxon(state, speciesStr);
//		TaxonNameBase nameUsedInSource = getNameUsedInSource(state, taxonLight.getNameUsedInSource());
//		NamedArea area = getArea(state, taxonLight.getArea());
		Reference ref = getReference(state, taxonLight.getReference());

//		makeCommonNames(state, taxonLight.getCommonNames(), taxon, ref, area, taxonLight.getNameUsedInSource());
		
//		getTaxonService().save(taxon);
		return success;
    }

//	private TaxonNameBase getNameUsedInSource(CichorieaeCommonNameImportState state, String nameUsedInSource) {
//		if (StringUtils.isBlank(nameUsedInSource)){
//			return null;
//		}else{
//			Pager<TaxonNameBase> list = getNameService().findByName(BotanicalName.class, nameUsedInSource, null, null, null, null, null, null);
//			if (list.getCount() > 0){
//				return list.getRecords().get(0);
//			}else{
//				return null;
//			}
//		}
//		
//	}


	private Map<String, Reference> referenceStore = new HashMap<String, Reference>();
	private Reference getReference(CichorieaeCommonNameImportState state, String reference) {
		Reference result = referenceStore.get(reference);
		if (result == null){
			result = (Reference)getCommonService().getSourcedObjectByIdInSource(Reference.class, reference, "import to Berlin Model");
			if (result == null){
				logger.warn("Reference not found: " + reference);
				result = ReferenceFactory.newGeneric();
				result.setTitleCache(reference);
			}
			referenceStore.put(reference, result);
		}
		return result;
	}


	
	private NamedArea getArea(CichorieaeCommonNameImportState state, String area) {
		NamedArea result;
		List<OrderHint> orderHints = null;
		List<Criterion> criteria = null;
		result = areaStore.get(area);
		
		if (result == null){
			try {
				result = state.getTransformer().getNamedAreaByKey(area);
			} catch (UndefinedTransformerMethodException e) {
				e.printStackTrace();
			}
			if (result == null){
				List<DefinedTermBase> candidates = getTermService().findByTitle(WaterbodyOrCountry.class, area, null, criteria, null, null, orderHints, null).getRecords();
				if (candidates.size() == 0){
					candidates = getTermService().findByTitle(NamedArea.class, area, null, criteria, null, null, orderHints, null).getRecords();
				}
				if (candidates.size()>0){
					//TODO
					result = (NamedArea)candidates.get(0);
				}else{
					UUID uuidArea;
					try {
						uuidArea = state.getTransformer().getNamedAreaUuid(area);
						if (uuidArea == null){
							logger.warn("Area not defined: " + area)  ;
						}
						result = getNamedArea(state, uuidArea, area, area, null, null, null);
					} catch (UndefinedTransformerMethodException e) {
						e.printStackTrace();
					}
					if (result == null){
						logger.warn("Area not defined: " + area)  ;
					}
				}
			}
			areaStore.put(area, result);
		}
		return result;
	}


	Map<String, Taxon> taxonStore = new HashMap<String, Taxon>();
	
	private Taxon getTaxon(CichorieaeCommonNameImportState state, String taxonNameStr) {
		Taxon result;

		if (taxonStore.get(taxonNameStr) != null){
			result = taxonStore.get(taxonNameStr);
		}else{
			result = getTaxonService().findBestMatchingTaxon(taxonNameStr);
//			TaxonNameBase name = BotanicalName.NewInstance(Rank.SPECIES());
//			name.setTitleCache(taxonNameStr, true);
//			
//			result = Taxon.NewInstance(name, null);
			if (result == null){
				logger.warn("Taxon not found: " +  taxonNameStr);
			}else{
				taxonStore.put(taxonNameStr, result);
			}
		}
		return result;
	}

	private void makeCommonNames(CichorieaeCommonNameImportState state, Map<String, List<String>> commonNamesMap, Taxon mainTaxon, Reference ref, NamedArea area, String nameUsedInSource) {
		//Common Names
		TaxonDescription td = this.getTaxonDescription(mainTaxon, false, true);
		for (String languageKey : commonNamesMap.keySet()){
			Language language = getLanguage(state, languageKey);
			List<String> commonNamesList = commonNamesMap.get(languageKey);
			for (String strCommonName : commonNamesList){
				CommonTaxonName commonName = CommonTaxonName.NewInstance(strCommonName, language, area);
				if (ref != null){
					DescriptionElementSource source = DescriptionElementSource.NewInstance(ref, null);
					source.setOriginalNameString(nameUsedInSource);
					commonName.addSource(source);
				}else{
					logger.warn("No reference defined");
				}
				td.addElement(commonName);
			}
		}
	}

	private Language getLanguage(CichorieaeCommonNameImportState state, String languageKey) {
		if (languageKey.equals("*")){
			return null;
		}
		Language result;
		languageKey = languageKey.replace(", no ISO-Code", "");
		result = languageStore.get(languageKey);
		if (result == null){
			try{
				if (languageKey.length()<4){
					try {
						result = getTermService().getLanguageByIso(languageKey);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					UUID uuid = state.getTransformer().getLanguageUuid(languageKey);
					result = (Language)getTermService().find(uuid);
				}
				if (result == null){
					result = state.getTransformer().getLanguageByKey(languageKey);
					if (result == null){
						UUID uuidLanguage;
						uuidLanguage = state.getTransformer().getLanguageUuid(languageKey);
						if (uuidLanguage == null){
							logger.warn("Language not defined: " + languageKey)  ;
						}
						result = getLanguage(state, uuidLanguage, languageKey, languageKey, null);
						if (result == null){
							logger.warn("Language not defined: " + languageKey)  ;
						}
					}else if (result.getId() == 0){
//						UUID uuidLanguageVoc = UUID.fromString("45ac7043-7f5e-4f37-92f2-3874aaaef2de"); 
						UUID uuidLanguageVoc = UUID.fromString("434cea89-9052-4567-b2db-ff77f42e9084"); 
						TermVocabulary<Language> voc = getVocabulary(uuidLanguageVoc, "User Defined Languages", "User Defined Languages", null);
//						TermVocabulary<Language> voc = getVocabularyService().find(uuidLanguageVoc);
						voc.addTerm(result);
						getTermService().saveOrUpdate(result);
						state.putLanguage(result);
					}
				}
				languageStore.put(languageKey, result);
			} catch (UndefinedTransformerMethodException e) {
				e.printStackTrace();
			}

		}
		return result;
	}


	/** 
	 *  Stores parent-child, synonym and common name relationships
	 */
	@Override
    protected boolean secondPass(CichorieaeCommonNameImportState state) {
		boolean success = true;
		return success;
	}





	
}
