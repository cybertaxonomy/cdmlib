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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.TaxonServiceImpl;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.common.mapping.out.DbConstantMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbLastActionMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbObjectMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.IdMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.ObjectChangeMapper;
import eu.etaxonomy.cdm.io.pesi.erms.ErmsTransformer;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.profiler.ProfilerController;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.name.BacterialNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.ZooNameNoMarkerCacheStrategy;

/**
 * The export class for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase TaxonNames}.<p>
 * Inserts into DataWarehouse database table <code>Taxon</code>.
 * It is divided into four phases:<p><ul>
 * <li>Phase 1:	Export of all {@link eu.etaxonomy.cdm.model.name.TaxonNameBase TaxonNames} except some data exported in the following phases.
 * <li>Phase 2:	Export of additional data: ParenTaxonFk and TreeIndex.
 * <li>Phase 3:	Export of additional data: Rank data, KingdomFk, TypeNameFk, expertFk and speciesExpertFk.
 * <li>Phase 4:	Export of Inferred Synonyms.</ul>
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
public class PesiTaxonExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiTaxonExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = TaxonBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Taxon";
	private static final String dbTableNameSynRel = "RelTaxon";
	private static final String dbTableAdditionalSourceRel = "AdditionalTaxonSource";
	private static final String pluralString = "Taxa";
	private static final String parentPluralString = "Taxa";
	private PreparedStatement parentTaxonFk_TreeIndex_KingdomFkStmt;
	private PreparedStatement parentTaxonFkStmt;
	private PreparedStatement rankTypeExpertsUpdateStmt;
	private PreparedStatement rankUpdateStmt;
	private NomenclaturalCode nomenclaturalCode;
	private Integer kingdomFk;
	private HashMap<Rank, Rank> rank2endRankMap = new HashMap<Rank, Rank>();
	private List<Rank> rankList = new ArrayList<Rank>();
	private static final UUID uuidTreeIndex = UUID.fromString("28f4e205-1d02-4d3a-8288-775ea8413009");
	private AnnotationType treeIndexAnnotationType;
	private static ExtensionType lastActionExtensionType;
	private static ExtensionType lastActionDateExtensionType;
	private static ExtensionType expertNameExtensionType;
	private static ExtensionType speciesExpertNameExtensionType;
	private static ExtensionType cacheCitationExtensionType;
	public static NonViralNameDefaultCacheStrategy<?> zooNameStrategy = ZooNameNoMarkerCacheStrategy.NewInstance();
	public static NonViralNameDefaultCacheStrategy<?> botanicalNameStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
	public static NonViralNameDefaultCacheStrategy<?> nonViralNameStrategy = NonViralNameDefaultCacheStrategy.NewInstance();
	public static NonViralNameDefaultCacheStrategy<?> bacterialNameStrategy = BacterialNameDefaultCacheStrategy.NewInstance();
	private static int currentTaxonId;
	
	
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
	protected void doInvoke(PesiExportState state) {
		try {
			logger.info("*** Started Making " + pluralString + " ...");

			initPreparedStatements(state);
			
			// Stores whether this invoke was successful or not.
			boolean success = true;
	
			// PESI: Clear the database table Taxon.
//			doDelete(state);
			
			// Get specific mappings: (CDM) Taxon -> (PESI) Taxon
			PesiExportMapping mapping = getMapping();
			PesiExportMapping synonymRelMapping = getSynRelMapping();
			PesiExportMapping additionalSourceMapping = getAdditionalSourceMapping(state); 
			
			// Initialize the db mapper
			mapping.initialize(state);
			synonymRelMapping.initialize(state);
			additionalSourceMapping.initialize(state);
			
			// Find extensionTypes
			lastActionExtensionType = (ExtensionType)getTermService().find(PesiTransformer.lastActionUuid);
			lastActionDateExtensionType = (ExtensionType)getTermService().find(PesiTransformer.lastActionDateUuid);
			expertNameExtensionType = (ExtensionType)getTermService().find(PesiTransformer.expertNameUuid);
			speciesExpertNameExtensionType = (ExtensionType)getTermService().find(PesiTransformer.speciesExpertNameUuid);
			cacheCitationExtensionType = (ExtensionType)getTermService().find(PesiTransformer.cacheCitationUuid);
			
			//Export Taxa..
			success &= doPhase01(state, mapping, additionalSourceMapping);

			//"PHASE 1b: Handle names without taxa ...
			success &= doNames(state, additionalSourceMapping);

			
			// 2nd Round: Add ParentTaxonFk to each taxon
			success &= doPhase02(state);
			
			//PHASE 3: Add Rank data, KingdomFk, TypeNameFk ...
			success &= doPhase03(state);
			
			// 4nd Round: Add TreeIndex to each taxon
			success &= doPhase04(state);
						
			
			//"PHASE 4: Creating Inferred Synonyms...
			success &= doPhase05(state, mapping, synonymRelMapping);
			
			logger.info("*** Finished Making " + pluralString + " ..." + getSuccessString(success));

			if (!success){
				state.setUnsuccessfull();
			}
			return;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
		}
	}

	
	private void initPreparedStatements(PesiExportState state) throws SQLException {
		initTreeIndexStatement(state);
		initRankExpertsUpdateStmt(state);
		initRankUpdateStatement(state);
		
		initParentFkStatement(state);
	}

	// Prepare TreeIndex-And-KingdomFk-Statement
	private void initTreeIndexStatement(PesiExportState state) throws SQLException {
		Connection connection = state.getConfig().getDestination().getConnection();
		String parentTaxonFk_TreeIndex_KingdomFkSql = "UPDATE Taxon SET ParentTaxonFk = ?, TreeIndex = ? WHERE TaxonId = ?"; 
		parentTaxonFk_TreeIndex_KingdomFkStmt = connection.prepareStatement(parentTaxonFk_TreeIndex_KingdomFkSql);
	}

	// Prepare TreeIndex-And-KingdomFk-Statement
	private void initParentFkStatement(PesiExportState state) throws SQLException {
		Connection connection = state.getConfig().getDestination().getConnection();
		String parentTaxonFkSql = "UPDATE Taxon SET ParentTaxonFk = ? WHERE TaxonId = ?"; 
		parentTaxonFkStmt = connection.prepareStatement(parentTaxonFkSql);
	}
	
	private void initRankUpdateStatement(PesiExportState state) throws SQLException {
		Connection connection = state.getConfig().getDestination().getConnection();
		String rankSql = "UPDATE Taxon SET RankFk = ?, RankCache = ?, KingdomFk = ? WHERE TaxonId = ?";
		rankUpdateStmt = connection.prepareStatement(rankSql);
	}

	private void initRankExpertsUpdateStmt(PesiExportState state) throws SQLException {
//		String sql_old = "UPDATE Taxon SET RankFk = ?, RankCache = ?, TypeNameFk = ?, KingdomFk = ?, " +
//				"ExpertFk = ?, SpeciesExpertFk = ? WHERE TaxonId = ?";
		//TODO handle experts GUIDs
		Connection connection = state.getConfig().getDestination().getConnection();
		
		String sql = "UPDATE Taxon SET RankFk = ?, RankCache = ?, TypeNameFk = ?, KingdomFk = ? " +
				" WHERE TaxonId = ?";
		rankTypeExpertsUpdateStmt = connection.prepareStatement(sql);
	}

	private boolean doPhase01(PesiExportState state, PesiExportMapping mapping, PesiExportMapping additionalSourceMapping) throws SQLException {
		int count = 0;
		int pastCount = 0;
		List<TaxonBase> list;
		boolean success = true;
		// Get the limit for objects to save within a single transaction.
		int limit = state.getConfig().getLimitSave();

		
		logger.info("PHASE 1: Export Taxa...limit is " + limit);
		// Start transaction
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
		
		
		
		int partitionCount = 0;

		logger.info("Taking snapshot at the beginning of phase 1 of taxonExport");
		ProfilerController.memorySnapshot();
		while ((list = getNextTaxonPartition(null, limit, partitionCount++, null)) != null   ) {
			
			logger.debug("Fetched " + list.size() + " " + pluralString + ". Exporting...");
			for (TaxonBase<?> taxon : list) {
				doCount(count++, modCount, pluralString);
				TaxonNameBase<?,?> taxonName = taxon.getName();
				NonViralName<?> nvn = CdmBase.deproxy(taxonName, NonViralName.class);
								
				if (! nvn.isProtectedTitleCache()){
					nvn.setTitleCache(null, false);	
				}
				if (! nvn.isProtectedNameCache()){
					nvn.setNameCache(null, false);	
				}
				if (! nvn.isProtectedFullTitleCache()){
					nvn.setFullTitleCache(null, false);	
				}
				if (! nvn.isProtectedAuthorshipCache()){
					nvn.setAuthorshipCache(null, false);	
				}
				
				//core mapping
				success &= mapping.invoke(taxon);
				//additional source
				if (nvn.getNomenclaturalReference() != null || StringUtils.isNotBlank(nvn.getNomenclaturalMicroReference() )){
					additionalSourceMapping.invoke(taxon);
				}
				
				validatePhaseOne(taxon, nvn);
				taxon = null;
				nvn = null;
				taxonName = null;

				
				
			}
			

			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
			logger.info("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
			pastCount = count;
			/*logger.warn("Taking snapshot at the end of the loop of phase 1 of taxonExport");
			ProfilerController.memorySnapshot();
			*/
			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			
		}
		if (list == null ) {
			logger.info("No " + pluralString + " left to fetch.");
		}
		
		
		
		// Commit transaction
		commitTransaction(txStatus);
		txStatus = null;
		logger.debug("Committed transaction.");
		list = null;
		if (logger.isDebugEnabled()){
			logger.debug("Taking snapshot at the end of phase 1 of taxonExport");
			ProfilerController.memorySnapshot();
		}
		return success;
	}


	private void validatePhaseOne(TaxonBase<?> taxon, NonViralName taxonName) {
		// Check whether some rules are violated
		nomenclaturalCode = taxonName.getNomenclaturalCode();
		String genusOrUninomial = taxonName.getGenusOrUninomial();
		String specificEpithet = taxonName.getSpecificEpithet();
		String infraSpecificEpithet = taxonName.getInfraSpecificEpithet();
		String infraGenericEpithet = taxonName.getInfraGenericEpithet();
		Integer rank = getRankFk(taxonName, nomenclaturalCode);
		
		if (rank == null) {
			logger.error("Rank was not determined: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
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
				if (validateAncestorOfSpecificRank(taxon, ancestorLevel, Rank.SUBGENUS())) {
					// The child (species or subspecies) of this parent (subgenus) has to have an infraGenericEpithet
					if (infraGenericEpithet == null) {
						logger.warn("InfraGenericEpithet does not exist even though it should for: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
						// maybe the taxon could be named here
					}
				}
			}
			
			if (infraGenericEpithet == null && rank.intValue() == 190) {
				logger.warn("InfraGenericEpithet was not determined although it should exist for rank 190: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
			}
			if (specificEpithet != null && rank.intValue() < 216) {
				logger.warn("SpecificEpithet was determined for rank " + rank + " although it should only exist for ranks higher or equal to 220: TaxonName " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
			if (infraSpecificEpithet != null && rank.intValue() < 225) {
				String message = "InfraSpecificEpithet '" +infraSpecificEpithet + "' was determined for rank " + rank + " although it should only exist for ranks higher or equal to 230: "  + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")"; 
				if (StringUtils.isNotBlank(infraSpecificEpithet)){
					logger.warn(message);
				}else{
					logger.warn(message);
				}
			}
		}
		if (infraSpecificEpithet != null && specificEpithet == null) {
			logger.warn("An infraSpecificEpithet was determined, but a specificEpithet was not determined: "  + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
		}
		if (genusOrUninomial == null) {
			logger.warn("GenusOrUninomial was not determined: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
		}
	}

	
	
	/**
	 * 2nd Round: Add ParentTaxonFk to each taxon and add Biota if not exists
	 * @param state
	 * @return
	 */
	private boolean doPhase02(PesiExportState state) {
		int count = 0;
		int pastCount = 0;
		boolean success = true;
		if (! state.getConfig().isDoParentAndBiota()){
			logger.info ("Ignore PHASE 2: Make ParentFk and Biota...");
			return success;
		}
		
		List<Taxon> list;
		
		// Get the limit for objects to save within a single transaction.
		int limit = state.getConfig().getLimitSave();
		
		insertBiota(state);
		
		logger.info("PHASE 2: Make ParentFk and Biota ... limit is " + limit);
		// Start transaction
		TransactionStatus txStatus = startTransaction(true);
		int partitionCount = 0;

//		ProfilerController.memorySnapshot();
		while ((list = getNextTaxonPartition(Taxon.class, limit, partitionCount++, null)) != null   ) {
			
			logger.info("Fetched " + list.size() + " " + pluralString + ". Exporting...");
			for (Taxon taxon : list) {
				for (TaxonNode node : taxon.getTaxonNodes()){
					doCount(count++, modCount, pluralString);
					TaxonNode parentNode = node.getParent();
					if (parentNode != null){
						int childId = state.getDbId( taxon); 
						int parentId = state.getDbId(parentNode.getTaxon());
						success &= invokeParentTaxonFk(parentId, childId);
					}
				}
				
			}
			
			// Commit transaction
			commitTransaction(txStatus);
			logger.info("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
			pastCount = count;
			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			
		}
		if (list == null ) {
			logger.info("No " + pluralString + " left to fetch.");
		}
		
		// Commit transaction
		commitTransaction(txStatus);
		
		return success;
		
	}

	/**
	 * Inserts the Biota Taxon if not yet exists.
	 * @param state
	 * @throws SQLException
	 */
	private void insertBiota(PesiExportState state) {
		try {
			ResultSet rs = state.getConfig().getDestination().getResultSet("SELECT * FROM Taxon WHERE GenusOrUninomial = 'Biota' ");
			if (rs.next() == false){
				int biotaId = state.getConfig().getNameIdStart() -1 ;
				String sqlInsertBiota = "INSERT INTO Taxon (TaxonId, KingdomFk, RankFk, RankCache, GenusOrUninomial, WebSearchName, WebShowName, FullName, DisplayName, TaxonStatusFk, TaxonStatusCache) " +
									       " VALUES (" + biotaId + ",    0,    0,   'Superdomain',   'Biota',          'Biota',  '<i>Biota</i>',   'Biota', '<i>Biota</i>',  1 ,      'accepted')";
				state.getConfig().getDestination().update(sqlInsertBiota);
			}
		} catch (SQLException e) {
			logger.warn ("Biota could not be requested or inserted");
		}
	}
	
	// 4th round: Add TreeIndex to each taxon
	private boolean doPhase04(PesiExportState state) {
		boolean success = true;
		
		logger.info("PHASE 4: Make TreeIndex ... ");
	
		//TODO test if possible to move to phase 02 
		String sql = " UPDATE Taxon SET ParentTaxonFk = (Select TaxonId from Taxon where RankFk = 0) " +
				" WHERE (RankFk = 10) and TaxonStatusFk = 1 ";
		state.getConfig().getDestination().update(sql);
		
		state.getConfig().getDestination().update("EXEC dbo.recalculateallstoredpaths");
		
		return success;
		
	}
	
	
	// 2nd Round: Add ParentTaxonFk, TreeIndex to each Taxon
	private boolean doPhase02_OLD(PesiExportState state) {
		boolean success = true;
		if (! state.getConfig().isDoTreeIndex()){
			logger.info ("Ignore PHASE 2: ParentTaxonFk and TreeIndex");
			return success;
		}
		
		List<Classification> classificationList = null;
		logger.info("PHASE 2: Add ParenTaxonFk and TreeIndex...");
		
		// Specify starting ranks for tree traversing
		rankList.add(Rank.KINGDOM());
		rankList.add(Rank.GENUS());

		// Specify where to stop traversing (value) when starting at a specific Rank (key)
		rank2endRankMap.put(Rank.GENUS(), null); // Since NULL does not match an existing Rank, traverse all the way down to the leaves
		rank2endRankMap.put(Rank.KINGDOM(), Rank.GENUS()); // excludes rank genus
		
		StringBuffer treeIndex = new StringBuffer();
		
		// Retrieve list of classifications
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started transaction for parentFk and treeIndex. Fetching all classifications...");
		classificationList = getClassificationService().listClassifications(null, 0, null, null);
		commitTransaction(txStatus);
		logger.debug("Committed transaction.");

		logger.info("Fetched " + classificationList.size() + " classification(s).");

		setTreeIndexAnnotationType(getAnnotationType(uuidTreeIndex, "TreeIndex", "TreeIndex", "TI"));
		List<TaxonNode> rankSpecificRootNodes;
		for (Classification classification : classificationList) {
			for (Rank rank : rankList) {
				
				txStatus = startTransaction(true);
				logger.info("Started transaction to fetch all rootNodes specific to Rank " + rank.getLabel() + " ...");

				rankSpecificRootNodes = getClassificationService().loadRankSpecificRootNodes(classification, rank, null, null, null);
				logger.info("Fetched " + rankSpecificRootNodes.size() + " RootNodes for Rank " + rank.getLabel());

				commitTransaction(txStatus);
				logger.debug("Committed transaction.");

				for (TaxonNode rootNode : rankSpecificRootNodes) {
					txStatus = startTransaction(false);
					Rank endRank = rank2endRankMap.get(rank);
					if (endRank != null) {
						logger.debug("Started transaction to traverse childNodes of rootNode (" + rootNode.getUuid() + ") till Rank " + endRank.getLabel() + " ...");
					} else {
						logger.debug("Started transaction to traverse childNodes of rootNode (" + rootNode.getUuid() + ") till leaves are reached ...");
					}

					TaxonNode newNode = getTaxonNodeService().load(rootNode.getUuid());

					if (isPesiTaxon(newNode.getTaxon())){
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
	//									logger.error("treeIndex: " + treeIndex);
										break;
									}
								}
								if (!annotationFound) {
									// This should not happen because it means that the treeIndex was not set correctly as an annotation to parentNode
									logger.error("TreeIndex could not be read from annotation of TaxonNode: " + parentNode.getUuid() + ", Taxon: " + parentNode.getTaxon().getUuid());
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
						traverseTree(newNode, parentNode, treeIndex, endRank, state);
						parentNode =null;
					}else{
						logger.debug("Taxon is not a PESI taxon: " + newNode.getTaxon().getUuid());
					}
					
					newNode = null;
					
					try {
						commitTransaction(txStatus);
						logger.debug("Committed transaction.");
					} catch (Exception e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}

				}
				rankSpecificRootNodes = null;
			}
			
		}
		
		logger.warn("Taking snapshot at the end of phase 2 of taxonExport");
		ProfilerController.memorySnapshot();
		return success;
	}	

	//PHASE 3: Add Rank data, KingdomFk, TypeNameFk, expertFk and speciesExpertFk...
	private boolean doPhase03(PesiExportState state) {
		int count = 0;
		int pastCount = 0;
		boolean success = true;
		if (! state.getConfig().isDoTreeIndex()){
			logger.info ("Ignore PHASE 3: Add Rank data, KingdomFk, TypeNameFk, expertFk and speciesExpertFk...");
			return success;
		}
		// Get the limit for objects to save within a single transaction.
		int limit = state.getConfig().getLimitSave();

		List<TaxonBase> list;
		logger.info("PHASE 3: Add Rank data, KingdomFk, TypeNameFk, expertFk and speciesExpertFk...");
		// Be sure to add rank information, KingdomFk, TypeNameFk, expertFk and speciesExpertFk to every taxonName
		
		// Start transaction
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started new transaction for rank, kingdom, typeName, expertFk and speciesExpertFK. Fetching some " + pluralString + " (max: " + limit + ") ...");
		int partitionCount = 0;
		while ((list = getNextTaxonPartition(TaxonBase.class, limit, partitionCount++, null)) != null) {

			logger.debug("Fetched " + list.size() + " " + pluralString + ". Exporting...");
			for (TaxonBase<?> taxon : list) {
				TaxonNameBase<?,?> taxonName = taxon.getName();
				// Determine expertFk
//				Integer expertFk = makeExpertFk(state, taxonName);
//
//				// Determine speciesExpertFk
//				Integer speciesExpertFk = makeSpeciesExpertFk(state, taxonName);

				doCount(count++, modCount, pluralString);
				Integer typeNameFk = getTypeNameFk(taxonName, state);
				kingdomFk = PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode);
				
				//TODO why are expertFks needed? (Andreas M.)
//				if (expertFk != null || speciesExpertFk != null) {
					invokeRankDataAndTypeNameFkAndKingdomFk(taxonName, nomenclaturalCode, state.getDbId(taxon), 
							typeNameFk, kingdomFk, state);
//				}
					
					taxon = null;
					taxonName = null;
			}

			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
			logger.info("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
			pastCount = count;

			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction for rank, kingdom, typeName, expertFk and speciesExpertFK. Fetching some " + pluralString + " (max: " + limit + ") ...");
		}
		if (list == null) {
			logger.info("No " + pluralString + " left to fetch.");
		}
		
		list = null;
		
		// Commit transaction
		commitTransaction(txStatus);
		
		logger.debug("Committed transaction.");
		logger.debug("Try to take snapshot at the end of phase 3 of taxonExport, number of partitions: " + partitionCount);
		ProfilerController.memorySnapshot();
		return success;
	}
	
	//	"PHASE 5: Creating Inferred Synonyms..."
	private boolean doPhase05(PesiExportState state, PesiExportMapping mapping, PesiExportMapping synRelMapping) throws SQLException {
		int count;
		int pastCount;
		boolean success = true;
		// Get the limit for objects to save within a single transaction.
		if (! state.getConfig().isDoInferredSynonyms()){
			logger.info ("Ignore PHASE 5: Creating Inferred Synonyms...");
			return success;
		}
		
		int limit = state.getConfig().getLimitSave();
		// Create inferred synonyms for accepted taxa
		logger.info("PHASE 4: Creating Inferred Synonyms...");

		// Determine the count of elements in datawarehouse database table Taxon
		currentTaxonId = determineTaxonCount(state);
		currentTaxonId++;

		count = 0;
		pastCount = 0;
		int pageSize = limit;
		int pageNumber = 1;
		String inferredSynonymPluralString = "Inferred Synonyms";
		
		// Start transaction
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
		List<TaxonBase> taxonList = null;
		
		
		
		while ((taxonList  = getTaxonService().listTaxaByName(Taxon.class, "*", "*", "*", "*", Rank.SPECIES(), pageSize, pageNumber)).size() > 0) {
			HashMap<Integer, TaxonNameBase<?,?>> inferredSynonymsDataToBeSaved = new HashMap<Integer, TaxonNameBase<?,?>>();

			logger.info("Fetched " + taxonList.size() + " " + parentPluralString + ". Exporting...");
			inferredSynonymsDataToBeSaved.putAll(createInferredSynonymsForTaxonList(state, mapping,
					synRelMapping, taxonList));
			
			doCount(count += taxonList.size(), modCount, inferredSynonymPluralString);
			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
			logger.info("Exported " + (taxonList.size()) + " " + inferredSynonymPluralString + ". Total: " + count);
			//pastCount = count;
			
			// Save Rank Data and KingdomFk for inferred synonyms
			for (Integer taxonFk : inferredSynonymsDataToBeSaved.keySet()) {
				invokeRankDataAndKingdomFk(inferredSynonymsDataToBeSaved.get(taxonFk), nomenclaturalCode, taxonFk, kingdomFk, state);
			}

			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
			
			// Increment pageNumber
			pageNumber++;
		}
		
		while ((taxonList  = getTaxonService().listTaxaByName(Taxon.class, "*", "*", "*", "*", Rank.SUBSPECIES(), pageSize, pageNumber)).size() > 0) {
			HashMap<Integer, TaxonNameBase<?,?>> inferredSynonymsDataToBeSaved = new HashMap<Integer, TaxonNameBase<?,?>>();

			logger.info("Fetched " + taxonList.size() + " " + parentPluralString + ". Exporting...");
			inferredSynonymsDataToBeSaved.putAll(createInferredSynonymsForTaxonList(state, mapping,
					synRelMapping, taxonList));
			;
			doCount(count += taxonList.size(), modCount, inferredSynonymPluralString);
			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
			logger.info("Exported " + taxonList.size()+ " " + inferredSynonymPluralString + ". Total: " + count);
			//pastCount = count;
			
			// Save Rank Data and KingdomFk for inferred synonyms
			for (Integer taxonFk : inferredSynonymsDataToBeSaved.keySet()) {
				invokeRankDataAndKingdomFk(inferredSynonymsDataToBeSaved.get(taxonFk), nomenclaturalCode, taxonFk, kingdomFk, state);
			}

			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
			
			// Increment pageNumber
			pageNumber++;
		}
		if (taxonList.size() == 0) {
			logger.info("No " + parentPluralString + " left to fetch.");
		}
		
		taxonList = null;
//		logger.warn("Taking snapshot at the end of phase 4 of taxonExport");
//		ProfilerController.memorySnapshot();
		
		// Commit transaction
		commitTransaction(txStatus);
		System.gc();
		logger.debug("Taking snapshot at the end of phase 4 after gc() of taxonExport");
		ProfilerController.memorySnapshot();
		logger.debug("Committed transaction.");
		return success;
	}

	/**
	 * @param state
	 * @param mapping
	 * @param synRelMapping
	 * @param currentTaxonId
	 * @param taxonList
	 * @param inferredSynonymsDataToBeSaved
	 * @return
	 */
	private HashMap<Integer, TaxonNameBase<?, ?>> createInferredSynonymsForTaxonList(PesiExportState state,
			PesiExportMapping mapping, PesiExportMapping synRelMapping,	 List<TaxonBase> taxonList) {
		
		Taxon acceptedTaxon;
		Classification classification = null;
		List<Synonym> inferredSynonyms = null;
		boolean localSuccess = true;
		
		HashMap<Integer, TaxonNameBase<?,?>> inferredSynonymsDataToBeSaved = new HashMap<Integer, TaxonNameBase<?,?>>();
		
		for (TaxonBase<?> taxonBase : taxonList) {
		
			if (taxonBase.isInstanceOf(Taxon.class)) { // this should always be the case since we should have fetched accepted taxon only, but you never know...
				acceptedTaxon = CdmBase.deproxy(taxonBase, Taxon.class);
				TaxonNameBase<?,?> taxonName = acceptedTaxon.getName();
				
				if (taxonName.isInstanceOf(ZoologicalName.class)) {
					nomenclaturalCode  = taxonName.getNomenclaturalCode();
					kingdomFk = PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode);

					Set<TaxonNode> taxonNodes = acceptedTaxon.getTaxonNodes();
					TaxonNode singleNode = null;
					
					if (taxonNodes.size() > 0) {
						// Determine the classification of the current TaxonNode
						
						singleNode = taxonNodes.iterator().next();
						if (singleNode != null) {
							classification = singleNode.getClassification();
						} else {
							logger.error("A TaxonNode belonging to this accepted Taxon is NULL: " + acceptedTaxon.getUuid() + " (" + acceptedTaxon.getTitleCache() +")");
						}
					} else {
						// Classification could not be determined directly from this TaxonNode
						// The stored classification from another TaxonNode is used. It's a simple, but not a failsafe fallback solution.
						if (taxonNodes.size() == 0) {
							logger.error("Classification could not be determined directly from this Taxon: " + acceptedTaxon.getUuid() + " is misapplication? "+acceptedTaxon.isMisapplication()+ "). The classification of the last taxon is used");
						
						}
					}
					
					if (classification != null) {
						try{
							TaxonNameBase name = acceptedTaxon.getName();
							//if (name.isSpecies() || name.isInfraSpecific()){
								inferredSynonyms  = getTaxonService().createAllInferredSynonyms(acceptedTaxon, classification, true);
							//}
//								inferredSynonyms = getTaxonService().createInferredSynonyms(classification, acceptedTaxon, SynonymRelationshipType.INFERRED_GENUS_OF());
							if (inferredSynonyms != null) {
								for (Synonym synonym : inferredSynonyms) {
//									TaxonNameBase<?,?> synonymName = synonym.getName();
									MarkerType markerType =getUuidMarkerType(PesiTransformer.uuidMarkerGuidIsMissing, state);
									synonym.addMarker(Marker.NewInstance(markerType, true));
									// Both Synonym and its TaxonName have no valid Id yet
									synonym.setId(currentTaxonId++);
									
									
									localSuccess &= mapping.invoke(synonym);
									//get SynonymRelationship and export
									if (synonym.getSynonymRelations().isEmpty() ){
										SynonymRelationship synRel;					
										IdentifiableSource source = synonym.getSources().iterator().next();
										if (source.getIdNamespace().contains("Potential combination")){
											synRel = acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.POTENTIAL_COMBINATION_OF());
											logger.error(synonym.getTitleCache() + " has no synonym relationship to " + acceptedTaxon.getTitleCache() + " type is set to potential combination");
										} else if (source.getIdNamespace().contains("Inferred Genus")){
											synRel = acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.INFERRED_GENUS_OF());
											logger.error(synonym.getTitleCache() + " has no synonym relationship to " + acceptedTaxon.getTitleCache() + " type is set to inferred genus");
										} else if (source.getIdNamespace().contains("Inferred Epithet")){
											synRel = acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.INFERRED_EPITHET_OF());
											logger.error(synonym.getTitleCache() + " has no synonym relationship to " + acceptedTaxon.getTitleCache() + " type is set to inferred epithet");
										} else{
											synRel = acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.INFERRED_SYNONYM_OF());
											logger.error(synonym.getTitleCache() + " has no synonym relationship to " + acceptedTaxon.getTitleCache() + " type is set to inferred synonym");
										}
										
										localSuccess &= synRelMapping.invoke(synRel);
										if (!localSuccess) {
											logger.error("Synonym relationship export failed " + synonym.getTitleCache() + " accepted taxon: " + acceptedTaxon.getUuid() + " (" + acceptedTaxon.getTitleCache()+")");
										}
										synRel = null;
									} else {
										for (SynonymRelationship synRel: synonym.getSynonymRelations()){
											localSuccess &= synRelMapping.invoke(synRel);
											if (!localSuccess) {
												logger.error("Synonym relationship export failed " + synonym.getTitleCache() + " accepted taxon: " + acceptedTaxon.getUuid() + " (" + acceptedTaxon.getTitleCache()+")");
											} else {
												logger.info("Synonym relationship successfully exported: " + synonym.getTitleCache() + "  " +acceptedTaxon.getUuid() + " (" + acceptedTaxon.getTitleCache()+")");
											}
											synRel = null;
										}
									}
									
									inferredSynonymsDataToBeSaved.put(synonym.getId(), synonym.getName());
								}
							}
						}catch(Exception e){
							logger.error(e.getMessage());
							e.printStackTrace();
						}
					} else {
						logger.error("Classification is NULL. Inferred Synonyms could not be created for this Taxon: " + acceptedTaxon.getUuid() + " (" + acceptedTaxon.getTitleCache() + ")");
					}
				} else {
//							logger.error("TaxonName is not a ZoologicalName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				}
			} else {
				logger.error("This TaxonBase is not a Taxon even though it should be: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
			}
		}
		return inferredSynonymsDataToBeSaved;
	}
	

	/**
	 * Handles names that do not appear in taxa
	 * @param state
	 * @param mapping
	 * @return
	 */
	private boolean doNames(PesiExportState state, PesiExportMapping additionalSourceMapping)  throws SQLException {
		
		boolean success = true;
		if (! state.getConfig().isDoPureNames()){
			logger.info ("Ignore PHASE 1b: PureNames");
			return success;
		}
		
		try {
			PesiExportMapping mapping = getPureNameMapping(state);
			mapping.initialize(state);
			int count = 0;
			int pastCount = 0;
			List<NonViralName<?>> list;
			success = true;
			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();

			
			logger.info("PHASE 1b: Export Pure Names ...");
			// Start transaction
			TransactionStatus txStatus = startTransaction(true);
			logger.info("Started new transaction for Pure Names. Fetching some " + pluralString + " (max: " + limit + ") ...");
			
			int partitionCount = 0;
			while ((list = getNextPureNamePartition(null, limit, partitionCount++)) != null   ) {

				logger.info("Fetched " + list.size() + " names without taxa. Exporting...");
				for (TaxonNameBase<?,?> taxonName : list) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(taxonName);
					//additional source
					if (taxonName.getNomenclaturalReference() != null || StringUtils.isNotBlank(taxonName.getNomenclaturalMicroReference() )){
						additionalSourceMapping.invoke(taxonName);
					}
				}

				// Commit transaction
				commitTransaction(txStatus);
				logger.debug("Committed transaction.");
				logger.info("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;

				// Start transaction
				txStatus = startTransaction(true);
				logger.info("Started new transaction for PureNames. Fetching some " + pluralString + " (max: " + limit + ") ...");
			}
			if (list == null) {
				logger.info("No " + pluralString + " left to fetch.");
			}
			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
		} catch (Exception e) {
			logger.error("Error occurred in pure name export");
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	/**
	 * Determines the current number of entries in the DataWarehouse database table <code>Taxon</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The count.
	 */
	private Integer determineTaxonCount(PesiExportState state) {
		Integer result = null;
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();
		sql = "SELECT max(taxonId) FROM Taxon";
		destination.setQuery(sql);
		ResultSet resultSet = destination.getResultSet();
		try {
			resultSet.next();
			result = resultSet.getInt(1);
		} catch (SQLException e) {
			logger.error("TaxonCount could not be determined: " + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Checks whether a parent at specific level has a specific Rank.
	 * @param taxonName A {@link TaxonNameBase TaxonName}.
	 * @param level The ancestor level.
	 * @param ancestorRank The ancestor rank.
	 * @return Whether a parent at a specific level has a specific Rank.
	 */
	private boolean validateAncestorOfSpecificRank(TaxonBase<?> taxonBase, int level, Rank ancestorRank) {
		boolean result = false;
		TaxonNode parentNode = null;
		if (taxonBase.isInstanceOf(Taxon.class)){
			Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
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
		//compare
		if (parentNode != null) {
			TaxonNode node = CdmBase.deproxy(parentNode, TaxonNode.class);
			Taxon parentTaxon = node.getTaxon();
			if (parentTaxon != null) {
				TaxonNameBase<?,?> parentTaxonName = parentTaxon.getName();
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
	 * @param uuid The Annotation UUID.
	 * @param label The Annotation label.
	 * @param text The Annotation text.
	 * @param labelAbbrev The Annotation label abbreviation.
	 * @return The AnnotationType.
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
	 * Traverses the classification recursively and stores determined values for every Taxon.
	 * @param childNode The {@link TaxonNode TaxonNode} to process.
	 * @param parentNode The parent {@link TaxonNode TaxonNode} of the childNode.
	 * @param treeIndex The TreeIndex at the current level.
	 * @param fetchLevel Rank to stop fetching at.
	 * @param state The {@link PesiExportState PesiExportState}.
	 */
	private void traverseTree(TaxonNode childNode, TaxonNode parentNode, StringBuffer treeIndex, Rank fetchLevel, PesiExportState state) {
		// Traverse all branches from this childNode until specified fetchLevel is reached.
		StringBuffer localTreeIndex = new StringBuffer(treeIndex);
		Taxon childTaxon = childNode.getTaxon();
		if (childTaxon != null) {
			if (isPesiTaxon(childTaxon)){
				Integer taxonId = state.getDbId(childTaxon);
				TaxonNameBase<?,?> childName = childTaxon.getName();
				if (taxonId != null) {
					Rank childRank = childName.getRank();
					if (childRank != null) {
						if (! childRank.equals(fetchLevel)) {
	
							localTreeIndex.append(taxonId + "#");
							
							saveData(childNode, parentNode, localTreeIndex, state, taxonId);
	
							// Store treeIndex as annotation for further use
							Annotation annotation = Annotation.NewInstance(localTreeIndex.toString(), getTreeIndexAnnotationType(), Language.DEFAULT());
							childNode.addAnnotation(annotation);
	
							for (TaxonNode newNode : childNode.getChildNodes()) {
								if (newNode.getTaxon() != null && isPesiTaxon(newNode.getTaxon())){
									traverseTree(newNode, childNode, localTreeIndex, fetchLevel, state);
								}
							}
							
						} else {
	//						logger.debug("Target Rank " + fetchLevel.getLabel() + " reached");
							return;
						}
					} else {
						logger.error("Rank is NULL. FetchLevel can not be checked: " + childName.getUuid() + " (" + childName.getTitleCache() + ")");
					}
				} else {
					logger.error("Taxon can not be found in state: " + childTaxon.getUuid() + " (" + childTaxon.getTitleCache() + ")");
				}
			}else{
				if (logger.isDebugEnabled()){ 
					logger.debug("Taxon is not a PESI taxon: " + childTaxon.getUuid());
				}
			}

		} else {
			logger.error("Taxon is NULL for TaxonNode: " + childNode.getUuid());
		}
	}

	/**
	 * Stores values in database for every recursive round.
	 * @param childNode The {@link TaxonNode TaxonNode} to process.
	 * @param parentNode The parent {@link TaxonNode TaxonNode} of the childNode.
	 * @param treeIndex The TreeIndex at the current level.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @param currentTaxonFk The TaxonFk to store the values for.
	 */
	private void saveData(TaxonNode childNode, TaxonNode parentNode, StringBuffer treeIndex, PesiExportState state, Integer currentTaxonFk) {
		// We are differentiating kingdoms by the nomenclatural code for now.
		// This needs to be handled in a better way as soon as we know how to differentiate between more kingdoms.
		Taxon childTaxon = childNode.getTaxon();
		if (isPesiTaxon(childTaxon)) {
			TaxonBase<?> parentTaxon = null;
			if (parentNode != null) {
				parentTaxon = parentNode.getTaxon();
				
			}

			invokeParentTaxonFkAndTreeIndex(state.getDbId(parentTaxon), currentTaxonFk,	treeIndex);
		}
		
	}

	/**
	 * Inserts values into the Taxon database table.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @param stmt The prepared statement.
	 * @return Whether save was successful or not.
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
			logger.error("ParentTaxonFk (" + parentTaxonFk ==null? "-":parentTaxonFk + ") and TreeIndex could not be inserted into database for taxon "+ (currentTaxonFk == null? "-" :currentTaxonFk) + ": " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	protected boolean invokeParentTaxonFk(Integer parentId, Integer childId) {
		try {
			parentTaxonFkStmt.setInt(1, parentId);
			parentTaxonFkStmt.setInt(2, childId);
			parentTaxonFkStmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.warn("ParentTaxonFk (" + parentId ==null? "-":parentId + ") could not be inserted into database for taxon "+ (childId == null? "-" :childId) + ": " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}


	/**
	 * Inserts Rank data and KingdomFk into the Taxon database table.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param nomenclaturalCode The {@link NomenclaturalCode NomenclaturalCode}.
	 * @param taxonFk The TaxonFk to store the values for.
	 * @param state 
	 * @param kindomFk The KingdomFk.
	 * @return Whether save was successful or not.
	 */
	private boolean invokeRankDataAndKingdomFk(TaxonNameBase<?,?> taxonName, NomenclaturalCode nomenclaturalCode, Integer taxonFk, Integer kingdomFk, PesiExportState state) {
		try {
			Integer rankFk = getRankFk(taxonName, nomenclaturalCode);
			if (rankFk != null) {
				rankUpdateStmt.setInt(1, rankFk);
			} else {
				rankUpdateStmt.setObject(1, null);
			}
	
			String rankCache = getRankCache(taxonName, nomenclaturalCode, state);
			if (rankCache != null) {
				rankUpdateStmt.setString(2, rankCache);
			} else {
				rankUpdateStmt.setObject(2, null);
			}
			
			if (kingdomFk != null) {
				rankUpdateStmt.setInt(3, kingdomFk);
			} else {
				rankUpdateStmt.setObject(3, null);
			}
			
			if (taxonFk != null) {
				rankUpdateStmt.setInt(4, taxonFk);
			} else {
				rankUpdateStmt.setObject(4, null);
			}
			
			rankUpdateStmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("Data (RankFk, RankCache, KingdomFk) could not be inserted into database: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Inserts Rank data, TypeNameFk, KingdomFk, expertFk and speciesExpertFk into the Taxon database table.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param nomenclaturalCode The {@link NomenclaturalCode NomenclaturalCode}.
	 * @param taxonFk The TaxonFk to store the values for.
	 * @param typeNameFk The TypeNameFk.
	 * @param state 
	 * @param kindomFk The KingdomFk.
	 * @param expertFk The ExpertFk.
	 * @param speciesExpertFk The SpeciesExpertFk.
	 * @return Whether save was successful or not.
	 */
	private boolean invokeRankDataAndTypeNameFkAndKingdomFk(TaxonNameBase<?,?> taxonName, NomenclaturalCode nomenclaturalCode, 
			Integer taxonFk, Integer typeNameFk, Integer kingdomFkk, PesiExportState state) {
		try {
			int index = 1;
			Integer rankFk = getRankFk(taxonName, nomenclaturalCode);
			if (rankFk != null) {
				rankTypeExpertsUpdateStmt.setInt(index++, rankFk);
			} else {
				rankTypeExpertsUpdateStmt.setObject(index++, null);
			}
	
			String rankCache = getRankCache(taxonName, nomenclaturalCode, state);
			if (rankCache != null) {
				rankTypeExpertsUpdateStmt.setString(index++, rankCache);
			} else {
				rankTypeExpertsUpdateStmt.setObject(index++, null);
			}
			
			if (typeNameFk != null) {
				rankTypeExpertsUpdateStmt.setInt(index++, typeNameFk);
			} else {
				rankTypeExpertsUpdateStmt.setObject(index++, null);
			}
			
			if (kingdomFk != null) {
				rankTypeExpertsUpdateStmt.setInt(index++, kingdomFk);
			} else {
				rankTypeExpertsUpdateStmt.setObject(index++, null);
			}
			
//			if (expertFk != null) {
//				rankTypeExpertsUpdateStmt.setInt(5, expertFk);
//			} else {
//				rankTypeExpertsUpdateStmt.setObject(5, null);
//			}
//
//			//TODO handle experts GUIDS
//			if (speciesExpertFk != null) {
//				rankTypeExpertsUpdateStmt.setInt(6, speciesExpertFk);
//			} else {
//				rankTypeExpertsUpdateStmt.setObject(6, null);
//			}
//			
			if (taxonFk != null) {
				rankTypeExpertsUpdateStmt.setInt(index++, taxonFk);
			} else {
				rankTypeExpertsUpdateStmt.setObject(index++, null);
			}

			rankTypeExpertsUpdateStmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("Data could not be inserted into database: " + e.getMessage());
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			logger.error("Some exception occurred: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes all entries of database tables related to <code>Taxon</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
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
	 * Creates the kingdom fk.
	 * @param taxonName
	 * @return
	 */
	@SuppressWarnings("unused")  //used by mapper
	private static Integer getKingdomFk(TaxonNameBase taxonName){
		return PesiTransformer.nomenClaturalCode2Kingdom(taxonName.getNomenclaturalCode());
	}
	
	/**
	 * Creates the parent fk.
	 * @param taxonName
	 * @return
	 */
	@SuppressWarnings("unused")  //used by mapper
	private static Integer getParentTaxonFk(TaxonBase<?> taxonBase, PesiExportState state){
		if (taxonBase.isInstanceOf(Taxon.class)){
			Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
			if (! isMisappliedName(taxon)){
				Set<TaxonNode> nodes = taxon.getTaxonNodes();
				if (nodes.size() == 0){
					if (taxon.getName().getRank().isLower(Rank.KINGDOM())){
						logger.warn("Accepted taxon has no parent. " + taxon.getTitleCache() + ", " +  taxon.getUuid());
					}
				}else if (nodes.size() > 1){
					logger.warn("Taxon has more than 1 node attached. This is not supported by PESI export." +  taxon.getTitleCache() + ", " +  taxon.getUuid());
				}else{
					Taxon parent =nodes.iterator().next().getParent().getTaxon();
					return state.getDbId(parent);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the rankFk for the taxon name based on the names nomenclatural code.
	 * You may not use this method for kingdoms other then Animalia, Plantae and Bacteria.
	 * @param taxonName
	 * @return
	 */
	@SuppressWarnings("unused")  //used by mapper
	private static Integer getRankFk(TaxonNameBase<?,?> taxonName) {
		return getRankFk(taxonName, taxonName.getNomenclaturalCode());
	}
		
	
	/**
	 * Returns the <code>RankFk</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param nomenclaturalCode The {@link NomenclaturalCode NomenclaturalCode}.
	 * @return The <code>RankFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getRankFk(TaxonNameBase<?,?> taxonName, NomenclaturalCode nomenclaturalCode) {
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
	 * Returns the rank cache for the taxon name based on the names nomenclatural code.
	 * You may not use this method for kingdoms other then Animalia, Plantae and Bacteria.
	 * @param taxonName
	 * @return
	 */
	@SuppressWarnings("unused")  //used by mapper
	private static String getRankCache(TaxonNameBase<?,?> taxonName, PesiExportState state) {
		return getRankCache(taxonName, taxonName.getNomenclaturalCode(), state);
	}

	
	/**
	 * Returns the <code>RankCache</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param nomenclaturalCode The {@link NomenclaturalCode NomenclaturalCode}.
	 * @param state 
	 * @return The <code>RankCache</code> attribute.
	 * @see MethodMapper
	 */
	private static String getRankCache(TaxonNameBase<?,?> taxonName, NomenclaturalCode nomenclaturalCode, PesiExportState state) {
		if (nomenclaturalCode != null) {
			return state.getTransformer().getCacheByRankAndKingdom(taxonName.getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
		}else{
			logger.warn("No nomenclatural code defined for name " + taxonName.getUuid());
			return null;
		}
		
	}

	
	/**
	 * Returns the <code>DisplayName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>DisplayName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")  //used by Mapper
	private static String getDisplayName(TaxonBase<?> taxon) {
		TaxonNameBase<?,?> taxonName = taxon.getName();
		String result = getDisplayName(taxonName);
		if (isMisappliedName(taxon)){
			result = result + " " + getAuthorString(taxon);
		}
		return result;
	}
	
	/**
	 * Returns the <code>AuthorString</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>AuthorString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused") //used by mapper
	protected static String getAuthorString(TaxonBase<?> taxon) {
		try {
			String result = null;
			boolean isNonViralName = false;
			String authorshipCache = null;
			TaxonNameBase<?,?> taxonName = taxon.getName();
			if (taxonName != null && taxonName.isInstanceOf(NonViralName.class)){
				authorshipCache = CdmBase.deproxy(taxonName, NonViralName.class).getAuthorshipCache();
				isNonViralName = true;
			}
			result = authorshipCache;
			
			// For a misapplied names there are special rules
			if (isMisappliedName(taxon)){
				if (taxon.getSec() != null){
					String secTitle = taxon.getSec().getTitleCache();
					if (! secTitle.startsWith("auct")){
						secTitle = "sensu " + secTitle;
					}else if (secTitle.equals("auct")){  //may be removed once the title cache is generated correctly for references with title auct. #
						secTitle = "auct.";
					}
					return secTitle;
				}else if (StringUtils.isBlank(authorshipCache)) {
					// Set authorshipCache to "auct."
					result = PesiTransformer.AUCT_STRING;
				}else{
					result = PesiTransformer.AUCT_STRING;
//					result = authorshipCache;
				}
			}
			
			if (taxonName == null){
				logger.warn("TaxonName does not exist for taxon: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
			}else if (! isNonViralName){
				logger.warn("TaxonName is not of instance NonViralName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
			
			if (StringUtils.isBlank(result)) {
				return null;
			} else {
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
		
	
	/**
	 * Returns the <code>DisplayName</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>DisplayName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")  //used by Mapper
	private static String getDisplayName(TaxonNameBase<?,?> taxonName) {
		// TODO: extension?
		if (taxonName == null) {
			return null;
		}else{
			INonViralNameCacheStrategy<NonViralName<?>> cacheStrategy = getCacheStrategy(taxonName);
			HTMLTagRules tagRules = new HTMLTagRules().
					addRule(TagEnum.name, "i").
					addRule(TagEnum.nomStatus, "@status@");
			
			NonViralName<?> nvn = CdmBase.deproxy(taxonName, NonViralName.class);
			String result = cacheStrategy.getFullTitleCache(nvn, tagRules);
			cacheStrategy = null;
			nvn = null;
			return result.replaceAll(",?\\<@status@\\>.*\\</@status@\\>", "");
		}
	}
	

	/**
	 * Returns the <code>WebShowName</code> attribute for a taxon.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>WebShowName</code> attribute.
	 * @see MethodMapper
	*/
	@SuppressWarnings("unused")
	private static String getWebShowName(TaxonBase<?> taxon) {
		TaxonNameBase<?,?> taxonName = taxon.getName();
		String result = getWebShowName(taxonName);
		if (isMisappliedName(taxon)){
			result = result + " " + getAuthorString(taxon);
		}
		return result;
	}
	
	/**
	 * Returns the <code>WebShowName</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>WebShowName</code> attribute.
	 * @see MethodMapper
	 */
	private static String getWebShowName(TaxonNameBase<?,?> taxonName) {
		//TODO extensions?
		if (taxonName == null) {
			return null;
		}else{
			INonViralNameCacheStrategy<NonViralName<?>> cacheStrategy = getCacheStrategy(taxonName);
		
			HTMLTagRules tagRules = new HTMLTagRules().addRule(TagEnum.name, "i");
			NonViralName<?> nvn = CdmBase.deproxy(taxonName, NonViralName.class);
			String result = cacheStrategy.getTitleCache(nvn, tagRules);
			cacheStrategy = null;
			nvn = null;
			return result;
		}
	}

 	
	/**
	 * Returns the <code>WebSearchName</code> attribute.
	 * @param taxonName The {@link NonViralName NonViralName}.
	 * @return The <code>WebSearchName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getWebSearchName(TaxonNameBase<?,?> taxonName) {
		//TODO extensions?
		NonViralName<?> nvn = CdmBase.deproxy(taxonName, NonViralName.class);
		NonViralNameDefaultCacheStrategy<NonViralName<?>> strategy = getCacheStrategy(nvn);
		String result = strategy.getNameCache(nvn);
		strategy = null;
		nvn = null;
		return result;
	}


	/**
	 * Returns the <code>FullName</code> attribute.
	 * @param taxonName The {@link NonViralName NonViralName}.
	 * @return The <code>FullName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFullName(TaxonNameBase taxonName) {
		//TODO extensions?
		NonViralName<?> nvn = CdmBase.deproxy(taxonName, NonViralName.class);
		String result = getCacheStrategy(nvn).getTitleCache(nvn);
		Iterator<TaxonBase> taxa = taxonName.getTaxa().iterator();
		if (taxonName.getTaxa().size() >0){
			if (taxonName.getTaxa().size() == 1){
				TaxonBase taxon = taxa.next();
				if (isMisappliedName(taxon)){
					result = result + " " + getAuthorString(taxon);
				}
				taxon = null;
			}
		}
		taxa = null;
		nvn = null;
		return result;
	}
	
	/**
	 * Returns the <code>FullName</code> attribute.
	 * @param taxon The {@link TaxonBase taxon}.
	 * @return The <code>FullName</code> attribute.
	 * @see MethodMapper
	 */
	/*@SuppressWarnings("unused")
	private static String getFullName(TaxonBase taxon) {
		//TODO extensions?
		TaxonNameBase name = taxon.getName();
		String result = getFullName(name);
		if (isMisappliedName(taxon)){
			result = result + " " + getAuthorString(taxon);
		}
		
		return result;
	}
*/
	
	/**
	 * Returns the nomenclatural reference which is the reference
	 * including the detail (microreference).
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>AuthorString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNomRefString(TaxonNameBase<?,?> taxonName) {
		INomenclaturalReference ref = taxonName.getNomenclaturalReference();
		return ref == null ? null : ref.getNomenclaturalCitation(taxonName.getNomenclaturalMicroReference());
	}
	

	/**
	 * Returns the <code>NameStatusFk</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>NameStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getNameStatusFk(TaxonNameBase<?,?> taxonName) {
		Integer result = null;

		NomenclaturalStatus state = getNameStatus(taxonName);
		if (state != null) {
			result = PesiTransformer.nomStatus2nomStatusFk(state.getType());
		}
		return result;
	}
	
	/**
	 * Returns the <code>NameStatusCache</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>NameStatusCache</code> attribute.
	 * @throws UndefinedTransformerMethodException 
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNameStatusCache(TaxonNameBase taxonName, PesiExportState state) throws UndefinedTransformerMethodException {
		String result = null;
		NomenclaturalStatus status = getNameStatus(taxonName);
		if (status != null) {
			result = state.getTransformer().getCacheByNomStatus(status.getType());
		}
		return result;
	}
	
	
	private static NomenclaturalStatus getNameStatus(TaxonNameBase<?,?> taxonName) {
		try {
			if (taxonName != null && (taxonName.isInstanceOf(NonViralName.class))) {
				NonViralName<?> nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
				Set<NomenclaturalStatus> states = nonViralName.getStatus();
				if (states.size() == 1) {
					NomenclaturalStatus status = states.iterator().next();
					return status;
				} else if (states.size() > 1) {
					logger.error("This TaxonName has more than one Nomenclatural Status: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Returns the <code>TaxonStatusFk</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>TaxonStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getTaxonStatusFk(TaxonBase<?> taxon, PesiExportState state) {
		Integer result = null;
		
		try {
			if (isMisappliedName(taxon)) {
				Synonym synonym = Synonym.NewInstance(null, null);
				
				// This works as long as only the instance is important to differentiate between TaxonStatus.
				result = PesiTransformer.taxonBase2statusFk(synonym); // Auct References are treated as Synonyms in Datawarehouse now.
			} else {
				result = PesiTransformer.taxonBase2statusFk(taxon);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>TaxonStatusCache</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>TaxonStatusCache</code> attribute.
	 * @throws UndefinedTransformerMethodException 
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonStatusCache(TaxonBase<?> taxon, PesiExportState state) throws UndefinedTransformerMethodException {
		return state.getTransformer().getTaxonStatusCacheByKey(getTaxonStatusFk(taxon, state));
	}
	
	/**
	 * Returns the <code>TypeNameFk</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>TypeNameFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getTypeNameFk(TaxonNameBase<?,?> taxonNameBase, PesiExportState state) {
		Integer result = null;
		if (taxonNameBase != null) {
			Set<NameTypeDesignation> nameTypeDesignations = taxonNameBase.getNameTypeDesignations();
			if (nameTypeDesignations.size() == 1) {
				NameTypeDesignation nameTypeDesignation = nameTypeDesignations.iterator().next();
				if (nameTypeDesignation != null) {
					TaxonNameBase<?,?> typeName = nameTypeDesignation.getTypeName();
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
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>TypeFullnameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTypeFullnameCache(TaxonNameBase<?,?> taxonName) {
		String result = null;
		
		try {
		if (taxonName != null) {
			Set<NameTypeDesignation> nameTypeDesignations = taxonName.getNameTypeDesignations();
			if (nameTypeDesignations.size() == 1) {
				NameTypeDesignation nameTypeDesignation = nameTypeDesignations.iterator().next();
				if (nameTypeDesignation != null) {
					TaxonNameBase<?,?> typeName = nameTypeDesignation.getTypeName();
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
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>QualityStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getQualityStatusFk(TaxonNameBase taxonName) {
		BitSet sources = getSources(taxonName);
		return PesiTransformer.getQualityStatusKeyBySource(sources, taxonName);
	}

	
	/**
	 * Returns the <code>QualityStatusCache</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>QualityStatusCache</code> attribute.
	 * @throws UndefinedTransformerMethodException 
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getQualityStatusCache(TaxonNameBase taxonName, PesiExportState state) throws UndefinedTransformerMethodException {
		return state.getTransformer().getQualityStatusCacheByKey(getQualityStatusFk(taxonName));
	}

	
	/**
	 * Returns the <code>TypeDesignationStatusFk</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>TypeDesignationStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTypeDesignationStatusFk(TaxonNameBase<?,?> taxonName) {
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
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>TypeDesignationStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTypeDesignationStatusCache(TaxonNameBase<?,?> taxonName) {
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
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>FossilStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getFossilStatusFk(IdentifiableEntity<?> identEntity, PesiExportState state) {
		Integer result = null;
		
		Set<String> fossilStatuus = identEntity.getExtensions(ErmsTransformer.uuidFossilStatus);
		if (fossilStatuus.size() == 0){
			return null;
		}else if (fossilStatuus.size() > 1){
			logger.warn("More than 1 fossil status given for " +  identEntity.getTitleCache() + " " + identEntity.getUuid());
		}
		String fossilStatus = fossilStatuus.iterator().next();
		
		int statusFk = state.getTransformer().FossilStatusCache2FossilStatusFk(fossilStatus);
		return statusFk;
	}
	
	/**
	 * Returns the <code>FossilStatusCache</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>FossilStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFossilStatusCache(IdentifiableEntity<?> identEntity, PesiExportState state) {
		String result = null;
		Set<String> fossilStatuus = identEntity.getExtensions(ErmsTransformer.uuidFossilStatus);
		if (fossilStatuus.size() == 0){
			return null;
		}
		for (String strFossilStatus : fossilStatuus){
			result = CdmUtils.concat(";", result, strFossilStatus);
		}
		return result;
	}
	
	/**
	 * Returns the <code>IdInSource</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>IdInSource</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getIdInSource(IdentifiableEntity taxonName) {
		String result = null;
		
		try {
			Set<IdentifiableSource> sources = getPesiSources(taxonName);
			if (sources.size() > 1){
				logger.warn("There is > 1 Pesi source. This is not yet handled.");
			}
			if (sources.size() == 0){
				logger.warn("There is no Pesi source!" +taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
			}
			for (IdentifiableSource source : sources) {
				Reference<?> ref = source.getCitation();
				UUID refUuid = ref.getUuid();
				String idInSource = source.getIdInSource();
				if (refUuid.equals(PesiTransformer.uuidSourceRefEuroMed)){
					result = idInSource != null ? ("NameId: " + source.getIdInSource()) : null;
				}else if (refUuid.equals(PesiTransformer.uuidSourceRefFaunaEuropaea)){
					result = idInSource != null ? ("TAX_ID: " + source.getIdInSource()) : null;
				}else if (refUuid.equals(PesiTransformer.uuidSourceRefErms)){
					result = idInSource != null ? ("tu_id: " + source.getIdInSource()) : null;
				}else if (refUuid.equals(PesiTransformer.uuidSourceRefIndexFungorum)){  //Index Fungorum
					result = idInSource != null ? ("if_id: " + source.getIdInSource()) : null;
				}else{
					if (logger.isDebugEnabled()){logger.debug("Not a PESI source");};
				}
				
				String sourceIdNameSpace = source.getIdNamespace();
				if (sourceIdNameSpace != null) {
					if (sourceIdNameSpace.equals(PesiTransformer.STR_NAMESPACE_NOMINAL_TAXON)) {
						result =  idInSource != null ? ("Nominal Taxon from TAX_ID: " + source.getIdInSource()):null;
					} else if (sourceIdNameSpace.equals(TaxonServiceImpl.INFERRED_EPITHET_NAMESPACE)) {
						result =  idInSource != null ? ("Inferred epithet from TAX_ID: " + source.getIdInSource()) : null;
					} else if (sourceIdNameSpace.equals(TaxonServiceImpl.INFERRED_GENUS_NAMESPACE)) {
						result =  idInSource != null ? ("Inferred genus from TAX_ID: " + source.getIdInSource()):null;
					} else if (sourceIdNameSpace.equals(TaxonServiceImpl.POTENTIAL_COMBINATION_NAMESPACE)) {
						result =  idInSource != null ? ("Potential combination from TAX_ID: " + source.getIdInSource()):null;
					} 
				}
				if (result == null) {
					logger.warn("IdInSource is NULL for this taxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +", sourceIdNameSpace: " + source.getIdNamespace()+")");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("An error occurs while creating idInSource..." + taxonName.getUuid() + " (" + taxonName.getTitleCache()+ e.getMessage());
		}

		if (result == null) {
			logger.warn("IdInSource is NULL for this taxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
		}
		return result;
	}
	
	/**
	 * Returns the idInSource for a given TaxonName only.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The idInSource.
	 */
	private static String getIdInSourceOnly(IdentifiableEntity identEntity) {
		String result = null;
		
		// Get the sources first
		Set<IdentifiableSource> sources = getPesiSources(identEntity);

		// Determine the idInSource
		if (sources.size() == 1) {
			IdentifiableSource source = sources.iterator().next();
			if (source != null) {
				result = source.getIdInSource();
			}
		} else if (sources.size() > 1) {
			int count = 1;
			result = "";
			for (IdentifiableSource source : sources) {
				result += source.getIdInSource();
				if (count < sources.size()) {
					result += "; ";
				}
				count++;
			}

		}
		
		return result;
	}
	
	/**
	 * Returns the Sources for a given TaxonName only.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The Sources.
	 */
	private static Set<IdentifiableSource> getPesiSources(IdentifiableEntity identEntity) {
		Set<IdentifiableSource> sources = new java.util.HashSet<IdentifiableSource>();

		//Taxon Names
		if (identEntity.isInstanceOf(TaxonNameBase.class)){
			// Sources from TaxonName
			TaxonNameBase taxonName = CdmBase.deproxy(identEntity, TaxonNameBase.class);
			Set<IdentifiableSource> testSources = identEntity.getSources();
			sources = filterPesiSources(identEntity.getSources());
			
			if (sources.size() == 0 && testSources.size()>0){
				IdentifiableSource source = testSources.iterator().next();
				logger.warn("There are sources, but they are no pesi sources!!!" + source.getIdInSource() + " - " + source.getIdNamespace() + " - "+source.getCitation().generateTitle());
			}
			if (sources.size() > 1) {
				logger.warn("This TaxonName has more than one Source: " + identEntity.getUuid() + " (" + identEntity.getTitleCache() + ")");
			}
			
			// name has no PESI source, take sources from TaxonBase
			if (sources == null || sources.isEmpty()) {
				Set<TaxonBase> taxa = taxonName.getTaxonBases();
				for (TaxonBase taxonBase: taxa){
					sources.addAll(filterPesiSources(taxonBase.getSources()));
				}
			}

		//for TaxonBases
		}else if (identEntity.isInstanceOf(TaxonBase.class)){
			sources = filterPesiSources(identEntity.getSources());	
		}

		/*TODO: deleted only for testing the inferred synonyms 
		if (sources == null || sources.isEmpty()) {
			logger.warn("This TaxonName has no PESI Sources: " + identEntity.getUuid() + " (" + identEntity.getTitleCache() +")");
		}else if (sources.size() > 1){
			logger.warn("This Taxon(Name) has more than 1 PESI source: " + identEntity.getUuid() + " (" + identEntity.getTitleCache() +")");
		}
		*/
		return sources;
	}
	
	// return all sources with a PESI reference	
	private static Set<IdentifiableSource> filterPesiSources(Set<? extends IdentifiableSource> sources) {
		Set<IdentifiableSource> result = new HashSet<IdentifiableSource>();
		for (IdentifiableSource source : sources){
			Reference ref = source.getCitation();
			UUID refUuid = ref.getUuid();
			if (refUuid.equals(PesiTransformer.uuidSourceRefEuroMed) || 
				refUuid.equals(PesiTransformer.uuidSourceRefFaunaEuropaea)||
				refUuid.equals(PesiTransformer.uuidSourceRefErms)||
				refUuid.equals(PesiTransformer.uuidSourceRefIndexFungorum) ||
				refUuid.equals(PesiTransformer.uuidSourceRefAuct)){
				result.add(source);
			}
		}
		return result;
	}

	/**
	 * Returns the <code>GUID</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>GUID</code> attribute.
	 * @see MethodMapper
	 */
	private static String getGUID(TaxonBase<?> taxon) {
		if (taxon.getLsid() != null ){
			return taxon.getLsid().getLsid();
		}else if (taxon.hasMarker(PesiTransformer.uuidMarkerGuidIsMissing, true)){
			return null;
		}else{
			return taxon.getUuid().toString();
		}
	}
	
	
	
	
	/**
	 * Returns the <code>DerivedFromGuid</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>DerivedFromGuid</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getDerivedFromGuid(TaxonBase<?> taxon) {
		String result = null;
		try {
		// The same as GUID for now
		result = getGUID(taxon);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Returns the <code>CacheCitation</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The CacheCitation.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getCacheCitation(TaxonBase taxon) {
		// !!! See also doPhaseUpdates
		
		TaxonNameBase<?,?> taxonName = taxon.getName();
		String result = "";
		//TODO implement anew for taxa
		try {
			BitSet sources = getSources(taxonName);
			if (sources.isEmpty()) {
//				logger.error("OriginalDB is NULL for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			} else if (sources.get(PesiTransformer.SOURCE_ERMS)) {
				// TODO: 19.08.2010: An import of CacheCitation does not exist in the ERMS import yet or it will be imported in a different way...
				// 		 So the following code is some kind of harmless assumption.
				Set<Extension> extensions = taxonName.getExtensions();
				for (Extension extension : extensions) {
					if (extension.getType().equals(cacheCitationExtensionType)) {
						result = extension.getValue();
					}
				}
			} else {
				String expertName = getExpertName(taxon);
				String webShowName = getWebShowName(taxonName);
				
				// idInSource only
				String idInSource = getIdInSourceOnly(taxonName);
				
				// build the cacheCitation
				if (expertName != null) {
					result += expertName + ". ";
				} else {
					if (logger.isDebugEnabled()){logger.debug("ExpertName could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");}
				}
				if (webShowName != null) {
					result += webShowName + ". ";
				} else {
					logger.warn("WebShowName could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				}
				
				if (getOriginalDB(taxonName).equals("FaEu")) {
					result += "Accessed through: Fauna Europaea at http://faunaeur.org/full_results.php?id=";
				} else if (getOriginalDB(taxonName).equals("EM")) {
					result += "Accessed through: Euro+Med PlantBase at http://ww2.bgbm.org/euroPlusMed/PTaxonDetail.asp?UUID=";
				}
				
				if (idInSource != null) {
					result += idInSource;
				} else {
					logger.warn("IdInSource could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (StringUtils.isBlank(result)) {
			return null;
		} else {
			return result;
		}
	}
	
	/**
	 * Returns the <code>OriginalDB</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>OriginalDB</code> attribute.
	 * @see MethodMapper
	 */
	private static String getOriginalDB(IdentifiableEntity identEntity) {
		// Sources from TaxonName
		BitSet sources  = getSources(identEntity);
		return PesiTransformer.getOriginalDbBySources(sources);
	}
	
	/**
	 * Returns the <code>LastAction</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>LastAction</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLastAction(IdentifiableEntity<?> identEntity) {
		String result = null;
		try {
		Set<Extension> extensions = identEntity.getExtensions();
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
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>LastActionDate</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings({ "unused" })
	private static DateTime getLastActionDate(IdentifiableEntity identEntity) {
		DateTime result = null;
		try {
			Set<Extension> extensions = identEntity.getExtensions();
			for (Extension extension : extensions) {
				if (extension.getType().equals(lastActionDateExtensionType)) {
					String dateTime = extension.getValue();
					if (dateTime != null) {
						DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S");
						result = formatter.parseDateTime(dateTime);
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
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>ExpertName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getExpertName(TaxonBase<?> taxonName) {
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
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>ExpertFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getExpertFk(Reference<?> reference, PesiExportState state) {
		Integer result = state.getDbId(reference);
		return result;
	}
	
	/**
	 * Returns the <code>SpeciesExpertName</code> attribute.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @return The <code>SpeciesExpertName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSpeciesExpertName(TaxonBase<?> taxonName) {
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
	 * @param reference The {@link Reference Reference}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>SpeciesExpertFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getSpeciesExpertFk(Reference<?> reference, PesiExportState state) {
		Integer result = state.getDbId(reference);
		return result;
	}
	
	
	/**
	 * Returns the source (E+M, Fauna Europaea, Index Fungorum, ERMS) of a given
	 * Identifiable Entity as a BitSet
	 * @param identEntity
	 * @return
	 */
	private static BitSet getSources(IdentifiableEntity<?> identEntity){
		BitSet bitSet = new BitSet();
		Set<IdentifiableSource> sources = getPesiSources(identEntity);
		for (IdentifiableSource source : sources) {
			Reference<?> ref = source.getCitation();
			UUID refUuid = ref.getUuid();
			if (refUuid.equals(PesiTransformer.uuidSourceRefEuroMed)){
				bitSet.set(PesiTransformer.SOURCE_EM);
			}else if (refUuid.equals(PesiTransformer.uuidSourceRefFaunaEuropaea)){
				bitSet.set(PesiTransformer.SOURCE_FE);
			}else if (refUuid.equals(PesiTransformer.uuidSourceRefErms)){
				bitSet.set(PesiTransformer.SOURCE_ERMS);
			}else if (refUuid.equals(PesiTransformer.uuidSourceRefIndexFungorum)){
				bitSet.set(PesiTransformer.SOURCE_IF);
			}else{
				if (logger.isDebugEnabled()){logger.debug("Not a PESI source");};
			}
		}
		return bitSet;
		
	}
	
	protected static NonViralNameDefaultCacheStrategy getCacheStrategy(TaxonNameBase<?, ?> taxonName) {
		taxonName = CdmBase.deproxy(taxonName, TaxonNameBase.class);
		NonViralNameDefaultCacheStrategy<?> cacheStrategy;
		if (taxonName.isInstanceOf(ZoologicalName.class)){
			cacheStrategy = zooNameStrategy;
		}else if (taxonName.isInstanceOf(BotanicalName.class)) {
			cacheStrategy = botanicalNameStrategy;
		}else if (taxonName.getClass().equals(NonViralName.class)) {
			cacheStrategy = nonViralNameStrategy;
		}else if (taxonName.getClass().equals(BacterialName.class)) {
			cacheStrategy = bacterialNameStrategy;
		}else{
			logger.error("Unhandled taxon name type. Can't define strategy class");
			cacheStrategy = botanicalNameStrategy;
		}
		return cacheStrategy;
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
	 * Returns the <code>RelQualifierCache</code> attribute.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @return The <code>RelQualifierCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRelQualifierCache(RelationshipBase<?, ?, ?> relationship, PesiExportState state) {
		String result = null;
		NomenclaturalCode code = null;
		if (relationship.isInstanceOf(TaxonRelationship.class)){
			code = CdmBase.deproxy(relationship, TaxonRelationship.class).getToTaxon().getName().getNomenclaturalCode();
		}else if (relationship.isInstanceOf(SynonymRelationship.class)){
			code = CdmBase.deproxy(relationship, SynonymRelationship.class).getAcceptedTaxon().getName().getNomenclaturalCode();
		}else if (relationship.isInstanceOf(NameRelationship.class)){
			code = CdmBase.deproxy(relationship,  NameRelationship.class).getFromName().getNomenclaturalCode();
		}else if (relationship.isInstanceOf(HybridRelationship.class)){
			code = CdmBase.deproxy(relationship,  HybridRelationship.class).getParentName().getNomenclaturalCode();
		}
		if (code != null) {
			result = state.getConfig().getTransformer().getCacheByRelationshipType(relationship, code);
		} else {
			logger.error("NomenclaturalCode is NULL while creating the following relationship: " + relationship.getUuid());
		}
		return result;
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
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(IdMapper.NewInstance("TaxonId"));
		mapping.addMapper(DbObjectMapper.NewInstance("sec", "sourceFk")); //OLD:mapping.addMapper(MethodMapper.NewInstance("SourceFK", this.getClass(), "getSourceFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusFk", this.getClass(), "getTaxonStatusFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusCache", this.getClass(), "getTaxonStatusCache", standardMethodParameter, PesiExportState.class));
		
		mapping.addMapper(MethodMapper.NewInstance("GUID", this));
		
		mapping.addMapper(MethodMapper.NewInstance("DerivedFromGuid", this));
		mapping.addMapper(MethodMapper.NewInstance("CacheCitation", this));
		mapping.addMapper(MethodMapper.NewInstance("AuthorString", this));  //For Taxon because Misallied Names are handled differently
		mapping.addMapper(MethodMapper.NewInstance("WebShowName", this));
		
		// DisplayName
		mapping.addMapper(MethodMapper.NewInstance("DisplayName", this));

		// FossilStatus (Fk, Cache)
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusCache", this, IdentifiableEntity.class, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusFk", this, IdentifiableEntity.class, PesiExportState.class)); // PesiTransformer.FossilStatusCache2FossilStatusFk?
		
		//handled by name mapping
		mapping.addMapper(DbLastActionMapper.NewInstance("LastActionDate", false));
		mapping.addMapper(DbLastActionMapper.NewInstance("LastAction", true));
		
		//experts
		ExtensionType extensionTypeSpeciesExpertName = (ExtensionType)getTermService().find(PesiTransformer.speciesExpertNameUuid);
		mapping.addMapper(DbExtensionMapper.NewInstance(extensionTypeSpeciesExpertName, "SpeciesExpertName"));
		ExtensionType extensionTypeExpertName = (ExtensionType)getTermService().find(PesiTransformer.expertNameUuid);
		mapping.addMapper(DbExtensionMapper.NewInstance(extensionTypeExpertName, "ExpertName"));
		
//		mapping.addMapper(MethodMapper.NewInstance("ParentTaxonFk", this, TaxonBase.class, PesiExportState.class));  //by AM, doesn't work, FK exception
		mapping.addMapper(ObjectChangeMapper.NewInstance(TaxonBase.class, TaxonNameBase.class, "Name"));
		
		addNameMappers(mapping);

		return mapping;
	}
	
	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @param state 
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 * @throws UndefinedTransformerMethodException 
	 */
	private PesiExportMapping getPureNameMapping(PesiExportState state) throws UndefinedTransformerMethodException {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(IdMapper.NewInstance("TaxonId"));

		//		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusFk", this.getClass(), "getTaxonStatusFk", standardMethodParameter, PesiExportState.class));

		mapping.addMapper(MethodMapper.NewInstance("KingdomFk", this, TaxonNameBase.class));
		mapping.addMapper(MethodMapper.NewInstance("RankFk", this, TaxonNameBase.class));
		mapping.addMapper(MethodMapper.NewInstance("RankCache", this, TaxonNameBase.class, PesiExportState.class));
		mapping.addMapper(DbConstantMapper.NewInstance("TaxonStatusFk", Types.INTEGER , PesiTransformer.T_STATUS_UNACCEPTED));
		mapping.addMapper(DbConstantMapper.NewInstance("TaxonStatusCache", Types.VARCHAR , state.getTransformer().getTaxonStatusCacheByKey( PesiTransformer.T_STATUS_UNACCEPTED)));
		mapping.addMapper(DbStringMapper.NewInstance("AuthorshipCache", "AuthorString").setBlankToNull(true));  
		mapping.addMapper(MethodMapper.NewInstance("WebShowName", this, TaxonNameBase.class));
		
		// DisplayName
		mapping.addMapper(MethodMapper.NewInstance("DisplayName", this, TaxonNameBase.class));
		
		mapping.addMapper(DbLastActionMapper.NewInstance("LastActionDate", false));
		mapping.addMapper(DbLastActionMapper.NewInstance("LastAction", true));
		
		addNameMappers(mapping);
		//TODO add author mapper, TypeNameFk

		return mapping;
	}

	private void addNameMappers(PesiExportMapping mapping) {
		mapping.addMapper(DbStringMapper.NewInstance("GenusOrUninomial", "GenusOrUninomial"));
		mapping.addMapper(DbStringMapper.NewInstance("InfraGenericEpithet", "InfraGenericEpithet"));
		mapping.addMapper(DbStringMapper.NewInstance("SpecificEpithet", "SpecificEpithet"));
		mapping.addMapper(DbStringMapper.NewInstance("InfraSpecificEpithet", "InfraSpecificEpithet"));
		
//		mapping.addMapper(DbStringMapper.NewInstance("NameCache", "WebSearchName"));  //does not work as we need other cache strategy
		mapping.addMapper(MethodMapper.NewInstance("WebSearchName", this, TaxonNameBase.class));
		
//		mapping.addMapper(DbStringMapper.NewInstance("TitleCache", "FullName"));    //does not work as we need other cache strategy
		mapping.addMapper(MethodMapper.NewInstance("FullName", this, TaxonNameBase.class));
		
		
		mapping.addMapper(MethodMapper.NewInstance("NomRefString", this, TaxonNameBase.class));
		
		mapping.addMapper(MethodMapper.NewInstance("NameStatusFk", this, TaxonNameBase.class));
		mapping.addMapper(MethodMapper.NewInstance("NameStatusCache", this, TaxonNameBase.class, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TypeFullnameCache", this, TaxonNameBase.class));
		//TODO TypeNameFk
		
		//quality status
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusFk", this, TaxonNameBase.class));
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusCache", this, TaxonNameBase.class, PesiExportState.class));
		
		mapping.addMapper(MethodMapper.NewInstance("IdInSource", this, IdentifiableEntity.class));
		mapping.addMapper(MethodMapper.NewInstance("OriginalDB", this, IdentifiableEntity.class) );

		//mapping.addMapper(ExpertsAndLastActionMapper.NewInstance());

	}
	
	private PesiExportMapping getSynRelMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableNameSynRel);
		
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk1", this.getClass(), "getTaxonFk1", RelationshipBase.class, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk2", this.getClass(), "getTaxonFk2", RelationshipBase.class, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("RelTaxonQualifierFk", this,  RelationshipBase.class));
		mapping.addMapper(MethodMapper.NewInstance("RelQualifierCache", this, RelationshipBase.class, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this,  RelationshipBase.class));

		return mapping;
	}

	private PesiExportMapping getAdditionalSourceMapping(PesiExportState state)  throws UndefinedTransformerMethodException{
		PesiExportMapping mapping = new PesiExportMapping(dbTableAdditionalSourceRel);
		
		mapping.addMapper(IdMapper.NewInstance("TaxonFk"));
		mapping.addMapper(ObjectChangeMapper.NewInstance(TaxonBase.class, TaxonNameBase.class, "Name"));
		
		mapping.addMapper(DbObjectMapper.NewInstance("NomenclaturalReference", "SourceFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("NomenclaturalReference", "SourceNameCache", IS_CACHE));

		//we have only nomenclatural references here
		mapping.addMapper(DbConstantMapper.NewInstance("SourceUseFk", Types.INTEGER , PesiTransformer.NOMENCLATURAL_REFERENCE));
		mapping.addMapper(DbConstantMapper.NewInstance("SourceUseCache", Types.VARCHAR, state.getTransformer().getSourceUseCacheByKey( PesiTransformer.NOMENCLATURAL_REFERENCE)));
		
		mapping.addMapper(DbStringMapper.NewInstance("NomenclaturalMicroReference", "SourceDetail"));
		
		return mapping;
	}

}
