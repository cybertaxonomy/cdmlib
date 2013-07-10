// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * The export class for relations between {@link eu.etaxonomy.cdm.model.taxon.TaxonBase TaxonBases}.<p>
 * Inserts into DataWarehouse database table <code>RelTaxon</code>.
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
public class PesiRelTaxonExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiRelTaxonExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = RelationshipBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "RelTaxon";
	private static final String pluralString = "Relationships";
	private static PreparedStatement synonymsStmt;
	
	private HashMap<Rank, Rank> rank2endRankMap = new HashMap<Rank, Rank>();
	private List<Rank> rankList = new ArrayList<Rank>();
	private PesiExportMapping mapping;
	private int count = 0;
	
	public PesiRelTaxonExport() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(PesiExportState state) {
		boolean result = true;
		return result;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected void doInvoke(PesiExportState state) {
		try {
			logger.info("*** Started Making " + pluralString + " ...");
	
			Connection connection = state.getConfig().getDestination().getConnection();
			String synonymsSql = "UPDATE Taxon SET KingdomFk = ?, RankFk = ?, RankCache = ? WHERE TaxonId = ?"; 
			synonymsStmt = connection.prepareStatement(synonymsSql);

			// Stores whether this invoke was successful or not.
			boolean success = true;

			// PESI: Clear the database table RelTaxon.
			//doDelete(state); -> done by stored procedure
			
			// Get specific mappings: (CDM) Relationship -> (PESI) RelTaxon
			mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);
			
			//Export taxon relations
			success &= doPhase01(state, mapping);

			
			// Export name relations
			success &= doPhase02(state, mapping);
			
			if (! success){
				state.setUnsuccessfull();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
			return;
		}
	}
	
	
	private boolean doPhase01(PesiExportState state, PesiExportMapping mapping2) throws SQLException {
		logger.info("PHASE 1: Taxon Relationships ...");
		boolean success = true;
		
		int limit = state.getConfig().getLimitSave();
		// Start transaction
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
		
		List<RelationshipBase> list;
		
		//taxon relations
		int partitionCount = 0;
		int totalCount = 0;
		while ((list = getNextTaxonRelationshipPartition( limit, partitionCount++, null)) != null ) {
			totalCount = totalCount + list.size();
			logger.info("Read " + list.size() + " PESI relations. Limit: " + limit + ". Total: " + totalCount );
//			if (list.size() > 0){
//				logger.warn("First relation type is " + list.get(0).getType().getTitleCache());
//			}
			for (RelationshipBase rel : list){
				try {
					mapping.invoke(rel);
				} catch (Exception e) {
					logger.error(e.getMessage() + ". Relationship: " +  rel.getUuid());
					e.printStackTrace();
				}
			}
			
			commitTransaction(txStatus);
			txStatus = startTransaction();
		}
		list = null;
		commitTransaction(txStatus);
		return success;
	}
	
	private boolean doPhase02(PesiExportState state, PesiExportMapping mapping2) throws SQLException {
		logger.info("PHASE 2: Name Relationships ...");
		boolean success = true;
		
		int limit = state.getConfig().getLimitSave();
		// Start transaction
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
		
		List<RelationshipBase> list;
		
		//name relations
		int partitionCount = 0;
		while ((list = getNextNameRelationshipPartition(null, limit, partitionCount++, null)) != null   ) {
			for (RelationshipBase rel : list){
				try {
					TaxonNameBase<?,?> name1;
					TaxonNameBase<?,?> name2;
					if (rel.isInstanceOf(HybridRelationship.class)){
						HybridRelationship hybridRel = CdmBase.deproxy(rel, HybridRelationship.class);
						name1 = hybridRel.getParentName();
						name2 = hybridRel.getHybridName();
						hybridRel = null;
					}else if (rel.isInstanceOf(NameRelationship.class)){
						NameRelationship nameRel = CdmBase.deproxy(rel, NameRelationship.class);
						name1 = nameRel.getFromName();
						name2 = nameRel.getToName();
						nameRel = null;
					}else{
						logger.warn ("Only hybrid- and name-relationships alowed here");
						continue;
					}
					List<IdentifiableEntity> fromList = new ArrayList<IdentifiableEntity>();
					List<IdentifiableEntity> toList = new ArrayList<IdentifiableEntity>();
					makeList(name1, fromList);
					makeList(name2, toList);
					
					for (IdentifiableEntity fromEntity : fromList){
						for (IdentifiableEntity toEntity : toList){
							//TODO set entities to state
							state.setCurrentFromObject(fromEntity);
							state.setCurrentToObject(toEntity);
							mapping.invoke(rel);
						}
					}
					fromList = null;
					toList = null;
					name1 = null;
					name2 = null;
					rel = null;
					
					
				} catch (Exception e) {
					logger.error(e.getMessage() + ". Relationship: " +  rel.getUuid());
					e.printStackTrace();
				}
			}
			commitTransaction(txStatus);
			txStatus = startTransaction();
		}
		commitTransaction(txStatus);
		list = null;
		logger.info("End PHASE 2: Name Relationships ...");
		state.setCurrentFromObject(null);
		state.setCurrentToObject(null);	
		return success;
	}

	private void makeList(TaxonNameBase<?, ?> name, List<IdentifiableEntity> list) {
		if (! hasPesiTaxon(name)){
			list.add(name);
		}else{
			for (TaxonBase taxon:  getPesiTaxa(name)){
				list.add(taxon);
			}
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	protected void doInvoke_Old(PesiExportState state) {
		try {
			logger.info("*** Started Making " + pluralString + " ...");
	
			Connection connection = state.getConfig().getDestination().getConnection();
			String synonymsSql = "UPDATE Taxon SET KingdomFk = ?, RankFk = ?, RankCache = ? WHERE TaxonId = ?"; 
			synonymsStmt = connection.prepareStatement(synonymsSql);

			// Stores whether this invoke was successful or not.
			boolean success = true;

			// PESI: Clear the database table RelTaxon.
			doDelete(state);
	
			// Get specific mappings: (CDM) Relationship -> (PESI) RelTaxon
			mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			TransactionStatus txStatus = null;
			List<Classification> classificationList = null;
			
			// Specify starting ranks for tree traversing
			rankList.add(Rank.KINGDOM());
			rankList.add(Rank.GENUS());

			// Specify where to stop traversing (value) when starting at a specific Rank (key)
			rank2endRankMap.put(Rank.GENUS(), null); // Since NULL does not match an existing Rank, traverse all the way down to the leaves
			rank2endRankMap.put(Rank.KINGDOM(), Rank.GENUS()); // excludes rank genus
			
			// Retrieve list of classifications
			txStatus = startTransaction(true);
			logger.info("Started transaction. Fetching all classifications...");
			classificationList = getClassificationService().listClassifications(null, 0, null, null);
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");

			logger.info("Fetched " + classificationList.size() + " classification(s).");

			for (Classification classification : classificationList) {
				for (Rank rank : rankList) {
					
					txStatus = startTransaction(true);
					logger.info("Started transaction to fetch all rootNodes specific to Rank " + rank.getLabel() + " ...");

					List<TaxonNode> rankSpecificRootNodes = getClassificationService().loadRankSpecificRootNodes(classification, rank, null, null, null);
					logger.info("Fetched " + rankSpecificRootNodes.size() + " RootNodes for Rank " + rank.getLabel());

					commitTransaction(txStatus);
					logger.debug("Committed transaction.");

					for (TaxonNode rootNode : rankSpecificRootNodes) {
						txStatus = startTransaction(false);
						Rank endRank = rank2endRankMap.get(rank);
						logger.debug("Started transaction to traverse childNodes of rootNode (" + rootNode.getUuid() + ") till " + (endRank == null ? "leaves are reached ..." : "Rank " + endRank.getLabel() + " ..."));
						
						TaxonNode newNode = getTaxonNodeService().load(rootNode.getUuid());

						if (isPesiTaxon(newNode.getTaxon())){
							
							TaxonNode parentNode = newNode.getParent();
	
							success &=traverseTree(newNode, parentNode, endRank, state);
	
							commitTransaction(txStatus);
							logger.debug("Committed transaction.");
						}else{
							logger.debug("Taxon is not in PESI");
						}

					}
				}
			}

			logger.warn("*** Finished Making " + pluralString + " ..." + getSuccessString(success));

			if (!success){
				state.setUnsuccessfull();
			}
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
			return;
		}
	}

	/**
	 * Traverses the classification recursively and stores determined values for every Taxon.
	 * @param childNode
	 * @param parentNode
	 * @param treeIndex
	 * @param fetchLevel
	 * @param state
	 */
	private boolean  traverseTree(TaxonNode childNode, TaxonNode parentNode, Rank fetchLevel, PesiExportState state) {
		boolean success = true;
		// Traverse all branches from this childNode until specified fetchLevel is reached.
		TaxonBase<?> childTaxon = childNode.getTaxon();
		if (childTaxon != null) {
			if (isPesiTaxon(childTaxon)){
				if (childTaxon.getName() != null) {
					Rank childTaxonNameRank = childTaxon.getName().getRank();
					if (childTaxonNameRank != null) {
						if (! childTaxonNameRank.equals(fetchLevel)) {
	
							success &= saveData(childNode, parentNode, state);
	
							for (TaxonNode newNode : childNode.getChildNodes()) {
								success &= traverseTree(newNode, childNode, fetchLevel, state);
							}
							
						} else {
	//						logger.error("Target Rank " + fetchLevel.getLabel() + " reached");
							return success;
						}
					} else {
						logger.warn("Rank is NULL. FetchLevel can not be checked: " + childTaxon.getUuid() + " (" + childTaxon.getTitleCache() + ")");
					}
				} else {
					logger.error("TaxonName is NULL for taxon: " + childTaxon.getUuid());
				}
			}else{
				logger.debug("Taxon is not a PESI taxon: " + childTaxon.getUuid());
			}

		} else {
			logger.error("Taxon is NULL for TaxonNode: " + childNode.getUuid());
		}
		return success;
	}

	/**
	 * Stores values in database for every recursive round.
	 * @param childNode
	 * @param parentNode
	 * @param treeIndex
	 * @param state
	 * @param currentTaxonFk
	 */
	private boolean saveData(TaxonNode childNode, TaxonNode parentNode, PesiExportState state) {
		boolean success = true;
		Taxon childNodeTaxon = childNode.getTaxon();
		if (childNodeTaxon != null) {
			// TaxonRelationships
			success &= saveTaxonRelationships(state, childNodeTaxon);
			// TaxonNameRelationships
			success &= saveNameRelationships(state, childNodeTaxon);
			// SynonymRelationships
			success &= saveSynonymAndSynNameRelationships(state, childNodeTaxon);
		}
		return success;
		
	}

	private boolean saveSynonymAndSynNameRelationships(PesiExportState state, Taxon childNodeTaxon) {
		boolean success = true;
		for (SynonymRelationship synRel : childNodeTaxon.getSynonymRelations()) { // synonyms of accepted taxon
			Synonym synonym = synRel.getSynonym();
			TaxonNameBase<?,?> synonymTaxonName = synonym.getName();
			if (! isPesiTaxon(synonym)){
				logger.warn("Synonym " + synonym.getId() + " of synonym relation " + synRel.getId() + " is not a PESI taxon. Can't export relationship");
				continue;
			}
			
			// Store synonym data in Taxon table
			invokeSynonyms(state, synonymTaxonName);

			
			
			Set<SynonymRelationship> synonymRelations = synonym.getSynonymRelations();
			state.setCurrentFromObject(synonym);
			for (SynonymRelationship synonymRelationship : synonymRelations) {  //needed? Maybe to make sure that there are no partial synonym relations missed ??
				try {
					if (neededValuesNotNull(synonymRelationship, state)) {
						doCount(count++, modCount, pluralString);
						success &= mapping.invoke(synonymRelationship);
						
					}
				} catch (SQLException e) {
					logger.error("SynonymRelationship (" + synonymRelationship.getUuid() + ") could not be stored : " + e.getMessage());
				}
			}

			// SynonymNameRelationship
			success &= saveNameRelationships(state, synonym);
		}
		state.setCurrentFromObject(null);
		return success;
	}

	private boolean saveNameRelationships(PesiExportState state, TaxonBase taxonBase) {
		boolean success = true;
		TaxonNameBase<?,?> childNodeTaxonName = taxonBase.getName();

		//from relations
		Set<NameRelationship> nameRelations = childNodeTaxonName.getRelationsFromThisName();
		state.setCurrentFromObject(taxonBase);
		boolean isFrom = true;
		success &= saveOneSideNameRelation(state, isFrom, nameRelations);
		
		//toRelations
		nameRelations = childNodeTaxonName.getRelationsToThisName();
		state.setCurrentToObject(taxonBase);
		isFrom = false;
		success &= saveOneSideNameRelation(state, isFrom, nameRelations);
		state.setCurrentToObject(null);
		return success;
	}

	private boolean saveOneSideNameRelation(PesiExportState state, boolean isFrom, Set<NameRelationship> nameRelations) {
		boolean success = true;
		for (NameRelationship nameRelation : nameRelations) {
			try {
				TaxonNameBase<?,?> relatedName = isFrom ? nameRelation.getToName(): nameRelation.getFromName();
				if ( isPurePesiName(relatedName)){
					success &= checkAndInvokeNameRelation(state, nameRelation, relatedName, isFrom);
				}else{
					for (TaxonBase<?> relatedTaxon : getPesiTaxa(relatedName)){
						success &= checkAndInvokeNameRelation(state, nameRelation, relatedTaxon, isFrom);
					}
				}
			} catch (SQLException e) {
				logger.error("NameRelationship " + nameRelation.getUuid() + " for " + nameRelation.getFromName().getTitleCache() + " and " + nameRelation.getToName().getTitleCache() + " could not be created: " + e.getMessage());
				success = false;
			}
		}
		return success;
	}

	private boolean checkAndInvokeNameRelation(PesiExportState state, NameRelationship nameRelation, IdentifiableEntity<?> relatedObject, boolean isFrom) throws SQLException {
		boolean success = true;
		if (isFrom){
			state.setCurrentToObject(relatedObject);
		}else{
			state.setCurrentFromObject(relatedObject);
		}
		if (neededValuesNotNull(nameRelation, state)) {
			doCount(count++, modCount, pluralString);
			success &= mapping.invoke(nameRelation);
		}
		state.setCurrentFromObject(null);
		state.setCurrentToObject(null);
		return success;
	}

	private boolean saveTaxonRelationships(PesiExportState state, Taxon childNodeTaxon) {
		boolean success = true;
		Taxon taxon = childNodeTaxon;
		Set<TaxonRelationship> taxonRelations = taxon.getRelationsToThisTaxon();
		for (TaxonRelationship taxonRelationship : taxonRelations) {
			try {
				if (neededValuesNotNull(taxonRelationship, state)) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(taxonRelationship);
				}
			} catch (SQLException e) {
				logger.error("TaxonRelationship could not be created for this TaxonRelation (" + taxonRelationship.getUuid() + "): " + e.getMessage());
			}
		}
		return success;
	}

	/**
	 * Determines synonym related data and saves them.
	 * @param state
	 * @param sr
	 */
	private void invokeSynonyms(PesiExportState state, TaxonNameBase synonymTaxonName) {
		// Store KingdomFk and Rank information in Taxon table
		Integer kingdomFk = PesiTransformer.nomenClaturalCode2Kingdom(synonymTaxonName.getNomenclaturalCode());
		Integer synonymFk = state.getDbId(synonymTaxonName);

		saveSynonymData(state, synonymTaxonName, synonymTaxonName.getNomenclaturalCode(), kingdomFk, synonymFk);
	}

	/**
	 * Stores synonym data.
	 * @param state 
	 * @param taxonName
	 * @param nomenclaturalCode
	 * @param kingdomFk
	 * @param synonymParentTaxonFk
	 * @param currentTaxonFk
	 */
	private boolean saveSynonymData(PesiExportState state, TaxonNameBase taxonName,
			NomenclaturalCode nomenclaturalCode, Integer kingdomFk,
			Integer currentSynonymFk) {
		try {
			if (kingdomFk != null) {
				synonymsStmt.setInt(1, kingdomFk);
			} else {
				synonymsStmt.setObject(1, null);
			}
			
			Integer rankFk = getRankFk(taxonName, nomenclaturalCode);
			if (rankFk != null) {
				synonymsStmt.setInt(2, rankFk);
			} else {
				synonymsStmt.setObject(2, null);
			}
			synonymsStmt.setString(3, getRankCache(taxonName, nomenclaturalCode, state));
			
			if (currentSynonymFk != null) {
				synonymsStmt.setInt(4, currentSynonymFk);
			} else {
				synonymsStmt.setObject(4, null);
			}
			synonymsStmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("SQLException during invoke for taxonName - " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + "): " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks whether needed values for an entity are NULL.
	 * @return
	 */
	private boolean neededValuesNotNull(RelationshipBase<?, ?, ?> relationship, PesiExportState state) {
		boolean result = true;
		if (getTaxonFk1(relationship, state) == null) {
			logger.warn("TaxonFk1 is NULL, but is not allowed to be. Therefore no record was written to export database for this relationship: " + relationship.getUuid());
			result = false;
		}
		if (getTaxonFk2(relationship, state) == null) {
			logger.warn("TaxonFk2 is NULL, but is not allowed to be. Therefore no record was written to export database for this relationship: " + relationship.getUuid());
			result = false;
		}
		return result;
	}
	
	/**
	 * Deletes all entries of database tables related to <code>RelTaxon</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear RelTaxon
		sql = "DELETE FROM " + dbTableName;
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(PesiExportState state) {
		return ! state.getConfig().isDoRelTaxa();
	}

	/**
	 * Returns the <code>TaxonFk1</code> attribute. It corresponds to a CDM <code>TaxonRelationship</code>.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>TaxonFk1</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getTaxonFk1(RelationshipBase<?, ?, ?> relationship, PesiExportState state) {
		return getObjectFk(relationship, state, true);
	}
	
	/**
	 * Returns the <code>TaxonFk2</code> attribute. It corresponds to a CDM <code>SynonymRelationship</code>.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>TaxonFk2</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getTaxonFk2(RelationshipBase<?, ?, ?> relationship, PesiExportState state) {
		return getObjectFk(relationship, state, false);
	}
	
	/**
	 * Returns the <code>RelTaxonQualifierFk</code> attribute.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @return The <code>RelTaxonQualifierFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getRelTaxonQualifierFk(RelationshipBase<?, ?, ?> relationship) {
		return PesiTransformer.taxonRelation2RelTaxonQualifierFk(relationship);
	}
	
	/**
	 * Returns the <code>RelQualifierCache</code> attribute.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @return The <code>RelQualifierCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRelQualifierCache(RelationshipBase<?, ?, ?> relationship, PesiExportState state) {
		String result = null;
		NomenclaturalCode code = null;
		Taxon taxon = null;
		TaxonNameBase name= null;
		if (relationship.isInstanceOf(TaxonRelationship.class)){
			TaxonRelationship rel = CdmBase.deproxy(relationship, TaxonRelationship.class);
			taxon = rel.getToTaxon();
			name = taxon.getName();
			code = name.getNomenclaturalCode();
			rel = null;
			
		}else if (relationship.isInstanceOf(SynonymRelationship.class)){
			SynonymRelationship rel = CdmBase.deproxy(relationship, SynonymRelationship.class);
			taxon = rel.getAcceptedTaxon();
			name = taxon.getName();
			code = name.getNomenclaturalCode();
			rel = null;

		}else if (relationship.isInstanceOf(NameRelationship.class)){
			NameRelationship rel = CdmBase.deproxy(relationship,  NameRelationship.class);
			name = rel.getFromName();
			code =name.getNomenclaturalCode();
			rel = null;
						
		}else if (relationship.isInstanceOf(HybridRelationship.class)){
			HybridRelationship rel =  CdmBase.deproxy(relationship,  HybridRelationship.class);
			name = rel.getParentName();
			code = name.getNomenclaturalCode();
			rel = null;
		}
		taxon = null;
		name = null;
		if (code != null) {
			result = state.getConfig().getTransformer().getCacheByRelationshipType(relationship, code);
		} else {
			logger.error("NomenclaturalCode is NULL while creating the following relationship: " + relationship.getUuid());
		}
		return result;
	}
	
	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @return The <code>Notes</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNotes(RelationshipBase<?, ?, ?> relationship) {
		// TODO
		return null;
	}

	/**
	 * Returns the database key of an object in the given relationship.
	 * @param relationship {@link RelationshipBase RelationshipBase}.
	 * @param state {@link PesiExportState PesiExportState}.
	 * @param isFrom A boolean value indicating whether the database key of the parent or child in this relationship is searched. <code>true</code> means the child is searched. <code>false</code> means the parent is searched.
	 * @return The database key of an object in the given relationship.
	 */
	private static Integer getObjectFk(RelationshipBase<?, ?, ?> relationship, PesiExportState state, boolean isFrom) {
		TaxonBase<?> taxonBase = null;
		if (relationship.isInstanceOf(TaxonRelationship.class)) {
			TaxonRelationship tr = (TaxonRelationship)relationship;
			taxonBase = (isFrom) ? tr.getFromTaxon():  tr.getToTaxon();
		} else if (relationship.isInstanceOf(SynonymRelationship.class)) {
			SynonymRelationship sr = (SynonymRelationship)relationship;
			taxonBase = (isFrom) ? sr.getSynonym() : sr.getAcceptedTaxon();
		} else if (relationship.isInstanceOf(NameRelationship.class) ||  relationship.isInstanceOf(HybridRelationship.class)) {
			if (isFrom){
				return state.getDbId(state.getCurrentFromObject());
			}else{
				return state.getDbId(state.getCurrentToObject());
			}
		}
		if (taxonBase != null) {
			if (! isPesiTaxon(taxonBase)){
				logger.warn("Related taxonBase is not a PESI taxon. Taxon: " + taxonBase.getId() + "/" + taxonBase.getUuid() + "; TaxonRel: " +  relationship.getId() + "(" + relationship.getType().getTitleCache() + ")");
				return null;
			}else{
				return state.getDbId(taxonBase);	
			}
			
		}
		logger.warn("No taxon found in state for relationship: " + relationship.toString());
		return null;
	}

	/**
	 * Returns the <code>RankFk</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param nomenclaturalCode The {@link NomenclaturalCode NomenclaturalCode}.
	 * @return The <code>RankFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getRankFk(TaxonNameBase taxonName, NomenclaturalCode nomenclaturalCode) {
		Integer result = null;
		if (nomenclaturalCode != null) {
			if (taxonName != null && taxonName.getRank() == null) {
				logger.warn("Rank is null: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
			result = PesiTransformer.rank2RankId(taxonName.getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
			if (result == null) {
				logger.warn("Rank could not be determined for PESI-Kingdom-Id " + PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode) + " and TaxonName " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		return result;
	}

	/**
	 * Returns the <code>RankCache</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param nomenclaturalCode The {@link NomenclaturalCode NomenclaturalCode}.
	 * @param state 
	 * @return The <code>RankCache</code> attribute.
	 * @see MethodMapper
	 */
	private static String getRankCache(TaxonNameBase taxonName, NomenclaturalCode nomenclaturalCode, PesiExportState state) {
		if (nomenclaturalCode != null) {
			return state.getTransformer().getCacheByRankAndKingdom(taxonName.getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
		}else{
			logger.warn("No nomenclatural code defined for rank cache search");
			return null;
		}
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk1", this.getClass(), "getTaxonFk1", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk2", this.getClass(), "getTaxonFk2", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("RelTaxonQualifierFk", this));
		mapping.addMapper(MethodMapper.NewInstance("RelQualifierCache", this, RelationshipBase.class, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));

		return mapping;
	}

}
