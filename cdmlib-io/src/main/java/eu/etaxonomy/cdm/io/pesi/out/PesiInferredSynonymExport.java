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
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbExtensionMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.erms.ErmsTransformer;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
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
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author e.-m.lee
 * @date 04.08.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiInferredSynonymExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiInferredSynonymExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = TaxonNameBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Taxon";
	private static final String pluralString = "Inferred Synonyms";
	private static final String parentPluralString = "Taxa";
	private static Integer kingdomFk;
	private static NomenclaturalCode nomenclaturalCode = null;
	private AnnotationType treeIndexAnnotationType;
	private static ExtensionType lastActionExtensionType;
	private static ExtensionType lastActionDateExtensionType;
	private static ExtensionType expertNameExtensionType;
	private static ExtensionType speciesExpertNameExtensionType;
	private static ExtensionType cacheCitationExtensionType;
	private static ExtensionType expertUserIdExtensionType;
	private static ExtensionType speciesExpertUserIdExtensionType;
	private static String auctString = "auct.";
	
	public PesiInferredSynonymExport() {
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
			expertUserIdExtensionType = (ExtensionType)getTermService().find(PesiTransformer.expertUserIdUuid);
			speciesExpertUserIdExtensionType = (ExtensionType)getTermService().find(PesiTransformer.speciesExpertUserIdUuid);

			int count = 0;
			int pastCount = 0;
			int pageSize = limit;
			int pageNumber = 1;
			TransactionStatus txStatus = null;

			logger.error("Creating Inferred Synonyms...");
			// Create inferred synonyms for all accepted taxa
			
			// Start transaction
			TaxonomicTree taxonTree = null;
			Taxon acceptedTaxon = null;
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
			List<TaxonBase> taxonList = null;
			List<Synonym> inferredSynonyms = null;
			while ((taxonList  = getTaxonService().listTaxaByName(Taxon.class, "*", "*", "*", "*", null, pageSize, pageNumber)).size() > 0) {

				logger.error("Fetched " + taxonList.size() + " " + parentPluralString + ". Exporting...");
				for (TaxonBase taxonBase : taxonList) {

					if (taxonBase.isInstanceOf(Taxon.class)) { // this should always be the case since we should have fetched accepted taxon only, but you never know...
						acceptedTaxon = CdmBase.deproxy(taxonBase, Taxon.class);
						TaxonNameBase taxonName = acceptedTaxon.getName();
						
						if (taxonName.isInstanceOf(ZoologicalName.class)) {
							nomenclaturalCode  = PesiTransformer.getNomenclaturalCode(taxonName);
							kingdomFk = PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode);
	
							Set<TaxonNode> taxonNodes = acceptedTaxon.getTaxonNodes();
							if (taxonNodes.size() > 0) {
								// Determine the taxonomicTree of the current Taxon
								TaxonNode singleNode = taxonNodes.iterator().next();
								if (singleNode != null) {
									taxonTree = singleNode.getTaxonomicTree();
								}
							} else {
								// TaxonomicTree could not be determined directly from this Taxon
								// The stored taxonomicTree from another Taxon is used. It's a simple, but not a failsafe fallback solution.
								logger.error("TaxonomicTree could not be determined directly from this Taxon. " +
										"This taxonomicTree stored from another Taxon is used: " + taxonTree.getTitleCache());
							}
							
							if (taxonTree != null) {
	//							inferredSynonyms  = getTaxonService().createAllInferredSynonyms(taxonTree, acceptedTaxon);
	
								inferredSynonyms = getTaxonService().createInferredSynonyms(taxonTree, acceptedTaxon, SynonymRelationshipType.INFERRED_GENUS_OF());
								if (inferredSynonyms != null) {
									for (Synonym synonym : inferredSynonyms) {
										doCount(count++, modCount, pluralString);
										success &= mapping.invoke(synonym.getName());
									}
								}
							} else {
								logger.error("TaxonomicTree is NULL. Inferred Synonyms could not be created for this Taxon: " + acceptedTaxon.getUuid() + " (" + acceptedTaxon.getTitleCache() + ")");
							}
						} else {
//							logger.error("TaxonName is not a ZoologicalName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
						}
					} else {
						logger.error("This TaxonBase is not a Taxon even though it should be: " + taxonBase.getUuid() + " (" + taxonBase.getTitleCache() + ")");
					}
				}

				// Commit transaction
				commitTransaction(txStatus);
				logger.error("Committed transaction.");
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;

				// Start transaction
				txStatus = startTransaction(true);
				logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
				
				// Increment pageNumber
				pageNumber++;
			}
			if (taxonList.size() == 0) {
				logger.error("No " + parentPluralString + " left to fetch.");
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
	private static Integer getTaxonStatusFk(TaxonNameBase taxonName, PesiExportState state) {
		Integer result = null;
		
		try {
			if (isAuctReference(taxonName, state)) {
				Synonym synonym = Synonym.NewInstance(null, null);
				
				// This works as long as only the instance is important to differentiate between TaxonStatus.
				result = PesiTransformer.taxonBase2statusFk(synonym); // Auct References are treated as Synonyms in Datawarehouse now.
			} else {
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
	private static String getTaxonStatusCache(TaxonNameBase taxonName, PesiExportState state) {
		String result = null;
		
		try {
			if (isAuctReference(taxonName, state)) {
				Synonym synonym = Synonym.NewInstance(null, null);
				
				// This works as long as only the instance is important to differentiate between TaxonStatus.
				result = PesiTransformer.taxonBase2statusCache(synonym); // Auct References are treated as Synonyms in Datawarehouse now.
			} else {
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
		
		try {
			
		Set<IdentifiableSource> nameSources = taxonName.getSources();
		for (IdentifiableSource nameSource : nameSources) {
			String sourceIdNameSpace = nameSource.getIdNamespace();
			if (sourceIdNameSpace != null && sourceIdNameSpace.equals("originalGenusId")) {
				result = "Nominal Taxon from TAX_ID: " + nameSource.getIdInSource();
			}
		}
		
		// Get idInSource
		if (result == null) {
			String originalTaxId = getIdInSourceOnly(taxonName);
			if (originalTaxId != null) {
				result = "TAX_ID: " + originalTaxId;
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Returns the idInSource for a given taxonName.
	 * @param taxonName
	 * @return
	 */
	private static String getIdInSourceOnly(TaxonNameBase taxonName) {
		String result = null;
		
		Set<IdentifiableSource> nameSources = taxonName.getSources();
		for (IdentifiableSource nameSource : nameSources) {
			String sourceIdNameSpace = nameSource.getIdNamespace();
			if (sourceIdNameSpace != null && sourceIdNameSpace.equals("originalGenusId")) {
				result = nameSource.getIdInSource();
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
	private static String getGUID(TaxonNameBase taxonName) {
		String result = null;
		try {
			result = "urn:lsid:faunaeur.org:taxname:" + getIdInSourceOnly(taxonName);
		} catch (Exception e) {
			logger.error("Text could not be excluded from idInSource for taxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
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
			
			// idInSource only
			String idInSource = getIdInSourceOnly(taxonName);
			
			// build the cacheCitation
			if (expertName != null) {
				result += expertName + ". ";
			} else {
//				logger.error("ExpertName could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
			if (webShowName != null) {
				result += webShowName + ". ";
			} else {
//				logger.error("WebShowName could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
			}
			
			if (getOriginalDB(taxonName).equals("FaEu")) {
				result += "Accessed through: Fauna Europaea at http://faunaeur.org/full_results.php?id=";
			} else if (getOriginalDB(taxonName).equals("EM")) {
				result += "Accessed through: Euro+Med PlantBase at http://ww2.bgbm.org/euroPlusMed/PTaxonDetail.asp?UUID=";
			}
			
			if (idInSource != null) {
				result += idInSource;
			} else {
//				logger.error("IdInSource could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
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
	@SuppressWarnings({ "unused" })
	private static DateTime getLastActionDate(TaxonNameBase taxonName) {
		DateTime result = null;
		try {
		Set<Extension> extensions = taxonName.getExtensions();
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
	@SuppressWarnings("unused")
	private static Integer getSourceFk(TaxonNameBase taxonName, PesiExportState state) {
		Integer result = null;
		
		try {
		TaxonBase taxonBase = getSourceTaxonBase(taxonName);

		if (taxonBase != null) {
			result = state.getDbId(taxonBase.getSec());
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Checks whether a Reference of a TaxonName's TaxonBase is an Auct Reference.
	 * @param taxonName
	 * @param state
	 * @return
	 */
	private static boolean isAuctReference(TaxonNameBase taxonName, PesiExportState state) {
		boolean result = false;
		
		try {
		TaxonBase taxonBase = getSourceTaxonBase(taxonName);

		if (taxonBase != null) {
			if (auctString.equals(taxonBase.getTitleCache())) {
				result = true;
			}
		} else {
			logger.error("A TaxonBase could not be determined for this TaxonName: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Determines the TaxonBase of a TaxonName.
	 * @param taxonName
	 * @return
	 */
	private static TaxonBase getSourceTaxonBase(TaxonNameBase taxonName) {
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
		return taxonBase;
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
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusFk", this.getClass(), "getTaxonStatusFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusCache", this.getClass(), "getTaxonStatusCache", standardMethodParameter, PesiExportState.class));
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
