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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
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
		super();
	}

	/**
	 * Creates a classification for each PTaxon reference which belongs to a taxon that is included at least in one
	 * <i>taxonomically included</i> relationship
	 * @param state
	 * @return
	 * @throws SQLException
	 */
	private boolean makeClassifications(BerlinModelImportState state) throws SQLException{
		logger.info("start make classification ...");
		Source source = state.getConfig().getSource();

		Set<String> idSet = getTreeReferenceIdSet(source);
		
		//nom reference map
		String nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
		Class cdmClass = Reference.class;
		idSet = new HashSet<String>();
		Map<String, Reference> nomRefMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
		
		//biblio reference map
		nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
		cdmClass = Reference.class;
//		idSet = new HashSet<String>();
		Map<String, Reference> biblioRefMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
		
		ResultSet rs = source.getResultSet(getClassificationQuery()) ;
		int i = 0;
		//for each reference
		try {
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RelPTaxa handled: " + (i-1));}
				
				Object ptRefFkObj = rs.getObject("PTRefFk");
				String ptRefFk= String.valueOf(ptRefFkObj);
				Reference<?> ref = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, ptRefFk);
				
				rs.getString("RefCache");
				String treeName = "Classification - No Name";
				String refCache = rs.getString("RefCache");
				if (CdmUtils.isNotEmpty(refCache)){
					treeName = refCache;
				}
				if (ref != null && CdmUtils.isNotEmpty(ref.getTitleCache())){
					treeName = ref.getTitleCache();
				}
				Classification tree = Classification.NewInstance(treeName);
				tree.setReference(ref);
				if (i == 1 && state.getConfig().getClassificationUuid() != null){
					tree.setUuid(state.getConfig().getClassificationUuid());
				}
				IdentifiableSource identifiableSource = IdentifiableSource.NewInstance(ptRefFk, TREE_NAMESPACE);
				tree.addSource(identifiableSource);
				
				getClassificationService().save(tree);
				state.putClassificationUuidInt((Integer)ptRefFkObj, tree);
			}
		} catch (SQLException e) {
			logger.error("Error in BerlinModleTaxonRelationImport.makeClassifications: " + e.getMessage());
			throw e;
		}
		logger.info("end make classification ...");

		return true;
	}

	/**
	 * @return
	 * @throws SQLException 
	 */
	private Set<String> getTreeReferenceIdSet(Source source) throws SQLException {
		Set<String> result = new HashSet<String>();
		ResultSet rs = source.getResultSet(getClassificationQuery()) ;
		while (rs.next()){
			Object id = rs.getObject("PTRefFk");
			result.add(String.valueOf(id));
		}
		return result;
	}

	/**
	 * @return
	 */
	private String getClassificationQuery() {
		String strQuery = "SELECT PTaxon.PTRefFk, Reference.RefCache " + 
						" FROM RelPTaxon INNER JOIN " + 
							" PTaxon AS PTaxon ON RelPTaxon.PTNameFk2 = PTaxon.PTNameFk AND RelPTaxon.PTRefFk2 = PTaxon.PTRefFk INNER JOIN " +
							" Reference ON PTaxon.PTRefFk = Reference.RefId " + 
						" WHERE (RelPTaxon.RelQualifierFk = 1) " + 
						" GROUP BY PTaxon.PTRefFk, Reference.RefCache ";
		return strQuery;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strQuery = 
			" SELECT RelPTaxon.*, FromTaxon.RIdentifier as taxon1Id, ToTaxon.RIdentifier as taxon2Id, ToTaxon.PTRefFk as treeRefFk, q.is_concept_relation " + 
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
				int taxon1Id = rs.getInt("taxon1Id");
				int taxon2Id = rs.getInt("taxon2Id");
				Object relRefFkObj = rs.getObject("relRefFk");
				int treeRefFk = rs.getInt("treeRefFk");
				int relQualifierFk = rs.getInt("relQualifierFk");
				String notes = rs.getString("notes");
				boolean isConceptRelationship = rs.getBoolean("is_concept_relation");
				
				TaxonBase taxon1 = taxonMap.get(String.valueOf(taxon1Id));
				TaxonBase taxon2 = taxonMap.get(String.valueOf(taxon2Id));
				
				String refFk = String.valueOf(relRefFkObj);
				Reference citation = getReferenceOnlyFromMaps(biblioRefMap,	nomRefMap, refFk);
				
				String microcitation = null; //does not exist in RelPTaxon
	
				if (taxon2 != null && taxon1 != null){
					if (!(taxon2 instanceof Taxon)){
						logger.error("TaxonBase (ID = " + taxon2.getId()+ ", RIdentifier = " + taxon2Id + ") can't be casted to Taxon");
						success = false;
						continue;
					}
					AnnotatableEntity taxonRelationship = null;
					Taxon toTaxon = (Taxon)taxon2;
					if (isTaxonRelationship(relQualifierFk)){
						if (!(taxon1 instanceof Taxon)){
							logger.error("TaxonBase (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Taxon");
							success = false;
							continue;
						}
						Taxon fromTaxon = (Taxon)taxon1;
						if (relQualifierFk == TAX_REL_IS_INCLUDED_IN){
							taxonRelationship = makeTaxonomicallyIncluded(state, classificationMap, treeRefFk, fromTaxon, toTaxon, citation, microcitation);
						}else if (relQualifierFk == TAX_REL_IS_MISAPPLIED_NAME_OF){
							 taxonRelationship = toTaxon.addMisappliedName(fromTaxon, citation, microcitation);
						}
					}else if (isSynonymRelationship(relQualifierFk)){
						if (!(taxon1 instanceof Synonym)){
							logger.error("Taxon (ID = " + taxon1.getId()+ ", RIdentifier = " + taxon1Id + ") can't be casted to Synonym");
							success = false;
							continue;
						}
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
							}
						} catch (UnknownCdmTypeException e) {
							//TODO other relationships
							logger.warn("TaxonRelationShipType " + relQualifierFk + " (conceptRelationship) not yet implemented");
							success = false;
						}
					}else {
						//TODO
						logger.warn("TaxonRelationShipType " + relQualifierFk + " not yet implemented");
						success = false;
					}
					
					doNotes(taxonRelationship, notes);
					taxaToSave.add(taxon2);
					
					//TODO
					//etc.
				}else{
					//TODO
					logger.warn("Taxa for RelPTaxon " + relPTaxonId + " do not exist in store");
					success = false;
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
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(BerlinModelImportState state){				
		try {
			boolean success = true;
			success &= makeClassifications(state);
			success &= super.doInvoke(state);
			return success;
		} catch (SQLException e) {
			throw new RuntimeException(e);
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
		Classification tree = classificationMap.get(treeRefFk);
		if (tree == null){
			UUID treeUuid = state.getTreeUuidByIntTreeKey(treeRefFk);
			tree = getClassificationService().getClassificationByUuid(treeUuid);
			classificationMap.put(treeRefFk, tree);
		}
		if (tree == null){
			throw new IllegalStateException("Tree for ToTaxon reference does not exist.");
		}
		return tree.addParentChild(parent, child, citation, microCitation);
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
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoRelTaxa();
	}

	
}
