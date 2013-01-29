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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.io.algaterra.validation.AlgaTerraTypeImportValidator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelReferenceImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonNameImport;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class AlgaTerraTypeImport  extends AlgaTerraSpecimenImportBase {
	private static final Logger logger = Logger.getLogger(AlgaTerraTypeImport.class);

	
	private static int modCount = 5000;
	private static final String pluralString = "types";
	private static final String dbTableName = "TypeDesignation";  //??  

	protected String getLocalityString(){
		return "TypeLocality";
	}
	
	public AlgaTerraTypeImport(){
		super(dbTableName, pluralString);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT TypeDesignationId "  
				+ " FROM TypeDesignation " 
				+ " ORDER BY NameFk ";
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =    
					
			" SELECT ts.*, ts.TypeSpecimenId as unitId, td.*, gz.ID as GazetteerId, gz.L2Code, gz.L3Code, gz.L4Code, gz.ISOCountry, gz.Country, ts.WaterBody " + 
               " " +
            " FROM TypeSpecimenDesignation tsd  " 
            	+ " LEFT OUTER JOIN TypeSpecimen AS ts ON tsd.TypeSpecimenFk = ts.TypeSpecimenId " 
            	+ " FULL OUTER JOIN TypeDesignation td ON  td.TypeDesignationId = tsd.TypeDesignationFk "
            	+ " LEFT OUTER JOIN TDWGGazetteer gz ON ts.TDWGGazetteerFk = gz.ID "
		+ 	" WHERE (td.TypeDesignationId IN (" + ID_LIST_TOKEN + ")  )"  
          + " ORDER BY NameFk "
            ;
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState bmState) {
		boolean success = true;
		
		AlgaTerraImportState state = (AlgaTerraImportState)bmState;
		
		//TODO check that no duplicate vocabularies will be created, also remove redundant code here
//		and in Specimen importer
		try {
			makeVocabulariesAndFeatures(state);
		} catch (SQLException e1) {
			logger.warn("Exception occurred when trying to create Type specimen vocabularies: " + e1.getMessage());
			e1.printStackTrace();
		}
		
		
		
		Set<TaxonNameBase> namesToSave = new HashSet<TaxonNameBase>();
		
		Map<String, TaxonNameBase> taxonNameMap = (Map<String, TaxonNameBase>) partitioner.getObjectMap(BerlinModelTaxonNameImport.NAMESPACE);
		Map<String, DerivedUnit> ecoFactMap = (Map<String, DerivedUnit>) partitioner.getObjectMap(AlgaTerraEcoFactImport.ECO_FACT_FIELD_OBSERVATION_NAMESPACE);
		Map<String, DerivedUnit> typeSpecimenMap = (Map<String, DerivedUnit>) partitioner.getObjectMap(TYPE_SPECIMEN_FIELD_OBSERVATION_NAMESPACE);
		Map<String, Reference> biblioReference = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("Type designations handled: " + (i-1));}
				
				int nameId = rs.getInt("nameFk");
				int typeSpecimenId = rs.getInt("TypeSpecimenId");
				int typeDesignationId = rs.getInt("TypeDesignationId");
				Integer typeStatusFk =  nullSafeInt(rs, "typeStatusFk");
				Integer ecoFactId = nullSafeInt(rs, "ecoFactFk");
//				String recordBasis = rs.getString("RecordBasis");
				
				try {
					
					//source ref
					Reference<?> sourceRef = state.getTransactionalSourceReference();
				
					//facade
					//FIXME - depends on material category
//					DerivedUnitType type = makeDerivedUnitType(recordBasis);
					DerivedUnitType type = DerivedUnitType.Specimen;
					DerivedUnitFacade facade = getDerivedUnit(state, typeSpecimenId, typeSpecimenMap, type, ecoFactMap, ecoFactId);
					
					//field observation
					handleFieldObservationSpecimen(rs, facade, state, partitioner);
					
					//TODO devide like in EcoFact (if necessary)
					handleTypeSpecimenSpecificSpecimen(rs,facade, state);
					
					handleFirstDerivedSpecimen(rs, facade, state, partitioner);
					
					
					//Designation
					TaxonNameBase<?,?> name = getTaxonName(state, taxonNameMap, nameId);
					SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();
					SpecimenTypeDesignationStatus status = getSpecimenTypeDesignationStatusByKey(typeStatusFk);
					designation.setTypeSpecimen(facade.innerDerivedUnit());
					designation.setTypeStatus(status);
					if (name != null){
						name.addTypeDesignation(designation, true); //TODO check if true is correct
					}else{
						logger.warn("Name could not be found for type designation " + typeDesignationId);
					}
					namesToSave.add(name); 
					

				} catch (Exception e) {
					logger.warn("Exception in TypeDesignation: TypeDesignationId " + typeDesignationId + ". " + e.getMessage());
					e.printStackTrace();
				} 
                
            }
           
//            logger.warn("Specimen: " + countSpecimen + ", Descriptions: " + countDescriptions );

			logger.warn("Names to save: " + namesToSave.size());
			getNameService().save(namesToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	
	protected String getDerivedUnitNameSpace(){
		return TYPE_SPECIMEN_DERIVED_UNIT_NAMESPACE;
	}
	
	protected String getFieldObservationNameSpace(){
		return TYPE_SPECIMEN_FIELD_OBSERVATION_NAMESPACE;
	}

	/**
	 * @param state 
	 * @param taxonNameMap
	 * @param nameId
	 * @return
	 */
	private TaxonNameBase<?,?> getTaxonName(AlgaTerraImportState state, Map<String, TaxonNameBase> taxonNameMap, int nameId) {
		TaxonNameBase<?,?> result;
		if (state.getConfig().isDoTaxonNames()){
			result = taxonNameMap.get(String.valueOf(nameId));
		}else{
			//for testing
			result = BotanicalName.NewInstance(Rank.SPECIES());
		}
		return result;
	}

	private void handleTypeSpecimenSpecificSpecimen(ResultSet rs, DerivedUnitFacade facade, AlgaTerraImportState state) throws SQLException {
		//TODO
		
		
		//collection
		String barcode = rs.getString("Barcode");
		if (StringUtils.isNotBlank(barcode)){
			facade.setBarcode(barcode);
		}
		
	}

	/**
	 * @param state
	 * @param ecoFactId
	 * @param derivedUnitMap
	 * @param type 
	 * @param ecoFactId2 
	 * @param ecoFactMap 
	 * @return
	 */
	private DerivedUnitFacade getDerivedUnit(AlgaTerraImportState state, int typeSpecimenId, Map<String, DerivedUnit> typeSpecimenMap, DerivedUnitType type, Map<String, DerivedUnit> ecoFactMap, Integer ecoFactId2) {
		//TODO implement ecoFact map - if not all null anymore
		String typeKey = String.valueOf(typeSpecimenId);
		DerivedUnit derivedUnit = typeSpecimenMap.get(typeKey);
		DerivedUnitFacade facade;
		if (derivedUnit == null){
			facade = DerivedUnitFacade.NewInstance(type);
			typeSpecimenMap.put(typeKey, derivedUnit);
		}else{
			try {
				facade = DerivedUnitFacade.NewInstance(derivedUnit);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				logger.error(e.getMessage());
				facade = DerivedUnitFacade.NewInstance(type);
			}
		}
		
		return facade;
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

	
	private SpecimenTypeDesignationStatus getSpecimenTypeDesignationStatusByKey(Integer typeStatusFk) {
		if (typeStatusFk == null){ return null;
		}else if (typeStatusFk == 1) { return SpecimenTypeDesignationStatus.HOLOTYPE();
		}else if (typeStatusFk == 2) { return SpecimenTypeDesignationStatus.LECTOTYPE();
		}else if (typeStatusFk == 3) { return SpecimenTypeDesignationStatus.NEOTYPE();
		}else if (typeStatusFk == 4) { return SpecimenTypeDesignationStatus.EPITYPE();
		}else if (typeStatusFk == 5) { return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
		}else if (typeStatusFk == 6) { return SpecimenTypeDesignationStatus.ISONEOTYPE();
		}else if (typeStatusFk == 7) { return SpecimenTypeDesignationStatus.ISOTYPE();
		}else if (typeStatusFk == 8) { return SpecimenTypeDesignationStatus.PARANEOTYPE();
		}else if (typeStatusFk == 9) { return SpecimenTypeDesignationStatus.PARATYPE();
		}else if (typeStatusFk == 10) { return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
		}else if (typeStatusFk == 11) { return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
		}else if (typeStatusFk == 12) { return SpecimenTypeDesignationStatus.SYNTYPE();
		}else if (typeStatusFk == 13) { return SpecimenTypeDesignationStatus.PARALECTOTYPE();
		}else if (typeStatusFk == 14) { return SpecimenTypeDesignationStatus.ISOEPITYPE();
		}else if (typeStatusFk == 21) { return SpecimenTypeDesignationStatus.ICONOTYPE();
		}else if (typeStatusFk == 22) { return SpecimenTypeDesignationStatus.PHOTOTYPE();
		}else if (typeStatusFk == 30) { return SpecimenTypeDesignationStatus.TYPE();
		}else if (typeStatusFk == 38) { return SpecimenTypeDesignationStatus.ISOEPITYPE();
//		}else if (typeStatusFk == 39) { return SpecimenTypeDesignationStatus.;
		}else if (typeStatusFk == 40) { return SpecimenTypeDesignationStatus.ORIGINAL_MATERIAL();
		}else{
			logger.warn("typeStatusFk undefined for " +  typeStatusFk);
			return SpecimenTypeDesignationStatus.TYPE();
		}
		
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
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> ecoFieldObservationIdSet = new HashSet<String>();
			Set<String> typeSpecimenIdSet = new HashSet<String>();
			Set<String> termsIdSet = new HashSet<String>();
			Set<String> collectionIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, nameIdSet, "nameFk");
				handleForeignKey(rs, ecoFieldObservationIdSet, "ecoFactFk");
				handleForeignKey(rs, typeSpecimenIdSet, "TypeSpecimenId");
				handleForeignKey(rs, collectionIdSet, "CollectionFk");
			}
			
			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, TaxonNameBase> objectMap = (Map<String, TaxonNameBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, objectMap);

			//eco fact field observation map
			nameSpace = AlgaTerraTypeImport.ECO_FACT_FIELD_OBSERVATION_NAMESPACE;
			cdmClass = FieldObservation.class;
			idSet = ecoFieldObservationIdSet;
			Map<String, FieldObservation> fieldObservationMap = (Map<String, FieldObservation>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, fieldObservationMap);

			//type specimen map
			nameSpace = AlgaTerraTypeImport.TYPE_SPECIMEN_FIELD_OBSERVATION_NAMESPACE;
			cdmClass = FieldObservation.class;
			idSet = typeSpecimenIdSet;
			Map<String, FieldObservation> typeSpecimenMap = (Map<String, FieldObservation>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, typeSpecimenMap);


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

			
			//
//			//terms
//			nameSpace = AlgaTerraTypeImport.TERMS_NAMESPACE;
//			cdmClass = FieldObservation.class;
//			idSet = taxonIdSet;
//			Map<String, DefinedTermBase> termMap = (Map<String, DefinedTermBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
//			result.put(nameSpace, termMap);

		
			
			
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
		IOValidator<BerlinModelImportState> validator = new AlgaTerraTypeImportValidator();
		return validator.validate(state);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoTypes();
	}
	
}
