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
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase.PostfixTerm;
import eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenRow.DeterminationLight;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @created 10.05.2011
 */
@Component
public class SpecimenCdmExcelImport
        extends ExcelTaxonOrSpecimenImportBase<SpecimenCdmExcelImportState, SpecimenCdmExcelImportConfigurator, SpecimenRow>
        implements ICdmIO<SpecimenCdmExcelImportState> {
    private static final long serialVersionUID = 5489033387543936839L;

    private static final Logger logger = Logger.getLogger(SpecimenCdmExcelImport.class);

	private static final String WORKSHEET_NAME = "Specimen";

	private static final String BASIS_OF_RECORD_COLUMN = "(?i)(BasisOfRecord)";
	private static final String COUNTRY_COLUMN = "(?i)(Country)";
	private static final String AREA_COLUMN = "(?i)(Area)";
	private static final String ISO_COUNTRY_COLUMN = "(?i)(ISOCountry|CountryCode)";
	private static final String LOCALITY_COLUMN = "(?i)(Locality)";
	private static final String ALTITUDE_COLUMN = "(?i)(AbsoluteElevation|Altitude)";
	private static final String ALTITUDE_MAX_COLUMN = "(?i)(AbsoluteElevation|Altitude)Max(imum)?";
	private static final String COLLECTION_DATE_COLUMN = "(?i)(CollectionDate)";
	private static final String COLLECTION_DATE_END_COLUMN = "(?i)(CollectionDateEnd)";
	private static final String COLLECTOR_COLUMN = "(?i)(Collector)";
	private static final String COLLECTORS_COLUMN = "(?i)(Collectors)";
	private static final String PRIMARY_COLLECTOR_COLUMN = "(?i)(PrimaryCollector)";
	private static final String LONGITUDE_COLUMN = "(?i)(Longitude)";
	private static final String LATITUDE_COLUMN = "(?i)(Latitude)";
	private static final String REFERENCE_SYSTEM_COLUMN = "(?i)(ReferenceSystem)";
	private static final String ERROR_RADIUS_COLUMN = "(?i)(ErrorRadius)";


	private static final String COLLECTORS_NUMBER_COLUMN = "(?i)((Collectors|Field)Number)";
	private static final String ECOLOGY_COLUMN = "(?i)(Ecology|Habitat)";
	private static final String PLANT_DESCRIPTION_COLUMN = "(?i)(PlantDescription)";
	private static final String FIELD_NOTES_COLUMN = "(?i)(FieldNotes)";
	private static final String SEX_COLUMN = "(?i)(Sex)";


	private static final String ACCESSION_NUMBER_COLUMN = "(?i)(AccessionNumber)";
	private static final String BARCODE_COLUMN = "(?i)(Barcode)";
	private static final String COLLECTION_CODE_COLUMN = "(?i)(CollectionCode)";
	private static final String COLLECTION_COLUMN = "(?i)(Collection)";
	private static final String UNIT_NOTES_COLUMN = "(?i)((Unit)?Notes)";


	private static final String TYPE_CATEGORY_COLUMN = "(?i)(TypeCategory)";
	private static final String TYPIFIED_NAME_COLUMN = "(?i)(TypifiedName|TypeOf)";


	private static final String SOURCE_COLUMN = "(?i)(Source)";
	private static final String ID_IN_SOURCE_COLUMN = "(?i)(IdInSource)";


	private static final String DETERMINATION_AUTHOR_COLUMN = "(?i)(Author)";
	private static final String DETERMINATION_MODIFIER_COLUMN = "(?i)(DeterminationModifier)";
	private static final String DETERMINED_BY_COLUMN = "(?i)(DeterminationBy)";
	private static final String DETERMINED_WHEN_COLUMN = "(?i)(Det(ermination)?When)";
	private static final String DETERMINATION_NOTES_COLUMN = "(?i)(DeterminationNote)";
	private static final String EXTENSION_COLUMN = "(?i)(Ext(ension)?)";


	public SpecimenCdmExcelImport() {
		super();
	}




	@Override
	protected void analyzeSingleValue(KeyValue keyValue, SpecimenCdmExcelImportState state) {
		SpecimenRow row = state.getCurrentRow();
		String value = keyValue.value;
		if(keyValue.key.matches(BASIS_OF_RECORD_COLUMN)) {
			row.setBasisOfRecord(value);
		} else if(keyValue.key.matches(COUNTRY_COLUMN)) {
			row.setCountry(value);
		} else if(keyValue.key.matches(ISO_COUNTRY_COLUMN)) {
			row.setIsoCountry(value);
		} else if(keyValue.key.matches(LOCALITY_COLUMN)) {
			row.setLocality(value);
		} else if(keyValue.key.matches(FIELD_NOTES_COLUMN)) {
			row.setLocality(value);
		} else if(keyValue.key.matches(ALTITUDE_COLUMN)) {
			row.setAltitude(value);
		} else if(keyValue.key.matches(ALTITUDE_MAX_COLUMN)) {
			row.setAltitudeMax(value);
		} else if(keyValue.key.matches(COLLECTOR_COLUMN)) {
			row.putCollector(keyValue.index, value);
		} else if(keyValue.key.matches(PRIMARY_COLLECTOR_COLUMN)) {
			row.setPrimaryCollector(value);
		} else if(keyValue.key.matches(ECOLOGY_COLUMN)) {
			row.setEcology(value);
		} else if(keyValue.key.matches(PLANT_DESCRIPTION_COLUMN)) {
			row.setPlantDescription(value);
		} else if(keyValue.key.matches(SEX_COLUMN)) {
			row.setSex(value);
		} else if(keyValue.key.matches(COLLECTION_DATE_COLUMN)) {
			row.setCollectingDate(value);
		} else if(keyValue.key.matches(COLLECTION_DATE_END_COLUMN)) {
			row.setCollectingDateEnd(value);
		} else if(keyValue.key.matches(COLLECTORS_COLUMN)) {
			row.setCollectors(value);
		} else if(keyValue.key.matches(COLLECTOR_COLUMN)) {
			row.putCollector(keyValue.index, value);
		} else if(keyValue.key.matches(COLLECTORS_NUMBER_COLUMN)) {
			row.setCollectorsNumber(value);
		} else if(keyValue.key.matches(LONGITUDE_COLUMN)) {
			row.setLongitude(value);
		} else if(keyValue.key.matches(LATITUDE_COLUMN)) {
			row.setLatitude(value);
		} else if(keyValue.key.matches(REFERENCE_SYSTEM_COLUMN)) {
			row.setReferenceSystem(value);
		} else if(keyValue.key.matches(ERROR_RADIUS_COLUMN)) {
			row.setErrorRadius(value);
		} else if(keyValue.key.matches(AREA_COLUMN)) {
			if (keyValue.postfix != null){
				row.addLeveledArea(keyValue.postfix, value);
			}else{
				logger.warn("Not yet implemented");
			}
		} else if(keyValue.key.matches(LANGUAGE)) {
			row.setLanguage(value);


		} else if(keyValue.key.matches(ACCESSION_NUMBER_COLUMN)) {
			row.setAccessionNumber(value);
		} else if(keyValue.key.matches(BARCODE_COLUMN)) {
			row.setBarcode(value);
		} else if(keyValue.key.matches(UNIT_NOTES_COLUMN)) {
			row.putUnitNote(keyValue.index, value);


		} else if(keyValue.key.matches(FAMILY_COLUMN)) {
			row.putDeterminationFamily(keyValue.index, value);
		} else if(keyValue.key.matches(GENUS_COLUMN)) {
			row.putDeterminationGenus(keyValue.index, value);
		} else if(keyValue.key.matches(SPECIFIC_EPITHET_COLUMN)) {
			row.putDeterminationSpeciesEpi(keyValue.index, value);
		} else if(keyValue.key.matches(INFRASPECIFIC_EPITHET_COLUMN)) {
			row.putDeterminationInfraSpeciesEpi(keyValue.index, value);
		} else if(keyValue.key.matches(RANK_COLUMN)) {
			row.putDeterminationRank(keyValue.index, value);
		} else if(keyValue.key.matches(TAXON_UUID_COLUMN)) {
			row.putDeterminationTaxonUuid(keyValue.index, value);
		} else if(keyValue.key.matches(FULL_NAME_COLUMN)) {
			row.putDeterminationFullName(keyValue.index, value);
		} else if(keyValue.key.matches(DETERMINATION_AUTHOR_COLUMN)) {
			row.putDeterminationAuthor(keyValue.index, value);
		} else if(keyValue.key.matches(DETERMINATION_MODIFIER_COLUMN)) {
			row.putDeterminationDeterminationModifier(keyValue.index, value);
		} else if(keyValue.key.matches(DETERMINATION_NOTES_COLUMN)) {
			row.putDeterminationDeterminationNotes(keyValue.index, value);
		} else if(keyValue.key.matches(DETERMINED_BY_COLUMN)) {
			row.putDeterminationDeterminedBy(keyValue.index, value);
		} else if(keyValue.key.matches(DETERMINED_WHEN_COLUMN)) {
			row.putDeterminationDeterminedWhen(keyValue.index, value);

		} else if(keyValue.key.matches(COLLECTION_CODE_COLUMN)) {
			row.setCollectionCode(value);
		} else if(keyValue.key.matches(COLLECTION_COLUMN)) {
			row.setCollection(value);

		} else if(keyValue.key.matches(TYPE_CATEGORY_COLUMN)) {
			row.putTypeCategory(keyValue.index, getSpecimenTypeStatus(state, value));
		} else if(keyValue.key.matches(TYPIFIED_NAME_COLUMN)) {
			row.putTypifiedName(keyValue.index, getTaxonName(state, value));


		} else if(keyValue.key.matches(SOURCE_COLUMN)) {
			row.putSourceReference(keyValue.index, getOrMakeReference(state, value) );
		} else if(keyValue.key.matches(ID_IN_SOURCE_COLUMN)) {
			row.putIdInSource(keyValue.index, value);
		} else if(keyValue.key.matches(EXTENSION_COLUMN)) {
			if (keyValue.postfix != null){
				row.addExtension(keyValue.postfix, value);
			}else{
				logger.warn("Extension without postfix not yet implemented");
			}

		}else {
			state.setUnsuccessfull();
			logger.error("Unexpected column header " + keyValue.originalKey);
		}

    	return;
	}


	@Override
	protected void firstPass(SpecimenCdmExcelImportState state) {
		SpecimenRow row = state.getCurrentRow();

		//basis of record
		SpecimenOrObservationType type = SpecimenOrObservationType.valueOf2(row.getBasisOfRecord());
		if (type == null){
			String message = "%s is not a valid BasisOfRecord. 'Unknown' is used instead in line %d.";
			message = String.format(message, row.getBasisOfRecord(), state.getCurrentLine());
			logger.warn(message);
			type = SpecimenOrObservationType.DerivedUnit;
		}
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(type);


		Language lang = Language.DEFAULT();
		if (StringUtils.isNotBlank(row.getLanguage())){
			Language langIso = getTermService().getLanguageByIso(row.getLanguage());
			if (langIso == null){
				String message = "Language could not be recognized: %s. Use default language instead. Line %d.";
				message = String.format(message, langIso, state.getCurrentLine());
			}else{
				lang = langIso;
			}
		}

		//country
		handleCountry(facade, row, state);
		handleAreas(facade,row, state);

		facade.setGatheringPeriod(getTimePeriod(row.getCollectingDate(), row.getCollectingDateEnd()));
		facade.setLocality(row.getLocality());
		facade.setFieldNotes(row.getFieldNotes());
		facade.setFieldNumber(row.getCollectorsNumber());
		facade.setEcology(row.getEcology(), lang);
		facade.setPlantDescription(row.getPlantDescription(), lang);
//		facade.setSex(row.get)
		handleExactLocation(facade, row, state);
		facade.setCollector(getOrMakeAgent(state, row.getCollectors()));
		facade.setPrimaryCollector(getOrMakePrimaryCollector(facade, row.getPrimaryCollector(), state));
		handleAbsoluteElevation(facade, row, state);

		//derivedUnit
		facade.setBarcode(row.getBarcode());
		facade.setAccessionNumber(row.getAccessionNumber());
		facade.setCollection(getOrMakeCollection(state, row.getCollectionCode(), row.getCollection()));
		for (IdentifiableSource source : row.getSources()){
			facade.addSource(source);
		}
		for (SpecimenTypeDesignation designation : row.getTypeDesignations()){
			logger.warn("FIXME"); //FIXME
//			facade.innerDerivedUnit().addSpecimenTypeDesignation(designation);
		}
		handleDeterminations(state, row, facade);
		handleExtensions(facade.innerDerivedUnit(),row, state);
		for (String note : row.getUnitNotes()){
			Annotation annotation = Annotation.NewInstance(note, AnnotationType.EDITORIAL(), Language.DEFAULT());
			facade.addAnnotation(annotation);
		}

		//save
		getOccurrenceService().save(facade.innerDerivedUnit());
		return;
	}

	private void handleAbsoluteElevation(DerivedUnitFacade facade, SpecimenRow row, SpecimenCdmExcelImportState state) {
		//altitude

		try {
			String altitude = row.getAltitude();
			if (StringUtils.isBlank(altitude)){
				return;
			}
//			if (altitude.endsWith(".0")){
//				altitude = altitude.substring(0, altitude.length() -2);
//			}
			int value = Integer.valueOf(altitude);
			facade.setAbsoluteElevation(value);
		} catch (NumberFormatException e) {
			String message = "Absolute elevation / altitude '%s' is not an integer number in line %d";
			message = String.format(message, row.getAltitude(), state.getCurrentLine());
			logger.warn(message);
			return;
		}

		//max

		try {
			String max = row.getAltitudeMax();
			if (StringUtils.isBlank(max)){
				return;
			}
//			if (max.endsWith(".0")){
//				max = max.substring(0, max.length() -2);
//			}
			int value = Integer.valueOf(max);
			//TODO avoid unequal distance
			int min = facade.getAbsoluteElevation();
			if ( (value - min) % 2 == 1 ){
				String message = "Altitude min-max difference ist not equal. Max reduced by 1 in line %d";
				message = String.format(message, state.getCurrentLine());
				logger.warn(message);
				value--;
			}
			facade.setAbsoluteElevationRange(min, value);
		} catch (NumberFormatException e) {
			String message = "Absolute elevation / Altitude maximum '%s' is not an integer number in line %d";
			message = String.format(message, row.getAltitudeMax(), state.getCurrentLine());
			logger.warn(message);
			return;
		}catch (Exception e){
			String message = "Error occurred when trying to write Absolute elevation / Altitude maximum '%s' in line %d";
			message = String.format(message, row.getAltitudeMax(), state.getCurrentLine());
			logger.warn(message);
			return;

		}


	}

	private void handleAreas(DerivedUnitFacade facade, SpecimenRow row, SpecimenCdmExcelImportState state) {
		List<PostfixTerm> areas = row.getLeveledAreas();

		for (PostfixTerm lArea : areas){
			String description = lArea.term;
			String abbrev = lArea.term;
			NamedAreaType type = null;
			String key = lArea.postfix + "_" + lArea.term;
			UUID areaUuid = state.getArea(key);
			NamedAreaLevel level = state.getPostfixLevel(lArea.postfix);

			TermMatchMode matchMode = state.getConfig().getAreaMatchMode();
			NamedArea area = getNamedArea(state, areaUuid, lArea.term, description, abbrev, type, level, null, matchMode);
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
		DeterminationLight commonDetermination = row.getCommonDetermination();
		Taxon commonTaxon = null;
		TaxonName commonName = null;

		boolean hasCommonTaxonInfo = (commonDetermination == null) ? false : commonDetermination.hasTaxonInformation();
		if (hasCommonTaxonInfo && commonDetermination != null){
			TaxonBase<?> taxonBase = null;
			if (StringUtils.isNotBlank(commonDetermination.taxonUuid)){
				UUID taxonUuid = UUID.fromString(commonDetermination.taxonUuid);
				taxonBase = getTaxonService().find(taxonUuid);
				if (taxonBase == null){
					String message = "Taxon for uuid %s not found in line %d.";
					message = String.format(message, taxonUuid.toString(), state.getCurrentLine());
					logger.warn(message);
				}
			}else{
				taxonBase = findBestMatchingTaxon(state, commonDetermination, state.getConfig().isCreateTaxonIfNotExists());
			}
			commonTaxon = getAcceptedTaxon(taxonBase);
			if (taxonBase != null){
				commonName = taxonBase.getName();
			}else{
				commonTaxon = createTaxonFromDetermination(state, commonDetermination);
				commonName = commonTaxon.getName();
			}
		}


		for (DeterminationLight determinationLight : row.getDetermination()){
			Taxon taxon;
			if (! hasCommonTaxonInfo){
				taxon = findBestMatchingTaxon(state, determinationLight, state.getConfig().isCreateTaxonIfNotExists());
			}else{
				taxon = commonTaxon;
			}
			if (taxon != null){
				getTaxonService().saveOrUpdate(taxon);
				if (state.getConfig().isMakeIndividualAssociations() && taxon != null){
					IndividualsAssociation indivAssociciation = IndividualsAssociation.NewInstance();
					DerivedUnit du = facade.innerDerivedUnit();
					indivAssociciation.setAssociatedSpecimenOrObservation(du);
					getTaxonDescription(taxon).addElement(indivAssociciation);
					Feature feature = Feature.INDIVIDUALS_ASSOCIATION();
					if (facade.getType().isPreservedSpecimen()){
						feature = Feature.SPECIMEN();
					}else if (facade.getType().isFeatureObservation()){
						feature = Feature.OBSERVATION();
					}
					if (state.getConfig().isUseMaterialsExaminedForIndividualsAssociations()){
						feature = Feature.MATERIALS_EXAMINED();
					}

					indivAssociciation.setFeature(feature);
				}
				if (state.getConfig().isDeterminationsAreDeterminationEvent()){
					DeterminationEvent detEvent = makeDeterminationEvent(state, determinationLight, taxon);
					detEvent.setPreferredFlag(isFirstDetermination);
					facade.addDetermination(detEvent);
				}
			}

			if (isFirstDetermination && state.getConfig().isFirstDeterminationIsStoredUnder()){
				TaxonName name;

				if (!hasCommonTaxonInfo){
					name = findBestMatchingName(state, determinationLight);
				}else{
					if (commonName == null){
						commonName = findBestMatchingName(state, commonDetermination);
					}
					name = commonName;
				}
				if (name != null){
					facade.setStoredUnder(name);
				}
			}
			isFirstDetermination = false;
		}
	}

	private Taxon createTaxonFromDetermination( SpecimenCdmExcelImportState state, DeterminationLight commonDetermination) {

		//rank
		Rank rank;
		try {
			rank = StringUtils.isBlank(commonDetermination.rank) ? null : Rank.getRankByNameOrIdInVoc(commonDetermination.rank, true);
		} catch (UnknownCdmTypeException e) {
			rank = null;
		}

		//name
		INonViralName name;
		INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
		NomenclaturalCode nc = state.getConfig().getNomenclaturalCode();
		if (StringUtils.isNotBlank(commonDetermination.fullName)){
			name = parser.parseFullName(commonDetermination.fullName, nc, rank);
			if (StringUtils.isBlank(name.getAuthorshipCache()) && StringUtils.isNotBlank(commonDetermination.author)){
				setAuthorship(name, commonDetermination.author, parser);
			}
		}else{
			if (nc != null){
				name = nc.getNewTaxonNameInstance(rank);
			}else{
				name = TaxonNameFactory.NewNonViralInstance(rank);
			}
			if (StringUtils.isNotBlank(commonDetermination.genus)){
				name.setGenusOrUninomial(commonDetermination.genus);
			}
			if (StringUtils.isNotBlank(commonDetermination.speciesEpi)){
				name.setSpecificEpithet(commonDetermination.speciesEpi);
			}
			if (StringUtils.isNotBlank(commonDetermination.infraSpeciesEpi)){
				name.setInfraSpecificEpithet(commonDetermination.infraSpeciesEpi);
			}
			if (StringUtils.isNotBlank(commonDetermination.author)){
				setAuthorship(name, commonDetermination.author, parser);
			}
			//guess rank if null
			if (name.getRank() == null){
				if (name.getInfraGenericEpithet() != null && name.getSpecificEpithet() == null){
					name.setRank(Rank.INFRAGENERICTAXON());
				}else if (name.getSpecificEpithet() != null && name.getInfraSpecificEpithet() == null){
					name.setRank(Rank.SPECIES());
				}else if (name.getInfraSpecificEpithet() != null){
					name.setRank(Rank.INFRASPECIFICTAXON());
				}

			}

		}
		//sec
		Reference sec = null;
		if (StringUtils.isNotBlank(commonDetermination.determinedBy)){
			sec = ReferenceFactory.newGeneric();
			TeamOrPersonBase<?> determinedBy;
			IBotanicalName dummyName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
			try {
				parser.parseAuthors(dummyName, commonDetermination.determinedBy);
				determinedBy = dummyName.getCombinationAuthorship();
			} catch (StringNotParsableException e) {
				determinedBy = Team.NewTitledInstance(commonDetermination.determinedBy, commonDetermination.determinedBy);
			}
			sec.setAuthorship(determinedBy);
		}

		//taxon
		Taxon taxon = Taxon.NewInstance(name, sec);

		if (StringUtils.isNotBlank(commonDetermination.family)){
			if (name.getRank() == null || name.getRank().isLower(Rank.FAMILY()) ){
				logger.warn("Family taxon could not be created");
			}
		}

		//return
		return taxon;

	}




	private void setAuthorship(INonViralName name, String author, INonViralNameParser<INonViralName> parser) {
		if (name.isBotanical() || name.isZoological()){
			try {
				parser.parseAuthors(name, author);
			} catch (StringNotParsableException e) {
				name.setAuthorshipCache(author);
			}
		}else{
			name.setAuthorshipCache(author);
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
		INonViralName name = makeTaxonName(state, determinationLight);

		String titleCache = makeSearchNameTitleCache(state, determinationLight, name);

		if (! StringUtils.isBlank(titleCache)){
			MatchingTaxonConfigurator matchConfigurator = MatchingTaxonConfigurator.NewInstance();
			matchConfigurator.setTaxonNameTitle(titleCache);
			matchConfigurator.setIncludeSynonyms(false);
			Taxon taxon = getTaxonService().findBestMatchingTaxon(matchConfigurator);

			if(taxon == null && createIfNotExists){
				logger.info("creating new Taxon from TaxonName '" + titleCache+"'");
				UUID secUuid = null; //TODO
				Reference sec = null;
				if (secUuid != null){
					sec = getReferenceService().find(secUuid);
				}
				taxon = Taxon.NewInstance(name, sec);
			}else if (taxon == null){
				String message = "Taxon '%s' not found in line %d";
				message = String.format(message, titleCache, state.getCurrentLine());
				logger.warn(message);
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
				INonViralName name) {
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
	private INonViralName makeTaxonName(SpecimenCdmExcelImportState state, DeterminationLight determinationLight) {
		INonViralName name = TaxonNameFactory.NewNonViralInstance(null);
		NomenclaturalCode nc = state.getConfig().getNomenclaturalCode();
		if (nc != null){
			name = nc.getNewTaxonNameInstance(null);
		}
		name.setGenusOrUninomial(determinationLight.genus);
		name.setSpecificEpithet(determinationLight.speciesEpi);
		name.setInfraSpecificEpithet(determinationLight.infraSpeciesEpi);

		//FIXME bracketAuthors and teams not yet implemented!!!
		List<String> authors = new ArrayList<String>();
		if (StringUtils.isNotBlank(determinationLight.author)){
			authors.add(determinationLight.author);
		}
		TeamOrPersonBase<?> agent = getOrMakeAgent(state, authors);
		name.setCombinationAuthorship(agent);

		try {
			if (StringUtils.isNotBlank(determinationLight.rank) ){
				name.setRank(Rank.getRankByNameOrIdInVoc(determinationLight.rank, nc, true));
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

	private TaxonName findBestMatchingName(SpecimenCdmExcelImportState state, DeterminationLight determinationLight) {

		INonViralName name = makeTaxonName(state, determinationLight);
		String titleCache = makeSearchNameTitleCache(state, determinationLight, name);

		//TODO
		List<TaxonName> matchingNames = getNameService().findByName(null, titleCache, MatchMode.EXACT, null, null, null, null, null).getRecords();
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
		//taxon
		event.setTaxon(taxon);

		//date
		TimePeriod date = TimePeriodParser.parseString(determination.determinedWhen);
		event.setTimeperiod(date);
		//by
		//FIXME bracketAuthors and teams not yet implemented!!!
		List<String> authors = new ArrayList<String>();
		if (StringUtils.isNotBlank(determination.determinedBy)){
			authors.add(determination.determinedBy);
		}
		TeamOrPersonBase<?> actor = getOrMakeAgent(state, authors);
		TeamOrPersonBase<?> secAuthor = taxon.getSec() == null ? null : taxon.getSec().getAuthorship();
		if (actor != null && secAuthor != null && secAuthor.getTitleCache().equals(actor.getTitleCache()) && secAuthor.getNomenclaturalTitle().equals(actor.getNomenclaturalTitle())) {
			actor = secAuthor;
		}

		event.setActor(actor);

		//TODO
		if (StringUtils.isNotBlank(determination.modifier)){
			logger.warn("DeterminationModifiers not yet implemented for specimen import");
		}
//		DeterminationModifier modifier = DeterminationModifier.NewInstance(term, label, labelAbbrev);
//		determination.modifier;
		//notes
		if (StringUtils.isNotEmpty(determination.notes)){
			Annotation annotation = Annotation.NewInstance(determination.notes, AnnotationType.EDITORIAL(), Language.DEFAULT());
			event.addAnnotation(annotation);
		}

		return event;
	}

	private TaxonDescription getTaxonDescription(Taxon taxon) {
		TaxonDescription desc = this.getTaxonDescription(taxon, ! IMAGE_GALLERY, CREATE);
		return desc;
	}

	private TeamOrPersonBase<?> getOrMakeAgent(SpecimenCdmExcelImportState state, List<String> agents) {
		if (agents.size() == 0){
			return null;
		}else if (agents.size() == 1){
			return getOrMakePerson(state, agents.get(0));
		}else{
			return getOrMakeTeam(state, agents);
		}
	}

	private Person getOrMakePrimaryCollector(DerivedUnitFacade facade, String primaryCollector, SpecimenCdmExcelImportState state) {
		if (StringUtils.isBlank(primaryCollector)){
			return null;
		}
		AgentBase<?> collector = facade.getCollector();
		List<Person> collectors = new ArrayList<Person>();
		if (collector.isInstanceOf(Team.class) ){
			Team team = CdmBase.deproxy(collector, Team.class);
			collectors.addAll(team.getTeamMembers());
		}else if (collector.isInstanceOf(Person.class)){
			collectors.add(CdmBase.deproxy(collector, Person.class));
		}else{
			throw new IllegalStateException("Unknown subclass of agentbase: " + collector.getClass().getName() );
		}
		for (Person person :collectors){
			if (primaryCollector.equalsIgnoreCase(person.getTitleCache())){
				return person;
			}
			if (primaryCollector.equalsIgnoreCase(person.getNomenclaturalTitle())){
				return person;
			}
		}
		String message = "Primary Agent '%s' could not be determined in collector(s) in line %d";
		message = String.format(message, primaryCollector, state.getCurrentLine());
		logger.warn(message);
		return null;
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

	private Reference getOrMakeReference(SpecimenCdmExcelImportState state, String value) {
		Reference result = state.getReference(value);
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


	private TaxonName getTaxonName(SpecimenCdmExcelImportState state, String name) {
		TaxonName result = null;
		result = state.getName(name);
		if (result != null){
			return result;
		}
		List<TaxonName> list = getNameService().findByTitle(null, name, null, null, null, null, null, null).getRecords();
		//TODO better strategy to find best name, e.g. depending on the classification it is used in
		if (! list.isEmpty()){
			result = list.get(0);
		}
		if (result == null){
			NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
			NomenclaturalCode code = state.getConfig().getNomenclaturalCode();
			result = (TaxonName)parser.parseFullName(name, code, null);

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

		//reference system
		ReferenceSystem refSys = null;
		if (StringUtils.isNotBlank(row.getReferenceSystem())){
			String strRefSys = row.getReferenceSystem().trim().replaceAll("\\s", "");
			UUID refUuid;
			try {
				refSys = state.getTransformer().getReferenceSystemByKey(strRefSys);
				if (refSys == null){
					//TODO we still need user defined Reference Systems here
					refUuid = state.getTransformer().getReferenceSystemUuid(strRefSys);
					if (refUuid == null){
						String message = "Unknown reference system %s in line %d";
						message = String.format(message, strRefSys, state.getCurrentLine());
						logger.warn(message);
					}
					refSys = getReferenceSystem(state, refUuid, strRefSys, strRefSys, strRefSys, null);
				}

			} catch (UndefinedTransformerMethodException e) {
				throw new RuntimeException(e);
			}
		}



		// lat/ long /error
		try {
			String longitude = row.getLongitude();
			String latitude = row.getLatitude();
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
			//all
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
			List<Country> countries = getOccurrenceService().getCountryByName(row.getCountry());
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

	@Override
	protected void secondPass(SpecimenCdmExcelImportState state) {
		//no second path defined yet
		return;
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
	 * @see eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase#createDataHolderRow()
	 */
	@Override
	protected SpecimenRow createDataHolderRow() {
		return new SpecimenRow();
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
