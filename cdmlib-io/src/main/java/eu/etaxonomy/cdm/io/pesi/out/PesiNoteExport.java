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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The export class for {@link eu.etaxonomy.cdm.model.description.DescriptionElementBase DescriptionElements}.<p>
 * Inserts into DataWarehouse database table <code>Note</code>.<p>
 * It is divided into two phases:<ul>
 * <li>Phase 1:	Export of DescriptionElements as Notes.
 * <li>Phase 2:	Export of TaxonName extensions <code>taxComment</code>, <code>fauComment</code> and <code>fauExtraCodes</code> as Notes.</ul>
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
public class PesiNoteExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiNoteExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = DescriptionElementBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Note";
	private static final String pluralString = "Notes";
	private static final String parentPluralString = "Taxa";

	public PesiNoteExport() {
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
			int pageSize = 1000;

			// Calculate the pageNumber
			int pageNumber = 1;

			// Stores whether this invoke was successful or not.
			boolean success = true;
	
			// PESI: Clear the database table Note.
			doDelete(state);
	
			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Note
			PesiExportMapping mapping = getMapping();
	
			// Initialize the db mapper
			mapping.initialize(state);
	
			// PESI: Create the Notes
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<DescriptionElementBase> list = null;
			
			// Start transaction
			logger.error("PHASE 1...");
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + pageSize + ") ...");
			while ((list = getDescriptionService().listDescriptionElements(null, null, null, pageSize, pageNumber, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (DescriptionElementBase description : list) {
					
					if (getNoteCategoryFk(description) != null) {
						doCount(count++, modCount, pluralString);
						success &= mapping.invoke(description);
					}
				}

				// Commit transaction
				commitTransaction(txStatus);
				logger.error("Committed transaction.");
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;
	
				// Start transaction
				txStatus = startTransaction(true);
				logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + pageSize + ") ...");
				
				// Increment pageNumber
				pageNumber++;
			}
			if (list.size() == 0) {
				logger.error("No " + pluralString + " left to fetch.");
			}
			// Commit transaction
			commitTransaction(txStatus);
			logger.error("Committed transaction.");

			
			logger.error("PHASE 2...");
			txStatus = startTransaction(true);
			List<TaxonNameBase> taxonNameList = null;
			ExtensionType taxCommentExtensionType = (ExtensionType)getTermService().find(PesiTransformer.taxCommentUuid);
			ExtensionType fauCommentExtensionType = (ExtensionType)getTermService().find(PesiTransformer.fauCommentUuid);
			ExtensionType fauExtraCodesExtensionType = (ExtensionType)getTermService().find(PesiTransformer.fauExtraCodesUuid);
			String taxComment = null;
			String fauComment = null;
			String fauExtraCodes = null;
			
			Connection connection = state.getConfig().getDestination().getConnection();
			logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
			while ((taxonNameList = getNameService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + parentPluralString + ". Exporting...");
				for (TaxonNameBase taxonName : taxonNameList) {
					Set<Extension> extensions = taxonName.getExtensions();
					for (Extension extension : extensions) {
						if (extension.getType().equals(taxCommentExtensionType)) {
							taxComment = extension.getValue();
							invokeNotes(taxComment, 
									PesiTransformer.getNoteCategoryFk(PesiTransformer.taxCommentUuid), 
									PesiTransformer.getNoteCategoryCache(PesiTransformer.taxCommentUuid),
									null, null, getTaxonFk(taxonName, state),connection);
						} else if (extension.getType().equals(fauCommentExtensionType)) {
							fauComment = extension.getValue();
							invokeNotes(fauComment, 
									PesiTransformer.getNoteCategoryFk(PesiTransformer.fauCommentUuid), 
									PesiTransformer.getNoteCategoryCache(PesiTransformer.fauCommentUuid),
									null, null, getTaxonFk(taxonName, state),connection);
						} else if (extension.getType().equals(fauExtraCodesExtensionType)) {
							fauExtraCodes = extension.getValue();
							invokeNotes(fauExtraCodes, 
									PesiTransformer.getNoteCategoryFk(PesiTransformer.fauExtraCodesUuid), 
									PesiTransformer.getNoteCategoryCache(PesiTransformer.fauExtraCodesUuid),
									null, null, getTaxonFk(taxonName, state),connection);
						}
					}
					
					doCount(count++, modCount, pluralString);
				}

				// Commit transaction
				commitTransaction(txStatus);
				logger.error("Committed transaction.");
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;

				// Start transaction
				txStatus = startTransaction(true);
				logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
			}
			if (list.size() == 0) {
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
	 * @param taxComment
	 * @param noteCategoryFk
	 * @param noteCategoryCache
	 * @param object
	 * @param object2
	 */
	private void invokeNotes(String note, Integer noteCategoryFk,
			String noteCategoryCache, Integer languageFk, String languageCache, 
			Integer taxonFk, Connection connection) {
		String notesSql = "UPDATE Note SET Note_1 = ?, NoteCategoryFk = ?, NoteCategoryCache = ?, LanguageFk = ?, LanguageCache = ? WHERE TaxonFk = ?"; 
		try {
			PreparedStatement notesStmt = connection.prepareStatement(notesSql);
			
			if (note != null) {
				notesStmt.setString(1, note);
			} else {
				notesStmt.setObject(1, null);
			}
			
			if (noteCategoryFk != null) {
				notesStmt.setInt(2, noteCategoryFk);
			} else {
				notesStmt.setObject(2, null);
			}
			
			if (noteCategoryCache != null) {
				notesStmt.setString(3, noteCategoryCache);
			} else {
				notesStmt.setObject(3, null);
			}
			
			if (languageFk != null) {
				notesStmt.setInt(4, languageFk);
			} else {
				notesStmt.setObject(4, null);
			}
			
			if (languageCache != null) {
				notesStmt.setString(5, languageCache);
			} else {
				notesStmt.setObject(5, null);
			}
			
			if (taxonFk != null) {
				notesStmt.setInt(6, taxonFk);
			} else {
				notesStmt.setObject(6, null);
			}
			
			notesStmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Note could not be created: " + note);
			e.printStackTrace();
		}


	}

	/**
	 * Deletes all entries of database tables related to <code>Note</code>.
	 * @param state The PesiExportState
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear NoteSource
		sql = "DELETE FROM NoteSource";
		destination.setQuery(sql);
		destination.update(sql);

		// Clear Note
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
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns the <code>Note_1</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>Note_1</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNote_1(DescriptionElementBase descriptionElement) {
		String result = null;

		if (descriptionElement.isInstanceOf(TextData.class)) {
			TextData textData = CdmBase.deproxy(descriptionElement, TextData.class);
			result = textData.getText(Language.DEFAULT());
		}

		return result;
	}

	/**
	 * Returns the <code>Note_2</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>Note_2</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNote_2(DescriptionElementBase descriptionElement) {
		// TODO: extension
		return null;
	}

	/**
	 * Returns the <code>NoteCategoryFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>NoteCategoryFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getNoteCategoryFk(DescriptionElementBase descriptionElement) {
		Integer result = null;
		
		result = PesiTransformer.textData2NodeCategoryFk(descriptionElement.getFeature());

		return result;
	}
	
	/**
	 * Returns the <code>NoteCategoryCache</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>NoteCategoryCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNoteCategoryCache(DescriptionElementBase descriptionElement) {
		String result = null;

		if (descriptionElement.isInstanceOf(TextData.class)) {
			result = PesiTransformer.textData2NodeCategoryCache(descriptionElement.getFeature());
		}

		return result;
	}

	/**
	 * Returns the <code>LanguageFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LanguageFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getLanguageFk(DescriptionElementBase descriptionElement) {
		Language language = null;

		Map<Language, LanguageString> multilanguageText = null;
		if (descriptionElement.isInstanceOf(CommonTaxonName.class)) {
			CommonTaxonName commonTaxonName = CdmBase.deproxy(descriptionElement, CommonTaxonName.class);
			language = commonTaxonName.getLanguage();
		} else if (descriptionElement.isInstanceOf(TextData.class)) {
			TextData textData = CdmBase.deproxy(descriptionElement, TextData.class);
			multilanguageText = textData.getMultilanguageText();
		} else if (descriptionElement.isInstanceOf(IndividualsAssociation.class)) {
			IndividualsAssociation individualsAssociation = CdmBase.deproxy(descriptionElement, IndividualsAssociation.class);
			multilanguageText = individualsAssociation.getDescription();
		} else if (descriptionElement.isInstanceOf(TaxonInteraction.class)) {
			TaxonInteraction taxonInteraction = CdmBase.deproxy(descriptionElement, TaxonInteraction.class);
			multilanguageText = taxonInteraction.getDescriptions();
		} else {
			logger.warn("Given descriptionElement is not of appropriate instance. Hence LanguageCache could not be determined: " + descriptionElement.getUuid());
		}
		
		if (multilanguageText != null) {
			Set<Language> languages = multilanguageText.keySet();

			// TODO: Think of something more sophisticated than this
			if (languages.size() > 0) {
				language = languages.iterator().next();
			}
		}

		return PesiTransformer.language2LanguageId(language);
	}

	/**
	 * Returns the <code>LanguageCache</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LanguageCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLanguageCache(DescriptionElementBase descriptionElement) {
		Language language = null;

		Map<Language, LanguageString> multilanguageText = null;
		if (descriptionElement.isInstanceOf(CommonTaxonName.class)) {
			CommonTaxonName commonTaxonName = CdmBase.deproxy(descriptionElement, CommonTaxonName.class);
			language = commonTaxonName.getLanguage();
		} else if (descriptionElement.isInstanceOf(TextData.class)) {
			TextData textData = CdmBase.deproxy(descriptionElement, TextData.class);
			multilanguageText = textData.getMultilanguageText();
		} else if (descriptionElement.isInstanceOf(IndividualsAssociation.class)) {
			IndividualsAssociation individualsAssociation = CdmBase.deproxy(descriptionElement, IndividualsAssociation.class);
			multilanguageText = individualsAssociation.getDescription();
		} else if (descriptionElement.isInstanceOf(TaxonInteraction.class)) {
			TaxonInteraction taxonInteraction = CdmBase.deproxy(descriptionElement, TaxonInteraction.class);
			multilanguageText = taxonInteraction.getDescriptions();
		} else {
			logger.warn("Given descriptionElement is not of appropriate instance. Hence LanguageCache could not be determined: " + descriptionElement.getUuid());
		}
		
		if (multilanguageText != null) {
			Set<Language> languages = multilanguageText.keySet();

			// TODO: Think of something more sophisticated than this
			if (languages.size() > 0) {
				language = languages.iterator().next();
			}
		}

		return PesiTransformer.language2LanguageCache(language);
	}

	/**
	 * Returns the <code>Region</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>Region</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRegion(DescriptionElementBase descriptionElement) {
		String result = null;
		DescriptionBase inDescription = descriptionElement.getInDescription();
		
		// Area information are associated to TaxonDescriptions and Distributions.
		if (descriptionElement.isInstanceOf(Distribution.class)) {
			Distribution distribution = CdmBase.deproxy(descriptionElement, Distribution.class);
			result = PesiTransformer.area2AreaCache(distribution.getArea());
		} else if (inDescription != null && inDescription.isInstanceOf(TaxonDescription.class)) {
			TaxonDescription taxonDescription = CdmBase.deproxy(inDescription, TaxonDescription.class);
			Set<NamedArea> namedAreas = taxonDescription.getGeoScopes();
			if (namedAreas.size() == 1) {
				result = PesiTransformer.area2AreaCache(namedAreas.iterator().next());
			} else if (namedAreas.size() > 1) {
				logger.warn("This TaxonDescription contains more than one NamedArea: " + taxonDescription.getTitleCache());
			}
		}
		return result;
	}

	/**
	 * Returns the <code>TaxonFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>TaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonFk(DescriptionElementBase descriptionElement, DbExportStateBase<?> state) {
		Integer result = null;
		DescriptionBase inDescription = descriptionElement.getInDescription();
		if (inDescription != null && inDescription.isInstanceOf(TaxonDescription.class)) {
			TaxonDescription taxonDescription = CdmBase.deproxy(inDescription, TaxonDescription.class);
			Taxon taxon = taxonDescription.getTaxon();
			result = state.getDbId(taxon.getName());
		}
		return result;
	}
	
	/**
	 * Returns the TaxonFk for a given TaxonName.
	 * @param taxonName
	 * @param state
	 * @return
	 */
	private static Integer getTaxonFk(TaxonNameBase taxonName, DbExportStateBase<?> state) {
		return state.getDbId(taxonName);
	}
	
	/**
	 * Returns the <code>LastAction</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LastAction</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLastAction(DescriptionElementBase descriptionElement) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>LastActionDate</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LastActionDate</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static DateTime getLastActionDate(DescriptionElementBase descriptionElement) {
		DateTime result = null;
		if (descriptionElement != null) {
			DateTime updated = descriptionElement.getUpdated();
			if (updated != null) {
//				logger.error("Note Updated: " + updated);
				Date updatedDate = updated.toDate();
				if (updatedDate != null) {
					result = new DateTime(updated.toDate());  // Unfortunately the time information gets lost here.
				} else {
					result = null;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(IdMapper.NewInstance("NoteId"));
		mapping.addMapper(MethodMapper.NewInstance("Note_1", this));
		mapping.addMapper(MethodMapper.NewInstance("Note_2", this));
		mapping.addMapper(MethodMapper.NewInstance("NoteCategoryFk", this));
		mapping.addMapper(MethodMapper.NewInstance("NoteCategoryCache", this));
		mapping.addMapper(MethodMapper.NewInstance("LanguageFk", this));
		mapping.addMapper(MethodMapper.NewInstance("LanguageCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Region", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk", this.getClass(), "getTaxonFk", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("LastAction", this));
//		mapping.addMapper(DbTimePeriodMapper.NewInstance("updated", "LastActionDate")); // This doesn't work since org.joda.time.DateTime cannot be cast to eu.etaxonomy.cdm.model.common.TimePeriod
		mapping.addMapper(MethodMapper.NewInstance("LastActionDate", this));

		return mapping;
	}

}
