/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.algaterra;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.io.algaterra.validation.AlgaTerraMorphologyImportValidator;
import eu.etaxonomy.cdm.io.algaterra.validation.AlgaTerraSpecimenImportValidator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author a.mueller
 * @created 01.09.2012
 */
@Component
public class AlgaTerraMorphologyImport  extends AlgaTerraSpecimenImportBase {
	private static final Logger logger = Logger.getLogger(AlgaTerraMorphologyImport.class);

	
	private static int modCount = 5000;
	private static final String pluralString = "eco facts";
	private static final String dbTableName = "EcoFact";  //??  


	public AlgaTerraMorphologyImport(){
		super(dbTableName, pluralString);
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT EcoFactId " + 
				" FROM EcoFact  " +
				" ORDER BY EcoFact.DuplicateFk, EcoFact.EcoFactId ";
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =   
            " SELECT EcoFact.*, EcoFact.EcoFactId as unitId, " + 
               " tg.ID AS GazetteerId, tg.L2Code, tg.L3Code, tg.L4Code, tg.Country, tg.ISOCountry, " +
               " ec.UUID as climateUuid, eh.UUID as habitatUuid, elf.UUID as lifeFormUuid " +
            " FROM EcoFact " +
                 " LEFT OUTER JOIN TDWGGazetteer tg ON EcoFact.TDWGGazetteerFk = tg.ID " +
                 " LEFT OUTER JOIN EcoClimate  ec  ON EcoFact.ClimateFk  = ec.ClimateId " +
                 " LEFT OUTER JOIN EcoHabitat  eh  ON EcoFact.HabitatFk  = eh.HabitatId " +
                 " LEFT OUTER JOIN EcoLifeForm elf ON EcoFact.LifeFormFk = elf.LifeFormId " +
              " WHERE (EcoFact.EcoFactId IN (" + ID_LIST_TOKEN + ")  )"  
            + " ORDER BY EcoFact.DuplicateFk, EcoFact.EcoFactId "
            ;
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState bmState) {
		boolean success = true;
		
		AlgaTerraImportState state = (AlgaTerraImportState)bmState;
		try {
			makeVocabulariesAndFeatures(state);
		} catch (SQLException e1) {
			logger.warn("Exception occurred when trying to create Ecofact vocabularies: " + e1.getMessage());
			e1.printStackTrace();
		}
		Set<SpecimenOrObservationBase> objectsToSave = new HashSet<SpecimenOrObservationBase>();
		
		//TODO do we still need this map? EcoFacts are not handled separate from Facts.
		//However, they have duplicates on derived unit level. Also check duplicateFk. 
		Map<String, FieldObservation> ecoFactFieldObservationMap = (Map<String, FieldObservation>) partitioner.getObjectMap(ECO_FACT_FIELD_OBSERVATION_NAMESPACE);
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}
				
				int ecoFactId = rs.getInt("EcoFactId");
				Integer duplicateFk = nullSafeInt(rs, "DuplicateFk");
				
				//FIXME RecordBasis is in Fact table, which is not part of the query anymore.
				//Some EcoFacts have multiple RecordBasis types in Fact. Henning will check this.
//				String recordBasis = rs.getString("RecordBasis");
				String recordBasis = "PreservedSpecimen";
				
				try {
					
					//source ref
					Reference<?> sourceRef = state.getTransactionalSourceReference();
				
					//facade
					DerivedUnitType type = makeDerivedUnitType(recordBasis);
					
					DerivedUnitFacade facade;
					//field observation
					if (duplicateFk == null){
						facade = DerivedUnitFacade.NewInstance(type);
						handleFieldObservationSpecimen(rs, facade, state, partitioner);
						handleEcoFactSpecificFieldObservation(rs,facade, state);
						FieldObservation fieldObservation = facade.getFieldObservation(true);
						ecoFactFieldObservationMap.put(String.valueOf(ecoFactId), fieldObservation);
					}else{
						FieldObservation fieldObservation = ecoFactFieldObservationMap.get(String.valueOf(duplicateFk));
						facade = DerivedUnitFacade.NewInstance(type, fieldObservation);
					}
						
					handleFirstDerivedSpecimen(rs, facade, state, partitioner);
					handleEcoFactSpecificDerivedUnit(rs,facade, state);

					
					DerivedUnitBase<?> objectToSave = facade.innerDerivedUnit();
					objectsToSave.add(objectToSave); 
					

				} catch (Exception e) {
					logger.warn("Exception in ecoFact: ecoFactId " + ecoFactId + ". " + e.getMessage());
					e.printStackTrace();
				} 
                
            }
           
//            logger.warn("Specimen: " + countSpecimen + ", Descriptions: " + countDescriptions );

			logger.warn("Taxa to save: " + objectsToSave.size());
			getOccurrenceService().save(objectsToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	protected String getDerivedUnitNameSpace(){
		return ECO_FACT_DERIVED_UNIT_NAMESPACE;
	}
	
	protected String getFieldObservationNameSpace(){
		return ECO_FACT_FIELD_OBSERVATION_NAMESPACE;
	}



	private void handleEcoFactSpecificFieldObservation(ResultSet rs, DerivedUnitFacade facade, AlgaTerraImportState state) throws SQLException {
		
		Object alkalinityFlag = rs.getBoolean("AlkalinityFlag");
		
		//alkalinity marker
		if (alkalinityFlag != null){
			MarkerType alkalinityMarkerType = getMarkerType(state, uuidMarkerAlkalinity, "Alkalinity", "Alkalinity", null);
			boolean alkFlag = Boolean.valueOf(alkalinityFlag.toString());
			Marker alkalinityMarker = Marker.NewInstance(alkalinityMarkerType, alkFlag);
			facade.getFieldObservation(true).addMarker(alkalinityMarker);
		}
		
		
		DescriptionBase<?> fieldDescription = getFieldObservationDescription(facade);

		//habitat, ecology, community, etc.
		String habitat = rs.getString("HabitatExplanation");
		
		if (isNotBlank(habitat)){
			Feature habitatExplanation = getFeature(state, uuidFeatureHabitatExplanation, "Habitat Explanation", "HabitatExplanation", null, null);
			TextData textData = TextData.NewInstance(habitatExplanation);
			textData.putText(Language.DEFAULT(), habitat);
			fieldDescription.addElement(textData);
		}
		
		String community = rs.getString("Comunity");
		if (isNotBlank(community)){
			Feature communityFeature = getFeature(state, uuidFeatureSpecimenCommunity, "Community", "The community of a specimen (e.g. other algae in the same sample)", null, null);
			TextData textData = TextData.NewInstance(communityFeature);
			textData.putText(Language.DEFAULT(), community);
			fieldDescription.addElement(textData);
		}

		String additionalData = rs.getString("AdditionalData");
		if (isNotBlank(additionalData)){  //or handle it as Annotation ??
			Feature additionalDataFeature = getFeature(state, uuidFeatureAdditionalData, "Additional Data", "Additional Data", null, null);
			TextData textData = TextData.NewInstance(additionalDataFeature);
			textData.putText(Language.DEFAULT(), additionalData);
			fieldDescription.addElement(textData);
		}
		
		String climateUuid = rs.getString("climateUuid");
		String habitatUuid = rs.getString("habitatUuid");
		String lifeFormUuid = rs.getString("lifeFormUuid");
		
		addCategoricalValue(state, fieldDescription, climateUuid, uuidFeatureAlgaTerraClimate);
		addCategoricalValue(state, fieldDescription, habitatUuid, Feature.HABITAT().getUuid());
		addCategoricalValue(state, fieldDescription, lifeFormUuid, uuidFeatureAlgaTerraLifeForm);
		

		
		//parameters
		makeParameter(state, rs, getFieldObservationDescription(facade));

	}
	
	private void handleEcoFactSpecificDerivedUnit(ResultSet rs, DerivedUnitFacade facade, AlgaTerraImportState state) throws SQLException {
		//collection
		String voucher = rs.getString("Voucher");
		if (StringUtils.isNotBlank(voucher)){
			facade.setAccessionNumber(voucher);
		}
	}





	private void addCategoricalValue(AlgaTerraImportState importState, DescriptionBase description, String uuidTerm, UUID featureUuid) {
		if (uuidTerm != null){
			State state = this.getStateTerm(importState, UUID.fromString(uuidTerm));
			Feature feature = getFeature(importState, featureUuid);
			CategoricalData categoricalData = CategoricalData.NewInstance(state, feature);
			description.addElement(categoricalData);
		}
	}

	private void makeParameter(AlgaTerraImportState state, ResultSet rs, DescriptionBase<?> descriptionBase) throws SQLException {
		for (int i = 1; i <= 10; i++){
			String valueStr = rs.getString(String.format("P%dValue", i));
			String unitStr = rs.getString(String.format("P%dUnit", i));
			String parameter = rs.getString(String.format("P%dParameter", i));
			String method = rs.getString(String.format("P%dMethod", i));
			
			//method
			if (StringUtils.isNotBlank(method)){
				logger.warn("Methods not yet handled: " + method);
			}
			//parameter
			TermVocabulary<Feature> vocParameter = getVocabulary(uuidVocParameter, "Feature vocabulary for AlgaTerra measurement parameters", "Parameters", null, null, false, Feature.COMMON_NAME());
			if (StringUtils.isNotBlank(parameter)){
				UUID featureUuid = getParameterFeatureUuid(state, parameter);
				Feature feature = getFeature(state, featureUuid, parameter, parameter, null, vocParameter);
				QuantitativeData quantData = QuantitativeData.NewInstance(feature);
				
				//unit
				MeasurementUnit unit = getMeasurementUnit(state, unitStr);
				quantData.setUnit(unit);
				try {
					
					Set<Modifier> valueModifier = new HashSet<Modifier>();
					valueStr = normalizeAndModifyValue(state, valueStr, valueModifier);
					//value
					Float valueFlt = Float.valueOf(valueStr);  //TODO maybe change model to Double ??
					
					StatisticalMeasure measureSingleValue = getStatisticalMeasure(state, uuidStatMeasureSingleValue, "Value", "Single measurement value", null, null);
					StatisticalMeasurementValue value = StatisticalMeasurementValue.NewInstance(measureSingleValue, valueFlt); 
					quantData.addStatisticalValue(value);
					descriptionBase.addElement(quantData);
					
				} catch (NumberFormatException e) {
					logger.warn(String.format("Value '%s' can't be converted to double. Parameter %s not imported.", valueStr, parameter));
				}
			}else if (isNotBlank(valueStr) || isNotBlank(unitStr) ){
				logger.warn("There is value or unit without parameter: " + i);
			}
			
			
		}
		
	}

	private String normalizeAndModifyValue(AlgaTerraImportState state, String valueStr, Set<Modifier> valueModifier) {
		valueStr = valueStr.replace(",", ".");
		if (valueStr.startsWith("<")){
			TermVocabulary<Modifier> measurementValueModifierVocabulary = getVocabulary(uuidMeasurementValueModifier, "Measurement value modifier", "Measurement value modifier", null, null, false, Modifier.NewInstance());
			Modifier modifier = getModifier(state, uuidModifierLowerThan, "Lower", "Lower than the given measurement value", "<", measurementValueModifierVocabulary);
			valueModifier.add(modifier);
			valueStr = valueStr.replace("<", "");
		}
		if (valueStr.startsWith(">")){
			TermVocabulary<Modifier> measurementValueModifierVocabulary = getVocabulary(uuidMeasurementValueModifier, "Measurement value modifier", "Measurement value modifier", null, null, false, Modifier.NewInstance());
			Modifier modifier = getModifier(state, uuidModifierGreaterThan, "Lower", "Lower than the given measurement value", "<", measurementValueModifierVocabulary);
			valueModifier.add(modifier);
			valueStr = valueStr.replace(">", "");
		}
		return valueStr;
	}



	private UUID getParameterFeatureUuid(AlgaTerraImportState state, String key) {
		try {
			return AlgaTerraImportTransformer.getFeatureUuid(key);
		} catch (UndefinedTransformerMethodException e) {
			throw new RuntimeException(e);
		}
	}



	/**
	 * TODO move to InputTransformerBase
	 * @param state
	 * @param unitStr
	 * @return
	 */
	private MeasurementUnit getMeasurementUnit(AlgaTerraImportState state, String unitStr) {
		if (StringUtils.isNotBlank(unitStr)){
			UUID uuid = AlgaTerraImportTransformer.getMeasurementUnitUuid(unitStr);
			if (uuid != null){
				return getMeasurementUnit(state, uuid, unitStr, unitStr, unitStr, null);
			}else{
				logger.warn("MeasurementUnit was not recognized");
				return null;
			}
		}else{
			return null;
		}
	}

	private Feature makeFeature(DerivedUnitType type) {
		if (type.equals(DerivedUnitType.DerivedUnit)){
			return Feature.INDIVIDUALS_ASSOCIATION();
		}else if (type.equals(DerivedUnitType.FieldObservation) || type.equals(DerivedUnitType.Observation) ){
			return Feature.OBSERVATION();
		}else if (type.equals(DerivedUnitType.Fossil) || type.equals(DerivedUnitType.LivingBeing) || type.equals(DerivedUnitType.Specimen )){
			return Feature.SPECIMEN();
		}
		logger.warn("No feature defined for derived unit type: " + type);
		return null;
	}


	private DerivedUnitType makeDerivedUnitType(String recordBasis) {
		DerivedUnitType result = null;
		if (StringUtils.isBlank(recordBasis)){
			result = DerivedUnitType.DerivedUnit;
		} else if (recordBasis.equalsIgnoreCase("FossileSpecimen")){
			result = DerivedUnitType.Fossil;
		}else if (recordBasis.equalsIgnoreCase("HumanObservation")){
			result = DerivedUnitType.Observation;
		}else if (recordBasis.equalsIgnoreCase("Literature")){
			logger.warn("Literature record basis not yet supported");
			result = DerivedUnitType.DerivedUnit;
		}else if (recordBasis.equalsIgnoreCase("LivingSpecimen")){
			result = DerivedUnitType.LivingBeing;
		}else if (recordBasis.equalsIgnoreCase("MachineObservation")){
			logger.warn("MachineObservation record basis not yet supported");
			result = DerivedUnitType.Observation;
		}else if (recordBasis.equalsIgnoreCase("PreservedSpecimen")){
			result = DerivedUnitType.Specimen;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> fieldObservationIdSet = new HashSet<String>();
			Set<String> termsIdSet = new HashSet<String>();
			Set<String> collectionIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, fieldObservationIdSet, "DuplicateFk");
				handleForeignKey(rs, termsIdSet, "ClimateFk");
				handleForeignKey(rs, termsIdSet, "HabitatFk");
				handleForeignKey(rs, termsIdSet, "LifeFormFk");
				handleForeignKey(rs, collectionIdSet, "CollectionFk");
			}
			
			//field observation map for duplicates
			nameSpace = AlgaTerraMorphologyImport.ECO_FACT_FIELD_OBSERVATION_NAMESPACE;
			cdmClass = FieldObservation.class;
			idSet = fieldObservationIdSet;
			Map<String, FieldObservation> fieldObservationMap = (Map<String, FieldObservation>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, fieldObservationMap);

			//collections
			nameSpace = AlgaTerraCollectionImport.NAMESPACE_COLLECTION;
			cdmClass = Collection.class;
			idSet = collectionIdSet;
			Map<String, Collection> collectionMap = (Map<String, Collection>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, collectionMap);

			//sub-collections
			nameSpace = AlgaTerraCollectionImport.NAMESPACE_SUBCOLLECTION;
			cdmClass = Collection.class;
			idSet = collectionIdSet;
			Map<String, Collection> subCollectionMap = (Map<String, Collection>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, subCollectionMap);

			//terms
			nameSpace = AlgaTerraMorphologyImport.TERMS_NAMESPACE;
			cdmClass = FieldObservation.class;
			idSet = termsIdSet;
			Map<String, DefinedTermBase> termMap = (Map<String, DefinedTermBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, termMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new AlgaTerraMorphologyImportValidator();
		return validator.validate(state);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! ((AlgaTerraImportState)state).getAlgaTerraConfigurator().isDoMorphology();
	}
	
}
