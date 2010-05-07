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
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
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

			// Prepare ParentTaxonFk-Statement
			String parentTaxonFkSql = "UPDATE Taxon SET ParentTaxonFk = ? WHERE TaxonId = ?"; 
			Connection con = state.getConfig().getDestination().getConnection();
			PreparedStatement stmt = con.prepareStatement(parentTaxonFkSql);

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
			logger.error("PHASE 2...");
			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getTaxonService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (TaxonBase taxonBase : list) {
					doCount(count++, modCount, "ParentTaxonFk");
					success &= invokeParentTaxonFk(taxonBase, state, stmt);
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
					logger.error("SQLException during parenTaxonFk invoke for taxon " + taxonBase.getTitleCache() + ": " + e.getMessage());
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
	@SuppressWarnings("unused")
	private static Integer getKingdomFk(TaxonBase<?> taxonBase, DbExportStateBase<?> state) {
		// Traverse taxon tree up until root is reached.
		Integer result = null;
		
		Taxon taxon = null;
		if (taxonBase.isInstanceOf(Synonym.class)) {
			Synonym synonym = (Synonym)taxonBase;
			Set<SynonymRelationship> relations = synonym.getSynonymRelations();
			if (relations.size() == 1) {
				taxon  = relations.iterator().next().getAcceptedTaxon();
			}
		} else {
			taxon = CdmBase.deproxy(taxonBase, Taxon.class);
		}
		boolean root = false;
		boolean error = false;
		if (taxon != null) {
			while (! root && ! error) {
				Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
				if (taxonNodes.size() == 1) {
					TaxonNode taxonNode = taxon.getTaxonNodes().iterator().next();
					ITreeNode parentNode = taxonNode.getParent();
					if (HibernateProxyHelper.isInstanceOf(parentNode, TaxonNode.class)) {
						TaxonNode node = CdmBase.deproxy(parentNode, TaxonNode.class);
						taxon = node.getTaxon();
//						logger.error("current taxon: " + state.getDbId(taxon));
					} else {
						// Root element reached
						result = state.getDbId(taxon);
//						logger.error("Root element reached. Highest Taxon: " + state.getDbId(taxon));
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
			nomenclaturalCode = taxon.getName().getNomenclaturalCode();

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
	@SuppressWarnings("unused")
	private static Integer getRankFk(TaxonBase<?> taxon) {
		Integer result = null;
		if (nomenclaturalCode != null && taxon.isInstanceOf(Taxon.class)) {
			result = PesiTransformer.rank2RankId(taxon.getName().getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
		}
		return result;
	}

	/**
	 * Returns the <code>RankCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>RankCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRankCache(TaxonBase<?> taxon) {
		String result = null;
		if (nomenclaturalCode != null && taxon.isInstanceOf(Taxon.class)) {
			result = PesiTransformer.rank2RankCache(taxon.getName().getRank(), PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
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
	@SuppressWarnings("unused")
	private static String getInfraGenericEpithet(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
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
	@SuppressWarnings("unused")
	private static String getSpecificEpithet(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
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
	@SuppressWarnings("unused")
	private static String getInfraSpecificEpithet(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
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
	private static String getWebSearchName(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName nonViralName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
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
	private static String getWebShowName(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null) {
			result = taxon.getName().getTitleCache();
//			List resultList = taxon.getName().getTaggedName();
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
		TeamOrPersonBase team = taxon.getSec().getAuthorTeam();
		if (team != null) {
			return team.getTitleCache();
		} else {
			return null;
		}
	}

	/**
	 * Returns the <code>FullName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FullName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFullName(TaxonBase<?> taxon) {
		if (taxon != null) {
			return taxon.getName().getTitleCache();
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
	private static String getNomRefString(TaxonBase<?> taxon) {
		String result = null;
		if (taxon != null) {
			try {
				if (taxon.getName().getNomenclaturalReference() != null) {
					result = taxon.getName().getNomenclaturalMicroReference();
				}
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
	private static String getDisplayName(TaxonBase<?> taxon) {
		// TODO: extension?
		if (taxon != null) {
			return taxon.getName().getFullTitleCache();
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
	private static String getFuzzyName(TaxonBase<?> taxon) {
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
	private static Integer getNameStatusFk(TaxonBase<?> taxon) {
		Integer result = null;
		if (taxon != null && (taxon.getName().isInstanceOf(NonViralName.class))) {
			NonViralName taxonName = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			Set<NomenclaturalStatus> states = taxonName.getStatus();
			if (states.size() == 1) {
				NomenclaturalStatusType statusType = states.iterator().next().getType();
				result = PesiTransformer.nomStatus2nomStatusFk(statusType);
			} else if (states.size() > 1) {
				logger.error("This taxon has more than one Nomenclatural Status: " + taxon.getTitleCache());
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
				logger.error("This taxon has more than one Nomenclatural Status: " + taxon.getTitleCache());
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
					logger.error("No parent taxon could be determined for taxon: " + taxon.getTitleCache());
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
			result = state.getDbId(taxonBase);
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
			result = taxon.getName().getTitleCache();
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
	private static Integer getQualityStatusFk(TaxonBase<?> taxon) {
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
	private static String getQualityStatusCache(TaxonBase<?> taxon) {
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
	private static Integer getTypeDesignationStatusFk(TaxonBase<?> taxon) {
		Integer result = null;
		if (taxon != null) {
			Set<NameTypeDesignation> typeDesignations = taxon.getName().getNameTypeDesignations();
			if (typeDesignations.size() == 1) {
				Object obj = typeDesignations.iterator().next().getTypeStatus();
				NameTypeDesignationStatus designationStatus = CdmBase.deproxy(obj, NameTypeDesignationStatus.class);
				result = PesiTransformer.nameTypeDesignationStatus2TypeDesignationStatusId(designationStatus);
			} else if (typeDesignations.size() > 1) {
				logger.error("Found a taxon with more than one NameTypeDesignation: " + taxon.getTitleCache());
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
				logger.error("Found a taxon with more than one NameTypeDesignation: " + taxon.getTitleCache());
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
		// Traverse taxon tree up until root is reached.
		String result = "";
		
		Taxon taxon = null;
		if (taxonBase.isInstanceOf(Synonym.class)) {
			Synonym synonym = (Synonym)taxonBase;
			Set<SynonymRelationship> relations = synonym.getSynonymRelations();
			if (relations.size() == 1) {
				taxon  = relations.iterator().next().getAcceptedTaxon();
			}
		} else {
			taxon = CdmBase.deproxy(taxonBase, Taxon.class);
		}
		boolean root = false;
		boolean error = false;
		boolean start = true;
		if (taxon != null) {
			// Add the current taxon
			result = "#" + state.getDbId(taxon) + "#";

			while (! root && ! error) {
				Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
				if (taxonNodes.size() == 1) {
					TaxonNode taxonNode = taxon.getTaxonNodes().iterator().next();
					ITreeNode parentNode = taxonNode.getParent();
					if (HibernateProxyHelper.isInstanceOf(parentNode, TaxonNode.class)) {
						TaxonNode node = CdmBase.deproxy(parentNode, TaxonNode.class);
						taxon = node.getTaxon();
						result = "#" + state.getDbId(taxon) + result;
//						logger.error("current taxon: " + state.getDbId(taxon));
					} else {
						// Root element reached
//						logger.error("Root element reached. Highest Taxon: " + state.getDbId(taxon));
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
		Taxon taxon;
		if (taxonBase.isInstanceOf(Taxon.class)) {
//			taxon = CdmBase.deproxy(taxonBase, Taxon.class);
//			Set<TaxonDescription> specimenDescription = taxon.;
//			result = PesiTransformer.fossil2FossilStatusId(fossil);
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
	private static String getFossilStatusCache(TaxonBase<?> taxon) {
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
	private static String getIdInSource(TaxonBase<?> taxon) {
		String result = null;
		
		Set<IdentifiableSource> sources = taxon.getSources();
		if (sources.size() == 1) {
			result = sources.iterator().next().getIdInSource();
		} else if (sources.size() > 1) {
			logger.warn("Taxon has multiple IdentifiableSources: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
			int count = 1;
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
	 * Returns the <code>GUID</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>GUID</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getGUID(TaxonBase<?> taxon) {
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
		Set<IdentifiableSource> sources = taxon.getSources();
		if (sources.size() == 1) {
			result = sources.iterator().next().getCitation().getTitleCache(); //or just title
		} else if (sources.size() > 1) {
			logger.warn("Taxon has multiple IdentifiableSources: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
			int count = 1;
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
	private static DateTime getLastActionDate(TaxonBase<?> taxonBase) {
		DateTime result = null;
		if (taxonBase != null) {
			DateTime updated = taxonBase.getUpdated();
			if (updated != null) {
//				logger.error("Taxon Updated: " + updated);
				result = new DateTime(updated.toDate()); // Unfortunately the time information gets lost here.
			}
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
		mapping.addMapper(MethodMapper.NewInstance("KingdomFk", this.getClass(), "getKingdomFk", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("RankFk", this));
		mapping.addMapper(MethodMapper.NewInstance("RankCache", this));
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
