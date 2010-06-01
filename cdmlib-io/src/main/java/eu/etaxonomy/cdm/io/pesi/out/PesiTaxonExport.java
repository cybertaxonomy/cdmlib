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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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
	private static final Class<? extends CdmBase> standardMethodParameter = TaxonNameBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Taxon";
	private static final String pluralString = "Taxa";
	
	public class Data {
		Integer kingdomId = null;
		Integer parentTaxonId = null;
		String treeIndex = null;
		String nomenclaturalCode = null;

		/**
		 * @return the nomenclaturalCode
		 */
		protected String getNomenclaturalCode() {
			return nomenclaturalCode;
		}
		/**
		 * @param nomenclaturalCode the nomenclaturalCode to set
		 */
		protected void setNomenclaturalCode(String nomenclaturalCode) {
			this.nomenclaturalCode = nomenclaturalCode;
		}
		
		/**
		 * @return the kingdomId
		 */
		protected Integer getKingdomId() {
			return kingdomId;
		}
		/**
		 * @param kingdomId the kingdomId to set
		 */
		protected void setKingdomId(Integer kingdomId) {
			this.kingdomId = kingdomId;
		}
		
		/**
		 * @return the parentTaxonId
		 */
		protected Integer getParentTaxonId() {
			return parentTaxonId;
		}
		/**
		 * @param parentTaxonid the parentTaxonid to set
		 */
		protected void setParentTaxonId(Integer parentTaxonId) {
			this.parentTaxonId = parentTaxonId;
		}
		
		/**
		 * @return the treeIndex
		 */
		protected String getTreeIndex() {
			return treeIndex;
		}
		/**
		 * @param treeIndex the treeIndex to set
		 */
		protected void setTreeIndex(String treeIndex) {
			this.treeIndex = treeIndex;
		}
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
			Connection con = state.getConfig().getDestination().getConnection();
			String parentTaxonFk_TreeIndex_KingdomFkSql = "UPDATE Taxon SET ParentTaxonFk = ?, TreeIndex = ?, " +
					"KingdomFk = ?, RankFk = ?, RankCache = ? WHERE TaxonId = ?"; 
			PreparedStatement parentTaxonFk_TreeIndex_KingdomFkStmt = con.prepareStatement(parentTaxonFk_TreeIndex_KingdomFkSql);

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
//						if (rank == null) {
//							logger.error("Rank was not determined: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
//						} else {
//							if (infraSpecificEpithet == null && rank.intValue() == 190) {
//								logger.error("InfraSpecificEpithet was not determined although it should exist for rank 190: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
//							}
//							if (specificEpithet != null && rank.intValue() < 220) {
//								logger.error("SpecificEpithet was determined for rank " + rank + " although it should only exist for ranks higher or equal to 220: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
//							}
//							if (infraSpecificEpithet != null && rank.intValue() < 230) {
//								logger.error("InfraSpecificEpithet was determined for rank " + rank + " although it should only exist for ranks higher or equal to 230: "  + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
//							}
//						}
//						if (infraSpecificEpithet != null && specificEpithet == null) {
//							logger.error("An infraSpecificEpithet was determined, but a specificEpithet was not determined: "  + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
//						}
//						if (genusOrUninomial == null) {
//							logger.error("GenusOrUninomial was not determined: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
//						}
//						
//						rank = null;
//						infraSpecificEpithet = null;
//						genusOrUninomial = null;
//						specificEpithet = null;

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
			while ((list = getNameService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (TaxonNameBase taxonName : list) {
					doCount(count++, modCount, "ParentTaxonFk");
					success &= invokeParentTaxonFkAndTreeIndexAndKindomFk(taxonName, state, parentTaxonFk_TreeIndex_KingdomFkStmt);
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

			state.clearIncludedInTreeIndex();

			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * 
	 * @param taxonNameBase
	 * @param state
	 * @param stmt
	 * @return
	 */
	protected boolean invokeParentTaxonFkAndTreeIndexAndKindomFk(TaxonNameBase taxonNameBase, PesiExportState state, PreparedStatement treeIndexAndKingdomFkStmt) {
		if (taxonNameBase == null) {
			return true;
		} else {
			Data newData = new Data();
			getParentTaxonFkAndTreeIndexAndKingdomFk(taxonNameBase, newData, state);
			if (newData.getTreeIndex() == null || newData.getKingdomId() == null ||
					newData.getParentTaxonId() == null || newData.getNomenclaturalCode() == null) {
				return true;
			} else {
				Integer taxonId = state.getDbId(taxonNameBase);
				try {
					treeIndexAndKingdomFkStmt.setInt(1, newData.getParentTaxonId());
					treeIndexAndKingdomFkStmt.setString(2, newData.getTreeIndex());
					treeIndexAndKingdomFkStmt.setInt(3, newData.getKingdomId());
					treeIndexAndKingdomFkStmt.setInt(4, getRankFk(taxonNameBase, newData.getNomenclaturalCode()));
					treeIndexAndKingdomFkStmt.setString(5, getRankCache(taxonNameBase, newData.getNomenclaturalCode()));
					treeIndexAndKingdomFkStmt.setInt(6, taxonId);
					treeIndexAndKingdomFkStmt.executeUpdate();
					return true;
				} catch (SQLException e) {
					logger.error("SQLException during treeIndex invoke for taxonName - " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + "): " + e.getMessage());
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
	 * Returns the <code>RankFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>RankFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getRankFk(TaxonNameBase taxonName, String nomenclaturalCode) {
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
	private static String getRankCache(TaxonNameBase taxonName, String nomenclaturalCode) {
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
		String result = null;
		try {
		if (taxonName != null) {
			if (taxonName != null && taxonName.isInstanceOf(NonViralName.class)) {
				NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
				String singleWhitespace = "\\s";
				String multipleWhitespaces = "\\s*";
				String whitespaceOrNothing = "\\s?";
				String singleBlank = " ";
				String anyNumberOfCharacters = ".*";
				String backSlashRegEx = "\\";
//				String periodRegEx = "\\.";
//				String asterixRegEx = "\\*";
				String questionMarkRegEx = "\\?";
//				String plusRegEx = "\\+";
//				String squareBracketRegEx = "\\[";
//				String curlyBracketRegEx = "\\{";
//				String pipeRegEx = "\\|";
//				String accentRegEx = "\\^";
//				String dollarSignRegEx = "\\$";
				String openingParenthesisRegEx = "\\(";
				String closingParenthesisRegEx = "\\)";
				String openParenthesis = "(";
				String closeParenthesis = ")";
				String italicBeginTag = "<i>";
				String italicEndTag = "</i>";
				
				String questionMarkReplacement = backSlashRegEx + questionMarkRegEx;
//				String backSlashReplacement = backSlashRegEx + backSlashRegEx;
//				String periodReplacement = backSlashRegEx + periodRegEx;
//				String asterixReplacement = backSlashRegEx + asterixRegEx;
//				String plusReplacement = backSlashRegEx + plusRegEx;
//				String squareBracketReplacement = backSlashRegEx + squareBracketRegEx;
//				String curlyBracketReplacement = backSlashRegEx + curlyBracketRegEx;
//				String pipeReplacement = backSlashRegEx + pipeRegEx;
//				String accentReplacement = backSlashRegEx + accentRegEx;
//				String dollarSignReplacement = backSlashRegEx + dollarSignRegEx;

				if (nonViralName != null) {
					if (nonViralName.getGenusOrUninomial() != null) {
						if (nonViralName.getTitleCache() != null) {
							try {
								String fullName = nonViralName.getTitleCache();
								
								// This deals with question marks that are reserved in regular expressions
								fullName = fullName.replaceAll(questionMarkRegEx, questionMarkReplacement);

								String genusOrUninomial = nonViralName.getGenusOrUninomial();
								String infraGenericEpithet = nonViralName.getInfraGenericEpithet();
								String specificEpithet = nonViralName.getSpecificEpithet();
								String infraSpecificEpithet = nonViralName.getInfraSpecificEpithet();
								
								boolean genusOrUninomialExist = false;
								boolean infraGenericEpithetExists = false;
								boolean specificEpithetExists = false;
								boolean infraSpecificEpithetExists = false;
								boolean rankExists = false;

								Matcher genusOrUninomialMatcher = null;
								// Define specific regular expressions and patterns
								if (genusOrUninomial != null) {
									String genusOrUninomialRegEx;
									if (fullName.length() > genusOrUninomial.length()) {
										// It's an assumption that there is a blank right after the specific epithet in this case. Keep an eye on it.
										genusOrUninomialRegEx = genusOrUninomial + singleWhitespace;
									} else {
										genusOrUninomialRegEx = genusOrUninomial;
									}
									Pattern genusOrUninomialPattern = Pattern.compile(genusOrUninomialRegEx);
									genusOrUninomialMatcher = genusOrUninomialPattern.matcher(fullName);
									
									if (genusOrUninomialMatcher.find()) {
										genusOrUninomialExist = true;
									}
								}

								Matcher infraGenericEpithetMatcher = null;
								if (infraGenericEpithet != null) {
									String infraGenericEpithetRegEx = openingParenthesisRegEx + infraGenericEpithet + closingParenthesisRegEx;
									Pattern infraGenericEpithetPattern = Pattern.compile(infraGenericEpithetRegEx);
									infraGenericEpithetMatcher = infraGenericEpithetPattern.matcher(fullName);
									
									if (infraGenericEpithetMatcher.find()) {
										infraGenericEpithetExists = true;
									}
								}

								Matcher specificEpithetMatcher;
								String specificEpithetRegEx;
								Pattern specificEpithetPattern;
								if (specificEpithet != null) {
									specificEpithetRegEx = singleWhitespace + specificEpithet + singleWhitespace;
									specificEpithetPattern = Pattern.compile(specificEpithetRegEx);
									specificEpithetMatcher = specificEpithetPattern.matcher(fullName);
									
									if (genusOrUninomialExist || infraGenericEpithetExists) {
										// it's in between somewhere
										specificEpithetRegEx = singleWhitespace + specificEpithet + singleWhitespace;
									} else {
										// it's at the beginning
										if (fullName.length() > specificEpithet.length()) {
											// It's an assumption that there is a blank right after the specific epithet in this case. Keep an eye on it.
											specificEpithetRegEx = specificEpithet + singleWhitespace;
										} else {
											specificEpithetRegEx = specificEpithet;
										}
									}
									specificEpithetPattern = Pattern.compile(specificEpithetRegEx);
									specificEpithetMatcher = specificEpithetPattern.matcher(fullName);
									if (specificEpithetMatcher.find()) {
										specificEpithetExists = true;
									}
								}

								if (infraSpecificEpithet != null) {
									String infraSpecificEpithetRegEx = infraSpecificEpithet;
									Pattern infraSpecificEpithetPattern = Pattern.compile(infraSpecificEpithetRegEx);
									Matcher infraSpecificEpithetMatcher = infraSpecificEpithetPattern.matcher(fullName);

									int count;
									// Infra specific epithet
									if (specificEpithet.equals(infraSpecificEpithet)) {
										count = 2;
									} else {
										count = 1;
									}
									
									int infraSpecificCount = 0;
									int index = 0;
									while ((index = fullName.indexOf(infraSpecificEpithet, index)) != -1) {
										infraSpecificCount++;
										index++;
									}
									if (infraSpecificCount == count) {
										infraSpecificEpithetExists = true;
									} else {
										infraSpecificEpithetExists = false;
									}
								}

								String rankExistsRegEx = null;
								// Check whether rank information is included in fullName
								if (specificEpithetExists && infraSpecificEpithetExists) {
									rankExistsRegEx = specificEpithet + singleWhitespace + anyNumberOfCharacters + singleWhitespace + infraSpecificEpithet;
									Pattern rankExistsPattern = Pattern.compile(rankExistsRegEx);
									Matcher rankExistsMatcher = rankExistsPattern.matcher(fullName);
									if (rankExistsMatcher.find()) {
										rankExists = true;
									} else {
										rankExists = false;
									}
								}
								
								// Replace
								String specificEpithetReplacement = specificEpithet + italicEndTag;
								String infraGenericEpithetReplacement = openParenthesis + infraGenericEpithet + closeParenthesis + italicEndTag;
								if (genusOrUninomialExist || infraGenericEpithetExists || specificEpithetExists) {
									fullName = italicBeginTag + fullName;
								}

								StringBuffer replaceFullName = new StringBuffer(fullName);
								if (specificEpithetExists) {
									// Determine index of specificEpithet and place italic end tag right after its position
									int specificEpithetPosition = replaceFullName.indexOf(specificEpithet) + specificEpithet.length();
									replaceFullName.insert(specificEpithetPosition, italicEndTag);
								} else if (infraGenericEpithetExists) {
									int infraGenericEpithetPosition = replaceFullName.indexOf(infraGenericEpithet) + infraGenericEpithet.length();
									replaceFullName.insert(infraGenericEpithetPosition, italicEndTag);
								} else if (genusOrUninomialExist) {
									replaceFullName.insert(genusOrUninomial.length() + italicBeginTag.length(), italicEndTag);
								}
								
								if (infraSpecificEpithetExists) {
									int infraSpecificEpithetPosition = replaceFullName.lastIndexOf(infraSpecificEpithet);
									int infraSpecificEpithetBeginPosition = infraSpecificEpithetPosition;
									int infraSpecificEpithetEndPosition = infraSpecificEpithetPosition + infraSpecificEpithet.length();
									
									if (rankExists) {
										replaceFullName.insert(infraSpecificEpithetBeginPosition, italicBeginTag);
									}
									replaceFullName.insert(infraSpecificEpithetEndPosition + italicBeginTag.length(), italicEndTag);
								}
								
								result = replaceFullName.toString();

							} catch (Exception e) {
								// This needs a workaround since very likely there is a character in the fullName that is reserved in regular expressions
								logger.error("WebShowName could not be determined for NonViralName " + nonViralName.getUuid() + " (" + nonViralName.getTitleCache() + "): " + e.getMessage());
								result = nonViralName.getTitleCache();
							}
						} else {
							logger.error("WebShowName could not be determined: Fullname is NULL for NonViralName " + nonViralName.getUuid() + " (" + nonViralName.getTitleCache() + ")");
						}
					} else {
						logger.error("WebShowName could not be determined: GenusOrUninomial is NULL for NonViralName " + nonViralName.getUuid() + " (" + nonViralName.getTitleCache() + ")");
					}
				} else {
					logger.error("WebShowName could not be determined: NonViralName is NULL for TaxonName" + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
				}
				
			}
		} else {
			logger.warn("WebShowName could not be determined: TaxonName is NULL");
		}
		
//		logger.error("final result: " + result);
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
//		if (taxonNameBase != null) {
//			Set<NameTypeDesignation> nameTypeDesignations = taxonNameBase.getNameTypeDesignations();
//			if (nameTypeDesignations.size() == 1) {
//				NameTypeDesignation nameTypeDesignation = nameTypeDesignations.iterator().next();
//				if (nameTypeDesignation != null) {
//					TaxonNameBase typeName = nameTypeDesignation.getTypeName();
//					if (typeName != null) {
//						Set<TaxonBase> taxa = typeName.getTaxa();
//						if (taxa.size() == 1) {
//							TaxonBase singleTaxon = taxa.iterator().next();
//							result = state.getDbId(singleTaxon.getName());
//						} else if (taxa.size() > 1) {
//							logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
//						}
//					}
//				}
//			} else if (nameTypeDesignations.size() > 1) {
//				logger.warn("This TaxonName has " + nameTypeDesignations.size() + " NameTypeDesignations: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
//			}
//		}
//		if (result != null) {
//			logger.error("Taxon Id: " + result);
//			logger.error("TaxonName: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() +")");
//		}
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
	 * returns the <code>TreeIndex</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TreeIndex</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static boolean getParentTaxonFkAndTreeIndexAndKingdomFk(TaxonNameBase taxonNameBase, Data newData, PesiExportState state) {
		// Traverse taxon tree up until root is reached.
		String result = "";
		try {
		String taxonIdString = "" + state.getDbId(taxonNameBase);
		if (state.isIncludedInTreeIndex(taxonNameBase.getId())) {
			getParentTaxonFkAndTreeIndexAndKingdomFkFromDatabase(taxonIdString, newData, state);
		} else {
			Taxon taxon = null;
			Set taxa = taxonNameBase.getTaxa();
			if (taxa.size() == 1) {
				CdmBase singleTaxon = (CdmBase) taxa.iterator().next();
				if (singleTaxon.isInstanceOf(Synonym.class)) {
					Synonym synonym = CdmBase.deproxy(singleTaxon, Synonym.class);
					Set<SynonymRelationship> relations = synonym.getSynonymRelations();
					if (relations.size() == 1) {
						SynonymRelationship relation = relations.iterator().next();
						if (relation != null) {
							taxon = relation.getAcceptedTaxon();
						}
					}
				} else {
					taxon = CdmBase.deproxy(singleTaxon, Taxon.class);
				}
			} else if (taxa.size() > 1) {
				logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
				return false;
			}

			// Process only taxa that are leafs in the taxonomic tree since all other taxa are included in their branches
			if (taxon != null) {
				Set<TaxonNode> taxonNodes = taxon.getTaxonNodes();
				if (taxonNodes.size() == 1 && taxonNodes.iterator().next().getCountChildren() == 0) {
		
					boolean root = false;
					boolean error = false;
					boolean start = true;
					if (taxon != null) {
						// Add the current taxon
						result = "#" + state.getDbId(taxon.getName()) + "#";
						if (taxon.getName() != null) {
							state.addToTreeIndex(taxon.getName().getId());
						}
						
						while (! root && ! error) {
							if (taxonNodes.size() == 1) {
								TaxonNode taxonNode = taxon.getTaxonNodes().iterator().next();
								ITreeNode parentNode = taxonNode.getParent();
								if (HibernateProxyHelper.isInstanceOf(parentNode, TaxonNode.class)) {
									TaxonNode node = CdmBase.deproxy(parentNode, TaxonNode.class);
									taxon = node.getTaxon();

									// Set ParentTaxonFk
									if (start) {
										newData.setParentTaxonId(state.getDbId(taxon.getName()));
									}
									
									result = "#" + state.getDbId(taxon.getName()) + result;
									if (taxon.getName() != null) {
										state.addToTreeIndex(taxon.getName().getId());
									}
								} else {
									// Root element reached
									root = true;
									
									// TODO: Set current nomenclatural code
									// We are differentiating kingdoms by the nomenclatural code for now.
									// This needs to be handled in a better way as soon as we know how to differentiate between more kingdoms.
									String nomenclaturalCode = taxon.getName().getNomenclaturalCode().getTitleCache();

									// Set newData
									newData.setKingdomId(PesiTransformer.nomenClaturalCode2Kingdom(nomenclaturalCode));
									newData.setNomenclaturalCode(nomenclaturalCode);
									newData.setTreeIndex(result);
									
									// Add new entry into processed parentTaxonFk and treeIndex database table
									state.addToAlreadyProcessedTreeIndexAndKingdomFk(newData);
								}
								start = false;
							} else if (taxonNodes.size() > 1) {
								logger.warn("This Taxon has " + taxonNodes.size() + " TaxonNodes: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
							} else if (taxonNodes.size() == 0) {
								logger.warn("This Taxon has no TaxonNode: " + taxon.getUuid() + " (" + taxon.getTitleCache() + ")");
								taxon = null;
								result = null;
								error = true;
							}
						}
					}
					if (error) {
						result = null;
					}
				}
			}

		}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * @param taxonNameBase
	 * @return
	 */
	private static void getParentTaxonFkAndTreeIndexAndKingdomFkFromDatabase(String taxonId, Data newData, PesiExportState state) {
		if (!state.getParentTaxonFkAndTreeIndexAndKingdomFk(taxonId, newData)) {
			logger.error("ParentTaxonFk, TreeIndex and KingdomFk could not be determined from database even though they should exist.");
		}
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
		String result = "Nominal Taxon from TAX_ID: ";
		boolean set = false;
		
		try {
		Set taxa = taxonName.getTaxa();
		if (taxa.size() == 1) {
			IdentifiableEntity singleTaxon = (IdentifiableEntity) taxa.iterator().next();
			if (singleTaxon != null) {
				Set<IdentifiableSource> sources = singleTaxon.getSources();
				if (sources.size() == 1) {
					IdentifiableSource source = sources.iterator().next();
					if (source != null) {
						result += source.getIdInSource();
						set = true;
					}
				} else if (sources.size() > 1) {
					logger.warn("Taxon has multiple IdentifiableSources: " + singleTaxon.getUuid() + " (" + singleTaxon.getTitleCache() + ")");
					int count = 1;
					for (IdentifiableSource source : sources) {
						result += source.getIdInSource();
						if (count < sources.size()) {
							result += "; ";
						}
						count++;
						set = true;
					}
				} else {
					result = null;
				}
			}
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (set) {
			return result;
		} else {
			return null;
		}
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
		Set taxa = taxonName.getTaxa();
		if (taxa.size() == 1) {
			IdentifiableEntity singleTaxon = (IdentifiableEntity) taxa.iterator().next();
			if (singleTaxon != null) {
				Set<IdentifiableSource> sources = singleTaxon.getSources();
				if (sources.size() == 1) {
					IdentifiableSource source = sources.iterator().next();
					if (source != null) {
						ReferenceBase citation = source.getCitation();
						if (citation != null) {
							result = PesiTransformer.databaseString2Abbreviation(citation.getTitleCache());
						}
					}
				} else if (sources.size() > 1) {
					logger.warn("Taxon has multiple IdentifiableSources: " + singleTaxon.getUuid() + " (" + singleTaxon.getTitleCache() + ")");
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
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() +")");
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
			Set taxa = taxonNameBase.getTaxa();
			if (taxa.size() == 1) {
				VersionableEntity singleTaxon = (VersionableEntity) taxa.iterator().next();
				if (singleTaxon != null) {
					DateTime updated = singleTaxon.getUpdated();
					if (updated != null) {
		//				logger.error("Taxon Updated: " + updated);
						result = new DateTime(updated.toDate()); // Unfortunately the time information gets lost here.
					}
				}
			} else if (taxa.size() > 1) {
				logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonNameBase.getUuid() + " (" + taxonNameBase.getTitleCache() + ")");
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
		Set taxa = taxonName.getTaxa();
		if (taxa.size() == 1) {
			TaxonBase singleTaxon = CdmBase.deproxy(taxa.iterator().next(), TaxonBase.class);
			result = state.getDbId(singleTaxon.getSec());
		} else if (taxa.size() > 1) {
			logger.warn("This TaxonName has " + taxa.size() + " Taxa: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
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
