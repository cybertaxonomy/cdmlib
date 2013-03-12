/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.algaterra;

import java.sql.Date;
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
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeCacheStrategy;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.io.algaterra.validation.AlgaTerraSpecimenImportValidator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelReferenceImport;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonImport;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * Alga Terra Import für den Fact mit FactId =202 (Ecology)
 * @author a.mueller
 * @created 01.09.2012
 */
@Component
public class AlgaTerraFactEcologyImport  extends AlgaTerraSpecimenImportBase {
	private static final Logger logger = Logger.getLogger(AlgaTerraFactEcologyImport.class);

	
	private static int modCount = 5000;
	private static final String pluralString = "determinations";
	private static final String dbTableName = "Fact"; 


	public AlgaTerraFactEcologyImport(){
		super(dbTableName, pluralString);
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT f.factId " + 
				" FROM Fact f LEFT JOIN PTaxon pt ON f.PTNameFk = pt.PTNameFk AND f.PTRefFk = pt.PTRefFk " 
				+ " WHERE f.FactCategoryFk = 202 "
				+ " ORDER BY pt.RIdentifier, f.FactId ";
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =   
            " SELECT pt.RIdentifier as taxonId, f.* " + 
            " FROM Fact f " + 
                 " LEFT JOIN PTaxon pt ON f.PTNameFk =pt.PTNameFk AND f.PTRefFk = pt.PTRefFk " +
             " WHERE f.FactCategoryFk = 202 AND (f.FactId IN (" + ID_LIST_TOKEN + ")  )"  
            + " ORDER BY pt.RIdentifier, f.FactId "
            ;
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState bmState) {
		boolean success = true;
		
		AlgaTerraImportState state = (AlgaTerraImportState)bmState;
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		
		Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
		Map<String, DerivedUnitBase> ecoFactDerivedUnitMap = (Map<String, DerivedUnitBase>) partitioner.getObjectMap(ECO_FACT_DERIVED_UNIT_NAMESPACE);
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}
				
				Integer taxonId = nullSafeInt(rs, "taxonId");
				int factId = rs.getInt("FactId");
				Integer ecoFactId = nullSafeInt(rs, "ExtensionFk");
				String recordBasis = rs.getString("RecordBasis");
				
				
				try {
					
					//source ref
					Reference<?> sourceRef = state.getTransactionalSourceReference();
				
					DerivedUnitBase<?> ecoFact = ecoFactDerivedUnitMap.get(String.valueOf(ecoFactId));
					
					
					//description element
					if (taxonId != null){
						Taxon taxon = getTaxon(state, taxonId, taxonMap, factId);		
						
						if(taxon != null){
							DerivedUnitBase identifiedSpecimen = makeIdentifiedSpecimen(ecoFact, recordBasis);
							
							makeDetermination(state, rs, taxon, identifiedSpecimen, factId, partitioner);
													
							makeIndividualsAssociation(state, taxon, sourceRef, identifiedSpecimen);
							
							this.doIdCreatedUpdatedNotes(state, identifiedSpecimen, rs, factId, getDerivedUnitNameSpace());

							identifiedSpecimen.setCacheStrategy(new DerivedUnitFacadeCacheStrategy());
							taxaToSave.add(taxon);
						}
					}else{
						logger.warn("No taxon defined for ecology fact: " +  factId);
					}
					

				} catch (Exception e) {
					logger.warn("Exception in FactEcology: FactId " + factId + ". " + e.getMessage());
					e.printStackTrace();
				} 
                
            }
           
//            logger.warn("Specimen: " + countSpecimen + ", Descriptions: " + countDescriptions );

			logger.warn("Taxa to save: " + taxaToSave.size());
			getTaxonService().save(taxaToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}
	
	private void makeIndividualsAssociation(AlgaTerraImportState state, Taxon taxon, Reference<?> sourceRef, DerivedUnitBase<?> identifiedSpecimen){
		TaxonDescription taxonDescription = getTaxonDescription(state, taxon, sourceRef);
		IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
		Feature feature = makeFeature(identifiedSpecimen);
		indAssociation.setAssociatedSpecimenOrObservation(identifiedSpecimen);
		indAssociation.setFeature(feature);
		taxonDescription.addElement(indAssociation);	
	}
	
	private void makeDetermination(AlgaTerraImportState state, ResultSet rs, Taxon taxon, DerivedUnitBase<?> identifiedSpecimen, int factId, ResultSetPartitioner partitioner) throws SQLException {
		Date identifiedWhen = rs.getDate("IdentifiedWhen");
		Date identifiedWhenEnd = rs.getDate("IdentiedWhenEnd");
		boolean restrictedFlag = rs.getBoolean("RestrictedFlag");
		//Team FK ist immer null
		String identifiedBy = rs.getString("IdentifiedBy");
		String identificationReference = rs.getString("IdentificationReference");
		Integer refFk = nullSafeInt(rs, "IdentifidationRefFk");
		
		
		DeterminationEvent determination = DeterminationEvent.NewInstance(taxon, identifiedSpecimen);
		TimePeriod determinationPeriod = TimePeriod.NewInstance(identifiedWhen, identifiedWhenEnd);
		determination.setTimeperiod(determinationPeriod);
		determination.setPreferredFlag(! restrictedFlag);
		//TODO 
		
		TeamOrPersonBase<?> author = getAuthor(identifiedBy);
		determination.setDeterminer(author);
		if (refFk != null){
			Map<String, Reference> biblioRefMap = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
			Map<String, Reference> nomRefMap = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);
			
			Reference<?> ref = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, String.valueOf(refFk));
			determination.addReference(ref);
		}else{
			//IdentificationReference is not to be handled according to Henning
			if (StringUtils.isNotBlank(identificationReference)){
				logger.warn("IdentificationReference exists without IdentificationRefFk. FactId: "+  factId);
			}
		}
		
		
		
		//TODO
//		kind of identification, IdentificationUncertainty, IdentificationMethod, 
		
		
	}



	private DerivedUnitBase<?> makeIdentifiedSpecimen(DerivedUnitBase<?> ecoFact, String recordBasis) {
		//TODO event type
		DerivationEvent event = DerivationEvent.NewInstance();
		DerivedUnitType derivedUnitType = makeDerivedUnitType(recordBasis);
		if (derivedUnitType == null){
			logger.warn("NULL");
		}
		
		DerivedUnitBase<?> result = derivedUnitType.getNewDerivedUnitInstance();
		result.setDerivedFrom(event);
		ecoFact.addDerivationEvent(event);
		
		return result;
	}



	protected String getDerivedUnitNameSpace(){
		return FACT_ECOLOGY_NAMESPACE;
	}
	
	protected String getFieldObservationNameSpace(){
		return null;
	}




	/**
	 * @param state
	 * @param ecoFactId
	 * @param derivedUnitMap
	 * @param type 
	 * @return
	 */
	private DerivedUnitFacade getDerivedUnit(AlgaTerraImportState state, int ecoFactId, Map<String, DerivedUnit> derivedUnitMap, DerivedUnitType type) {
		String key = String.valueOf(ecoFactId);
		DerivedUnit derivedUnit = derivedUnitMap.get(key);
		DerivedUnitFacade facade;
		if (derivedUnit == null){
			facade = DerivedUnitFacade.NewInstance(type);
			derivedUnitMap.put(key, derivedUnit);
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
	
	private Feature makeFeature(SpecimenOrObservationBase unit) {
		if (unit.isInstanceOf(DerivedUnit.class)){
			return Feature.INDIVIDUALS_ASSOCIATION();
		}else if (unit.isInstanceOf(FieldObservation.class) || unit.isInstanceOf(Observation.class) ){
			return Feature.OBSERVATION();
		}else if (unit.isInstanceOf(Fossil.class) || unit.isInstanceOf(LivingBeing.class) || unit.isInstanceOf(Specimen.class )){
			return Feature.SPECIMEN();
		}
		logger.warn("No feature defined for derived unit class: " + unit.getClass().getSimpleName());
		return null;
	}


	private DerivedUnitType makeDerivedUnitType(String recordBasis) {
		DerivedUnitType result = null;
		if (StringUtils.isBlank(recordBasis)){
			result = DerivedUnitType.DerivedUnit;
		} else if (recordBasis.equalsIgnoreCase("FossileSpecimen")){
			result = DerivedUnitType.Fossil;
		}else if (recordBasis.equalsIgnoreCase("Observation")){
			result = DerivedUnitType.Observation;
		}else if (recordBasis.equalsIgnoreCase("HumanObservation")){
			result = DerivedUnitType.Observation;
		}else if (recordBasis.equalsIgnoreCase("Literature")){
			logger.warn("Literature record basis not yet supported");
			result = DerivedUnitType.DerivedUnit;
		}else if (recordBasis.equalsIgnoreCase("LivingSpecimen")){
			result = DerivedUnitType.LivingBeing;
		}else if (recordBasis.equalsIgnoreCase("LivingCulture")){
			logger.warn("LivingCulture record basis not yet supported");
			result = DerivedUnitType.DerivedUnit;
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
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> extensionFkSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "taxonId");
				handleForeignKey(rs, extensionFkSet, "extensionFk");
				handleForeignKey(rs, referenceIdSet, "IdentifidationRefFk");
			}
			
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> objectMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, objectMap);

			//derived unit map
			nameSpace = AlgaTerraFactEcologyImport.ECO_FACT_DERIVED_UNIT_NAMESPACE;
			cdmClass = DerivedUnitBase.class;
			idSet = extensionFkSet;
			Map<String, DerivedUnitBase> derivedUnitMap = (Map<String, DerivedUnitBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, derivedUnitMap);

			//nom reference map
			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> nomReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomReferenceMap);

			//biblio reference map
			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> biblioReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioReferenceMap);

			
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
		IOValidator<BerlinModelImportState> validator = new AlgaTerraSpecimenImportValidator();
		return validator.validate(state);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! ((AlgaTerraImportState)state).getAlgaTerraConfigurator().isDoEcoFacts();
	}
	
}
