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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbObjectMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.erms.ErmsTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.ITreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

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
	private static final Class<? extends CdmBase> standardMethodParameter = TaxonBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Taxon";
	private static final String pluralString = "Taxa";
	private static NomenclaturalCode nomenclaturalCode;

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

			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();

			// Stores whether this invoke was successful or not.
			boolean success = true;
	
			// PESI: Clear the database table Taxon.
			doDelete(state);
	
			// Get specific mappings: (CDM) Taxon -> (PESI) Taxon
			PesiExportMapping mapping = getMapping();
	
			// Initialize the db mapper
			mapping.initialize(state);
	
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<TaxonBase> list = null;

			// 1st Round: Make Taxa
			logger.error("PHASE 1...");
			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getTaxonService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (TaxonBase taxonBase : list) {
				doCount(count++, modCount, pluralString);
					success &= mapping.invoke(taxonBase);
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
			// 2nd Round: Add ParentTaxonFk to each Taxon
			Connection con = state.getConfig().getDestination().getConnection();

			// Prepare ParentTaxonFk-Statement
			String parentTaxonFkSql = "UPDATE Taxon SET ParentTaxonFk = ? WHERE TaxonId = ?"; 
			PreparedStatement parentTaxonFkStmt = con.prepareStatement(parentTaxonFkSql);

			// Prepare KingdomFk-Statement
			String kingdomFkSql = "UPDATE Taxon SET KingdomFk = ?, RankFk = ?, RankCache = ? WHERE TaxonId = ?"; 
			PreparedStatement kingdomFkStmt = con.prepareStatement(kingdomFkSql);

			logger.error("PHASE 2...");
			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getTaxonService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (TaxonBase taxonBase : list) {
					doCount(count++, modCount, "ParentTaxonFk"); // KingdomFk as well, but this does not really count here.
					success &= invokeParentTaxonFk(taxonBase, state, parentTaxonFkStmt);
					success &= invokeKingdomFk(taxonBase, state, kingdomFkStmt);
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
	 * Updates a Taxon database record with its parentTaxonId.
	 * @param taxonBase The {@link TaxonBase TaxonBase}.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @param stmt The sql statement for this update.
	 * @return Whether this update was successful or not.
	 */
	protected boolean invokeParentTaxonFk(TaxonBase taxonBase, DbExportStateBase<?> state, PreparedStatement stmt) {
		if (taxonBase == null) {
			return true;
		} else {
			Integer parentTaxonId = getParentTaxonFk(taxonBase, state);
			if (parentTaxonId == null){
				return true;
			} else {
				Integer taxonId = state.getDbId(taxonBase);
				try {
					stmt.setInt(1, parentTaxonId);
					stmt.setInt(2, taxonId);
					stmt.executeUpdate();
					return true;
				} catch (SQLException e) {
					logger.error("SQLException during parenTaxonFk invoke for taxon " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + "): " + e.getMessage());
					e.printStackTrace();
					return false;
				}
			}
		}
	}

	/**
	 * Updates a Taxon database record with its kingdomId.
	 * @param taxonBase
	 * @param state
	 * @param stmt
	 * @return
	 */
	protected boolean invokeKingdomFk(TaxonBase taxonBase, DbExportStateBase<?> state, PreparedStatement stmt) {
		if (taxonBase == null) {
			logger.warn("Taxon is NULL. Therefore KingdomFk, RankFk and RankCache could not be inserted into database.");
			return true;
		} else {
			Integer kingdomId = getKingdomFk(taxonBase, state);
			if (kingdomId == null) {
				// This is possible because Synonyms are still allowed not to have synonymRelationships.
				logger.warn("KingdomFk could not be determined. Therefore KingdomFk, RankFk and RankCache could not be inserted into database for the following taxon: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
				return true;
			} else {
				Integer taxonId = state.getDbId(taxonBase);
				Integer rankId = getRankFk(taxonBase);
				
				// This taxon was not exported during PesiTaxonExport.
				if (taxonId == null) {
					logger.warn("Taxon has no entry in state hashmap. Therefore KingdomFk, RankFk and RankCache could not be inserted into database for the following taxon: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
					return true;
				}

				// In the current ERMS data (cdm_test_andreasM2, 24.03.2010) there is a taxon that has no associated rank information: 4840e6de-fa7b-4c33-a601-58b86d619a6b (Monera)
				// The method 'setInt' on a preparedStatement crashes (NPE) in those cases, because a null value is set.
				if (rankId == null) {
					logger.warn("Taxon has no associated rank information. Therefore KingdomFk, RankFk and RankCache could not be inserted into database for the following taxon: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
					return true;
				}
				String rankCache = getRankCache(taxonBase);
				try {
					stmt.setInt(1, kingdomId);
					stmt.setInt(2, rankId);
					stmt.setString(3, rankCache);
					stmt.setInt(4, taxonId);
					stmt.executeUpdate();
					return true;
				} catch (SQLException e) {
					logger.error("SQLException during kingdomFk invoke for taxon " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + "): " + e.getMessage());
					e.printStackTrace();
					return false;
				}
			}
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
	 * Returns the <code>KingdomFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>KingdomFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getKingdomFk(TaxonBase<?> taxonBase, DbExportStateBase<?> state) {
		Integer result = null;
		
		Taxon taxon = null;
		if (taxonBase.isInstanceOf(Synonym.class)) {
			Synonym synonym = (Synonym)taxonBase;
			Set<SynonymRelationship> relations = synonym.getSynonymRelations();
			if (relations.size() == 1) {
				taxon  = relations.iterator().next().getAcceptedTaxon();
			} else if (relations.size() > 1) {
				logger.error("Synonym has multiple synonymRelations: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
			} else if (relations.size() == 0) {
				logger.error("Synonym has no synonymRelations: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() +")");
	}
		} else {
			taxon = CdmBase.deproxy(taxonBase, Taxon.class);
		}
		boolean root = false;
		boolean error = false;
		if (taxon != null) {
		// Traverse taxon tree up until root is reached.
			while (! root && ! error) {
				Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
				if (taxonNodes.size() == 1) {
					TaxonNode taxonNode = taxon.getTaxonNodes().iterator().next();
					ITreeNode parentNode = taxonNode.getParent();
					if (parentNode == null) {
						logger.error("TaxonNode has no Parent: " + taxonNode.getUuid());
						error = true;
					} else if (HibernateProxyHelper.isInstanceOf(parentNode, TaxonNode.class)) {
						TaxonNode node = CdmBase.deproxy(parentNode, TaxonNode.class);
						if (node == null) {
							logger.error("ParentNode of the following taxoNode is Null: " + taxonNode.getUuid());
							error = true;
						} else {
						taxon = node.getTaxon();
							if (taxon == null) {
								logger.error("TaxonNode has no Taxon: " + node.getUuid());
								error = true;
			}
						}
					} else {
						// Root element reached
						root = true;
					}
				} else {
					// This should not be the case.
					taxon = null;
					result = null;
					error = true;
				}
			}
		}
		if (error) {
			// TODO: Handle this gracefully. For now 'result' is left as null.
		} else if (taxon != null) {
		
			// TODO: Set current nomenclatural code
			// We are differentiating kingdoms by the nomenclatural code for now.
			// This needs to be handled in a better way as soon as we know how to differentiate between more kingdoms.
			TaxonNameBase taxonName = taxon.getName();
			if (taxonName.isInstanceOf(BotanicalName.class)) {
				BotanicalName botanicalName = CdmBase.deproxy(taxonName, BotanicalName.class);
				nomenclaturalCode = botanicalName.getNomenclaturalCode();
			} else if (taxonName.isInstanceOf(ZoologicalName.class)) {
				ZoologicalName zoologicalName = CdmBase.deproxy(taxonName, ZoologicalName.class);
				nomenclaturalCode = zoologicalName.getNomenclaturalCode();
			} else if (taxonName.isInstanceOf(BacterialName.class)) {
				BacterialName bacterialName = CdmBase.deproxy(taxonName, BacterialName.class);
				nomenclaturalCode = bacterialName.getNomenclaturalCode();
			}

			// Set result
			result = PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode);
		}

		return result;
	}

	/**
	 * Returns the <code>RankFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>RankFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getRankFk(TaxonBase<?> taxonBase) {
		Integer result = null;
		if (taxonBase != null && nomenclaturalCode != null) {
			TaxonNameBase taxonName = taxonBase.getName();
			if (taxonName != null) {
				result = PesiTransformer.rank2RankId(taxonName.getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
				if (result == null) {
					logger.error("Rank2RankId delivered NULL as result.");
	}
			} else {
				logger.error("TaxonName is NULL.");
			}
		} else {
			if (taxonBase == null) {
				logger.error("TaxonBase is NULL.");
			}
			if (nomenclaturalCode == null) {
				logger.error("NomenclaturalCode is NULL.");
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
	private static String getRankCache(TaxonBase<?> taxonBase) {
		String result = null;
		if (taxonBase != null & nomenclaturalCode != null) {
			TaxonNameBase taxonName = taxonBase.getName();
			if (taxonName != null) {
				result = PesiTransformer.rank2RankCache(taxonName.getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
	}
		}
		return result;
	}

	/**
	 * 
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return Whether it's genus or uninomial.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getGenusOrUninomial(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			if (nonViralName != null) {
			result = nonViralName.getGenusOrUninomial();
	}
		}
		return result;
	}

	/**
	 * Returns the <code>InfraGenericEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>InfraGenericEpithet</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getInfraGenericEpithet(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			if (nonViralName != null) {
			result = nonViralName.getInfraGenericEpithet();
	}
		}
		return result;
	}

	/**
	 * Returns the <code>SpecificEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>SpecificEpithet</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSpecificEpithet(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			if (nonViralName != null) {
			result = nonViralName.getSpecificEpithet();
	}
		}
		return result;
	}

	/**
	 * Returns the <code>InfraSpecificEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>InfraSpecificEpithet</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getInfraSpecificEpithet(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			if (nonViralName != null) {
			result = nonViralName.getInfraSpecificEpithet();
	}
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
	private static String getWebSearchName(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			if (nonViralName != null) {
			result = nonViralName.getNameCache();
	}
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
	private static String getWebShowName(TaxonBase<?> taxon) {
		// TODO
		// format: <span type="" text="" class=""></span>
		String result = null;
		if (taxon != null) {
			TaxonNameBase taxonName = taxon.getName();
			if (taxonName != null) {
				List resultList = taxonName.getTaggedName();
				//			result = resultList.toString(); // testing purpose
//			List resultList = taxon.getName().getTaggedName();
	}
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
	private static String getAuthorString(TaxonBase<?> taxon) {
		String result = null;
		TeamOrPersonBase team = null;
		ReferenceBase sec = taxon.getSec();
		if (sec != null) {
			team = sec.getAuthorTeam();
		if (team != null) {
				result = team.getTitleCache();
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
	private static String getFullName(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null) {
			TaxonNameBase taxonName = taxon.getName();
			if (taxonName != null) {
				result = taxonName.getTitleCache();
		}
	}
		return result;
	}

	/**
	 * Returns the <code>NomRefString</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>NomRefString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNomRefString(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null) {
			try {
				TaxonNameBase taxonName = taxon.getName();
				if (taxonName != null) {
					if (taxonName.getNomenclaturalReference() != null) {
						result = taxonName.getNomenclaturalMicroReference();
	}
				}
			} catch (Exception e) {
				logger.error("While getting NomRefString for taxon: " + taxon.getUuid() + " (" + taxon.getTitleCache() +")");
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
	private static String getDisplayName(TaxonBase<?> taxonBase) {
		String result = null;
		Set<Extension> extensions = taxonBase.getExtensions();
		for (Extension extension : extensions) {
			ExtensionType extensionType = extension.getType();
			if (extensionType != null && extensionType.getUuid().equals(ErmsTransformer.uuidDisplayName)) {
				// DisplayName found
				result = extension.getValue();
	}
	}
	
//		if (taxon != null) {
//			return taxon.getName().getFullTitleCache();
//		} else {
//			return null;
//		}
		return result;
	}
	
	/**
	 * Returns the <code>FuzzyName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FuzzyName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFuzzyName(TaxonBase<?> taxonBase) {
		String result = null;
		Set<Extension> extensions = taxonBase.getExtensions();
		for (Extension extension : extensions) {
			ExtensionType extensionType = extension.getType();
			if (extensionType != null && extensionType.getUuid().equals(ErmsTransformer.uuidFuzzyName)) {
				// FuzzyName found
				result = extension.getValue();
	}
		}
		return result;
	}

	/**
	 * Returns the <code>NameStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>NameStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getNameStatusFk(TaxonBase<?> taxon) {
		Integer result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName taxonName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			Set<NomenclaturalStatus> states = taxonName.getStatus();
			if (states.size() == 1) {
				NomenclaturalStatusType statusType = states.iterator().next().getType();
				result = PesiTransformer.nomStatus2nomStatusFk(statusType);
			} else if (states.size() > 1) {
				logger.error("This taxon has more than one Nomenclatural Status: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
	}
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
	private static String getNameStatusCache(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName taxonName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			Set<NomenclaturalStatus> states = taxonName.getStatus();
			if (states.size() == 1) {
				result = PesiTransformer.nomStatus2NomStatusCache(states.iterator().next().getType());
			} else if (states.size() > 1) {
				logger.error("This taxon has more than one Nomenclatural Status: " + taxon.getUuid() + " (" + taxon.getTitleCache() +")");
	}
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
	private static Integer getTaxonStatusFk(TaxonBase<?> taxon) {
		return PesiTransformer.taxonBase2statusFk(taxon);
	}
	
	/**
	 * Returns the <code>TaxonStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TaxonStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonStatusCache(TaxonBase<?> taxon) {
		return PesiTransformer.taxonBase2statusCache(taxon);
	}
	
	/**
	 * Returns the <code>ParentTaxonFk</code> attribute. Used by invokeParentTaxonFk().
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>ParentTaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getParentTaxonFk(TaxonBase<?> taxonBase, DbExportStateBase<?> state) {
		Integer result = null;
		Taxon taxon = null;
		if (taxonBase.isInstanceOf(Synonym.class)) {
			Synonym synonym = (Synonym)taxonBase;
			Set<SynonymRelationship> relations = synonym.getSynonymRelations();
			if (relations.size() == 1) {
				taxon = relations.iterator().next().getAcceptedTaxon();
			} else if (relations.size() > 1) {
				logger.error("Synonym has multiple synonymRelations: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
			} else if (relations.size() == 0) {
				logger.error("Synonym has no synonymRelations: " + taxonBase.getUuid() + " (" +  taxonBase.getTitleCache() + ")");
	}
		} else {
			taxon = CdmBase.deproxy(taxonBase, Taxon.class);
		}
		if (taxon != null) {
			Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
			if (taxonNodes.size() == 1) {
				TaxonNode taxonNode = taxon.getTaxonNodes().iterator().next();
				ITreeNode parentNode = taxonNode.getParent();
				if (HibernateProxyHelper.isInstanceOf(parentNode, TaxonNode.class)) {
					TaxonNode node = CdmBase.deproxy(parentNode, TaxonNode.class);
					Taxon parent = node.getTaxon();
					result = state.getDbId(parent);
				} else {
					logger.error("No parent taxon could be determined for taxon: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
				}
			}
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
	private static Integer getTypeNameFk(TaxonBase<?> taxonBase, DbExportStateBase<?> state) {
		Integer result = null;
		if (taxonBase != null) {
			// TODO: NameTypeDesignation - typeName.taxonName.datawarehouse_id
//			result = PesiTransformer.taxon.taxonName2TypeNameId(getName().getId());
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
	private static String getTypeFullnameCache(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null) {
			TaxonNameBase taxonName = taxon.getName();
			result = taxonName.getTitleCache();
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
	private static Integer getQualityStatusFk(TaxonBase<?> taxonBase) {
		// TODO: Not represented in CDM now. Depends on import.
		Integer result = null;
		Set<Extension> extensions = taxonBase.getExtensions();
		for (Extension extension : extensions) {
			ExtensionType extensionType = extension.getType();
			if (extensionType != null && extensionType.getUuid().equals(ErmsTransformer.uuidQualityStatus)) {
				String extensionValue = extension.getValue();
				if (extensionValue != null) {
					result = PesiTransformer.qualityStatus2QualityStatusFk(extensionValue);
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the <code>QualityStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>QualityStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getQualityStatusCache(TaxonBase<?> taxonBase) {
		// TODO: Not represented in CDM now. Depends on import.
		String result = null;
		Set<Extension> extensions = taxonBase.getExtensions();
		for (Extension extension : extensions) {
			ExtensionType extensionType = extension.getType();
			if (extensionType != null && extensionType.getUuid().equals(ErmsTransformer.uuidQualityStatus)) {
				result = extension.getValue();
			}
		}
		return result;
	}
	
	/**
	 * Returns the <code>TypeDesignationStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeDesignationStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTypeDesignationStatusFk(TaxonBase<?> taxon) {
		Integer result = null;
		if (taxon != null) {
			Set<NameTypeDesignation> typeDesignations = taxon.getName().getNameTypeDesignations();
			if (typeDesignations.size() == 1) {
				Object obj = typeDesignations.iterator().next().getTypeStatus();
				NameTypeDesignationStatus designationStatus = CdmBase.deproxy(obj, NameTypeDesignationStatus.class);
				result = PesiTransformer.nameTypeDesignationStatus2TypeDesignationStatusId(designationStatus);
			} else if (typeDesignations.size() > 1) {
				logger.error("Found a taxon with more than one NameTypeDesignation: " + taxon.getUuid() + " (" + taxon.getTitleCache() +")");
	}
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
	private static String getTypeDesignationStatusCache(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null) {
			Set<NameTypeDesignation> typeDesignations = taxon.getName().getNameTypeDesignations();
			if (typeDesignations.size() == 1) {
				Object obj = typeDesignations.iterator().next().getTypeStatus();
				NameTypeDesignationStatus designationStatus = CdmBase.deproxy(obj, NameTypeDesignationStatus.class);
				result = PesiTransformer.nameTypeDesignationStatus2TypeDesignationStatusCache(designationStatus);
			} else if (typeDesignations.size() > 1) {
				logger.error("Found a taxon with more than one NameTypeDesignation: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
	}
		}
		return result;
	}
	
	/**
	 * returns the <code>TreeIndex</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TreeIndex</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTreeIndex(TaxonBase<?> taxonBase, DbExportStateBase<?> state) {
		// TODO: If the current taxonBase is of instance Synonym, should it be added to the treeIndex next to its (parent) accepted taxon (on the right hand side)?
		String result = "";
		
		Taxon taxon = null;
		if (taxonBase.isInstanceOf(Synonym.class)) {
			Synonym synonym = (Synonym)taxonBase;
			Set<SynonymRelationship> relations = synonym.getSynonymRelations();
			if (relations.size() == 1) {
				taxon  = relations.iterator().next().getAcceptedTaxon();
			} else if (relations.size() > 1) {
				logger.error("Synonym has multiple synonymRelations: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
			} else if (relations.size() == 0) {
				logger.error("Synonym has no synonymRelations: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
			}
		} else {
			taxon = CdmBase.deproxy(taxonBase, Taxon.class);
		}
		boolean root = false;
		boolean error = false;
		boolean start = true;
		if (taxon != null) {
			// Traverse taxon tree up until root is reached.

			// Add the current taxon
			result = "#" + state.getDbId(taxon) + "#";

			while (! root && ! error) {
				Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
				if (taxonNodes.size() == 1) {
					TaxonNode taxonNode = taxon.getTaxonNodes().iterator().next();
					ITreeNode parentNode = taxonNode.getParent();
					if (parentNode == null) {
						logger.error("TaxonNode has no Parent: " + taxonNode.getUuid());
						error = true;
					} else if (HibernateProxyHelper.isInstanceOf(parentNode, TaxonNode.class)) {
						TaxonNode node = CdmBase.deproxy(parentNode, TaxonNode.class);
						taxon = node.getTaxon();
						if (taxon == null) {
							logger.error("TaxonNode has no Taxon: " + node.getUuid());
							error = true;
						} else {
						result = "#" + state.getDbId(taxon) + result;
						}
					} else {
						// Root element reached
						root = true;
					}
					start = false;
				} else {
					// This should not be the case.
					taxon = null;
					result = null;
					error = true;
				}
			}
		}
		if (error) {
		return null;
		} else {
			return result;
	}
	}
	
	/**
	 * Returns the <code>FossilStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FossilStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getFossilStatusFk(TaxonBase<?> taxonBase) {
		Integer result = null;
		Set<Extension> extensions = taxonBase.getExtensions();
		for (Extension extension : extensions) {
			ExtensionType extensionType = extension.getType();
			if (extensionType != null && extensionType.getUuid().equals(ErmsTransformer.uuidFossilStatus)) {
				String extensionValue = extension.getValue();
				if (extensionValue != null) {
					result = PesiTransformer.fossilStatus2FossilStatusId(extensionValue);
	}
			}
		}
		return result;
	}
	
	/**
	 * Returns the <code>FossilStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FossilStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFossilStatusCache(TaxonBase<?> taxonBase) {
		// TODO
		String result = null;
		Set<Extension> extensions = taxonBase.getExtensions();
		for (Extension extension : extensions) {
			ExtensionType extensionType = extension.getType();
			if (extensionType != null && extensionType.getUuid().equals(ErmsTransformer.uuidFossilStatus)) {
				result = extension.getValue();
			}
		}
		return result;
	}
	
	/**
	 * Returns the <code>IdInSource</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>IdInSource</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getIdInSource(TaxonBase<?> taxon) {
		String result = null;

		Set<IdentifiableSource> sources = taxon.getSources();
		if (sources != null && sources.size() == 1) {
			result = sources.iterator().next().getIdInSource();
		} else {
			logger.warn("Taxon has more than one source: " + taxon.getUuid() + " (" + taxon.getTitleCache() +")");
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
	private static String getGUID(TaxonBase<?> taxon) {
		String result = taxon.getUuid().toString(); // Is there a better way to convert a UUID to String?
		return result;
	}
	
	/**
	 * Returns the <code>DerivedFromGuid</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>DerivedFromGuid</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getDerivedFromGuid(TaxonBase<?> taxon) {
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
	private static String getOriginalDB(TaxonBase<?> taxon) {
		String result = null;
		for (IdentifiableSource source : taxon.getSources()) {
			if (source != null) {
				ReferenceBase citation = source.getCitation();
				if (citation != null) {
					result = citation.getTitleCache();  //or just title
	}
		}
		}
		return result;
	}
	
	/**
	 * Returns the <code>LastAction</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>LastAction</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLastAction(TaxonBase<?> taxon) {
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
	private static String getLastActionDate(TaxonBase<?> taxonBase) {
		String result = null;
//		if (taxonBase != null) {
//			DateTime updated = taxonBase.getUpdated();
//			if (updated != null) {
////				logger.error("Taxon Updated: " + updated);
//				result = new DateTime(updated.toDate()); // Unfortunately the time information gets lost here.
//			}
//		}
		return result;
	}
	
	/**
	 * Returns the <code>ExpertName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>ExpertName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getExpertName(TaxonBase<?> taxon) {
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
	private static String getExpertFk(TaxonBase<?> taxon) {
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
//		mapping.addMapper(MethodMapper.NewInstance("SourceFK", this.getClass(), "getSourceFK", DbExportStateBase.class, standardMethodParameter));
		mapping.addMapper(DbObjectMapper.NewInstance("sec", "SourceFK"));
//		mapping.addMapper(MethodMapper.NewInstance("KingdomFk", this.getClass(), "getKingdomFk", standardMethodParameter, DbExportStateBase.class));
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
		mapping.addMapper(MethodMapper.NewInstance("TypeNameFk", this.getClass(), "getTypeNameFk", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("TypeFullnameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeDesignationStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeDesignationStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TreeIndex", this.getClass(), "getTreeIndex", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("IdInSource", this));
		mapping.addMapper(MethodMapper.NewInstance("GUID", this));
		mapping.addMapper(MethodMapper.NewInstance("DerivedFromGuid", this));
		mapping.addMapper(MethodMapper.NewInstance("OriginalDB", this));
		mapping.addMapper(MethodMapper.NewInstance("LastAction", this));
//		mapping.addMapper(DbTimePeriodMapper.NewInstance("updated", "LastActionDate"));
		mapping.addMapper(MethodMapper.NewInstance("LastActionDate", this));
		mapping.addMapper(MethodMapper.NewInstance("ExpertName", this));
		mapping.addMapper(MethodMapper.NewInstance("ExpertFk", this));

		return mapping;
	}

}
