/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.algaterra;

import java.net.URI;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @created 12.09.2012
 */
public abstract class AlgaTerraSpecimenImportBase extends BerlinModelImportBase{
	private static final Logger logger = Logger.getLogger(AlgaTerraSpecimenImportBase.class);

	public static final String ECO_FACT_FIELD_OBSERVATION_NAMESPACE = "EcoFact_FieldObservation";
	public static final String ECO_FACT_DERIVED_UNIT_NAMESPACE = "EcoFact_DerivedUnit";
	public static final String TYPE_SPECIMEN_FIELD_OBSERVATION_NAMESPACE = "TypeSpecimen_FieldObservation";
	public static final String TYPE_SPECIMEN_DERIVED_UNIT_NAMESPACE = "TypeSpecimen_DerivedUnit";
	public static final String FACT_ECOLOGY_NAMESPACE = "Fact(Ecology)";
	
	
	public static final String TERMS_NAMESPACE = "ALGA_TERRA_TERMS";
	
	//TODO move to transformrer
	final static UUID uuidMarkerAlkalinity = UUID.fromString("e52d0ea2-0c1f-4d95-ae6d-e21ab317c594");  
	final static UUID uuidRefSystemGps = UUID.fromString("c23e4928-c137-4e4a-b6ab-b430da3d0b94");  
	public final static UUID uuidFeatureSpecimenCommunity = UUID.fromString("3ff5b1ab-3999-4b5a-b8f7-01fd2f6c12c7");
	public final static UUID uuidFeatureAdditionalData = UUID.fromString("0ac82ab8-2c2b-4953-98eb-a9f718eb9c57");
	public final static UUID uuidFeatureHabitatExplanation = UUID.fromString("6fe32295-61a3-44fc-9fcf-a85790ea888f");
	
	final static UUID uuidVocAlgaTerraClimate = UUID.fromString("b0a677c6-8bb6-43f4-b1b8-fc377a10feb5");
	final static UUID uuidVocAlgaTerraHabitat = UUID.fromString("06f30114-e19c-4e7d-a8e5-5488c41fcbc5");
	final static UUID uuidVocAlgaTerraLifeForm = UUID.fromString("3c0b194e-809c-4b42-9498-6ff034066ed7");
	
	public final static UUID uuidFeatureAlgaTerraClimate = UUID.fromString("8754674c-9ab9-4f28-95f1-91eeee2314ee");
	public final static UUID uuidFeatureAlgaTerraHabitat = UUID.fromString("7def3fc2-cdc5-4739-8e13-62edbd053415");
	public final static UUID uuidFeatureAlgaTerraLifeForm = UUID.fromString("9b657901-1b0d-4a2a-8d21-dd8c1413e2e6");
	
	final static UUID uuidVocParameter = UUID.fromString("45888b40-5bbb-4293-aa1e-02479796cd7c");
	final static UUID uuidStatMeasureSingleValue = UUID.fromString("eb4c3d98-4d4b-4c37-8eb4-17315ce79920");
	final static UUID uuidMeasurementValueModifier = UUID.fromString("0218a7a3-f6c0-4d06-a4f8-6b50b73aef5e");
	
	final static UUID uuidModifierLowerThan = UUID.fromString("2b500085-6bef-4003-b6ea-e0ad0237d79d");
	final static UUID uuidModifierGreaterThan = UUID.fromString("828df49d-c745-48f7-b083-0ada43356c34");

	public AlgaTerraSpecimenImportBase(String tableName, String pluralString) {
		super(tableName, pluralString);
	}
	
	/**
	 * Creates the vocabularies and the features for Climate, Habitat and Lifeform
	 * @param state
	 * @throws SQLException
	 */
	protected void makeVocabulariesAndFeatures(AlgaTerraImportState state) throws SQLException {
		String abbrevLabel = null;
		URI uri = null;
		
		if (! state.isSpecimenVocabulariesCreated()){
			
			TransactionStatus txStatus = this.startTransaction();
		
			boolean isOrdered = true;
			OrderedTermVocabulary<State> climateVoc = (OrderedTermVocabulary)getVocabulary(uuidVocAlgaTerraClimate, "Climate", "Climate", abbrevLabel, uri, isOrdered, null);
			OrderedTermVocabulary<State> habitatVoc = (OrderedTermVocabulary)getVocabulary(uuidVocAlgaTerraHabitat, "Habitat", "Habitat", abbrevLabel, uri, isOrdered, null);
			OrderedTermVocabulary<State> lifeformVoc = (OrderedTermVocabulary)getVocabulary(uuidVocAlgaTerraLifeForm, "Lifeform", "Lifeform", abbrevLabel, uri, isOrdered, null);
			
			
			Feature feature = getFeature(state, uuidFeatureAlgaTerraClimate, "Climate","Climate", null, null);
			feature.setSupportsCategoricalData(true);
			
			feature = getFeature(state, uuidFeatureAlgaTerraLifeForm, "LifeForm","LifeForm", null, null);
			feature.setSupportsCategoricalData(true);
			
			feature = Feature.HABITAT();
			feature.setSupportsCategoricalData(true);
			getTermService().saveOrUpdate(feature);
			
			Source source = state.getAlgaTerraConfigurator().getSource();
			
			String climateSql = "SELECT * FROM EcoClimate";
			ResultSet rs = source.getResultSet(climateSql);
			while (rs.next()){
				String climate = rs.getString("Climate");
				String description = rs.getString("Description");
				Integer id = rs.getInt("ClimateId");
				UUID uuid = UUID.fromString(rs.getString("UUID"));
				State stateTerm = getStateTerm(state, uuid, climate, description, null, climateVoc);
				addOriginalSource(stateTerm, id.toString(), "EcoClimate", state.getTransactionalSourceReference());
				getTermService().saveOrUpdate(stateTerm);
			}
			
			String habitatSql = "SELECT * FROM EcoHabitat";
			rs = source.getResultSet(habitatSql);
			while (rs.next()){
				String habitat = rs.getString("Habitat");
				String description = rs.getString("Description");
				Integer id = rs.getInt("HabitatId");
				UUID uuid = UUID.fromString(rs.getString("UUID"));
				State stateTerm = getStateTerm(state, uuid, habitat, description, null, habitatVoc);
				addOriginalSource(stateTerm, id.toString(), "EcoHabitat", state.getTransactionalSourceReference());
				getTermService().saveOrUpdate(stateTerm);
			}
			
			String lifeformSql = "SELECT * FROM EcoLifeForm";
			rs = source.getResultSet(lifeformSql);
			while (rs.next()){
				String lifeform = rs.getString("LifeForm");
				String description = rs.getString("Description");
				Integer id = rs.getInt("LifeFormId");
				UUID uuid = UUID.fromString(rs.getString("UUID"));
				State stateTerm = getStateTerm(state, uuid, lifeform, description, null, lifeformVoc);
				addOriginalSource(stateTerm, id.toString(), "EcoLifeForm", state.getTransactionalSourceReference());
				getTermService().saveOrUpdate(stateTerm);
			}
			
			this.commitTransaction(txStatus);
			
			state.setSpecimenVocabulariesCreated(true);
		}
		
	}
	
	protected String getLocalityString(){
		return "Locality";
	}
	
	protected void handleFieldObservationSpecimen(ResultSet rs, DerivedUnitFacade facade, AlgaTerraImportState state, ResultSetPartitioner partitioner) throws SQLException {
		//FIXME missing fields #3084, #3085, #3080
		try {
			
			Integer unitId = nullSafeInt(rs, "unitId");
			String locality = rs.getString(getLocalityString());
			Double latitude = nullSafeDouble(rs, "Latitude");
			Double longitude = nullSafeDouble(rs, "Longitude");
			Integer errorRadius = nullSafeInt(rs,"Prec");
			String geoCodeMethod = rs.getString("GeoCodeMethod");
			
			Integer altitude = nullSafeInt(rs, "Altitude");
			Integer lowerAltitude = nullSafeInt(rs,"AltitudeLowerValue");
			String altitudeUnit = rs.getString("AltitudeUnit");
			Double depth = nullSafeDouble(rs, "Depth");
			Double depthLow = nullSafeDouble(rs, "DepthLow");
			   	
			String collectorsNumber = rs.getString("CollectorsNumber");
			Date collectionDateStart = rs.getDate("CollectionDate");
			Date collectionDateEnd = rs.getDate("CollectionDateEnd");
			
			//location
			facade.setLocality(locality);
			    	
			//exact location
			ReferenceSystem referenceSystem = makeRefrenceSystem(geoCodeMethod, state);
			if (longitude != null || latitude != null || referenceSystem != null || errorRadius != null){
				Point exactLocation = Point.NewInstance(longitude, latitude, referenceSystem, errorRadius);
				facade.setExactLocation(exactLocation);
			}
			
			//altitude, depth
			if (StringUtils.isNotBlank(altitudeUnit) && ! altitudeUnit.trim().equalsIgnoreCase("m")){
				logger.warn("Altitude unit is not [m] but: " +  altitudeUnit);
			}
			if ( altitude != null){
				if (lowerAltitude == null){
					facade.setAbsoluteElevation(altitude);
				}else{
			   		if (! facade.isEvenDistance(lowerAltitude, altitude)){
			   			//FIXME there is a ticket for this
			   			altitude = altitude + 1;
			   			logger.info("Current implementation of altitude does not allow uneven distances");
			   		}
					facade.setAbsoluteElevationRange(lowerAltitude,altitude);
			   	}
			}
			if ( depth != null){
				//FIXME needs model change to accept double #3072
				Integer intDepth = depth.intValue();
				if (depthLow == null){
					facade.setDistanceToWaterSurface(intDepth);
				}else{
					//FIXME range not yet in model #3074
			   		facade.setDistanceToWaterSurface(intDepth);
			   	}
			}
			
			//field
			facade.setFieldNumber(collectorsNumber);
			TimePeriod gatheringPeriod = TimePeriod.NewInstance(collectionDateStart, collectionDateEnd);
			facade.setGatheringPeriod(gatheringPeriod);
			handleCollectorTeam(state, facade, rs);
			
			//areas
			makeAreas(state, rs, facade);

			//notes
			//TODO is this an annotation on field observation or on the derived unit?
			
			//id, created, updated, notes
			if (unitId != null){
				this.doIdCreatedUpdatedNotes(state, facade.innerFieldObservation(), rs, unitId, getFieldObservationNameSpace());
			}else{
				logger.warn("FieldObservation has no unitId: " +  facade.innerFieldObservation() + ": " + getFieldObservationNameSpace());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    	
	}
	
	protected void handleFirstDerivedSpecimen(ResultSet rs, DerivedUnitFacade facade, AlgaTerraImportState state, ResultSetPartitioner partitioner) throws SQLException {
		Integer unitId = nullSafeInt(rs, "unitId");
		Integer collectionFk = nullSafeInt(rs,"CollectionFk");
		
		//collection
		if (collectionFk != null){
			Collection subCollection = state.getRelatedObject(AlgaTerraCollectionImport.NAMESPACE_SUBCOLLECTION, String.valueOf(collectionFk), Collection.class);
			if (subCollection != null){
				facade.setCollection(subCollection);
			}else{
				Collection collection = state.getRelatedObject(AlgaTerraCollectionImport.NAMESPACE_COLLECTION, String.valueOf(collectionFk), Collection.class);
				facade.setCollection(collection);
			}
		}
		
		//TODO id, created for fact +  ecoFact
		//    	this.doIdCreatedUpdatedNotes(state, descriptionElement, rs, id, namespace);
		if (unitId != null){
			this.doIdCreatedUpdatedNotes(state, facade.innerDerivedUnit(), rs, unitId, getDerivedUnitNameSpace());
		}else{
			logger.warn("Specimen has no unitId: " +  facade.innerDerivedUnit() + ": " + getDerivedUnitNameSpace());
		}
	}
		
	
	
	protected abstract String getDerivedUnitNameSpace();
	
	protected abstract String getFieldObservationNameSpace();
	

	protected DescriptionBase getFieldObservationDescription(DerivedUnitFacade facade) {
		Set<DescriptionBase> descriptions = facade.innerFieldObservation().getDescriptions();
		for (DescriptionBase desc : descriptions){
			if (desc.isImageGallery() == false){
				return desc;
			}
		}
		SpecimenDescription specDesc = SpecimenDescription.NewInstance(facade.innerFieldObservation());
		descriptions.add(specDesc);
		return specDesc;
	}
	

	private void makeAreas(AlgaTerraImportState state, ResultSet rs, DerivedUnitFacade facade) throws SQLException {
	   	Object gazetteerId = rs.getObject("GazetteerId");
	   	if (gazetteerId != null){
	   		//TDWG
	   		NamedArea tdwgArea;
	   		String tdwg4 = rs.getString("L4Code");
	   		if (isNotBlank(tdwg4)){
	   			tdwgArea = TdwgArea.getAreaByTdwgAbbreviation(tdwg4);
	   		}else{
	   			String tdwg3 = rs.getString("L3Code");
	   			if (isNotBlank(tdwg3)){
	   				tdwgArea = TdwgArea.getAreaByTdwgAbbreviation(tdwg3);
	   			}else{
	   				Integer tdwg2 = rs.getInt("L2Code");   				
	   				tdwgArea = TdwgArea.getAreaByTdwgAbbreviation(String.valueOf(tdwg2));
		   		}
	   		}
	   		if (tdwgArea == null){
	   			logger.warn("TDWG area could not be defined for gazetterId: " + gazetteerId);
	   		}else{
	   			facade.addCollectingArea(tdwgArea);
	   		}
	   		
	   		//Countries
	   		WaterbodyOrCountry country = null;
	   		String isoCountry = rs.getString("ISOCountry");
	   		String countryStr = rs.getString("Country");
	   		if (isNotBlank(isoCountry)){
		   		country = WaterbodyOrCountry.getWaterbodyOrCountryByIso3166A2(isoCountry);
	   		}else if (isNotBlank(countryStr)){
	   			logger.warn("Country exists but no ISO code");
	   		}
	   		if (country == null){
	   			logger.warn("Country does not exist for GazetteerID " + gazetteerId);
	   		}else{
	   			facade.setCountry(country);
	   		}
	   		
	   	}
	    
	   	//Waterbody
	   	WaterbodyOrCountry waterbody = null;
	   	String waterbodyStr = rs.getString("WaterBody");
	   	if (isNotBlank(waterbodyStr)){
	   		if (waterbodyStr.equals("Atlantic Ocean")){
	   			waterbody = WaterbodyOrCountry.ATLANTICOCEAN();
	   		}else{
	   			logger.warn("Waterbody not recognized: " + waterbody);
	   		}
	   		if (waterbody != null){
	   			facade.addCollectingArea(waterbody);
	   		}
	   	}

		
	   	//countries sub
	   	//TODO -> SpecimenImport (not existing in TypeSpecimen)
	}


	

	private ReferenceSystem makeRefrenceSystem(String geoCodeMethod, AlgaTerraImportState state) {
		if (StringUtils.isBlank(geoCodeMethod)){
			return null;
		}else if(geoCodeMethod.startsWith("GPS")){
			getReferenceSystem(state, uuidRefSystemGps, "GPS", "GPS", "GPS", ReferenceSystem.GOOGLE_EARTH().getVocabulary());
			return ReferenceSystem.WGS84(); 
		}else if(geoCodeMethod.startsWith("Google")){
			return ReferenceSystem.GOOGLE_EARTH();
		}else if(geoCodeMethod.startsWith("Map")){
			logger.warn("Reference system " +  geoCodeMethod +  " not yet supported.");
			return null;
		}else if(geoCodeMethod.startsWith("WikiProjekt Georeferenzierung") || geoCodeMethod.startsWith("http://toolserver.org/~geohack/geohack.php") ){
			return ReferenceSystem.WGS84();
		}else {
			logger.warn("Reference system " +  geoCodeMethod +  " not yet supported.");
			return null;
		}
	}
	

	

	private void handleCollectorTeam(AlgaTerraImportState state, DerivedUnitFacade facade, ResultSet rs) throws SQLException {
		String collector = rs.getString("Collector");
		TeamOrPersonBase<?> author = getAuthor(collector);
		facade.setCollector(author);
	}

	/**
	 * @param facade
	 * @param collector
	 */
	protected TeamOrPersonBase<?> getAuthor(String author) {
		// FIXME TODO parsen und deduplizieren
		Team team = Team.NewTitledInstance(author, author);
		return team;
	}
	

	/**
	 * Use same TaxonDescription if two records belong to the same taxon 
	 * @param state 
	 * @param newTaxonId
	 * @param oldTaxonId
	 * @param oldDescription
	 * @param taxonMap
	 * @return
	 */
	protected TaxonDescription getTaxonDescription(AlgaTerraImportState state, Taxon taxon, Reference<?> sourceSec){
		TaxonDescription result = null;
		Set<TaxonDescription> descriptionSet= taxon.getDescriptions();
		if (descriptionSet.size() > 0) {
			result = descriptionSet.iterator().next(); 
		}else{
			result = TaxonDescription.NewInstance();
			result.setTitleCache(sourceSec.getTitleCache(), true);
			taxon.addDescription(result);
		}
		return result;
	}

	


}
