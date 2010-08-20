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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbExtensionMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.erms.ErmsTransformer;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
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
	private PreparedStatement sqlStmt;
	private NomenclaturalCode nomenclaturalCode;
	private Integer kingdomFk;
	private HashMap<Rank, Rank> rankMap = new HashMap<Rank, Rank>();
	private List<Rank> rankList = new ArrayList<Rank>();
	private static final UUID uuidTreeIndex = UUID.fromString("28f4e205-1d02-4d3a-8288-775ea8413009");
	private AnnotationType treeIndexAnnotationType;
	private static ExtensionType lastActionExtensionType;
	private static ExtensionType lastActionDateExtensionType;
	private static ExtensionType expertNameExtensionType;
	private static ExtensionType speciesExpertNameExtensionType;
	private static ExtensionType cacheCitationExtensionType;
	
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
			String parentTaxonFk_TreeIndex_KingdomFkSql = "UPDATE Taxon SET ParentTaxonFk = ?, TreeIndex = ? WHERE TaxonId = ?"; 
			parentTaxonFk_TreeIndex_KingdomFkStmt = connection.prepareStatement(parentTaxonFk_TreeIndex_KingdomFkSql);
			
			String sql = "UPDATE Taxon SET RankFk = ?, RankCache = ?, TypeNameFk = ?, KingdomFk = ?, " +
					"ExpertFk = ?, SpeciesExpertFk = ? WHERE TaxonId = ?";
			sqlStmt = connection.prepareStatement(sql);
			
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

			// Find extensionTypes
			lastActionExtensionType = (ExtensionType)getTermService().find(PesiTransformer.lastActionUuid);
			lastActionDateExtensionType = (ExtensionType)getTermService().find(PesiTransformer.lastActionDateUuid);
			expertNameExtensionType = (ExtensionType)getTermService().find(PesiTransformer.expertNameUuid);
			speciesExpertNameExtensionType = (ExtensionType)getTermService().find(PesiTransformer.speciesExpertNameUuid);
			cacheCitationExtensionType = (ExtensionType)getTermService().find(PesiTransformer.cacheCitationUuid);

			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<TaxonNameBase> list = null;
			
			logger.error("PHASE 1: Export Taxa...");
			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getNameService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (TaxonNameBase taxonName : list) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(taxonName);
					
					// Check whether some rules are violated
					nomenclaturalCode = PesiTransformer.getNomenclaturalCode(taxonName);
					String genusOrUninomial = getGenusOrUninomial(taxonName);
					String specificEpithet = getSpecificEpithet(taxonName);
					String infraSpecificEpithet = getInfraSpecificEpithet(taxonName);
					String infraGenericEpithet = getInfraGenericEpithet(taxonName);
					Integer rank = getRankFk(taxonName, nomenclaturalCode);
					
					if (rank == null) {
						logger.error("Rank was not determined: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
					} else {
						
						// Check whether infraGenericEpithet is set correctly
						// 1. Childs of an accepted taxon of rank subgenus that are accepted taxa of rank species have to have an infraGenericEpithet
						// 2. Grandchilds of an accepted taxon of rank subgenus that are accepted taxa of rank subspecies have to have an infraGenericEpithet
						
						int ancestorLevel = 0;
						if (taxonName.getRank().equals(Rank.SUBSPECIES())) {
							// The accepted taxon two rank levels above should be of rank subgenus
							ancestorLevel  = 2;
						}
						if (taxonName.getRank().equals(Rank.SPECIES())) {
							// The accepted taxon one rank level above should be of rank subgenus
							ancestorLevel = 1;
						}
						if (ancestorLevel > 0) {
							if (ancestorOfSpecificRank(taxonName, ancestorLevel, Rank.SUBGENUS())) {
								// The child (species or subspecies) of this parent (subgenus) has to have an infraGenericEpithet
								if (infraGenericEpithet == null) {
									logger.error("InfraGenericEpithet does not exist even though it should for: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
									// maybe the taxon could be named here
								}
							}
						}
						
						if (infraGenericEpithet == null && rank.intValue() == 190) {
							logger.error("InfraGenericEpithet was not determined although it should exist for rank 190: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
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
			// 2nd Round: Add ParentTaxonFk, TreeIndex to each Taxon
			logger.error("PHASE 2: Add ParenTaxonFk and TreeIndex...");
			
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
//										logger.error("treeIndex: " + treeIndex);
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
						
						nomenclaturalCode = PesiTransformer.getNomenclaturalCode(newNode.getTaxon().getName());
						kingdomFk = PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode);
						traverseTree(newNode, parentNode, treeIndex, rankMap.get(rank), state);
						
						commitTransaction(txStatus);
						logger.error("Committed transaction.");

					}
				}
			}

			logger.error("PHASE 3: Add Rank data, KingdomFk and TypeNameFk...");
			// Be sure to add rank information, KingdomFk, TypeNameFk, expertFk and speciesExpertFk to every taxonName
			
			// Start transaction
			int pageSize = 10;
			int pageNumber = 1;
			List<ReferenceBase> referenceList;
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getNameService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (TaxonNameBase taxonName : list) {

					// Determine expertFk
					String expertName = getExpertName(taxonName);
					referenceList = getReferenceService().listByTitle(null, expertName, null, null, pageSize, pageNumber, null, null);
					Integer expertFk = null;
					if (referenceList.size() == 1) {
						expertFk  = getExpertFk(referenceList.iterator().next(), state);
					} else if (referenceList.size() > 1) {
						logger.error("Found more than one match using listByTitle() searching for a Reference with this expertName as title: " + expertName);
					} else if (referenceList.size() == 0) {
						logger.error("Found no match using listByTitle() searching for a Reference with this expertName as title: " + expertName);
					}

					// Determine speciesExpertFk
					String speciesExpertName = getSpeciesExpertName(taxonName);
					referenceList = getReferenceService().listByTitle(null, speciesExpertName, null, null, pageSize, pageNumber, null, null);
					Integer speciesExpertFk = null;
					if (referenceList.size() == 1) {
						speciesExpertFk  = getSpeciesExpertFk(referenceList.iterator().next(), state);
					} else if (referenceList.size() > 1) {
						logger.error("Found more than one match using listByTitle() searching for a Reference with this speciesExpertName as title: " + speciesExpertName);
					} else if (referenceList.size() == 0) {
						logger.error("Found no match using listByTitle() searching for a Reference with this speciesExpertName as title: " + speciesExpertName);
					}


					doCount(count++, modCount, pluralString);
					Integer typeNameFk = getTypeNameFk(taxonName, state);
					invokeRankDataAndTypeNameFkAndKingdomFk(taxonName, nomenclaturalCode, state.getDbId(taxonName), typeNameFk, kingdomFk,
							expertFk, speciesExpertFk);
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
			
			logger.error("*** Finished Making " + pluralString + " ..." + getSuccessString(success));

			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Checks whether a parent at specific level has a specific Rank.
	 * @param taxonName
	 * @param i
	 * @param subgenus
	 * @return
	 */
	private boolean ancestorOfSpecificRank(TaxonNameBase taxonName, int level,
			Rank ancestorRank) {
		boolean result = false;
		Set<Taxon> taxa = taxonName.getTaxa();
		TaxonNode parentNode = null;
		if (taxa != null && taxa.size() > 0) {
			if (taxa.size() == 1) {
				Taxon taxon = taxa.iterator().next();
				if (taxon != null) {
					// Get ancestor Taxon via TaxonNode
					
					Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
					if (taxonNodes.size() == 1) {
						TaxonNode taxonNode = taxonNodes.iterator().next();
						if (taxonNode != null) {
							for (int i = 0; i < level; i++) {
								if (taxonNode != null) {
									taxonNode  = taxonNode.getParent();
								}
							}
							parentNode = taxonNode;
						}
					} else if (taxonNodes.size() > 1) {
						logger.error("This taxon has " + taxonNodes.size() + " taxonNodes: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
					}
				}
			} else {
				logger.error("This taxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		if (parentNode != null) {
			TaxonNode node = CdmBase.deproxy(parentNode, TaxonNode.class);
			Taxon parentTaxon = node.getTaxon();
			if (parentTaxon != null) {
				TaxonNameBase parentTaxonName = parentTaxon.getName();
				if (parentTaxonName != null && parentTaxonName.getRank().equals(ancestorRank)) {
					result = true;
				}
			} else {
				logger.error("This TaxonNode has no Taxon: " + node.getUuid());
			}
		}
		return result;
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

			invokeParentTaxonFkAndTreeIndex(
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
	protected boolean invokeParentTaxonFkAndTreeIndex(Integer parentTaxonFk, Integer currentTaxonFk, StringBuffer treeIndex) {
		try {
			if (parentTaxonFk != null) {
				parentTaxonFk_TreeIndex_KingdomFkStmt.setInt(1, parentTaxonFk);
			} else {
				parentTaxonFk_TreeIndex_KingdomFkStmt.setObject(1, null);
			}

			if (treeIndex != null) {
				parentTaxonFk_TreeIndex_KingdomFkStmt.setString(2, treeIndex.toString());
			} else {
				parentTaxonFk_TreeIndex_KingdomFkStmt.setObject(2, null);
			}

			if (currentTaxonFk != null) {
				parentTaxonFk_TreeIndex_KingdomFkStmt.setInt(3, currentTaxonFk);
			} else {
				parentTaxonFk_TreeIndex_KingdomFkStmt.setObject(3, null);
			}
			
			parentTaxonFk_TreeIndex_KingdomFkStmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("ParentTaxonFk and TreeIndex could not be inserted into database: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Inserts Rank data, TypeNameFk, KingdomFk, expertFk and speciesExpertFk into the Taxon database table.
	 * @param taxonName
	 * @param nomenclaturalCode
	 * @param taxonFk
	 * @param typeNameFk
	 * @param kindomFk
	 * @param expertFk
	 * @param speciesExpertFk
	 * @return
	 */
	private boolean invokeRankDataAndTypeNameFkAndKingdomFk(TaxonNameBase taxonName, NomenclaturalCode nomenclaturalCode, 
			Integer taxonFk, Integer typeNameFk, Integer kindomFk,
			Integer expertFk, Integer speciesExpertFk) {
		try {
			Integer rankFk = getRankFk(taxonName, nomenclaturalCode);
			if (rankFk != null) {
				sqlStmt.setInt(1, rankFk);
			} else {
				sqlStmt.setObject(1, null);
			}
	
			String rankCache = getRankCache(taxonName, nomenclaturalCode);
			if (rankCache != null) {
				sqlStmt.setString(2, rankCache);
			} else {
				sqlStmt.setObject(2, null);
			}
			
			if (typeNameFk != null) {
				sqlStmt.setInt(3, typeNameFk);
			} else {
				sqlStmt.setObject(3, null);
			}
			
			if (kingdomFk != null) {
				sqlStmt.setInt(4, kingdomFk);
			} else {
				sqlStmt.setObject(4, null);
			}
			
			if (expertFk != null) {
				sqlStmt.setInt(5, expertFk);
			} else {
				sqlStmt.setObject(5, null);
			}
			
			if (speciesExpertFk != null) {
				sqlStmt.setInt(6, speciesExpertFk);
			} else {
				sqlStmt.setObject(6, null);
			}
			
			if (taxonFk != null) {
				sqlStmt.setInt(7, taxonFk);
			} else {
				sqlStmt.setObject(7, null);
			}
			
			sqlStmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("Data could not be inserted into database: " + e.getMessage());
			e.printStackTrace();
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
		try {
		if (nomenclaturalCode != null) {
			if (taxonName != null) {
				if (taxonName.getRank() == null) {
					logger.warn("Rank is null: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				} else {
					result = PesiTransformer.rank2RankId(taxonName.getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
				}
				if (result == null) {
					logger.warn("Rank could not be determined for PESI-Kingdom-Id " + PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode) + " and TaxonName " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
		if (nomenclaturalCode != null) {
			result = PesiTransformer.rank2RankCache(taxonName.getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
		}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getGenusOrUninomial();
		}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getInfraGenericEpithet();
		}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getSpecificEpithet();
		}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getInfraSpecificEpithet();
		}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
		if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
			result = nonViralName.getNameCache();
		}
		} catch (Exception e) {
			e.printStackTrace();
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
		
		try {
		List taggedName = taxonName.getTaggedName();
		boolean openTag = false;
		boolean start = true;
		for (Object object : taggedName) {
			if (object instanceof String) {
				// Name
				if (! openTag) {
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
			} else if (object instanceof Rank) {
				// Rank
				Rank rank = CdmBase.deproxy(object, Rank.class);
				
				if ("".equals(rank.getAbbreviation().trim())) {
					logger.error("Rank abbreviation is an empty string: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				} else {
					if (openTag) {
						result += "</i> ";
						openTag = false;
					} else {
						result += " ";
					}
					result += rank.getAbbreviation();
				}
			} else if (object instanceof Team) {
				if (openTag) {
					result += "</i> ";
					openTag = false;
				} else {
					result += " ";
				}
				result += object;
			} else if (object instanceof Date) {
				if (openTag) {
					result += "</i> ";
					openTag = false;
				} else {
					result += " ";
				}
				result += object;
			} else if (object instanceof ReferenceBase) {
				if (openTag) {
					result += "</i> ";
					openTag = false;
				} else {
					result += " ";
				}
				result += object;
			} else {
				logger.error("Instance unknown: " + object.getClass());
			}
		}
		if (openTag) {
			result += "</i>";
		}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
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
		try {
		if (taxonName != null && taxonName != null) {
			if (taxonName.isInstanceOf(NonViralName.class)) {
				NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
				result = nonViralName.getAuthorshipCache();
			} else {
				logger.warn("TaxonName is not of instance NonViralName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
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
		try {
		if (taxonName != null) {
			try {
				result = taxonName.getNomenclaturalMicroReference();
			} catch (Exception e) {
				logger.error("While getting NomRefString");
				e.printStackTrace();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
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
	
//	/**
//	 * Returns the <code>FuzzyName</code> attribute.
//	 * @param taxon The {@link TaxonBase Taxon}.
//	 * @return The <code>FuzzyName</code> attribute.
//	 * @see MethodMapper
//	 */
//	@SuppressWarnings("unused")
//	private static String getFuzzyName(TaxonNameBase taxonName) {
//		// TODO: extension
//		return null;
//	}

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
	private static Integer getTypeNameFk(TaxonNameBase taxonNameBase, PesiExportState state) {
		Integer result = null;
		if (taxonNameBase != null) {
			Set<NameTypeDesignation> nameTypeDesignations = taxonNameBase.getNameTypeDesignations();
			if (nameTypeDesignations.size() == 1) {
				NameTypeDesignation nameTypeDesignation = nameTypeDesignations.iterator().next();
				if (nameTypeDesignation != null) {
					TaxonNameBase typeName = nameTypeDesignation.getTypeName();
					if (typeName != null) {
						result = state.getDbId(typeName);
					}
				}
			} else if (nameTypeDesignations.size() > 1) {
				logger.warn("This TaxonName has " + nameTypeDesignations.size() + " NameTypeDesignations: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
			}
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
			
		Set<IdentifiableSource> nameSources = taxonName.getSources();
		for (IdentifiableSource nameSource : nameSources) {
			String sourceIdNameSpace = nameSource.getIdNamespace();
			if (sourceIdNameSpace != null && sourceIdNameSpace.equals("originalGenusId")) {
				result = "Nominal Taxon from TAX_ID: " + nameSource.getIdInSource();
			}
		}
		
		// Get idInSource from its Taxa or Synonyms
		if (result == null) {
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
			} else if (synonyms.size() > 1) {
				logger.warn("This TaxonName has " + synonyms.size() + " Synonyms: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
			}
			
			if (singleEntity != null) {
				Set<IdentifiableSource> sources = singleEntity.getSources();
				if (sources.size() == 1) {
					IdentifiableSource source = sources.iterator().next();
					if (source != null) {
						result = "TAX_ID: " + source.getIdInSource();
					}
				} else if (sources.size() > 1) {
					int count = 1;
					result = "TAX_ID: ";
					for (IdentifiableSource source : sources) {
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
	private static String getGUID(TaxonNameBase taxonName) {
		String result = null;
		try {
		String idInSource = getIdInSource(taxonName);
		if (idInSource != null) {
			result = "urn:lsid:faunaeur.org:taxname:" + 
			idInSource.substring(idInSource.indexOf(":") + 1, idInSource.length());
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String result = null;
		try {
		// The same as GUID for now
		result = getGUID(taxonName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 
	 * @param taxonName
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String getCacheCitation(TaxonNameBase taxonName) {
		String result = "";
		try {
		if (getOriginalDB(taxonName).equals("ERMS")) {
			// TODO: 19.08.2010: An import of CacheCitation does not exist in the ERMS import yet or it will be imported in a different way...
			// 		 So the following code is some kind of harmless assumption.
			Set<Extension> extensions = taxonName.getExtensions();
			for (Extension extension : extensions) {
				if (extension.getType().equals(cacheCitationExtensionType)) {
					result = extension.getValue();
				}
			}
		} else {
			String expertName = getExpertName(taxonName);
			String webShowName = getWebShowName(taxonName);
			
			// idInSource without text
			String idInSource = getGUID(taxonName);
			
			// build the cacheCitation
			if (expertName != null) {
				result += expertName + ". ";
			} else {
				logger.error("ExpertName could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
			if (webShowName != null) {
				result += webShowName + ". ";
			} else {
				logger.error("WebShowName could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
			
			if (getOriginalDB(taxonName).equals("FaEu")) {
				result += "Accessed through: Fauna Europaea at http://faunaeur.org/full_results.php?id=";
			} else if (getOriginalDB(taxonName).equals("EM")) {
				result += "Accessed through: Euro+Med PlantBase at http://ww2.bgbm.org/euroPlusMed/PTaxonDetail.asp?UUID=";
			}
			
			if (idInSource != null) {
				result += getGUID(taxonName);
			} else {
				logger.error("IdInSource could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>OriginalDB</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>OriginalDB</code> attribute.
	 * @see MethodMapper
	 */
	private static String getOriginalDB(TaxonNameBase taxonName) {
		String result = "";
		try {

		// Sources from TaxonName
		Set<IdentifiableSource> sources = taxonName.getSources();

		IdentifiableEntity taxonBase = null;
		if (sources == null) {
			// Sources from Taxa or Synonyms
			Set taxa = taxonName.getTaxa();
			if (taxa.size() == 1) {
				taxonBase = (IdentifiableEntity) taxa.iterator().next();
				sources  = taxonBase.getSources();
			} else if (taxa.size() > 1) {
				logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
			}
			Set synonyms = taxonName.getSynonyms();
			if (synonyms.size() == 1) {
				taxonBase = (IdentifiableEntity) synonyms.iterator().next();
				sources = taxonBase.getSources();
			} else if (synonyms.size() > 1) {
				logger.warn("This TaxonName has " + synonyms.size() + " Synonyms: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
			}
		}

		if (sources != null) {
			if (sources.size() == 1) {
				IdentifiableSource source = sources.iterator().next();
				if (source != null) {
					ReferenceBase citation = source.getCitation();
					if (citation != null) {
						result = PesiTransformer.databaseString2Abbreviation(citation.getTitleCache());
					}
				}
			} else if (sources.size() > 1) {
				int count = 1;
				for (IdentifiableSource source : sources) {
					ReferenceBase citation = source.getCitation();
					if (citation != null) {
						if (count > 1) {
							result += "; ";
						}
						result += PesiTransformer.databaseString2Abbreviation(citation.getTitleCache());
						count++;
					}
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
		String result = null;
		try {
		Set<Extension> extensions = taxonName.getExtensions();
		for (Extension extension : extensions) {
			if (extension.getType().equals(lastActionExtensionType)) {
				result = extension.getValue();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>LastActionDate</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>LastActionDate</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static DateTime getLastActionDate(TaxonNameBase taxonName) {
		DateTime result = null;
		try {
		Set<Extension> extensions = taxonName.getExtensions();
		for (Extension extension : extensions) {
			if (extension.getType().equals(lastActionDateExtensionType)) {
				String dateTime = extension.getValue();
				if (dateTime != null) {
					String date = dateTime.substring(0, dateTime.indexOf(" ") -1 );
					result = new DateTime(date);
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
	private static String getExpertName(TaxonNameBase taxonName) {
		String result = null;
		try {
		Set<Extension> extensions = taxonName.getExtensions();
		for (Extension extension : extensions) {
			if (extension.getType().equals(expertNameExtensionType)) {
				result = extension.getValue();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>ExpertFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>ExpertFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getExpertFk(ReferenceBase reference, PesiExportState state) {
		Integer result = state.getDbId(reference);
		return result;
	}
	
	/**
	 * Returns the <code>SpeciesExpertName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>SpeciesExpertName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSpeciesExpertName(TaxonNameBase taxonName) {
		String result = null;
		try {
		Set<Extension> extensions = taxonName.getExtensions();
		for (Extension extension : extensions) {
			if (extension.getType().equals(speciesExpertNameExtensionType)) {
				result = extension.getValue();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>SpeciesExpertFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>SpeciesExpertFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getSpeciesExpertFk(ReferenceBase reference, PesiExportState state) {
		Integer result = state.getDbId(reference);
		return result;
	}

	/**
	 * 
	 * @param taxonName
	 * @param state
	 * @return
	 */
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
		ExtensionType extensionType = null;
		
		mapping.addMapper(IdMapper.NewInstance("TaxonId"));
		mapping.addMapper(MethodMapper.NewInstance("SourceFK", this.getClass(), "getSourceFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("GenusOrUninomial", this));
		mapping.addMapper(MethodMapper.NewInstance("InfraGenericEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("SpecificEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("InfraSpecificEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("WebSearchName", this));
		mapping.addMapper(MethodMapper.NewInstance("WebShowName", this));
		mapping.addMapper(MethodMapper.NewInstance("AuthorString", this));
		mapping.addMapper(MethodMapper.NewInstance("FullName", this));
		mapping.addMapper(MethodMapper.NewInstance("NomRefString", this));
		
		// DisplayName
		extensionType = (ExtensionType)getTermService().find(ErmsTransformer.uuidDisplayName);		
		if (extensionType != null) {
			mapping.addMapper(DbExtensionMapper.NewInstance(extensionType, "DisplayName"));
		} else {
			mapping.addMapper(MethodMapper.NewInstance("DisplayName", this));
		}

		// FuzzyName
//		extensionType = (ExtensionType)getTermService().find(ErmsTransformer.uuidFuzzyName);
//		if (extensionType != null) {
//			mapping.addMapper(DbExtensionMapper.NewInstance(extensionType, "FuzzyName"));
//		} else {
//			mapping.addMapper(MethodMapper.NewInstance("FuzzyName", this));
//		}
		
		mapping.addMapper(MethodMapper.NewInstance("NameStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("NameStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusCache", this));
//		mapping.addMapper(MethodMapper.NewInstance("TypeNameFk", this.getClass(), "getTypeNameFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TypeFullnameCache", this));

		// QualityStatus (Fk, Cache)
		extensionType = (ExtensionType)getTermService().find(ErmsTransformer.uuidQualityStatus);
		if (extensionType != null) {
			mapping.addMapper(DbExtensionMapper.NewInstance(extensionType, "QualityStatusCache"));
		} else {
			mapping.addMapper(MethodMapper.NewInstance("QualityStatusCache", this));
		}
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusFk", this)); // PesiTransformer.QualityStatusCache2QualityStatusFk?
		
		mapping.addMapper(MethodMapper.NewInstance("TypeDesignationStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeDesignationStatusCache", this));

		// FossilStatus (Fk, Cache)
		extensionType = (ExtensionType)getTermService().find(ErmsTransformer.uuidFossilStatus);
		if (extensionType != null) {
			mapping.addMapper(DbExtensionMapper.NewInstance(extensionType, "FossilStatusCache"));
		} else {
			mapping.addMapper(MethodMapper.NewInstance("FossilStatusCache", this));
		}
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusFk", this)); // PesiTransformer.FossilStatusCache2FossilStatusFk?

		mapping.addMapper(MethodMapper.NewInstance("IdInSource", this));
		mapping.addMapper(MethodMapper.NewInstance("GUID", this));
		mapping.addMapper(MethodMapper.NewInstance("DerivedFromGuid", this));
		mapping.addMapper(MethodMapper.NewInstance("CacheCitation", this));
		mapping.addMapper(MethodMapper.NewInstance("OriginalDB", this));
		mapping.addMapper(MethodMapper.NewInstance("LastAction", this));
		mapping.addMapper(MethodMapper.NewInstance("LastActionDate", this));
		mapping.addMapper(MethodMapper.NewInstance("ExpertName", this));
//		mapping.addMapper(MethodMapper.NewInstance("ExpertFk", this.getClass(), "getExpertFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SpeciesExpertName", this));
//		mapping.addMapper(MethodMapper.NewInstance("SpeciesExpertFk", this.getClass(), "getSpeciesExpertFk", standardMethodParameter, PesiExportState.class));

		return mapping;
	}
}
