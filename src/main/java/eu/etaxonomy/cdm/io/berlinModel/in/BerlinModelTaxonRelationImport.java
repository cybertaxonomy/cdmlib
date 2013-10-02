/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_HETEROTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_HOMOTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_INCLUDED_IN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_MISAPPLIED_NAME_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PARTIAL_SYN_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_PROPARTE_SYN_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.TAX_REL_IS_SYNONYM_OF;

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

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelTaxonRelationImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonRelationImport  extends BerlinModelImportBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonRelationImport.class);

	public static final String TREE_NAMESPACE = "PTRefFk";
	
	private static int modCount = 30000;
	private static final String pluralString = "taxon relations";
	private static final String dbTableName = "RelPTaxon";

	
	public BerlinModelTaxonRelationImport(){
		super(dbTableName, pluralString);
	}

	/**
	 * Creates a classification for each PTaxon reference which belongs to a taxon that is included at least in one
	 * <i>taxonomically included</i> relationship
	 * @param state
	 * @return
	 * @throws SQLException
	 */
	private void makeClassifications(BerlinModelImportState state) throws SQLException{
		logger.info("start make classification ...");
		
		Set<String> idSet = getTreeReferenceIdSet(state);
		
		//nom reference map
		String nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
		Class cdmClass = Reference.class;
//			idSet = new HashSet<String>();
		Map<String, Reference> nomRefMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
		
		//biblio reference map
		nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
		cdmClass = Reference.class;
//		idSet = new HashSet<String>();
		Map<String, Reference> biblioRefMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
		
		String treeName = "Classification - No Name";
		
		ResultSet rs = state.getConfig().getSource().getResultSet(getClassificationQuery(state)) ;
		int i = 0;
		//for each reference
		try {
			//TODO handle case useSingleClassification = true && sourceSecId = null, which returns no record
			while (rs.next()){
				
				try {
					if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RelPTaxa handled: " + (i-1));}
					
					Integer ptRefFkInt = nullSafeInt(rs,"PTRefFk");
					String ptRefFk= String.valueOf(ptRefFkInt);
					Reference<?> ref = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, ptRefFk);
					
					String refCache = rs.getString("RefCache");
					if (StringUtils.isNotBlank(refCache)){
						treeName = refCache;
					}
					if (ref != null && StringUtils.isNotBlank(ref.getTitleCache())){
						treeName = ref.getTitleCache();
					}
					Classification tree = Classification.NewInstance(treeName);
					tree.setReference(ref);
					if (i == 1 && state.getConfig().getClassificationUuid() != null){
						tree.setUuid(state.getConfig().getClassificationUuid());
					}
					IdentifiableSource identifiableSource = IdentifiableSource.NewDataImportInstance(ptRefFk, TREE_NAMESPACE);
					tree.addSource(identifiableSource);
					
					getClassificationService().save(tree);
					state.putClassificationUuidInt(ptRefFkInt, tree);
				} catch (Exception e) {
					logger.error("Error in BerlinModleTaxonRelationImport.makeClassifications: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			logger.error("Error in BerlinModleTaxonRelationImport.makeClassifications: " + e.getMessage());
			throw e;
		}
		logger.info("end make classification ...");

		return;
	}

	/**
	 * @return
	 * @throws SQLException 
	 */
	private Set<String> getTreeReferenceIdSet(BerlinModelImportState state) throws SQLException {
		Source source = state.getConfig().getSource();
		Set<String> result = new HashSet<String>();
		ResultSet rs = source.getResultSet(getClassificationQuery(state)) ;
		while (rs.next()){
			Object id = rs.getObject("PTRefFk");
			result.add(String.valueOf(id));
		}
		return result;
	}

	/**
	 * @return
	 */
	private String getClassificationQuery(BerlinModelImportState state) {
		boolean includeAllClassifications = state.getConfig().isIncludeAllNonMisappliedRelatedClassifications();
		String strQuerySelect = "SELECT PTaxon.PTRefFk, r.RefCache ";  
		String strQueryFrom = " FROM RelPTaxon " + 
							" INNER JOIN PTaxon AS PTaxon ON RelPTaxon.PTNameFk2 = PTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = PTaxon.PTRefFk " +
							" INNER JOIN Reference r ON PTaxon.PTRefFk = r.RefId "; 
		String strQueryWhere = " WHERE (RelPTaxon.RelQualifierFk = 1) "; 
		if (includeAllClassifications){
			strQueryWhere = " WHERE (RelPTaxon.RelQualifierFk <> 3) ";
		}else{
			if (state.getConfig().isUseSingleClassification()){
				if (state.getConfig().getSourceSecId()!= null){
					strQueryWhere += " AND PTaxon.PTRefFk = " + state.getConfig().getSourceSecId() +  " ";
				}else{
					strQueryWhere += " AND (1=0) ";
				}
			}
		}
		
		String strQueryGroupBy = " GROUP BY PTaxon.PTRefFk, r.RefCache ";
		String strQuery = strQuerySelect + " " + strQueryFrom + " " + strQueryWhere + " " + strQueryGroupBy;
		
		
		if (includeAllClassifications){
			//add otherdirection
			strQuerySelect = "SELECT PTaxon.PTRefFk, r.RefCache ";  
			strQueryFrom = " FROM RelPTaxon rel " + 
								" INNER JOIN PTaxon AS PTaxon ON rel.PTNameFk1 = PTaxon.PTNameFk AND rel.PTRefFk1 = PTaxon.PTRefFk " +
								" INNER JOIN Reference r ON PTaxon.PTRefFk = r.RefId "; 
			strQueryWhere =" WHERE (rel.RelQualifierFk <> 3) ";
			String strAllQuery =  strQuerySelect + " " + strQueryFrom + " " + strQueryWhere + " " + strQueryGroupBy;
			strQuery = strQuery + " UNION " + strAllQuery;
		}
		
		
		
		boolean includeFlatClassifications = state.getConfig().isIncludeFlatClassifications();
		//concepts with 
		if (includeFlatClassifications){
			String strFlatQuery = 
					" SELECT pt.PTRefFk AS secRefFk, r.RefCache AS secRef " +
					" FROM PTaxon AS pt LEFT OUTER JOIN " + 
					          " Reference r ON pt.PTRefFk = r.RefId LEFT OUTER JOIN " +
					          " RelPTaxon rel1 ON pt.PTNameFk = rel1.PTNameFk2 AND pt.PTRefFk = rel1.PTRefFk2 LEFT OUTER JOIN " +
					          " RelPTaxon AS rel2 ON pt.PTNameFk = rel2.PTNameFk1 AND pt.PTRefFk = rel2.PTRefFk1 " + 
					" WHERE (rel2.RelQualifierFk IS NULL) AND (rel1.RelQualifierFk IS NULL) " + 
					" GROUP BY pt.PTRefFk, r.RefCache "
					;
			
			strQuery = strQuery + " UNION " + strFlatQuery;
		}

		
		
		if (state.getConfig().getClassificationQuery() != null){
			strQuery = state.getConfig().getClassificationQuery();
		}
		return strQuery;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strQuery = 
			" SELECT RelPTaxon.*, FromTaxon.RIdentifier as taxon1Id, ToTaxon.RIdentifier as taxon2Id, ToTaxon.PTRefFk as treeRefFk, FromTaxon.PTRefFk as fromRefFk, q.is_concept_relation " + 
			" FROM PTaxon as FromTaxon " +
              	" INNER JOIN RelPTaxon ON FromTaxon.PTNameFk = RelPTaxon.PTNameFk1 AND FromTaxon.PTRefFk = RelPTaxon.PTRefFk1 " +
              	" INNER JOIN PTaxon AS ToTaxon ON RelPTaxon.PTNameFk2 = ToTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = ToTaxon.PTRefFk " +
              	" INNER JOIN RelPTQualifier q ON q.RelPTQualifierId = RelPTaxon.RelQualifierFk " + 
            " WHERE RelPTaxon.RelPTaxonId IN ("+ID_LIST_TOKEN+") ";
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true ;
		BerlinModelImportConfigurator config = state.getConfig();
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
		Map<Integer, Classification> classificationMap = new HashMap<Integer, Classification>();
		Map<String, Reference> biblioRefMap = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, Reference> nomRefMap = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);

		ResultSet rs = partitioner.getResultSet();
			
		try{
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RelPTaxa handled: " + (i-1));}
					
				int relPTaxonId = rs.getInt("RelPTaxonId");
				Integer taxon1Id = nullSafeInt(rs, "taxon1Id");
				Integer taxon2Id = nullSafeInt(rs, "taxon2Id");
				try {
					Integer relRefFk = nullSafeInt(rs,"relRefFk");
					int treeRefFk = rs.getInt("treeRefFk");
					int fromRefFk = rs.getInt("fromRefFk");
					
					int relQualifierFk = rs.getInt("relQualifierFk");
					String notes = rs.getString("notes");
					boolean isConceptRelationship = rs.getBoolean("is_concept_relation");
					
					TaxonBase taxon1 = taxonMap.get(String.valueOf(taxon1Id));
					TaxonBase taxon2 = taxonMap.get(String.valueOf(taxon2Id));
					
					String refFk = String.valueOf(relRefFk);
					Reference citation = getReferenceOnlyFromMaps(biblioRefMap,	nomRefMap, refFk);
					
					String microcitation = null; //does not exist in RelPTaxon

					if (taxon2 != null && taxon1 != null){
						if (!(taxon2 instanceof Taxon)){
							logger.error("ToTaxon (ID = " + taxon2.getId()+ ", RIdentifier = " + taxon2Id + ") can't be casted to Taxon. RelPTaxon: " + relPTaxonId );
							success = false;
							continue;
						}
						AnnotatableEntity taxonRelationship = null;
						Taxon toTaxon = (Taxon)taxon2;
						if (isTaxonRelationship(relQualifierFk)){
							if (!(taxon1 instanceof Taxon)){
								logger.error("TaxonBase (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") for TaxonRelation ("+relPTaxonId+") can't be casted to Taxon");
								success = false;
								continue;
							}
							Taxon fromTaxon = (Taxon)taxon1;
							if (relQualifierFk == TAX_REL_IS_INCLUDED_IN){
								taxonRelationship = makeTaxonomicallyIncluded(state, classificationMap, treeRefFk, fromTaxon, toTaxon, citation, microcitation);
							}else if (relQualifierFk == TAX_REL_IS_MISAPPLIED_NAME_OF){
								 taxonRelationship = toTaxon.addMisappliedName(fromTaxon, citation, microcitation);
							}else{
								handleAllRelatedTaxa(state, fromTaxon, classificationMap, fromRefFk);
								handleAllRelatedTaxa(state, toTaxon, classificationMap, treeRefFk);
								logger.warn("Unhandled taxon relationship: RelId:" + relPTaxonId + "; QualifierId: " + relQualifierFk);
							}
						}else if (isSynonymRelationship(relQualifierFk)){
							if (!(taxon1 instanceof Synonym)){
								logger.warn("Validated: Taxon (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Synonym");
								success = false;
								continue;
							}
							handleAllRelatedTaxa(state, toTaxon, classificationMap, treeRefFk);
							Synonym synonym = (Synonym)taxon1;
							SynonymRelationship synRel = getSynRel(relQualifierFk, toTaxon, synonym, citation, microcitation);
							taxonRelationship = synRel;
							
							if (relQualifierFk == TAX_REL_IS_SYNONYM_OF || 
									relQualifierFk == TAX_REL_IS_HOMOTYPIC_SYNONYM_OF ||
									relQualifierFk == TAX_REL_IS_HETEROTYPIC_SYNONYM_OF){
								addProParteAndPartial(synRel, synonym, config);
							}else if (relQualifierFk == TAX_REL_IS_PROPARTE_SYN_OF ||
									relQualifierFk == TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF ||
									relQualifierFk == TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF ){
									synRel.setProParte(true);
							}else if(relQualifierFk == TAX_REL_IS_PARTIAL_SYN_OF || 
									relQualifierFk == TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF ||
									relQualifierFk == TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF ){
									synRel.setPartial(true);
							}else{
								success = false;
								logger.warn("Proparte/Partial not yet implemented for TaxonRelationShipType " + relQualifierFk);
							}
						}else if (isConceptRelationship){
							ResultWrapper<Boolean> isInverse = new ResultWrapper<Boolean>();
							try {
								TaxonRelationshipType relType = BerlinModelTransformer.taxonRelId2TaxonRelType(relQualifierFk, isInverse);	
								if (! (taxon1 instanceof Taxon)){
									success = false;
									logger.error("TaxonBase (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Taxon");
								}else{
									Taxon fromTaxon = (Taxon)taxon1;
									taxonRelationship = fromTaxon.addTaxonRelation(toTaxon, relType, citation, microcitation);
									handleAllRelatedTaxa(state, toTaxon, classificationMap, treeRefFk);
									handleAllRelatedTaxa(state, fromTaxon, classificationMap, fromRefFk);
								}
							} catch (UnknownCdmTypeException e) {
								//TODO other relationships
								logger.warn("TaxonRelationShipType " + relQualifierFk + " (conceptRelationship) not yet implemented");
								success = false;
							}
						}else {
							//TODO
							logger.warn("TaxonRelationShipType " + relQualifierFk + " not yet implemented: RelPTaxonId = " + relPTaxonId );
							success = false;
						}
						
						doNotes(taxonRelationship, notes);
						taxaToSave.add(taxon2);
						
						//TODO
						//etc.
					}else{
						if (taxon2 != null && taxon1 == null){
							logger.warn("First taxon ("+taxon1Id+") for RelPTaxon " + relPTaxonId + " does not exist in store. RelType: " + relQualifierFk);
						}else if (taxon2 == null && taxon1 != null){
							logger.warn("Second taxon ("+taxon2Id +") for RelPTaxon " + relPTaxonId + " does not exist in store. RelType: " + relQualifierFk);
						}else{
							logger.warn("Both taxa ("+taxon1Id+","+taxon2Id +") for RelPTaxon " + relPTaxonId + " do not exist in store. RelType: " + relQualifierFk);
						}
						
						success = false;
					}
				} catch (Exception e) {
					logger.error("Exception occurred when trying to handle taxon relationship " + relPTaxonId + " (" + taxon1Id + ","+ taxon2Id + "): " + e.getMessage());
//					e.printStackTrace();
				}
			}
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
		logger.info("Taxa to save: " + taxaToSave.size());
		partitioner.startDoSave();
		getTaxonService().save(taxaToSave);
		classificationMap = null;
		taxaToSave = null;
			
			return success;
	}
	

	private void handleAllRelatedTaxa(BerlinModelImportState state, Taxon taxon, Map<Integer, Classification> classificationMap, Integer secRefFk) {
		if (taxon.getTaxonNodes().size() > 0){
			return;
		}else{
			Classification classification = getClassificationTree(state, classificationMap, secRefFk);
			classification.addChildTaxon(taxon, null, null);
		}
		

		
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected void doInvoke(BerlinModelImportState state){				
		try {
			makeClassifications(state);
			super.doInvoke(state);
			makeFlatClassificationTaxa(state);
			return;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	private void makeFlatClassificationTaxa(BerlinModelImportState state) {
		//Note: this part still does not use partitions
		logger.info("Flat classifications start");
		TransactionStatus txStatus = startTransaction();
		if (! state.getConfig().isIncludeFlatClassifications()){
			return;
		}
		String sql = " SELECT pt.PTRefFk AS secRefFk, pt.RIdentifier " +
						" FROM PTaxon AS pt " +
							" LEFT OUTER JOIN RelPTaxon ON pt.PTNameFk = RelPTaxon.PTNameFk2 AND pt.PTRefFk = RelPTaxon.PTRefFk2 " +
							"  LEFT OUTER JOIN RelPTaxon AS RelPTaxon_1 ON pt.PTNameFk = RelPTaxon_1.PTNameFk1 AND pt.PTRefFk = RelPTaxon_1.PTRefFk1 " +
						" WHERE (RelPTaxon_1.RelQualifierFk IS NULL) AND (dbo.RelPTaxon.RelQualifierFk IS NULL) " +
						" ORDER BY pt.PTRefFk "	;
		ResultSet rs = state.getConfig().getSource().getResultSet(sql);
		Map<Object, Map<String, ? extends CdmBase>> maps = getRelatedObjectsForFlatPartition(rs);
		
		Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>) maps.get(BerlinModelTaxonImport.NAMESPACE);
		Map<Integer, Classification> classificationMap = new HashMap<Integer, Classification>();
				
		rs = state.getConfig().getSource().getResultSet(sql);
		try {
			while (rs.next()){
				Integer treeRefFk = rs.getInt("secRefFk");
				String taxonId = rs.getString("RIdentifier");
				Classification classification = getClassificationTree(state, classificationMap, treeRefFk);
				TaxonBase<?> taxon = taxonMap.get(taxonId);
				if (taxon.isInstanceOf(Taxon.class)){
					classification.addChildTaxon(CdmBase.deproxy(taxon, Taxon.class), null, null);
				}else{
					String message = "TaxonBase for taxon is not of class Taxon but %s (RIdentifier %s)";
					logger.warn(String.format(message, taxon.getClass(), taxonId));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		commitTransaction(txStatus);
		logger.info("Flat classifications end");
		
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		if (state.getConfig().getRelTaxaIdQuery() != null){
			return state.getConfig().getRelTaxaIdQuery();
		}else{
			return super.getIdQuery(state);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition( ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
//			Set<String> classificationIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "taxon1Id");
				handleForeignKey(rs, taxonIdSet, "taxon2Id");
//				handleForeignKey(rs, classificationIdSet, "treeRefFk");
				handleForeignKey(rs, referenceIdSet, "RelRefFk");
	}
	
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);

//			//tree map
//			nameSpace = "Classification";
//			cdmClass = Classification.class;
//			idSet = classificationIdSet;
//			Map<String, Classification> treeMap = (Map<String, Classification>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
//			result.put(cdmClass, treeMap);
//			Set<UUID> treeUuidSet = state
//			getClassificationService().find(uuidSet);
//			
			
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
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForFlatPartition( ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
//			Set<String> classificationIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "RIdentifier");
//				handleForeignKey(rs, classificationIdSet, "treeRefFk");
	}
	
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);

//			//tree map
//			nameSpace = "Classification";
//			cdmClass = Classification.class;
//			idSet = classificationIdSet;
//			Map<String, Classification> treeMap = (Map<String, Classification>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
//			result.put(cdmClass, treeMap);
//			Set<UUID> treeUuidSet = state
//			getClassificationService().find(uuidSet);
//			

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}


	private SynonymRelationship getSynRel (int relQualifierFk, Taxon toTaxon, Synonym synonym, Reference citation, String microcitation){
		SynonymRelationship result;
		if (relQualifierFk == TAX_REL_IS_HOMOTYPIC_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF){
			result = toTaxon.addHomotypicSynonym(synonym, citation, microcitation);
		}else if (relQualifierFk == TAX_REL_IS_HETEROTYPIC_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF){
			result = toTaxon.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), citation, microcitation);
		}else if (relQualifierFk == TAX_REL_IS_SYNONYM_OF ||
				relQualifierFk == TAX_REL_IS_PROPARTE_SYN_OF ||
				relQualifierFk == TAX_REL_IS_PARTIAL_SYN_OF){
			result = toTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(), citation, microcitation);
		}else{
			logger.warn("SynonymyRelationShipType could not be defined for relQualifierFk " + relQualifierFk + ". 'Unknown'-Type taken instead.");
			result = toTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(), citation, microcitation);
		}
		return result;

	}
	
	private  boolean isSynonymRelationship(int relQualifierFk){
		if (relQualifierFk == TAX_REL_IS_SYNONYM_OF || 
			relQualifierFk == TAX_REL_IS_HOMOTYPIC_SYNONYM_OF || 
			relQualifierFk == TAX_REL_IS_HETEROTYPIC_SYNONYM_OF ||
			relQualifierFk == TAX_REL_IS_PROPARTE_SYN_OF || 
			relQualifierFk == TAX_REL_IS_PARTIAL_SYN_OF ||
			relQualifierFk == TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF ||
			relQualifierFk == TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF ||
			relQualifierFk == TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF ||
			relQualifierFk == TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF
		){
			return true;
		}else{
			return false;
		}
	}
	
	private  boolean isTaxonRelationship(int relQualifierFk){
		if (relQualifierFk == TAX_REL_IS_INCLUDED_IN || 
		relQualifierFk == TAX_REL_IS_MISAPPLIED_NAME_OF){
			return true;
		}else{
			return false;
		}
	}
	
	private void addProParteAndPartial(SynonymRelationship synRel, Synonym synonym, BerlinModelImportConfigurator bmiConfig){
		if (bmiConfig.isPartialSynonym(synonym)){
			synRel.setPartial(true);
		}
		if (bmiConfig.isProParteSynonym(synonym)){
			synRel.setProParte(true);
		}
	}
	
	private TaxonNode makeTaxonomicallyIncluded(BerlinModelImportState state, Map<Integer, Classification> classificationMap, int treeRefFk, Taxon child, Taxon parent, Reference citation, String microCitation){
		Classification tree = getClassificationTree(state, classificationMap, treeRefFk);
		return tree.addParentChild(parent, child, citation, microCitation);
	}

	private Classification getClassificationTree(BerlinModelImportState state, Map<Integer, Classification> classificationMap, int treeRefFk) {
		if (state.getConfig().isUseSingleClassification()){
			if (state.getConfig().getSourceSecId() != null){
				treeRefFk = (Integer)state.getConfig().getSourceSecId();	
			}else{
				treeRefFk = 1;
			}
			
		}
		Classification tree = classificationMap.get(treeRefFk);
		if (tree == null){
			UUID treeUuid = state.getTreeUuidByIntTreeKey(treeRefFk);
			tree = getClassificationService().find(treeUuid);
			classificationMap.put(treeRefFk, tree);
		}
		if (tree == null){
			throw new IllegalStateException("Tree for ToTaxon reference " + treeRefFk + " does not exist.");
		}
		return tree;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelTaxonRelationImportValidator();
		return validator.validate(state);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoRelTaxa();
	}

	
}
