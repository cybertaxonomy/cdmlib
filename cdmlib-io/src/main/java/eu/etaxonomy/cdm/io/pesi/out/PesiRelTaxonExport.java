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

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiRelTaxonExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiRelTaxonExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = RelationshipBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "RelTaxon";
	private static final String pluralString = "Relationships";
	private static PreparedStatement synonymsStmt;
	private HashMap<Rank, Rank> rankMap = new HashMap<Rank, Rank>();
	private List<Rank> rankList = new ArrayList<Rank>();
	private PesiExportMapping mapping;
	private int count = 0;
	private boolean success = true;
	private static NomenclaturalCode nomenclaturalCode;
	
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
	protected boolean doInvoke(PesiExportState state) {
		try {
			logger.error("*** Started Making " + pluralString + " ...");
	
			Connection connection = state.getConfig().getDestination().getConnection();
			String synonymsSql = "UPDATE Taxon SET KingdomFk = ?, RankFk = ?, RankCache = ? WHERE TaxonId = ?"; 
			synonymsStmt = connection.prepareStatement(synonymsSql);
			
			// Get the limit for objects to save within a single transaction.
			int pageSize = 1000;

			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();

			// Stores whether this invoke was successful or not.
			boolean success = true;

			// PESI: Clear the database table RelTaxon.
			doDelete(state);
	
			// Get specific mappings: (CDM) Relationship -> (PESI) RelTaxon
			mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			TransactionStatus txStatus = null;
			List<TaxonomicTree> taxonomicTreeList = null;
			
			// Specify starting ranks for tree traversing
			rankList.add(Rank.KINGDOM());
			rankList.add(Rank.GENUS());

			// Specify where to stop traversing (value) when starting at a specific Rank (key)
			rankMap.put(Rank.GENUS(), null); // Since NULL does not match an existing Rank, traverse all the way down to the leaves
			rankMap.put(Rank.KINGDOM(), Rank.GENUS()); // excludes rank genus
			
			// Retrieve list of Taxonomic Trees
			txStatus = startTransaction(true);
			logger.error("Started transaction. Fetching all Taxonomic Trees...");
			taxonomicTreeList = getTaxonTreeService().listTaxonomicTrees(null, 0, null, null);
			commitTransaction(txStatus);
			logger.error("Committed transaction.");

			logger.error("Fetched " + taxonomicTreeList.size() + " Taxonomic Tree.");

			for (TaxonomicTree taxonomicTree : taxonomicTreeList) {
				for (Rank rank : rankList) {
					
					txStatus = startTransaction(true);
					logger.error("Started transaction to fetch all rootNodes specific to Rank " + rank.getLabel() + " ...");

					List<TaxonNode> rankSpecificRootNodes = getTaxonTreeService().loadRankSpecificRootNodes(taxonomicTree, rank, null);
					logger.error("Fetched " + rankSpecificRootNodes.size() + " RootNodes for Rank " + rank.getLabel());

					commitTransaction(txStatus);
					logger.error("Committed transaction.");

					for (TaxonNode rootNode : rankSpecificRootNodes) {
						txStatus = startTransaction(false);
						Rank endRank = rankMap.get(rank);
						if (endRank != null) {
							logger.error("Started transaction to traverse childNodes of rootNode (" + rootNode.getUuid() + ") till Rank " + endRank.getLabel() + " ...");
						} else {
							logger.error("Started transaction to traverse childNodes of rootNode (" + rootNode.getUuid() + ") till leaves are reached ...");
						}

						TaxonNode newNode = getTaxonNodeService().load(rootNode.getUuid());

						TaxonNode parentNode = newNode.getParent();

						traverseTree(newNode, parentNode, rankMap.get(rank), state);

						commitTransaction(txStatus);
						logger.error("Committed transaction.");

					}
				}
			}

			logger.error("*** Finished Making " + pluralString + " ..." + getSuccessString(success));


			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Traverses the TaxonTree recursively and stores determined values for every Taxon.
	 * @param childNode
	 * @param parentNode
	 * @param treeIndex
	 * @param fetchLevel
	 * @param state
	 */
	private void traverseTree(TaxonNode childNode, TaxonNode parentNode, Rank fetchLevel, PesiExportState state) {
		// Traverse all branches from this childNode until specified fetchLevel is reached.
		if (childNode.getTaxon() != null) {
			TaxonNameBase taxonName = childNode.getTaxon().getName();
			if (taxonName != null) {
				Rank childTaxonNameRank = taxonName.getRank();
				if (childTaxonNameRank != null) {
					if (! childTaxonNameRank.equals(fetchLevel)) {

						saveData(childNode, parentNode, state);

						for (TaxonNode newNode : childNode.getChildNodes()) {
							traverseTree(newNode, childNode, fetchLevel, state);
						}
						
					} else {
//						logger.error("Target Rank " + fetchLevel.getLabel() + " reached");
						return;
					}
				} else {
					logger.error("Rank is NULL. FetchLevel can not be checked: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				}
			} else {
				logger.error("TaxonName is NULL for this node: " + childNode.getUuid());
			}

		} else {
			logger.error("Taxon is NULL for TaxonNode: " + childNode.getUuid());
		}
	}

	/**
	 * Stores values in database for every recursive round.
	 * @param childNode
	 * @param parentNode
	 * @param treeIndex
	 * @param state
	 * @param currentTaxonFk
	 */
	private void saveData(TaxonNode childNode, TaxonNode parentNode, PesiExportState state) {
		Taxon childNodeTaxon = childNode.getTaxon();
		if (childNodeTaxon != null) {
			TaxonNameBase childNodeTaxonName = childNodeTaxon.getName();
			nomenclaturalCode = PesiTransformer.getNomenclaturalCode(childNodeTaxonName);

			if (childNodeTaxonName != null) {

				// TaxonRelationships
				Set<Taxon> taxa = childNodeTaxonName.getTaxa(); // accepted taxa
				if (taxa.size() == 1) {
					Taxon taxon = CdmBase.deproxy(taxa.iterator().next(), Taxon.class);
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
				} else if (taxa.size() > 1) {
					logger.error("TaxonRelationship could not be created. This TaxonNode has " + taxa.size() + " Taxa: " + childNodeTaxon.getUuid() + " (" + childNodeTaxon.getTitleCache() + ")");
				}
				
				// TaxonNameRelationships
				Set<NameRelationship> nameRelations = childNodeTaxonName.getRelationsFromThisName();
				for (NameRelationship nameRelation : nameRelations) {
					try {
						if (neededValuesNotNull(nameRelation, state)) {
							doCount(count++, modCount, pluralString);
							success &= mapping.invoke(nameRelation);
						}
					} catch (SQLException e) {
						logger.error("NameRelationship could not be created: " + e.getMessage());
					}
				}

			}
			
			// SynonymRelationships
			Set<Synonym> synonyms = childNodeTaxon.getSynonyms(); // synonyms of accepted taxon
			for (Synonym synonym : synonyms) {
				TaxonNameBase synonymTaxonName = synonym.getName();
				
				// Store synonym data in Taxon table
				invokeSynonyms(state, synonymTaxonName);

				Set<SynonymRelationship> synonymRelations = synonym.getSynonymRelations();
				for (SynonymRelationship synonymRelationship : synonymRelations) {
					try {
						if (neededValuesNotNull(synonymRelationship, state)) {
							doCount(count++, modCount, pluralString);
							success &= mapping.invoke(synonymRelationship);
							
						}
					} catch (SQLException e) {
						logger.error("SynonymRelationship could not be created for this SynonymRelation (" + synonymRelationship.getUuid() + "): " + e.getMessage());
					}
				}

				// SynonymNameRelationship
				Set<NameRelationship> nameRelations = synonymTaxonName.getRelationsFromThisName();
				for (NameRelationship nameRelation : nameRelations) {
					try {
						if (neededValuesNotNull(nameRelation, state)) {
							doCount(count++, modCount, pluralString);
							success &= mapping.invoke(nameRelation);
						}
					} catch (SQLException e) {
						logger.error("NameRelationship could not be created for this NameRelation (" + nameRelation.getUuid() + "): " + e.getMessage());
					}
				}

			}
			
		}
		
	}

	/**
	 * Determines synonym related data.
	 * @param state
	 * @param sr
	 */
	private static void invokeSynonyms(PesiExportState state, TaxonNameBase synonymTaxonName) {
		// Store KingdomFk and Rank information in Taxon table
		Integer kingdomFk = PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode);
		Integer synonymFk = state.getDbId(synonymTaxonName);

		saveSynonymData(synonymTaxonName, nomenclaturalCode, kingdomFk, synonymFk);
	}

	/**
	 * Stores synonym data.
	 * @param taxonName
	 * @param nomenclaturalCode
	 * @param kingdomFk
	 * @param synonymParentTaxonFk
	 * @param currentTaxonFk
	 */
	private static boolean saveSynonymData(TaxonNameBase taxonName,
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
			synonymsStmt.setString(3, getRankCache(taxonName, nomenclaturalCode));
			
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
			logger.error("TaxonFk1 is NULL, but is not allowed to be. Therefore no record was written to export database for this relationship: " + relationship.getUuid());
			result = false;
		}
		if (getTaxonFk2(relationship, state) == null) {
			logger.error("TaxonFk2 is NULL, but is not allowed to be. Therefore no record was written to export database for this relationship: " + relationship.getUuid());
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
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return The <code>TaxonFk1</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getTaxonFk1(RelationshipBase<?, ?, ?> relationship, PesiExportState state) {
		return getObjectFk(relationship, state, true);
	}
	
	/**
	 * Returns the <code>TaxonFk2</code> attribute. It corresponds to a CDM <code>SynonymRelationship</code>.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @param state The {@link DbExportStateBase DbExportState}.
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
	private static String getRelQualifierCache(RelationshipBase<?, ?, ?> relationship) {
		String result = null;
		if (nomenclaturalCode != null) {
			if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
				result = PesiTransformer.zoologicalTaxonRelation2RelTaxonQualifierCache(relationship);
			} else {
				result = PesiTransformer.taxonRelation2RelTaxonQualifierCache(relationship);
			}
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
	 * @param state {@link DbExportStateBase DbExportStateBase}.
	 * @param isFrom A boolean value indicating whether the database key of the parent or child in this relationship is searched. <code>true</code> means the child is searched. <code>false</code> means the parent is searched.
	 * @return The database key of an object in the given relationship.
	 */
	private static Integer getObjectFk(RelationshipBase<?, ?, ?> relationship, PesiExportState state, boolean isFrom) {
		TaxonBase<?> taxon = null;
		if (relationship.isInstanceOf(TaxonRelationship.class)) {
			TaxonRelationship tr = (TaxonRelationship)relationship;
			taxon = (isFrom) ? tr.getFromTaxon():  tr.getToTaxon();
		} else if (relationship.isInstanceOf(SynonymRelationship.class)) {
			SynonymRelationship sr = (SynonymRelationship)relationship;
			taxon = (isFrom) ? sr.getSynonym() : sr.getAcceptedTaxon();
		} else if (relationship.isInstanceOf(NameRelationship.class)) {
			NameRelationship nr = (NameRelationship)relationship;
			TaxonNameBase taxonName = (isFrom) ? nr.getFromName() : nr.getToName();
			return state.getDbId(taxonName);
		}
		if (taxon != null) {
			return state.getDbId(taxon.getName());
		}
		logger.warn("No taxon found in state for relationship: " + relationship.toString());
		return null;
	}

	/**
	 * Returns the <code>RankFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
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
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>RankCache</code> attribute.
	 * @see MethodMapper
	 */
	private static String getRankCache(TaxonNameBase taxonName, NomenclaturalCode nomenclaturalCode) {
		String result = null;
		if (nomenclaturalCode != null) {
			result = PesiTransformer.rank2RankCache(taxonName.getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
		}
		return result;
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
//		mapping.addMapper(IdMapper.NewInstance("RelTaxonId")); // Automagically generated on database level as primary key
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk1", this.getClass(), "getTaxonFk1", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk2", this.getClass(), "getTaxonFk2", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("RelTaxonQualifierFk", this));
		mapping.addMapper(MethodMapper.NewInstance("RelQualifierCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));
//		mapping.addMapper(CreatedAndNotesMapper.NewInstance(false));

		return mapping;
	}

}
