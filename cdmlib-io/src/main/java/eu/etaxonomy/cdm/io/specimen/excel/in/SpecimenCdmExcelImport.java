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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
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
	
	
	private static final String SPECIFIC_EPITHET_COLUMN = "SpecificEpithet";
	private static final String FAMILY_COLUMN = "Family";
	private static final String GENUS_COLUMN = "Genus";
	private static final String AUTHOR_COLUMN = "Author";
	


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
    		String indexedKey = CdmUtils.removeDuplicateWhitespace(originalKey.trim()).toString();
    		String[] split = indexedKey.split("_");
    		String key = split[0];
    		if (split.length > 1){
    			String indexString = split[split.length - 1];
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
			
			} else if(key.equalsIgnoreCase(ACCESSION_NUMBER_COLUMN)) {
				row.setLocality(value);		
			} else if(key.equalsIgnoreCase(BARCODE_COLUMN)) {
				row.setBarcode(value);		
			} else if(key.equalsIgnoreCase(AUTHOR_COLUMN)) {
				row.setAuthor(value);		
			} else if(key.equalsIgnoreCase(FAMILY_COLUMN)) {
				row.setFamily(value);		
			} else if(key.equalsIgnoreCase(GENUS_COLUMN)) {
				row.setGenus(value);		
			} else if(key.equalsIgnoreCase(SPECIFIC_EPITHET_COLUMN)) {
				row.setSpecificEpithet(value);		
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
			facade.innerDerivedUnit().addSpecimenTypeDesignation(designation);
		}
		
		
		
		//save
		getOccurrenceService().save(facade.innerDerivedUnit());
		return true;
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
		TaxonNameBase result = null;
		result = state.getName(name);
		if (result != null){
			return result;
		}
		List<TaxonNameBase> list = getNameService().findNamesByTitle(name);
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
				String message = "Type status not recognized for %s %d";
				message = String.format(message, key, state.getCurrentLine());
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
		

	private DerivedUnitType getDerivedUnitType(String basisOfRecord) {
		return null;
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
