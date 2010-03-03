/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms;

import static eu.etaxonomy.cdm.io.erms.ErmsTransformer.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.erms.validation.ErmsSourceUsesImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class ErmsSourceUsesImport  extends ErmsImportBase<CommonTaxonName> {

	private static final Logger logger = Logger.getLogger(ErmsSourceUsesImport.class);

	private static final String SOURCEUSE_NAMESPACE = "tu_sources";
	
	private DbImportMapping mapping; //not needed
	
	
	private int modCount = 10000;
	private static final String pluralString = "source uses";
	private String dbTableName = "tu_sources";
	private Class cdmTargetClass = null;

	public ErmsSourceUsesImport(){
		super();
	}

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strQuery = " SELECT sourceuse_id, source_id, tu_id " + " " +
						" FROM tu_sources " + 
						" ORDER BY tu_id, sourceuse_id, source_id  ";
		return strQuery;	
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getRecordQuery(eu.etaxonomy.cdm.io.erms.ErmsImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " + 
			" FROM tu_sources INNER JOIN sourceuses ON tu_sources.sourceuse_id = sourceuses.sourceuse_id" +
			" WHERE ( sourceuses.id IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}

	
	public boolean doPartition(ResultSetPartitioner partitioner, ErmsImportState state) {
		boolean success = true ;
		ErmsImportConfigurator config = state.getConfig();
		Set referencesToSave = new HashSet<TaxonBase>();
		
// 		DbImportMapping<?, ?> mapping = getMapping();
//		mapping.initialize(state, cdmTargetClass);
		
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){
				//success &= mapping.invoke(rs,referencesToSave);
				
				//read and normalize values
				int sourceUseId = rs.getInt("sourceuse_id");
				int sourceId = rs.getInt("source_id");
				String strSourceId = String.valueOf(sourceId);
				int taxonId = rs.getInt("tu_id");
				String strTaxonId = String.valueOf(taxonId);
				String strPageNr = rs.getString("pagenr");
				if (CdmUtils.isEmpty(strPageNr)){
					strPageNr = null;
				}
				ReferenceBase ref = (ReferenceBase)state.getRelatedObject(ErmsTaxonImport.NAME_NAMESPACE, strSourceId);
				
				
				//invoke methods for each sourceUse type
				if (sourceUseId == SOURCE_USE_ORIGINAL_DESCRIPTION){
					makeOriginalDescription(partitioner, state, ref, strTaxonId, strPageNr);
				}else if (sourceUseId == SOURCE_USE_BASIS_OF_RECORD){
					makeBasisOfRecord(partitioner, state, ref, strTaxonId, strPageNr);
				}else if (sourceUseId == SOURCE_USE_ADDITIONAL_SOURCE){
					makeAdditionalSource(partitioner, state, ref, strTaxonId, strPageNr);
				}else if (sourceUseId == SOURCE_USE_SOURCE_OF_SYNONYMY){
					makeSourceOfSynonymy(partitioner, state, ref, strTaxonId, strPageNr);
				}else if (sourceUseId == SOURCE_USE_REDESCRIPTION){
					makeRedescription(partitioner, state, ref, strTaxonId, strPageNr);
				}else if (sourceUseId == SOURCE_USE_NEW_COMBINATION_REFERENCE){
					makeCombinationReference(partitioner, state, ref, strTaxonId, strPageNr);
				}else if (sourceUseId == SOURCE_USE_STATUS_SOURCE){
					makeEmendation(partitioner, state, ref, strTaxonId, strPageNr);
				}else if (sourceUseId == SOURCE_USE_EMENDATION){
					
				}
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
		partitioner.startDoSave();
		getReferenceService().save(referencesToSave);
		return success;
	}


	/**
	 * @param strTaxonId 
	 * @param ref 
	 * @param state 
	 * @param partitioner 
	 * @param strPageNr 
	 * 
	 */
	private void makeOriginalDescription(ResultSetPartitioner partitioner, ErmsImportState state, ReferenceBase ref, String strTaxonId, String strPageNr) {
		TaxonNameBase taxonName = (TaxonNameBase)state.getRelatedObject(ErmsTaxonImport.NAME_NAMESPACE, strTaxonId);
		taxonName.setNomenclaturalReference(ref);
		taxonName.setNomenclaturalMicroReference(strPageNr);
	}
	
	/**
	 * @param partitioner
	 * @param state
	 * @param ref
	 * @param strTaxonId
	 * @param strPageNr
	 */
	private boolean isFirstBasisOfRecord = true; 
	private void makeBasisOfRecord(ResultSetPartitioner partitioner, ErmsImportState state, ReferenceBase ref, String strTaxonId, String strPageNr) {
		if (isFirstBasisOfRecord){
			logger.warn("Basis of record not yet implemented");
			isFirstBasisOfRecord = false;
		}
	}
	
	/**
	 * @param partitioner
	 * @param state
	 * @param ref
	 * @param strTaxonId
	 * @param strPageNr
	 */
	private void makeAdditionalSource(ResultSetPartitioner partitioner, ErmsImportState state, ReferenceBase ref, String strTaxonId, String strPageNr) {
		Feature citationFeature = Feature.CITATION();
		DescriptionElementBase element = TextData.NewInstance(citationFeature);
		DescriptionElementSource source = element.addSource(null, null, ref, strPageNr);
		TaxonBase taxonBase = (TaxonBase)state.getRelatedObject(ErmsTaxonImport.TAXON_NAMESPACE, strTaxonId);
		Taxon taxon;
		
		//if taxon base is a synonym, add the description to the accepted taxon
		if (! taxonBase.isInstanceOf(Synonym.class)){
			Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
			Set<Taxon> taxa = synonym.getAcceptedTaxa();
			if (taxa.size() != 1){
				String warning = "Synonym "+ strTaxonId + " has more or less then 1 accepted taxon";
				throw new IllegalStateException(warning);
			}
			taxon = taxa.iterator().next();
			//add synonym name as name used in source
			source.setNameUsedInSource(synonym.getName());
		}else{
			taxon = (Taxon)taxonBase;
		}
		
		//get or create description and add the element
		TaxonDescription description;
		if (taxon.getDescriptions().size() > 0){
			description = taxon.getDescriptions().iterator().next();
		}else{
			description = TaxonDescription.NewInstance(taxon);
		}
		description.addElement(element);
	}
	
	/**
	 * @param partitioner
	 * @param state
	 * @param ref
	 * @param strTaxonId
	 * @param strPageNr
	 */
	private void makeSourceOfSynonymy(ResultSetPartitioner partitioner, ErmsImportState state, ReferenceBase ref, String strTaxonId, String strPageNr) {
		TaxonBase taxonBase = (TaxonBase)state.getRelatedObject(ErmsTaxonImport.TAXON_NAMESPACE, strTaxonId);
		Synonym synonym = (Synonym)taxonBase;
		Set<SynonymRelationship> synRels = synonym.getSynonymRelations();
		if (synRels.size() != 1){
			logger.warn("Synonym has not 1 but " + synRels.size() + " relations!");
		}else{
			SynonymRelationship synRel = synRels.iterator().next();
			synRel.setCitation(ref);
			synRel.setCitationMicroReference(strPageNr);
		}
	}
	
	/**
	 * @param partitioner
	 * @param state
	 * @param ref
	 * @param strTaxonId
	 * @param strPageNr
	 */
	private boolean isFirstRediscription = true; 
	private void makeRedescription(ResultSetPartitioner partitioner, ErmsImportState state, ReferenceBase ref, String strTaxonId, String strPageNr) {
		if (isFirstRediscription){
			logger.warn("Rediscription not yet implemented");
			isFirstRediscription = false;
		}
	}

	/**
	 * @param partitioner
	 * @param state
	 * @param ref
	 * @param strTaxonId
	 * @param strPageNr
	 */
	private void makeCombinationReference(ResultSetPartitioner partitioner, ErmsImportState state, ReferenceBase ref, String strTaxonId, String strPageNr) {
		// Kopie von Orig. Comb.
		//TODO ist das wirklich der richtige Name, oder muss ein verknüpfter Name verwendet werden
		TaxonNameBase taxonName = (TaxonNameBase)state.getRelatedObject(ErmsTaxonImport.NAME_NAMESPACE, strTaxonId);
		taxonName.setNomenclaturalReference(ref);
		taxonName.setNomenclaturalMicroReference(strPageNr);
	}


	/**
	 * @param partitioner
	 * @param state
	 * @param ref
	 * @param strTaxonId
	 * @param strPageNr
	 */
	private boolean isFirstEmendation = true; 
	private void makeEmendation(ResultSetPartitioner partitioner, ErmsImportState state, ReferenceBase ref, String strTaxonId, String strPageNr) {
		if (isFirstRediscription){
			logger.warn("Emmendation not yet implemented");
			isFirstRediscription = false;
		}
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet, eu.etaxonomy.cdm.io.common.ImportStateBase)
	 */
	public CommonTaxonName createObject(ResultSet rs, ErmsImportState state)
			throws SQLException {
		return null;  //not needed
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
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "tu_id");
				handleForeignKey(rs, nameIdSet, "tu_id");
				handleForeignKey(rs, nameIdSet, "sources_id");
			}
			
			//name map
			nameSpace = ErmsTaxonImport.NAME_NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nameMap);
			
			//taxon map
			nameSpace = ErmsTaxonImport.TAXON_NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);
			
			//reference map
			nameSpace = ErmsReferenceImport.REFERENCE_NAMESPACE;
			cdmClass = ReferenceBase.class;
			idSet = referenceIdSet;
			Map<String, ReferenceBase> referenceMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(ReferenceBase.class, idSet, nameSpace);
			result.put(nameSpace, referenceMap);
	
				
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;

	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		IOValidator<ErmsImportState> validator = new ErmsSourceUsesImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		boolean result = state.getConfig().getDoReferences() != IImportConfigurator.DO_REFERENCES.ALL;
		result &= state.getConfig().isDoTaxa();
		return state.getConfig().getDoReferences() != IImportConfigurator.DO_REFERENCES.ALL;
	}

}
