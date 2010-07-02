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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiTaxonExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiTaxonExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = TaxonNameBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Taxon";
	private static final String pluralString = "Taxa";
	private PreparedStatement parentTaxonFk_TreeIndex_KingdomFkStmt;
	private NomenclaturalCode nomenclaturalCode;
	private Integer kingdomFk;
	private HashMap<Rank, Rank> rankMap = new HashMap<Rank, Rank>();
	private List<Rank> rankList = new ArrayList<Rank>();
	private static final UUID uuidTreeIndex = UUID.fromString("28f4e205-1d02-4d3a-8288-775ea8413009");
	private AnnotationType treeIndexAnnotationType;
	
	/**
	 * @return the treeIndexAnnotationType
	 */
	protected AnnotationType getTreeIndexAnnotationType() {
		return treeIndexAnnotationType;
	}

	/**
	 * @param treeIndexAnnotationType the treeIndexAnnotationType to set
	 */
	protected void setTreeIndexAnnotationType(AnnotationType treeIndexAnnotationType) {
		this.treeIndexAnnotationType = treeIndexAnnotationType;
	}

	enum NamePosition {
		beginning,
		end,
		between,
		alone,
		nowhere
	}

	public PesiTaxonExport() {
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

			// Prepare TreeIndex-And-KingdomFk-Statement
			Connection connection = state.getConfig().getDestination().getConnection();
			String parentTaxonFk_TreeIndex_KingdomFkSql = "UPDATE Taxon SET ParentTaxonFk = ?, TreeIndex = ?, " +
					"KingdomFk = ?, RankFk = ?, RankCache = ? WHERE TaxonId = ?"; 
			parentTaxonFk_TreeIndex_KingdomFkStmt = connection.prepareStatement(parentTaxonFk_TreeIndex_KingdomFkSql);

			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();

			// Stores whether this invoke was successful or not.
			boolean success = true;
	
			// PESI: Clear the database table Taxon.
			doDelete(state);
			
			// CDM: Get the number of all available taxa.
//			int maxCount = getTaxonService().count(null);
//			logger.error("Total amount of " + maxCount + " " + pluralString + " will be exported.");

			// Get specific mappings: (CDM) Taxon -> (PESI) Taxon
			PesiExportMapping mapping = getMapping();
	
			// Initialize the db mapper
			mapping.initialize(state);
	
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<TaxonNameBase> list = null;

			// 1st Round: Make Taxa
			logger.error("PHASE 1...");
			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getNameService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (TaxonNameBase taxonName : list) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(taxonName);
					
					// Check whether some rules are violated
					nomenclaturalCode = taxonName.getNomenclaturalCode();
					String genusOrUninomial = getGenusOrUninomial(taxonName);
					String specificEpithet = getSpecificEpithet(taxonName);
					String infraSpecificEpithet = getInfraSpecificEpithet(taxonName);
					String infraGenericEpithet = getInfraGenericEpithet(taxonName);
					Integer rank = getRankFk(taxonName, nomenclaturalCode);

					if (rank == null) {
						logger.error("Rank was not determined: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
					} else {
						if (infraGenericEpithet == null && rank.intValue() == 190) {
							logger.error("InfraSpecificEpithet was not determined although it should exist for rank 190: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
						}
						if (specificEpithet != null && rank.intValue() < 220) {
							logger.error("SpecificEpithet was determined for rank " + rank + " although it should only exist for ranks higher or equal to 220: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
						}
						if (infraSpecificEpithet != null && rank.intValue() < 230) {
							logger.error("InfraSpecificEpithet was determined for rank " + rank + " although it should only exist for ranks higher or equal to 230: "  + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
						}
					}
					if (infraSpecificEpithet != null && specificEpithet == null) {
						logger.error("An infraSpecificEpithet was determined, but a specificEpithet was not determined: "  + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
					}
					if (genusOrUninomial == null) {
						logger.error("GenusOrUninomial was not determined: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
					}
					
				}

				// Commit transaction
				commitTransaction(txStatus);
				logger.error("Committed transaction.");
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;

				// Start transaction
				txStatus = startTransaction(true);
				logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			}
			if (list.size() == 0) {
				logger.error("No " + pluralString + " left to fetch.");
			}
			// Commit transaction
			commitTransaction(txStatus);
			logger.error("Committed transaction.");

			count = 0;
			pastCount = 0;
			List<TaxonomicTree> taxonomicTreeList = null;
			// 2nd Round: Add ParentTaxonFk, TreeIndex, Rank and KingdomFk to each Taxon
			logger.error("PHASE 2...");
			
			// Specify starting ranks for tree traversing
			rankList.add(Rank.KINGDOM());
			rankList.add(Rank.GENUS());

			// Specify where to stop traversing (value) when starting at a specific Rank (key)
			rankMap.put(Rank.GENUS(), null); // Since NULL does not match an existing Rank, traverse all the way down to the leaves
			rankMap.put(Rank.KINGDOM(), Rank.GENUS()); // excludes rank genus
			
			StringBuffer treeIndex = new StringBuffer();
			
			// Retrieve list of Taxonomic Trees
			txStatus = startTransaction(true);
			logger.error("Started transaction. Fetching all Taxonomic Trees...");
			taxonomicTreeList = getTaxonTreeService().listTaxonomicTrees(null, 0, null, null);
			commitTransaction(txStatus);
			logger.error("Committed transaction.");

			logger.error("Fetched " + taxonomicTreeList.size() + " Taxonomic Tree.");

			setTreeIndexAnnotationType(getAnnotationType(uuidTreeIndex, "TreeIndex", "", "TI"));
			
			for (TaxonomicTree taxonomicTree : taxonomicTreeList) {
				for (Rank rank : rankList) {
					
					txStatus = startTransaction(true);
					logger.error("Started transaction to fetch all rootNodes specific to Rank " + rank.getLabel() + " ...");

					List<TaxonNode> rankSpecificRootNodes = getTaxonTreeService().loadRankSpecificRootNodes(taxonomicTree, rank, null);
					logger.error("Fetched " + rankSpecificRootNodes.size() + " RootNodes for Rank " + rank.getLabel());

					commitTransaction(txStatus);
					logger.error("Committed transaction.");

//					int elementCount = 0;
//					int halfCount = rankSpecificRootNodes.size() / 2;

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
						if (rank.equals(Rank.KINGDOM())) {
							treeIndex = new StringBuffer();
							treeIndex.append("#");
						} else {
							// Get treeIndex from parentNode
							if (parentNode != null) {
								boolean annotationFound = false;
								Set<Annotation> annotations = parentNode.getAnnotations();
								for (Annotation annotation : annotations) {
									AnnotationType annotationType = annotation.getAnnotationType();
									if (annotationType != null && annotationType.equals(getTreeIndexAnnotationType())) {
										treeIndex = new StringBuffer(CdmUtils.Nz(annotation.getText()));
										annotationFound = true;
										logger.error("treeIndex: " + treeIndex);
										break;
									}
								}
								if (!annotationFound) {
									// This should not happen because it means that the treeIndex was not set correctly as an annotation to parentNode
									logger.error("TreeIndex could not be read from annotation of this TaxonNode: " + parentNode.getUuid());
									treeIndex = new StringBuffer();
									treeIndex.append("#");
								}
							} else {
								// TreeIndex could not be determined, but it's unclear how to proceed to generate a correct treeIndex if the parentNode is NULL
								logger.error("ParentNode for RootNode is NULL. TreeIndex could not be determined: " + newNode.getUuid());
								treeIndex = new StringBuffer(); // This just prevents growing of the treeIndex in a wrong manner
								treeIndex.append("#");
							}
						}
						
						nomenclaturalCode = newNode.getTaxon().getName().getNomenclaturalCode();
						kingdomFk = PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode);
						traverseTree(newNode, parentNode, treeIndex, rankMap.get(rank), state);
						
						commitTransaction(txStatus);
						logger.error("Committed transaction.");

//						elementCount++;
//						if (elementCount == halfCount) {
//							logger.error("50% of " + rank.getLabel() + " RootNodes processed...");
//						}
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
	 * Returns the AnnotationType for a given UUID.
	 * @param state
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected AnnotationType getAnnotationType(UUID uuid, String label, String text, String labelAbbrev){
		AnnotationType annotationType = (AnnotationType)getTermService().find(uuid);
		if (annotationType == null) {
			annotationType = AnnotationType.NewInstance(label, text, labelAbbrev);
			annotationType.setUuid(uuid);
//			annotationType.setVocabulary(AnnotationType.EDITORIAL().getVocabulary());
			getTermService().save(annotationType);
		}
		return annotationType;
	}

	/**
	 * Traverses the TaxonTree recursively and stores determined values for every Taxon.
	 * @param childNode
	 * @param parentNode
	 * @param treeIndex
	 * @param fetchLevel
	 * @param state
	 */
	private void traverseTree(TaxonNode childNode, TaxonNode parentNode, StringBuffer treeIndex, Rank fetchLevel, PesiExportState state) {
		// Traverse all branches from this childNode until specified fetchLevel is reached.
		StringBuffer localTreeIndex = new StringBuffer(treeIndex);
		if (childNode.getTaxon() != null) {
			TaxonNameBase taxonName = childNode.getTaxon().getName();
			Integer taxonNameId = state.getDbId(taxonName);
			if (taxonNameId != null) {
				Rank childTaxonNameRank = taxonName.getRank();
				if (childTaxonNameRank != null) {
					if (! childTaxonNameRank.equals(fetchLevel)) {

						localTreeIndex.append(taxonNameId);
						localTreeIndex.append("#");

						saveData(childNode, parentNode, localTreeIndex, state, taxonNameId);

						// Store treeIndex as annotation for further use
						Annotation annotation = Annotation.NewInstance(localTreeIndex.toString(), getTreeIndexAnnotationType(), Language.DEFAULT());
						childNode.addAnnotation(annotation);

						for (TaxonNode newNode : childNode.getChildNodes()) {
							traverseTree(newNode, childNode, localTreeIndex, fetchLevel, state);
						}
						
					} else {
//						logger.error("Target Rank " + fetchLevel.getLabel() + " reached");
						return;
					}
				} else {
					logger.error("Rank is NULL. FetchLevel can not be checked: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				}
			} else {
				logger.error("TaxonName can not be found in State: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
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
	private void saveData(TaxonNode childNode, TaxonNode parentNode, StringBuffer treeIndex, PesiExportState state, Integer currentTaxonFk) {
		// We are differentiating kingdoms by the nomenclatural code for now.
		// This needs to be handled in a better way as soon as we know how to differentiate between more kingdoms.
		Taxon childNodeTaxon = childNode.getTaxon();
		TaxonNameBase childNodeTaxonName = childNode.getTaxon().getName();
		if (childNodeTaxon != null && childNodeTaxonName != null) {
			TaxonNameBase parentNodeTaxonName = null;
			if (parentNode != null) {
				Taxon parentNodeTaxon = parentNode.getTaxon();
				if (parentNodeTaxon != null) {
					parentNodeTaxonName  = parentNodeTaxon.getName();
				}
			}

			invokeParentTaxonFkAndTreeIndexAndKingdomFk(childNodeTaxonName, 
					nomenclaturalCode, 
					kingdomFk, 
					state.getDbId(parentNodeTaxonName), 
					currentTaxonFk, 
					treeIndex);
		}
		
	}

	/**
	 * Inserts values into the Taxon database table.
	 * @param taxonNameBase
	 * @param state
	 * @param stmt
	 * @return
	 */
	protected boolean invokeParentTaxonFkAndTreeIndexAndKingdomFk(TaxonNameBase taxonName, NomenclaturalCode nomenclaturalCode, Integer kingdomFk, Integer parentTaxonFk, Integer currentTaxonFk, StringBuffer treeIndex) {
		try {
			if (parentTaxonFk != null) {
				parentTaxonFk_TreeIndex_KingdomFkStmt.setInt(1, parentTaxonFk);
			} else {
				parentTaxonFk_TreeIndex_KingdomFkStmt.setObject(1, parentTaxonFk);
			}
			parentTaxonFk_TreeIndex_KingdomFkStmt.setString(2, treeIndex.toString());
			parentTaxonFk_TreeIndex_KingdomFkStmt.setInt(3, kingdomFk);
			parentTaxonFk_TreeIndex_KingdomFkStmt.setInt(4, getRankFk(taxonName, nomenclaturalCode));
			parentTaxonFk_TreeIndex_KingdomFkStmt.setString(5, getRankCache(taxonName, nomenclaturalCode));
			parentTaxonFk_TreeIndex_KingdomFkStmt.setInt(6, currentTaxonFk);
			parentTaxonFk_TreeIndex_KingdomFkStmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("SQLException during treeIndex invoke for taxonName - " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + "): " + e.getMessage());
			logger.error("parentTaxonFk: " + parentTaxonFk);
			logger.error("treeIndex: " + treeIndex);
			logger.error("kingdomFk: " + kingdomFk);
			logger.error("rankFk: " + getRankFk(taxonName, nomenclaturalCode));
			logger.error("rankCache: " + getRankCache(taxonName, nomenclaturalCode));
			logger.error("taxonFk: " + currentTaxonFk);
//			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes all entries of database tables related to <code>Taxon</code>.
	 * @param state The PesiExportState
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear Taxon
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
		return ! state.getConfig().isDoTaxa();
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
				logger.warn("Rank " + taxonName.getRank().getLabel() + " could not be determined for PESI-Kingdom-Id " + PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
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
	 * 
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return Whether it's genus or uninomial.
	 * @see MethodMapper
	 */
	private static String getGenusOrUninomial(TaxonNameBase taxonName) {
		String result = null;
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getGenusOrUninomial();
		}
		return result;
	}

	/**
	 * Returns the <code>InfraGenericEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>InfraGenericEpithet</code> attribute.
	 * @see MethodMapper
	 */
	private static String getInfraGenericEpithet(TaxonNameBase taxonName) {
		String result = null;
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getInfraGenericEpithet();
		}
		return result;
	}

	/**
	 * Returns the <code>SpecificEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>SpecificEpithet</code> attribute.
	 * @see MethodMapper
	 */
	private static String getSpecificEpithet(TaxonNameBase taxonName) {
		String result = null;
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getSpecificEpithet();
		}
		return result;
	}

	/**
	 * Returns the <code>InfraSpecificEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>InfraSpecificEpithet</code> attribute.
	 * @see MethodMapper
	 */
	private static String getInfraSpecificEpithet(TaxonNameBase taxonName) {
		String result = null;
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getInfraSpecificEpithet();
		}
		return result;
	}

	/**
	 * Returns the <code>WebSearchName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>WebSearchName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getWebSearchName(TaxonNameBase taxonName) {
		String result = null;
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getNameCache();
		}
		return result;
	}
	
	/**
	 * Returns the <code>WebShowName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>WebShowName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getWebShowName(TaxonNameBase taxonName) {
		String result = "";
		
		List taggedName = taxonName.getTaggedName();
//		logger.error("----------------------------------------");
		boolean stringPart = false;
		boolean rankPart = false;
		boolean teamPart = false;
		boolean datePart = false;
		boolean referencePart = false;
		boolean openTag = false;
		boolean start = true;
		for (Object object : taggedName) {
			if (object instanceof String) {
				// Name part
//				logger.error("Name part found: " + object);
				if (! openTag && ! teamPart) {
					if (start) {
						result = "<i>";
						start = false;
					} else {
						result += " <i>";
					}
					openTag = true;
				} else {
					result += " ";
				}
				result += object;
				stringPart = true;
			} else if (object instanceof Rank) {
				// Rank
				Rank rank = CdmBase.deproxy(object, Rank.class);
//				logger.error("Rank found: " + rank.getLabel());
				if (openTag) {
					result += "</i> ";
					openTag = false;
				} else {
					result += " ";
				}
				result += rank.getAbbreviation();
				rankPart = true;
			} else if (object instanceof Team) {
//				logger.error("Team: " + object);
				if (openTag) {
					result += "</i> ";
					openTag = false;
				} else {
					result += " ";
				}
				result += object;
				teamPart = true;
			} else if (object instanceof Date) {
//				logger.error("Date found: " + object);
				if (openTag) {
					result += "</i> ";
					openTag = false;
				} else {
					result += " ";
				}
				result += object;
				datePart = true;
			} else if (object instanceof ReferenceBase) {
//				logger.error("Reference found: " + object);
				if (openTag) {
					result += "</i> ";
					openTag = false;
				} else {
					result += " ";
				}
				result += object;
				referencePart = true;
			} else {
				logger.error("Instance unknown: " + object.getClass());
			}
		}
		if (openTag) {
			result += "</i>";
		}
		logger.error("WebShowName: " + result);

		return result;
	}

	/**
	 * 
	 * @param searchString
	 * @param searchedString
	 * @return
	 */
	private static Integer countPattern(String searchString, String searchedString) {
		Integer count = 0;
		if (searchString != null && searchedString != null) {
			Integer index = 0;
			while ((index = searchedString.indexOf(searchString, index)) != -1) {
				count++;
				index++;
			}
		} else {
//			logger.error("Either searchString or searchedString is NULL");
		}
		return count;
	}
	
	/**
	 * @param namePosition
	 * @return
	 */
	private static boolean nameExists(List<NamePosition> namePosition) {
		boolean result = false;
		if (namePosition != null) {
			if (namePosition.contains(NamePosition.nowhere)) {
				result = false;
			} else {
				result = true;
			}
		}
		return result;
	}

	/**
	 * 
	 * @param name
	 * @param targetString
	 * @return
	 */
	private static List<NamePosition> getPosition(String name, String targetString) {
		List<NamePosition> result = new ArrayList<NamePosition>();
		boolean touched = false;
		if (name != null && targetString != null) {
			if ("".equals(name.trim())) {
				result.add(NamePosition.nowhere);
			} else {
				String beginningAnchor = "^";
				String endAnchor = "$";
				String anyNumberOfCharacters = ".*";
				
				String beginningRegEx = beginningAnchor + name;
				String endRegEx = name + endAnchor;
				String middleRegEx = anyNumberOfCharacters + name + anyNumberOfCharacters;
				if (stringExists(beginningRegEx, targetString)) {
					result.add(NamePosition.beginning);
					touched = true;
				}
				if (stringExists(endRegEx, targetString)) {
					result.add(NamePosition.end);
					touched = true;
				}
				if (stringExists(middleRegEx, targetString)) {
					if (result.contains(NamePosition.beginning) && result.contains(NamePosition.end)) {
						result.add(NamePosition.alone);
					} else {
						result.add(NamePosition.between);
					}
					touched = true;
				}
				if (!touched) {
					result.add(NamePosition.nowhere);
				}
			}
		} else {
//			logger.error("Either name or targetString is NULL");
			result = null;
		}
		return result;
	}
	

	/**
	 * @param beginningRegEx
	 * @param targetString
	 */
	private static boolean stringExists(String regEx, String targetString) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(targetString);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the <code>AuthorString</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>AuthorString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAuthorString(TaxonNameBase taxonName) {
		String result = null;
		if (taxonName != null && taxonName != null) {
			if (taxonName.isInstanceOf(NonViralName.class)) {
				NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
				result = nonViralName.getAuthorshipCache();
			} else {
				logger.warn("TaxonName is not of instance NonViralName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		return result;
	}

	/**
	 * Returns the <code>FullName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FullName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFullName(TaxonNameBase taxonName) {
		if (taxonName != null) {
			return taxonName.getTitleCache();
		} else {
			return null;
		}
	}

	/**
	 * Returns the <code>NomRefString</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>NomRefString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNomRefString(TaxonNameBase taxonName) {
		String result = null;
		if (taxonName != null) {
			try {
				result = taxonName.getNomenclaturalMicroReference();
			} catch (Exception e) {
				logger.error("While getting NomRefString");
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * Returns the <code>DisplayName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>DisplayName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getDisplayName(TaxonNameBase taxonName) {
		// TODO: extension?
		if (taxonName != null) {
			return taxonName.getFullTitleCache();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the <code>FuzzyName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FuzzyName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFuzzyName(TaxonNameBase taxonName) {
		// TODO: extension
		return null;
	}

	/**
	 * Returns the <code>NameStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>NameStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getNameStatusFk(TaxonNameBase taxonName) {
		Integer result = null;
		
		try {
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			Set<NomenclaturalStatus> states = nonViralName.getStatus();
			if (states.size() == 1) {
				NomenclaturalStatus state = states.iterator().next();
				NomenclaturalStatusType statusType = null;
				if (state != null) {
					statusType = state.getType();
				}
				if (statusType != null) {
					result = PesiTransformer.nomStatus2nomStatusFk(statusType);
				}
			} else if (states.size() > 1) {
				logger.error("This TaxonName has more than one Nomenclatural Status: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>NameStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>NameStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNameStatusCache(TaxonNameBase taxonName) {
		String result = null;
		
		try {
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			Set<NomenclaturalStatus> states = nonViralName.getStatus();
			if (states.size() == 1) {
				NomenclaturalStatus state = states.iterator().next();
				if (state != null) {
					result = PesiTransformer.nomStatus2NomStatusCache(state.getType());
				}
			} else if (states.size() > 1) {
				logger.error("This TaxonName has more than one Nomenclatural Status: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>TaxonStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TaxonStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonStatusFk(TaxonNameBase taxonName) {
		Integer result = null;
		
		try {
		Set taxa = taxonName.getTaxa();
		if (taxa.size() == 1) {
			result = PesiTransformer.taxonBase2statusFk((TaxonBase<?>) taxa.iterator().next());
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
		}
		
		Set synonyms = taxonName.getSynonyms();
		if (synonyms.size() == 1) {
			result = PesiTransformer.taxonBase2statusFk((TaxonBase<?>) synonyms.iterator().next());
		} else if (synonyms.size() > 1) {
			logger.warn("This TaxonName has " + synonyms.size() + " Synonyms: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>TaxonStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TaxonStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonStatusCache(TaxonNameBase taxonName) {
		String result = null;
		
		try {
		Set taxa = taxonName.getTaxa();
		if (taxa.size() == 1) {
			result = PesiTransformer.taxonBase2statusCache((TaxonBase<?>) taxa.iterator().next());
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
		}
		
		Set synonyms = taxonName.getSynonyms();
		if (synonyms.size() == 1) {
			result = PesiTransformer.taxonBase2statusCache((TaxonBase<?>) synonyms.iterator().next());
		} else if (synonyms.size() > 1) {
			logger.warn("This TaxonName has " + synonyms.size() + " Synonyms: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>TypeNameFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeNameFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTypeNameFk(TaxonNameBase taxonNameBase, PesiExportState state) {
		Integer result = null;
		if (taxonNameBase != null) {
			Set<NameTypeDesignation> nameTypeDesignations = taxonNameBase.getNameTypeDesignations();
			if (nameTypeDesignations.size() == 1) {
				NameTypeDesignation nameTypeDesignation = nameTypeDesignations.iterator().next();
				if (nameTypeDesignation != null) {
					TaxonNameBase typeName = nameTypeDesignation.getTypeName();
					if (typeName != null) {
						Set<TaxonBase> taxa = typeName.getTaxa();
						if (taxa.size() == 1) {
							TaxonBase singleTaxon = taxa.iterator().next();
							result = state.getDbId(singleTaxon.getName());
						} else if (taxa.size() > 1) {
							logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
						}
					}
				}
			} else if (nameTypeDesignations.size() > 1) {
				logger.warn("This TaxonName has " + nameTypeDesignations.size() + " NameTypeDesignations: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
			}
		}
		if (result != null) {
			logger.error("Taxon Id: " + result);
			logger.error("TaxonName: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() +")");
		}
		return result;
	}
	
	/**
	 * Returns the <code>TypeFullnameCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeFullnameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTypeFullnameCache(TaxonNameBase taxonName) {
		String result = null;
		
		try {
		if (taxonName != null) {
			Set<NameTypeDesignation> nameTypeDesignations = taxonName.getNameTypeDesignations();
			if (nameTypeDesignations.size() == 1) {
				NameTypeDesignation nameTypeDesignation = nameTypeDesignations.iterator().next();
				if (nameTypeDesignation != null) {
					TaxonNameBase typeName = nameTypeDesignation.getTypeName();
					if (typeName != null) {
						result = typeName.getTitleCache();
					}
				}
			} else if (nameTypeDesignations.size() > 1) {
				logger.warn("This TaxonName has " + nameTypeDesignations.size() + " NameTypeDesignations: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>QualityStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>QualityStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getQualityStatusFk(TaxonNameBase taxonName) {
		// TODO: Not represented in CDM right now. Depends on import.
		Integer result = null;
		return result;
	}
	
	/**
	 * Returns the <code>QualityStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>QualityStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getQualityStatusCache(TaxonNameBase taxonName) {
		// TODO: Not represented in CDM right now. Depends on import.
		String result = null;
		return result;
	}
	
	/**
	 * Returns the <code>TypeDesignationStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeDesignationStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTypeDesignationStatusFk(TaxonNameBase taxonName) {
		Integer result = null;
		
		try {
		if (taxonName != null) {
			Set<NameTypeDesignation> typeDesignations = taxonName.getNameTypeDesignations();
			if (typeDesignations.size() == 1) {
				Object obj = typeDesignations.iterator().next().getTypeStatus();
				NameTypeDesignationStatus designationStatus = CdmBase.deproxy(obj, NameTypeDesignationStatus.class);
				result = PesiTransformer.nameTypeDesignationStatus2TypeDesignationStatusId(designationStatus);
			} else if (typeDesignations.size() > 1) {
				logger.error("Found a TaxonName with more than one NameTypeDesignation: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Returns the <code>TypeDesignationStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeDesignationStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTypeDesignationStatusCache(TaxonNameBase taxonName) {
		String result = null;
		
		try {
		if (taxonName != null) {
			Set<NameTypeDesignation> typeDesignations = taxonName.getNameTypeDesignations();
			if (typeDesignations.size() == 1) {
				Object obj = typeDesignations.iterator().next().getTypeStatus();
				NameTypeDesignationStatus designationStatus = CdmBase.deproxy(obj, NameTypeDesignationStatus.class);
				result = PesiTransformer.nameTypeDesignationStatus2TypeDesignationStatusCache(designationStatus);
			} else if (typeDesignations.size() > 1) {
				logger.error("Found a TaxonName with more than one NameTypeDesignation: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>FossilStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FossilStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getFossilStatusFk(TaxonNameBase taxonNameBase) {
		Integer result = null;
//		Taxon taxon;
//		if (taxonBase.isInstanceOf(Taxon.class)) {
//			taxon = CdmBase.deproxy(taxonBase, Taxon.class);
//			Set<TaxonDescription> specimenDescription = taxon.;
//			result = PesiTransformer.fossil2FossilStatusId(fossil);
//		}
		return result;
	}
	
	/**
	 * Returns the <code>FossilStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FossilStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFossilStatusCache(TaxonNameBase taxonName) {
		// TODO
		String result = null;
		return result;
	}
	
	/**
	 * Returns the <code>IdInSource</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>IdInSource</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getIdInSource(TaxonNameBase taxonName) {
		String result = null;
		String defaultResult = null;
		
		try {
			
		Set<Taxon> taxa = taxonName.getTaxa();
		Set<Synonym> synonyms = taxonName.getSynonyms();
		IdentifiableEntity singleEntity = null;
		if (taxa.size() == 1) {
			singleEntity = (IdentifiableEntity) taxa.iterator().next();
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
		}
		if (synonyms.size() == 1) {
			singleEntity = (IdentifiableEntity) synonyms.iterator().next();
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + synonyms.size() + " Synonyms: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
		}
		
		if (singleEntity != null) {
			Set<IdentifiableSource> sources = singleEntity.getSources();
			if (sources.size() == 1) {
				IdentifiableSource source = sources.iterator().next();
				if (source != null) {
					String sourceIdNameSpace = source.getIdNamespace();
					if (sourceIdNameSpace != null && sourceIdNameSpace.equals("originalGenusId")) {
						// This should never be the case for source of synonyms
						result = "Nominal Taxon from TAX_ID: " + source.getIdInSource();
					} else {
						result = "TAX_ID: " + source.getIdInSource();
					}
				}
			} else if (sources.size() > 1) {
				logger.warn("Taxon has multiple IdentifiableSources: " + singleEntity.getUuid() + " (" + singleEntity.getTitleCache() + ")");
				int count = 1;
				for (IdentifiableSource source : sources) {
					result = "TAX_ID: ";
					String sourceIdNameSpace = source.getIdNamespace();
					if (sourceIdNameSpace.equals("originalGenusId")) {
						result = "Nominal Taxon from TAX_ID: " + source.getIdInSource();
						break;
					} else {
						result += source.getIdInSource();
						if (count < sources.size()) {
							result += "; ";
						}
						count++;
					}
				}

			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Returns the <code>GUID</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>GUID</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getGUID(TaxonNameBase taxonName) {
		// TODO
		String result = null;
		return result;
	}
	
	/**
	 * Returns the <code>DerivedFromGuid</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>DerivedFromGuid</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getDerivedFromGuid(TaxonNameBase taxonName) {
		// TODO
		String result = null;
		return result;
	}
	
	/**
	 * Returns the <code>OriginalDB</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>OriginalDB</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOriginalDB(TaxonNameBase taxonName) {
		String result = "";
		try {
		IdentifiableEntity taxonBase = null;
		Set taxa = taxonName.getTaxa();
		if (taxa.size() == 1) {
			taxonBase = (IdentifiableEntity) taxa.iterator().next();
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
		}
		Set synonyms = taxonName.getSynonyms();
		if (synonyms.size() == 1) {
			taxonBase = (IdentifiableEntity) synonyms.iterator().next();
		} else if (synonyms.size() > 1) {
			logger.warn("This TaxonName has " + synonyms.size() + " Synonyms: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
		}

		if (taxonBase != null) {
			Set<IdentifiableSource> sources = taxonBase.getSources();
			if (sources.size() == 1) {
				IdentifiableSource source = sources.iterator().next();
				if (source != null) {
					ReferenceBase citation = source.getCitation();
					if (citation != null) {
						result = PesiTransformer.databaseString2Abbreviation(citation.getTitleCache());
					}
				}
			} else if (sources.size() > 1) {
				logger.warn("Taxon has multiple IdentifiableSources: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
				int count = 1;
				for (IdentifiableSource source : sources) {
					result += PesiTransformer.databaseString2Abbreviation(source.getCitation().getTitleCache());
					if (count < sources.size()) {
						result += "; ";
					}
					count++;
				}
			} else {
				result = null;
			}
		}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if ("".equals(result)) {
			return null;
		} else {
			return result;
		}
	}
	
	/**
	 * Returns the <code>LastAction</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>LastAction</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLastAction(TaxonNameBase taxonName) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>LastActionDate</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>LastActionDate</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static DateTime getLastActionDate(TaxonNameBase taxonNameBase) {
		DateTime result = null;
		
		try {
		if (taxonNameBase != null) {
			VersionableEntity taxonBase = null;
			Set taxa = taxonNameBase.getTaxa();
			if (taxa.size() == 1) {
				taxonBase  = (VersionableEntity) taxa.iterator().next();
			} else if (taxa.size() > 1) {
				logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
			}
			Set synonyms = taxonNameBase.getSynonyms();
			if (synonyms.size() == 1) {
				taxonBase  = (VersionableEntity) synonyms.iterator().next();
			} else if (synonyms.size() > 1) {
				logger.warn("This TaxonName has " + synonyms.size() + " Synonyms: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
			}
			
			if (taxonBase != null) {
				DateTime updated = taxonBase.getUpdated();
				if (updated != null) {
	//				logger.error("Taxon Updated: " + updated);
					result = new DateTime(updated.toDate()); // Unfortunately the time information gets lost here.
				}
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>ExpertName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>ExpertName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getExpertName(TaxonNameBase taxonName) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>ExpertFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>ExpertFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getExpertFk(TaxonNameBase taxonName) {
		// TODO
		return null;
	}
	
	@SuppressWarnings("unused")
	private static Integer getSourceFk(TaxonNameBase taxonName, PesiExportState state) {
		Integer result = null;
		
		try {
		TaxonBase taxonBase = null;
		Set taxa = taxonName.getTaxa();
		if (taxa.size() == 1) {
			taxonBase = CdmBase.deproxy(taxa.iterator().next(), TaxonBase.class);
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
		}
		
		Set synonyms  = taxonName.getSynonyms();
		if (synonyms.size() == 1) {
			taxonBase = CdmBase.deproxy(synonyms.iterator().next(), TaxonBase.class);
		} else if (synonyms.size() > 1) {
			logger.warn("This TaxonName has " + synonyms.size() + " Synonyms: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
		}

		if (taxonBase != null) {
			result = state.getDbId(taxonBase.getSec());
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(IdMapper.NewInstance("TaxonId"));
		mapping.addMapper(MethodMapper.NewInstance("SourceFK", this.getClass(), "getSourceFk", standardMethodParameter, PesiExportState.class));
//		mapping.addMapper(MethodMapper.NewInstance("KingdomFk", this.getClass(), "getKingdomFk", standardMethodParameter, PesiExportState.class));
//		mapping.addMapper(MethodMapper.NewInstance("RankFk", this));
//		mapping.addMapper(MethodMapper.NewInstance("RankCache", this));
		mapping.addMapper(MethodMapper.NewInstance("GenusOrUninomial", this));
		mapping.addMapper(MethodMapper.NewInstance("InfraGenericEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("SpecificEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("InfraSpecificEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("WebSearchName", this));
		mapping.addMapper(MethodMapper.NewInstance("WebShowName", this));
		mapping.addMapper(MethodMapper.NewInstance("AuthorString", this));
		mapping.addMapper(MethodMapper.NewInstance("FullName", this));
		mapping.addMapper(MethodMapper.NewInstance("NomRefString", this));
		mapping.addMapper(MethodMapper.NewInstance("DisplayName", this));
		mapping.addMapper(MethodMapper.NewInstance("FuzzyName", this));
		mapping.addMapper(MethodMapper.NewInstance("NameStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("NameStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeNameFk", this.getClass(), "getTypeNameFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TypeFullnameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeDesignationStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeDesignationStatusCache", this));
//		mapping.addMapper(MethodMapper.NewInstance("TreeIndex", this.getClass(), "getTreeIndex", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("IdInSource", this));
		mapping.addMapper(MethodMapper.NewInstance("GUID", this)); // TODO
		mapping.addMapper(MethodMapper.NewInstance("DerivedFromGuid", this)); // TODO
		mapping.addMapper(MethodMapper.NewInstance("OriginalDB", this));
		mapping.addMapper(MethodMapper.NewInstance("LastAction", this));
//		mapping.addMapper(DbTimePeriodMapper.NewInstance("updated", "LastActionDate"));
		mapping.addMapper(MethodMapper.NewInstance("LastActionDate", this));
		mapping.addMapper(MethodMapper.NewInstance("ExpertName", this)); // TODO
		mapping.addMapper(MethodMapper.NewInstance("ExpertFk", this)); // TODO

		return mapping;
	}
}
