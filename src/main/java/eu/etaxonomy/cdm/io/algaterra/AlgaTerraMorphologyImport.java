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
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.algaterra.validation.AlgaTerraMorphologyImportValidator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
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
	private static final String pluralString = "morpho facts";
	private static final String dbTableName = "MorphoFact";   


	public AlgaTerraMorphologyImport(){
		super(dbTableName, pluralString);
	}
	

	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT MorphoFactId " + 
				" FROM MorphoFact  " +
				" ORDER BY MorphoFact.MorphoFactId ";
		return result;
	}


	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =   
            " SELECT mf.*, mf.MorphoFactId as unitId, ecoFact.ecoFactId as ecoFactId,  size.* " + 
            " FROM MorphoFact mf " +
            	" LEFT OUTER JOIN MorphoSizeRange size ON mf.SizeRangeFk = size.SizeRangeId " +
            	" LEFT OUTER JOIN MorphoValveDescription valve1Desc ON mf.Valve1DescriptionFk = valve1Desc.ValveDescriptionId " +
            	" LEFT OUTER JOIN MorphoValveDescription valve2Desc ON mf.Valve2DescriptionFk = valve2Desc.ValveDescriptionId " +
            	" LEFT OUTER JOIN EcoFact ecoFact ON ecoFact.CultureStrain = mf.CultureStrainNo " +
              " WHERE (mf.MorphoFactId IN (" + ID_LIST_TOKEN + ")  )"  
            + " ORDER BY mf.MorphoFactId "
            ;
		return strQuery;
	}
	
	
	private Map<String, TermVocabulary<State>> vocabularyMap = new HashMap<String, TermVocabulary<State>>();
	private Map<String, Feature> featureMap = new HashMap<String, Feature>();
	private Map<String, Map<Integer, State>> algaTerraMorphoStates = new HashMap<String, Map<Integer, State>>();
	private TermVocabulary<Feature> algaTerraMorphoFeatures = TermVocabulary
			.NewInstance(TermType.Feature, "Alga Terra Morphology Features", "AT Morphology Features", null, null);
	
	private void doMorphoListen(AlgaTerraImportState state) throws SQLException{
		
		TransactionStatus txStatus = this.startTransaction();
		
		getVocabularyService().save(algaTerraMorphoFeatures);
		
		//chloroplast position
		String baseName = "Chloroplast Position";
		UUID uuidStateVocabulary = AlgaTerraImportTransformer.uuidVocChloroplastPosition;
		boolean isOrdered = false;
		makeFeatureAndVocabulary(state, vocabularyMap, featureMap, algaTerraMorphoStates, 
				algaTerraMorphoFeatures, baseName, uuidStateVocabulary, isOrdered);
		
		//Chloroplast Shape
		baseName = "Chloroplast Shape";
		uuidStateVocabulary = AlgaTerraImportTransformer.uuidVocChloroplastShape;
		isOrdered = false;
		makeFeatureAndVocabulary(state, vocabularyMap, featureMap, algaTerraMorphoStates,
				algaTerraMorphoFeatures, baseName, uuidStateVocabulary, isOrdered);

		//Chloroplast Shape
		baseName = "Chloroplast Structure";
		uuidStateVocabulary = AlgaTerraImportTransformer.uuidVocChloroplastStructure;
		isOrdered = false;
		makeFeatureAndVocabulary(state, vocabularyMap, featureMap, algaTerraMorphoStates,
				algaTerraMorphoFeatures, baseName, uuidStateVocabulary, isOrdered);

		//Growth Form
		baseName = "Growth Form";
		uuidStateVocabulary = AlgaTerraImportTransformer.uuidVocGrowthForm;
		isOrdered = false;
		makeFeatureAndVocabulary(state, vocabularyMap, featureMap, algaTerraMorphoStates,
				algaTerraMorphoFeatures, baseName, uuidStateVocabulary, isOrdered);

		//Organisation Level
		baseName = "Organisation Level";
		uuidStateVocabulary = AlgaTerraImportTransformer.uuidVocOrganisationLevel;
		isOrdered = false;
		makeFeatureAndVocabulary(state, vocabularyMap, featureMap, algaTerraMorphoStates,
				algaTerraMorphoFeatures, baseName, uuidStateVocabulary, isOrdered);


		//Raphe
		baseName = "Raphe";
		uuidStateVocabulary = AlgaTerraImportTransformer.uuidVocRaphe;
		isOrdered = false;
		makeFeatureAndVocabulary(state, vocabularyMap, featureMap, algaTerraMorphoStates,
				algaTerraMorphoFeatures, baseName, uuidStateVocabulary, isOrdered);

		//Shape
		baseName = "Shape";
		uuidStateVocabulary = AlgaTerraImportTransformer.uuidVocShape;
		isOrdered = false;
		makeFeatureAndVocabulary(state, vocabularyMap, featureMap, algaTerraMorphoStates,
				algaTerraMorphoFeatures, baseName, uuidStateVocabulary, isOrdered);

		//Symmetrie
		baseName = "Symmetry";
		uuidStateVocabulary = AlgaTerraImportTransformer.uuidVocSymmetry;
		isOrdered = false;
		makeFeatureAndVocabulary(state, vocabularyMap, featureMap, algaTerraMorphoStates,
				algaTerraMorphoFeatures, baseName, uuidStateVocabulary, isOrdered);

		getVocabularyService().saveOrUpdate(algaTerraMorphoFeatures);
		
		this.commitTransaction(txStatus);
	}


	private void makeFeatureAndVocabulary(AlgaTerraImportState state,
			Map<String, TermVocabulary<State>> vocabularyMap,
			Map<String, Feature> featureMap, Map<String, Map<Integer, State>> allMorphoStates, 
			TermVocabulary<Feature> algaTerraMorphoFeatures, String baseName,
			UUID uuidStateVocabulary, boolean isOrdered)
			throws SQLException {
		
		Source source = state.getAlgaTerraConfigurator().getSource();
		
		Map<Integer, State> morphoStates = new HashMap<Integer, State>();
		allMorphoStates.put(baseName, morphoStates);	
		
		String baseNameCamel = baseName.replace(" ", "");
		
		//make feature
		handleSingleFeature(state, featureMap, algaTerraMorphoFeatures,	baseName);
		
		//make term vocabulary
		String vocDescription = "The vocabulary for the " + baseName + " in AlgaTerra";
		String vocLabel = baseName;
		String vocAbbrevLabel = null;
		URI termSourceUri = null;
		TermVocabulary<State> voc = getVocabulary(TermType.State, uuidStateVocabulary,vocDescription, vocLabel, vocAbbrevLabel, termSourceUri, isOrdered, null);
		vocabularyMap.put(vocLabel, voc);
		
		String idField = baseNameCamel + "Id";
		String sql =  "SELECT " + idField + "," + baseNameCamel + ", Description FROM Morpho" + baseNameCamel;
		ResultSet rs = source.getResultSet(sql);
		while (rs.next()){
			Integer id = rs.getInt(idField);
			String label = rs.getString(baseNameCamel);
			String description = rs.getString("Description");
			State term = State.NewInstance(description, label, null);
			voc.addTerm(term);
			term.addSource(OriginalSourceType.Import, String.valueOf(id), baseNameCamel, state.getTransactionalSourceReference() , null);
			morphoStates.put(id, term);
		}
		getVocabularyService().saveOrUpdate(voc);
	}


	private Feature handleSingleFeature(AlgaTerraImportState state,
			Map<String, Feature> featureMap,
			TermVocabulary<Feature> algaTerraMorphoFeatures, String baseName) {
		
		String baseNameCamel = baseName.replace(" ", "");
		
		UUID uuidFeature = null;
		try {
			uuidFeature = AlgaTerraImportTransformer.getFeatureUuid(baseName);
		} catch (UndefinedTransformerMethodException e) {
			throw new RuntimeException(e);
		}
		Feature feature = getFeature(state, uuidFeature, baseName, baseName, null, algaTerraMorphoFeatures);
		algaTerraMorphoFeatures.addTerm(feature);
		featureMap.put(baseNameCamel, feature);
		return feature;
	}
	
	private void doNonListenFeatures(AlgaTerraImportState state) throws SQLException{
		String baseName = "Apices";
		handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName);
		
		baseName = "Chloroplast Number";
		handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName);
		
		baseName = "Pyrenoid";
		handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName);
		
		baseName = "Cell Wall";
		handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName);
		
		baseName = "Reproductive Stages";
		handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName);
		
		makeValveFeatures(state, featureMap, algaTerraMorphoFeatures);
		
	}

	


	private void makeValveFeatures(AlgaTerraImportState state,
			Map<String, Feature> featureMap2,
			TermVocabulary<Feature> algaTerraMorphoFeatures2) {
		
		String baseName = "Valve 1";
		handleSingleValve(state, featureMap, algaTerraMorphoFeatures, baseName);

		baseName = "Valve 2";
		handleSingleValve(state, featureMap, algaTerraMorphoFeatures, baseName);
		
	}


	private void handleSingleValve(AlgaTerraImportState state,
			Map<String, Feature> featureMap2,
			TermVocabulary<Feature> algaTerraMorphoFeatures2, String valveStr) {
		
		Feature featureValve = handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, valveStr);
		
		String baseName = "Striae Frequency";
		Feature featureSub = handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName + " " + valveStr);
		//TODO is partOf correct here? see also below
		featureSub.setPartOf(featureValve);
		
		baseName = "Striae Orientation Mid"; //Mid Valve
		featureSub = handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName + " " + valveStr);
		featureSub.setPartOf(featureValve);
		
		baseName = "Striae Orientation Apices";
		featureSub = handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName + " " + valveStr);
		featureSub.setPartOf(featureValve);
		
		baseName = "Central Area";
		featureSub = handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName + " " + valveStr);
		featureSub.setPartOf(featureValve);
		
		baseName = "Axial Area";
		featureSub = handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName + " " + valveStr);
		featureSub.setPartOf(featureValve);
		
		baseName = "has Raphe";
		featureSub = handleSingleFeature(state, featureMap, algaTerraMorphoFeatures, baseName + " " + valveStr);
		featureSub.setPartOf(featureValve);
	}


	@Override
	protected void doInvoke(BerlinModelImportState state) {
		AlgaTerraImportState atState = (AlgaTerraImportState)state;
		try {
			doMorphoListen(atState);
			doNonListenFeatures(atState);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		super.doInvoke(state);
	}


	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState bmState) {
		boolean success = true;
		
		AlgaTerraImportState state = (AlgaTerraImportState)bmState;
		Set<SpecimenOrObservationBase> objectsToSave = new HashSet<SpecimenOrObservationBase>();
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}
				
				int morphoFactId = rs.getInt("MorphoFactId");
				String cultureStrainNo = rs.getString("CultureStrainNo");
				
				try {
					
					//source ref
					Reference<?> sourceRef = state.getTransactionalSourceReference();
					
					//ecoFact
					DerivedUnit ecoFact = makeDerivationFromEcoFact(state, rs, morphoFactId);
					if (ecoFact != null){
						SpecimenDescription desc = SpecimenDescription.NewInstance();
						desc.setTitleCache("Morphology for " + cultureStrainNo , true);
						
						ecoFact.addDescription(desc);
						
						String baseLabel = "Organisation Level";
						handleStateTerm(state, rs, baseLabel, desc);
						
						baseLabel = "Growth Form";
						handleStateTerm(state, rs, baseLabel, desc);
						
						baseLabel = "Shape";
						handleStateTerm(state, rs, baseLabel, desc);
						
						baseLabel = "Symmetry";
						handleStateTerm(state, rs, baseLabel, desc);
						
						baseLabel = "Raphe";
						handleStateTerm(state, rs, baseLabel, desc);
	
						baseLabel = "Chloroplast Shape";
						handleStateTerm(state, rs, baseLabel, desc);
	
						baseLabel = "Chloroplast Structure";
						handleStateTerm(state, rs, baseLabel, desc);
	
						baseLabel = "Chloroplast Position";
						handleStateTerm(state, rs, baseLabel, desc);
						
						baseLabel = "Apices";
						handleTextData(state, rs, baseLabel, desc);
						
						baseLabel = "Chloroplast Number";
						handleTextData(state, rs, baseLabel, desc);
						
						baseLabel = "Pyrenoid";
						handleTextData(state, rs, baseLabel, desc);
						
						baseLabel = "Cell Wall";
						handleTextData(state, rs, baseLabel, desc);
						
						baseLabel = "Reproductive Stages";
						handleTextData(state, rs, baseLabel, desc);
						
						//TODO to which object to add this information
						this.doId(state, desc, morphoFactId, dbTableName);
						String notes = rs.getString("Notes");
						this.doNotes(desc, notes);
	
			            objectsToSave.add(ecoFact); 
					}else if (cultureStrainNo != null) {
						logger.warn("cultureStrainNo (" + cultureStrainNo + ") exists but no ecoFact found for morphoFact " + morphoFactId);
					}else{
						logger.info("No cultureStrainNo defined for morphoFact " + morphoFactId);
					}
				} catch (Exception e) {
					logger.warn("Exception in morphoFact: morphoFactId " + morphoFactId + ". " + e.getMessage());
					e.printStackTrace();
				} 
                
            }
            
            //TODO DataEntryBy, 
            //GrowthFormExplanation, ShapeExplanation, RapheExplanation, ChloroplastExplanation, 
            //SizeRangeFk, SizeRangeExplanation,
            //Valve1RapheFlag, Valve1DescriptionFk,
            //Valve2RapheFlag, Valve2DescriptionFk,
            //StriaeFrequencyExplanation,
            //PyrenoidExplanation,

            //Not required:  OrganisationLevelExplanation, ValveLinearShape, ValveShape, SymmetryExplanation,
            //Rimoportula, Fultoportula, Description, CultureCollection
           
			logger.warn("Specimen to save: " + objectsToSave.size());
			getOccurrenceService().save(objectsToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	private DerivedUnit makeDerivationFromEcoFact(AlgaTerraImportState state, ResultSet rs, Integer morphoFactId) throws SQLException {
		Integer ecoFactFk = nullSafeInt(rs, "ecoFactId");
		if (ecoFactFk != null){
			
			DerivedUnit ecoFact = (DerivedUnit)state.getRelatedObject(ECO_FACT_DERIVED_UNIT_NAMESPACE, ecoFactFk.toString());
			if (ecoFact == null){
				logger.warn("EcoFact is null for ecoFactFk: " + ecoFactFk + ", morphoFactId: " + morphoFactId);
				return null;
			}else{
				return ecoFact;
			}
		}else{
			return null;
		}
	}
	
	private void handleTextData(AlgaTerraImportState state, ResultSet rs,
			String baseLabel, SpecimenDescription desc) throws SQLException {
		String baseLabelCamel = baseLabel.replace(" ", "");
		
		String value = rs.getString(baseLabelCamel);
		if (value != null){
			Feature feature = this.featureMap.get(baseLabelCamel);
			if (feature == null){
				logger.warn("Feature is null");
			}
			TextData textData = TextData.NewInstance(feature);
			desc.addElement(textData);
		}
	}


	private void handleStateTerm(AlgaTerraImportState algaTerraState, ResultSet rs,
			String baseLabel, SpecimenDescription desc) throws SQLException {
		
		String baseLabelCamel = baseLabel.replace(" ", "");
		Integer id = nullSafeInt(rs, baseLabelCamel + "Fk");
		if (id != null){
			Feature feature = this.featureMap.get(baseLabelCamel);
			State state = getState(baseLabel, id);
			if (feature == null || state == null){
				logger.warn("Feature or state is null");
			}
			DescriptionElementBase deb = CategoricalData.NewInstance(state, feature);
			desc.addElement(deb);
		}
	}


	private State getState(String baseLabel, Integer id) {
		Map<Integer, State> stateMap = this.algaTerraMorphoStates.get(baseLabel);
		return stateMap.get(id);
	}


	protected String getDerivedUnitNameSpace(){
		return ECO_FACT_DERIVED_UNIT_NAMESPACE;
	}
	
	protected String getFieldObservationNameSpace(){
		return ECO_FACT_FIELD_OBSERVATION_NAMESPACE;
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
			TermVocabulary<Feature> vocParameter = getVocabulary(TermType.Feature, uuidVocParameter, "Feature vocabulary for AlgaTerra measurement parameters", "Parameters", null, null, false, Feature.COMMON_NAME());
			if (StringUtils.isNotBlank(parameter)){
				UUID featureUuid = getParameterFeatureUuid(state, parameter);
				Feature feature = getFeature(state, featureUuid, parameter, parameter, null, vocParameter);
				QuantitativeData quantData = QuantitativeData.NewInstance(feature);
				
				//unit
				MeasurementUnit unit = getMeasurementUnit(state, unitStr);
				quantData.setUnit(unit);
				try {
					
					Set<DefinedTerm> valueModifier = new HashSet<DefinedTerm>();
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

	private String normalizeAndModifyValue(AlgaTerraImportState state, String valueStr, Set<DefinedTerm> valueModifier) {
		valueStr = valueStr.replace(",", ".");
		if (valueStr.startsWith("<")){
			TermVocabulary<DefinedTerm> measurementValueModifierVocabulary = getVocabulary(TermType.Modifier, uuidMeasurementValueModifier, "Measurement value modifier", "Measurement value modifier", null, null, false, DefinedTerm.NewModifierInstance(null, null, null));
			DefinedTerm modifier = getModifier(state, uuidModifierLowerThan, "Lower", "Lower than the given measurement value", "<", measurementValueModifierVocabulary);
			valueModifier.add(modifier);
			valueStr = valueStr.replace("<", "");
		}
		if (valueStr.startsWith(">")){
			TermVocabulary<DefinedTerm> measurementValueModifierVocabulary = getVocabulary(TermType.Modifier, uuidMeasurementValueModifier, "Measurement value modifier", "Measurement value modifier", null, null, false, DefinedTerm.NewModifierInstance(null, null, null));
			DefinedTerm modifier = getModifier(state, uuidModifierGreaterThan, "Lower", "Lower than the given measurement value", "<", measurementValueModifierVocabulary);
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


	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, BerlinModelImportState state) {
		String nameSpace;
		Class<?> cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> ecoFactFkSet = new HashSet<String>();
						
			while (rs.next()){
				handleForeignKey(rs, ecoFactFkSet, "ecoFactId");
			}
			
			//eco fact derived unit map
			nameSpace = AlgaTerraFactEcologyImport.ECO_FACT_DERIVED_UNIT_NAMESPACE;
			cdmClass = DerivedUnit.class;
			idSet = ecoFactFkSet;
			Map<String, DerivedUnit> derivedUnitMap = (Map<String, DerivedUnit>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, derivedUnitMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new AlgaTerraMorphologyImportValidator();
		return validator.validate(state);
	}

	@Override
	protected boolean isIgnore(BerlinModelImportState state){
		return ! ((AlgaTerraImportState)state).getAlgaTerraConfigurator().isDoMorphology();
	}
	
}
