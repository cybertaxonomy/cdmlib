/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenRow.DeterminationLight;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenRow.PostfixTerm;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @created 10.05.2011
 * @version 1.0
 */
@Component
public class SpecimenCdmExcelImport  extends ExcelImporterBase<SpecimenCdmExcelImportState>  implements ICdmIO<SpecimenCdmExcelImportState> {
	private static final Logger logger = Logger.getLogger(SpecimenCdmExcelImport.class);

	private static final String WORKSHEET_NAME = "Specimen";

	private static final String UUID_COLUMN = "UUID";
	private static final String BASIS_OF_RECORD_COLUMN = "BasisOfRecord";
	private static final String COUNTRY_COLUMN = "Country";
	private static final String AREA_COLUMN = "Area";
	private static final String ISO_COUNTRY_COLUMN = "ISOCountry";
	private static final String LOCALITY_COLUMN = "Locality";
	private static final String ABSOLUTE_ELEVATION_COLUMN = "AbsoluteElevation";
	private static final String COLLECTION_DATE_COLUMN = "CollectionDate";
	private static final String COLLECTION_DATE_END_COLUMN = "CollectionDateEnd";
	private static final String COLLECTOR_COLUMN = "Collector";
	private static final String LONGITUDE_COLUMN = "Longitude";
	private static final String LATITUDE_COLUMN = "Latitude";
	private static final String REFERENCE_SYSTEM_COLUMN = "ReferenceSystem";
	private static final String ERROR_RADIUS_COLUMN = "ErrorRadius";
	
	
	private static final String COLLECTORS_NUMBER_COLUMN = "CollectorsNumber";
	private static final String ECOLOGY_COLUMN = "Ecology";
	private static final String PLANT_DESCRIPTION_COLUMN = "PlantDescription";
	private static final String FIELD_NOTES_COLUMN = "FieldNotes";
	private static final String SEX_COLUMN = "Sex";
	
	
	private static final String ACCESSION_NUMBER_COLUMN = "AccessionNumber";
	private static final String BARCODE_COLUMN = "Barcode";
	private static final String COLLECTION_CODE_COLUMN = "CollectionCode";
	private static final String COLLECTION_COLUMN = "Collection";
	
	private static final String TYPE_CATEGORY_COLUMN = "TypeCategory";
	private static final String TYPIFIED_NAME_COLUMN = "TypifiedName";
	
	
	private static final String SOURCE_COLUMN = "Source";
	private static final String ID_IN_SOURCE_COLUMN = "IdInSource";
	
	
	private static final String RANK_COLUMN = "Rank";
	private static final String FULL_NAME_COLUMN = "FullName";
	private static final String FAMILY_COLUMN = "Family";
	private static final String GENUS_COLUMN = "Genus";
	private static final String SPECIFIC_EPITHET_COLUMN = "SpecificEpithet";
	private static final String INFRASPECIFIC_EPITHET_COLUMN = "InfraSpecificEpithet";
	private static final String DETERMINATION_AUTHOR_COLUMN = "Author";
	private static final String DETERMINATION_MODIFIER_COLUMN = "DeterminationModifier";
	private static final String DETERMINED_BY_COLUMN = "DeterminationBy";
	private static final String DETERMINED_WHEN_COLUMN = "DeterminationWhen";
	private static final String DETERMINATION_NOTES_COLUMN = "DeterminationNote";
	private static final String EXTENSION_COLUMN = "Ext(ension)?";
	
	

	public SpecimenCdmExcelImport() {
		super();
	}

	
	@Override
	protected boolean analyzeRecord(HashMap<String, String> record, SpecimenCdmExcelImportState state) {
		boolean success = true;
    	Set<String> keys = record.keySet();
    	
    	SpecimenRow row = new SpecimenRow();
    	state.setSpecimenRow(row);
    	
    	for (String originalKey: keys) {
    		Integer index = 0;
    		String postfix = null;
    		String indexedKey = CdmUtils.removeDuplicateWhitespace(originalKey.trim()).toString();
    		String[] split = indexedKey.split("_");
    		String key = split[0];
    		if (split.length > 1){
    			for (int i = 1 ; i < split.length ; i++ ){
    				String indexString = split[i];
        			if (isInteger(indexString)){
        				index = Integer.valueOf(indexString);
        			}else{
        				postfix = split[i];
        			}
    			}
    		}
    		
    		String value = (String) record.get(indexedKey);
    		if (! StringUtils.isBlank(value)) {
    			if (logger.isDebugEnabled()) { logger.debug(key + ": " + value); }
        		value = CdmUtils.removeDuplicateWhitespace(value.trim()).toString();
    		}else{
    			continue;
    		}
    		
    		if (key.equalsIgnoreCase(UUID_COLUMN)) {
    			row.setUuid(UUID.fromString(value)); //VALIDATE UUID
 			} else if(key.equalsIgnoreCase(BASIS_OF_RECORD_COLUMN)) {
				row.setBasisOfRecord(value);
			} else if(key.equalsIgnoreCase(COUNTRY_COLUMN)) {
				row.setCountry(value);
			} else if(key.equalsIgnoreCase(ISO_COUNTRY_COLUMN)) {
				row.setIsoCountry(value);
			} else if(key.equalsIgnoreCase(LOCALITY_COLUMN)) {
				row.setLocality(value);
			} else if(key.equalsIgnoreCase(FIELD_NOTES_COLUMN)) {
				row.setLocality(value);
			} else if(key.equalsIgnoreCase(ABSOLUTE_ELEVATION_COLUMN)) {
				row.setAbsoluteElevation(value);		
			} else if(key.equalsIgnoreCase(COLLECTOR_COLUMN)) {
				row.putCollector(index, value);		
			} else if(key.equalsIgnoreCase(ECOLOGY_COLUMN)) {
				row.setEcology(value);
			} else if(key.equalsIgnoreCase(PLANT_DESCRIPTION_COLUMN)) {
				row.setPlantDescription(value);		
			} else if(key.equalsIgnoreCase(SEX_COLUMN)) {
				row.setSex(value);
			} else if(key.equalsIgnoreCase(COLLECTION_DATE_COLUMN)) {
				row.setCollectingDate(value);		
			} else if(key.equalsIgnoreCase(COLLECTION_DATE_END_COLUMN)) {
				row.setCollectingDateEnd(value);		
			} else if(key.equalsIgnoreCase(COLLECTOR_COLUMN)) {
				row.putCollector(index, value);	
			} else if(key.equalsIgnoreCase(COLLECTORS_NUMBER_COLUMN)) {
				row.setCollectorsNumber(value);		
			} else if(key.equalsIgnoreCase(LONGITUDE_COLUMN)) {
				row.setLongitude(value);		
			} else if(key.equalsIgnoreCase(LATITUDE_COLUMN)) {
				row.setLatitude(value);		
			} else if(key.equalsIgnoreCase(REFERENCE_SYSTEM_COLUMN)) {
				row.setReferenceSystem(value);		
			} else if(key.equalsIgnoreCase(ERROR_RADIUS_COLUMN)) {
				row.setErrorRadius(value);		
			} else if(key.equalsIgnoreCase(AREA_COLUMN)) {
				if (postfix != null){
					row.addLeveledArea(postfix, value);		
				}else{
					logger.warn("Not yet implemented");
				}
			
				
				
			} else if(key.equalsIgnoreCase(ACCESSION_NUMBER_COLUMN)) {
				row.setLocality(value);		
			} else if(key.equalsIgnoreCase(BARCODE_COLUMN)) {
				row.setBarcode(value);		
			
			} else if(key.equalsIgnoreCase(FAMILY_COLUMN)) {
				row.putDeterminationFamily(index, value);		
			} else if(key.equalsIgnoreCase(GENUS_COLUMN)) {
				row.putDeterminationGenus(index, value);		
			} else if(key.equalsIgnoreCase(SPECIFIC_EPITHET_COLUMN)) {
				row.putDeterminationSpeciesEpi(index, value);			
			} else if(key.equalsIgnoreCase(INFRASPECIFIC_EPITHET_COLUMN)) {
				row.putDeterminationInfraSpeciesEpi(index, value);			
			} else if(key.equalsIgnoreCase(RANK_COLUMN)) {
				row.putDeterminationRank(index, value);			
			} else if(key.equalsIgnoreCase(FULL_NAME_COLUMN)) {
				row.putDeterminationFullName(index, value);			
			} else if(key.equalsIgnoreCase(DETERMINATION_AUTHOR_COLUMN)) {
				row.putDeterminationAuthor(index, value);			
			} else if(key.equalsIgnoreCase(DETERMINATION_MODIFIER_COLUMN)) {
				row.putDeterminationDeterminationModifier(index, value);			
			} else if(key.equalsIgnoreCase(DETERMINATION_NOTES_COLUMN)) {
				row.putDeterminationDeterminationNotes(index, value);			
			} else if(key.equalsIgnoreCase(DETERMINED_BY_COLUMN)) {
				row.putDeterminationDeterminedBy(index, value);			
			} else if(key.equalsIgnoreCase(DETERMINED_WHEN_COLUMN)) {
				row.putDeterminationDeterminedWhen(index, value);			
			
			} else if(key.equalsIgnoreCase(COLLECTION_CODE_COLUMN)) {
				row.setCollectionCode(value);		
			} else if(key.equalsIgnoreCase(COLLECTION_COLUMN)) {
				row.setCollection(value);		
			
			} else if(key.equalsIgnoreCase(TYPE_CATEGORY_COLUMN)) {
				row.putTypeCategory(index, getSpecimenTypeStatus(state, value));	
			} else if(key.equalsIgnoreCase(TYPIFIED_NAME_COLUMN)) {
				row.putTypifiedName(index, getTaxonName(state, value));		
			
			
			} else if(key.equalsIgnoreCase(SOURCE_COLUMN)) {
				row.putSourceReference(index, getOrMakeReference(state, value));	
			} else if(key.equalsIgnoreCase(ID_IN_SOURCE_COLUMN)) {
				row.putIdInSource(index, value);		
			} else if(key.matches(EXTENSION_COLUMN)) {
				if (postfix != null){
					row.addExtensionTypes(postfix, value);		
				}else{
					logger.warn("Extension without postfix not yet implemented");
				}
			
			
			}else {
				success = false;
				logger.error("Unexpected column header " + key);
			}
    	}
    	return success;
	}

	@Override
	protected boolean firstPass(SpecimenCdmExcelImportState state) {
		SpecimenRow row = state.getSpecimenRow();
		
		//basis of record
		DerivedUnitType type = DerivedUnitType.valueOf2(row.getBasisOfRecord());
		if (type == null){
			String message = "%s is not a valid BasisOfRecord. 'Unknown' is used instead.";
			message = String.format(message, row.getBasisOfRecord());
			logger.warn(message);
			type = DerivedUnitType.DerivedUnit;
		}
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(type);
		
		//country
		handleCountry(facade, row, state);
		handleAreas(facade,row, state);
		
		facade.setGatheringPeriod(getTimePeriod(row.getCollectingDate(), row.getCollectingDateEnd()));
		facade.setLocality(row.getLocality());
		facade.setFieldNotes(row.getFieldNotes());
		facade.setFieldNumber(row.getCollectorsNumber());
		facade.setEcology(row.getEcology());
		facade.setPlantDescription(row.getPlantDescription());
//		facade.setSex(row.get)
		handleExactLocation(facade, row, state);
		facade.setCollector(getOrMakeAgent(state, row.getCollectors()));
		
		
		//derivedUnit
		facade.setBarcode(row.getBarcode());
		facade.setAccessionNumber(row.getAccessionNumber());
		facade.setCollection(getOrMakeCollection(state, row.getCollectionCode(), row.getCollection()));
		for (IdentifiableSource source : row.getSources()){
			facade.addSource(source);
		}
		for (SpecimenTypeDesignation designation : row.getTypeDesignations()){
			//FIXME
//			facade.innerDerivedUnit().addSpecimenTypeDesignation(designation);
		}
		handleDeterminations(state, row, facade);
		handleExtensions(facade,row, state);
		
		
		//save
		getOccurrenceService().save(facade.innerDerivedUnit());
		return true;
	}

	private void handleExtensions(DerivedUnitFacade facade, SpecimenRow row, SpecimenCdmExcelImportState state) {
		List<PostfixTerm> extensionTypes = row.getExtensionTypes();
		
		for (PostfixTerm exType : extensionTypes){
			ExtensionType extensionType = state.getPostfixExtensionType(exType.postfix);
			
			Extension extension = Extension.NewInstance();
			extension.setType(extensionType);
			extension.setValue(exType.term);
			facade.innerDerivedUnit().addExtension(extension);
		}
		
	}


	private void handleAreas(DerivedUnitFacade facade, SpecimenRow row, SpecimenCdmExcelImportState state) {
		List<PostfixTerm> areas = row.getLeveledAreas();
		
		for (PostfixTerm lArea : areas){
			String description = null;
			String abbrev = null;
			NamedAreaType type = null;
			String key = lArea.postfix + "_" + lArea.term;
			UUID areaUuid = state.getArea(key);
			NamedAreaLevel level = state.getPostfixLevel(lArea.postfix);
			NamedArea area = getNamedArea(state, areaUuid, lArea.term, description, abbrev, type, level);
			facade.addCollectingArea(area);
			if (areaUuid == null){
				state.putArea(key, area.getUuid());
			}
		}
	}


	/**
	 * @param state
	 * @param row
	 * @param facade
	 */
	private void handleDeterminations(SpecimenCdmExcelImportState state,SpecimenRow row, DerivedUnitFacade facade) {
		boolean isFirstDetermination = true;
		for (DeterminationLight determinationLight : row.getDetermination()){
			Taxon taxon = findBestMatchingTaxon(state, determinationLight, true);
			getTaxonService().saveOrUpdate(taxon);
			TaxonNameBase<?,?> name = findBestMatchingName(state, determinationLight);
			if (state.getConfig().isMakeIndividualAssociations() && taxon != null){
				IndividualsAssociation indivAssociciation = IndividualsAssociation.NewInstance();
				DerivedUnitBase<?> du = facade.innerDerivedUnit();
				indivAssociciation.setAssociatedSpecimenOrObservation(du);
				getTaxonDescription(taxon).addElement(indivAssociciation);
			}
			if (isFirstDetermination && state.getConfig().isFirstDeterminationIsStoredUnder()){
				facade.setStoredUnder(name);
			}
			if (state.getConfig().isDeterminationsAreDeterminationEvent()){
				DeterminationEvent detEvent = makeDeterminationEvent(state, determinationLight, taxon);
				facade.addDetermination(detEvent);
			}
			isFirstDetermination = false;
		}
	}

	/**
	 * This method tries to find the best matching taxon depending on the import configuration,
	 * the taxon name information and the concept information available.
	 * 
	 * 
	 * @param state
	 * @param determinationLight
	 * @param createIfNotExists
	 * @return
	 */
	private Taxon findBestMatchingTaxon(SpecimenCdmExcelImportState state, DeterminationLight determinationLight, boolean createIfNotExists) {
		NonViralName name = makeTaxonName(state, determinationLight);
		
		String titleCache = makeSearchNameTitleCache(state, determinationLight, name);
		
		if (! StringUtils.isBlank(titleCache)){
			MatchingTaxonConfigurator matchConfigurator = MatchingTaxonConfigurator.NewInstance();
			matchConfigurator.setTaxonNameTitle(determinationLight.fullName);
			Taxon taxon = getTaxonService().findBestMatchingTaxon(matchConfigurator);
		
			if(taxon == null && createIfNotExists){
				logger.info("creating new Taxon from TaxonName '" + titleCache+"'");
				UUID secUuid = null; //TODO
				Reference sec = null;
				if (secUuid != null){
					sec = getReferenceService().find(secUuid);
				}
				taxon = Taxon.NewInstance(name, sec);
			}
			return taxon;
		}else {
			return null;
		}
	}

	/**
	 * @param state
	 * @param determinationLight
	 * @param name
	 * @return
	 */
	private String makeSearchNameTitleCache(SpecimenCdmExcelImportState state, DeterminationLight determinationLight, 
				NonViralName name) {
		String titleCache = determinationLight.fullName;
		if (! state.getConfig().isPreferNameCache() || StringUtils.isBlank(titleCache) ){
			String computedTitleCache = name.getTitleCache();
			if (StringUtils.isNotBlank(computedTitleCache)){
				titleCache = computedTitleCache;
			}
			
		}
		return titleCache;
	}

	/**
	 * @param state
	 * @param determinationLight
	 * @return
	 */
	private NonViralName makeTaxonName(SpecimenCdmExcelImportState state, DeterminationLight determinationLight) {
		//TODO correct type by config.nc
		NonViralName name =NonViralName.NewInstance(null);
		name.setGenusOrUninomial(determinationLight.genus);
		name.setSpecificEpithet(determinationLight.speciesEpi);
		name.setInfraSpecificEpithet(determinationLight.infraSpeciesEpi);
		
		//FIXME bracketAuthors and teams not yet implemented!!!
		List<String> authors = new ArrayList<String>();
		if (StringUtils.isNotBlank(determinationLight.author)){
			authors.add(determinationLight.author);
		}
		TeamOrPersonBase agent = (TeamOrPersonBase)getOrMakeAgent(state, authors);
		name.setCombinationAuthorTeam(agent);
		
		NomenclaturalCode nc = state.getConfig().getNomenclaturalCode();
		try {
			if (StringUtils.isNotBlank(determinationLight.rank) ){
				name.setRank(Rank.getRankByNameOrAbbreviation(determinationLight.rank, nc, true));
			}
		} catch (UnknownCdmTypeException e) {
			String message = "Rank not found: %s: ";
			message = String.format(message, determinationLight.rank);
			logger.warn(message);
		}
		if (StringUtils.isBlank(name.getInfraSpecificEpithet()) && StringUtils.isNotBlank(name.getSpecificEpithet() )){
			name.setRank(Rank.SPECIES());
		}
		if (StringUtils.isBlank(name.getSpecificEpithet()) && StringUtils.isNotBlank(name.getGenusOrUninomial() )){
			name.setRank(Rank.SPECIES());
		}
		if (StringUtils.isBlank(name.getTitleCache())){
			//TODO test
			name.setTitleCache(determinationLight.fullName, true);
		}
		return name;
	}

	private TaxonNameBase findBestMatchingName(SpecimenCdmExcelImportState state, DeterminationLight determinationLight) {
		
		NonViralName name = makeTaxonName(state, determinationLight);
		String titleCache = makeSearchNameTitleCache(state, determinationLight, name);
		
		//TODO
		List<TaxonNameBase> matchingNames = getNameService().findByName(null, titleCache, MatchMode.EXACT, null, null, null, null, null).getRecords();
		if (matchingNames.size() > 0){
			return matchingNames.get(0);
		} else if (matchingNames.size() > 0){
			logger.warn("Get best matching taxon name not yet fully implemeted for specimen import");
			return matchingNames.get(0);
		}else{
			return null;	
		}
		
	}

	
	private DeterminationEvent makeDeterminationEvent(SpecimenCdmExcelImportState state, DeterminationLight determination, Taxon taxon) {
		DeterminationEvent event = DeterminationEvent.NewInstance();
		//date
		TimePeriod date = TimePeriod.parseString(determination.determinedWhen);
		event.setTimeperiod(date);
		//by
		//FIXME bracketAuthors and teams not yet implemented!!!
		List<String> authors = new ArrayList<String>();
		if (StringUtils.isNotBlank(determination.determinedBy)){
			authors.add(determination.determinedBy);
		}
		AgentBase actor = getOrMakeAgent(state, authors);
		event.setActor(actor);
		
		//TODO
		if (StringUtils.isNotBlank(determination.modifier)){
			logger.warn("DeterminationModifiers not yet implemented for specimen import");
		}
//		DeterminationModifier modifier = DeterminationModifier.NewInstance(term, label, labelAbbrev);
//		determination.modifier;
		//notes
		Annotation annotation = Annotation.NewInstance(determination.notes, AnnotationType.EDITORIAL(), Language.DEFAULT());
		event.addAnnotation(annotation);
		return event;
	}

	private TaxonDescription getTaxonDescription(Taxon taxon) {
		TaxonDescription desc = this.getTaxonDescription(taxon, ! IMAGE_GALLERY, CREATE);
		return desc;
	}

	private AgentBase<?> getOrMakeAgent(SpecimenCdmExcelImportState state, List<String> agents) {
		if (agents.size() == 0){
			return null;
		}else if (agents.size() == 1){
			return getOrMakePerson(state, agents.get(0));
		}else{
			return getOrMakeTeam(state, agents);
		}
	}

	private Team getOrMakeTeam(SpecimenCdmExcelImportState state, List<String> agents) {
		String key = CdmUtils.concat("_", agents.toArray(new String[0]));
		
		Team result = state.getTeam(key);
		if (result == null){
			result = Team.NewInstance();
			for (String member : agents){
				Person person = getOrMakePerson(state, member);
				result.addTeamMember(person);
			}
			state.putTeam(key, result);
		}
		return result;
	}

	private Person getOrMakePerson(SpecimenCdmExcelImportState state, String value) {
		Person result = state.getPerson(value);
		if (result == null){
			result = Person.NewInstance();
			result.setTitleCache(value, true);
			state.putPerson(value, result);
		}
		return result;
	}

	private Reference<?> getOrMakeReference(SpecimenCdmExcelImportState state, String value) {
		Reference<?> result = state.getReference(value);
		if (result == null){
			result = ReferenceFactory.newGeneric();
			result.setTitleCache(value, true);
			state.putReference(value, result);
		}
		return result;
	}



	private Collection getOrMakeCollection(SpecimenCdmExcelImportState state, String collectionCode, String collectionString) {
		Collection result = state.getCollection(collectionCode);
		if (result == null){
			result = Collection.NewInstance();
			result.setCode(collectionCode);
			result.setName(collectionString);
			state.putCollection(collectionCode, result);
		}
		return result;
	}
	

	private TaxonNameBase<?, ?> getTaxonName(SpecimenCdmExcelImportState state, String name) {
		TaxonNameBase<?,?> result = null;
		result = state.getName(name);
		if (result != null){
			return result;
		}
		List<TaxonNameBase<?,?>> list = getNameService().findNamesByTitle(name);
		//TODO better strategy to find best name, e.g. depending on the classification it is used in
		if (! list.isEmpty()){
			result = list.get(0);
		}
		if (result == null){
			NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
			NomenclaturalCode code = state.getConfig().getNomenclaturalCode();
			result = parser.parseFullName(name, code, null);
			
		}
		if (result != null){
			state.putName(name, result);
		}
		return result;
	}

	private SpecimenTypeDesignationStatus getSpecimenTypeStatus(SpecimenCdmExcelImportState state, String key)  {
		SpecimenTypeDesignationStatus result = null;
		try {
			result = state.getTransformer().getSpecimenTypeDesignationStatusByKey(key);
			if (result == null){
				String message = "Type status not recognized for %s in line %d";
				message = String.format(message, key, state.getCurrentLine());
				logger.warn(message);
			}
			return result;
		} catch (UndefinedTransformerMethodException e) {
			throw new RuntimeException("getSpecimenTypeDesignationStatusByKey not yet implemented");
		}
		
		
	}


	private void handleExactLocation(DerivedUnitFacade facade, SpecimenRow row, SpecimenCdmExcelImportState state) {
		try {
			String longitude = row.getLongitude();
			String latitude = row.getLatitude();
			ReferenceSystem refSys = null;
			if (StringUtils.isNotBlank(row.getReferenceSystem())){
				String strRefSys = row.getReferenceSystem().trim().replaceAll("\\s", "").toLowerCase();
				//TODO move to reference system class ??
				if (strRefSys.equals("wgs84")){
					refSys = ReferenceSystem.WGS84();
				}else if (strRefSys.equals("gazetteer")){
					refSys = ReferenceSystem.GAZETTEER();
				}else if (strRefSys.equals("googleearth")){
					refSys = ReferenceSystem.GOOGLE_EARTH();
				}else{
					String message = "Reference system %s not recognized in line %d";
					message = String.format(message, strRefSys, state.getCurrentLine());
					logger.warn(message);
				}
				
			}
			Integer errorRadius = null;
			if (StringUtils.isNotBlank(row.getErrorRadius())){
				try {
					errorRadius = Integer.valueOf(row.getErrorRadius());
				} catch (NumberFormatException e) {
					String message = "Error radius %s could not be transformed to Integer in line %d";
					message = String.format(message, row.getErrorRadius(), state.getCurrentLine());
					logger.warn(message);
				}
			}
			facade.setExactLocationByParsing(longitude, latitude, refSys, errorRadius);
		} catch (ParseException e) {
			String message = "Problems when parsing exact location for line %d";
			message = String.format(message, state.getCurrentLine());
			logger.warn(message);
			
		}
		
		
	}


	/*
	 * Set the current Country
	 * Search in the DB if the isoCode is known
	 * If not, search if the country name is in the DB
	 * If not, create a new Label with the Level Country
	 * @param iso: the country iso code
	 * @param fullName: the country's full name
	 * @param app: the CDM application controller
	 */
	private void handleCountry(DerivedUnitFacade facade, SpecimenRow row, SpecimenCdmExcelImportState state) {
		
		if (StringUtils.isNotBlank(row.getIsoCountry())){
			NamedArea country = getOccurrenceService().getCountryByIso(row.getIsoCountry());
			if (country != null){
				facade.setCountry(country);
				return;
			}
		}
		if (StringUtils.isNotBlank(row.getCountry())){
			List<WaterbodyOrCountry> countries = getOccurrenceService().getWaterbodyOrCountryByName(row.getCountry());
			if (countries.size() >0){
				facade.setCountry(countries.get(0));
			}else{
				UUID uuid = UUID.randomUUID();
				String label = row.getCountry();
				String text = row.getCountry();
				String labelAbbrev = null;
				NamedAreaType areaType = NamedAreaType.ADMINISTRATION_AREA();
				NamedAreaLevel level = NamedAreaLevel.COUNTRY();
				NamedArea newCountry = this.getNamedArea(state, uuid, label, text, labelAbbrev, areaType, level);
				facade.setCountry(newCountry);
			}
		}
	}
		

	
	
	protected boolean isInteger(String value){
		try {
			Integer.valueOf(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	protected boolean secondPass(SpecimenCdmExcelImportState state) {
		//no second path defined yet
		return true;
	}


	@Override
	protected String getWorksheetName() {
		return WORKSHEET_NAME;
	}
	
	@Override
	protected boolean needsNomenclaturalCode() {
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(SpecimenCdmExcelImportState state) {
		logger.warn("Validation not yet implemented for " + this.getClass().getSimpleName());
		return true;
	}



	@Override
	protected boolean isIgnore(SpecimenCdmExcelImportState state) {
		return !state.getConfig().isDoSpecimen();
	}


}
