// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out.old;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.common.mapping.out.IdMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportBase;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportConfigurator;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportMapping;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportState;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
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
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

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
	protected void doInvoke(PesiExportState state) {
		try {
			logger.info("*** Started Making " + pluralString + " ...");

			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();
			
			// Stores whether this invoke was successful or not.
			boolean success = true;
	
			// PESI: Clear the database table Note.
			doDelete(state);
		
			// Start transaction
			success &= doPhase01(state);

			
			logger.info("PHASE 2...");
			doPhase02(state, limit);


			logger.info("*** Finished Making " + pluralString + " ..." + getSuccessString(success));
			
			if (!success){
				state.setUnsuccessfull();
			}
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
		}
	}

	//PHASE 01: Description Elements
	private boolean doPhase01(PesiExportState state) throws SQLException {
		logger.info("PHASE 1...");
		int count = 0;
		int pastCount = 0;
		 boolean success = true;
		
		 // Calculate the pageNumber
		int pageNumber = 1;
		int pageSize = 1000;


		// Get specific mappings: (CDM) DescriptionElement -> (PESI) Note
		PesiExportMapping mapping = getMapping();

		// Initialize the db mapper
		mapping.initialize(state);

		
		List<DescriptionElementBase> list = null;
		
		TransactionStatus txStatus = startTransaction(true);
		logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + pageSize + ") ...");
		List<String> propPath = Arrays.asList(new String[]{"inDescription.taxon"});
		while ((list = getDescriptionService().listDescriptionElements(null, null, null, pageSize, pageNumber, propPath)).size() > 0) {

			logger.info("Fetched " + list.size() + " " + pluralString + ". Exporting...");
			for (DescriptionElementBase descriptionElement : list) {
				if (getNoteCategoryFk(descriptionElement) != null) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(descriptionElement);
				}
			}

			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
			logger.info("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
			pastCount = count;

			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + pageSize + ") ...");
			
			// Increment pageNumber
			pageNumber++;
		}
		if (list.size() == 0) {
			logger.info("No " + pluralString + " left to fetch.");
		}
		// Commit transaction
		commitTransaction(txStatus);
		logger.info("Committed transaction.");
		return success;
	}

	//PHASE 02: Taxa extensions
	private void doPhase02(PesiExportState state, int limit) {
		TransactionStatus txStatus;
		txStatus = startTransaction(true);
		ExtensionType taxCommentExtensionType = (ExtensionType)getTermService().find(PesiTransformer.taxCommentUuid);
		ExtensionType fauCommentExtensionType = (ExtensionType)getTermService().find(PesiTransformer.fauCommentUuid);
		ExtensionType fauExtraCodesExtensionType = (ExtensionType)getTermService().find(PesiTransformer.fauExtraCodesUuid);
		List<TaxonBase> taxonBaseList = null;
		
		int count = 0;
		int pastCount = 0;
		Connection connection = state.getConfig().getDestination().getConnection();
		logger.info("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
		//logger.warn("TODO handle extensions on taxon level, not name level (");
		while ((taxonBaseList = getTaxonService().list(null, limit, count, null, null)).size() > 0) {

			logger.info("Fetched " + taxonBaseList.size() + " names. Exporting...");
			for (TaxonBase<?> taxon : taxonBaseList) {
				Set<Extension> extensions = taxon.getExtensions();
				for (Extension extension : extensions) {
					if (extension.getType().equals(taxCommentExtensionType)) {
						String taxComment = extension.getValue();
						invokeNotes(taxComment, 
								PesiTransformer.getNoteCategoryFk(PesiTransformer.taxCommentUuid), 
								PesiTransformer.getNoteCategoryCache(PesiTransformer.taxCommentUuid),
								null, null, getTaxonFk(taxon.getName(), state),connection);
					} else if (extension.getType().equals(fauCommentExtensionType)) {
						String fauComment = extension.getValue();
						invokeNotes(fauComment, 
								PesiTransformer.getNoteCategoryFk(PesiTransformer.fauCommentUuid), 
								PesiTransformer.getNoteCategoryCache(PesiTransformer.fauCommentUuid),
								null, null, getTaxonFk(taxon.getName(), state),connection);
					} else if (extension.getType().equals(fauExtraCodesExtensionType)) {
						String fauExtraCodes = extension.getValue();
						invokeNotes(fauExtraCodes, 
								PesiTransformer.getNoteCategoryFk(PesiTransformer.fauExtraCodesUuid), 
								PesiTransformer.getNoteCategoryCache(PesiTransformer.fauExtraCodesUuid),
								null, null, getTaxonFk(taxon.getName(), state),connection);
					}
				}
				
				doCount(count++, modCount, pluralString);
			}

			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
			logger.info("Exported " + (count - pastCount) + " names. Total: " + count);
			pastCount = count;

			// Start transaction
			txStatus = startTransaction(true);
			logger.info("Started new transaction. Fetching some taxa first (max: " + limit + ") ...");
		}
		if (taxonBaseList.size() == 0) {
			logger.info("No taxa left to fetch.");
		}
		// Commit transaction
		commitTransaction(txStatus);
		logger.debug("Committed transaction.");
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
		return ! state.getConfig().isDoNotes();
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
		logger.warn("Not yet implemented");
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
		result = PesiTransformer.feature2NoteCategoryFk(descriptionElement.getFeature());
		return result;
	}
	
	/**
	 * Returns the <code>NoteCategoryCache</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>NoteCategoryCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNoteCategoryCache(DescriptionElementBase descriptionElement, PesiExportState state) {
		return state.getTransformer().getCacheByFeature(descriptionElement.getFeature());
	}

	/**
	 * Returns the <code>LanguageFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LanguageFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getLanguageFk(DescriptionElementBase descriptionElement) {
		Language language = getLanguage(descriptionElement);

		return PesiTransformer.language2LanguageId(language);
	}

	/**
	 * Returns the <code>LanguageCache</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LanguageCache</code> attribute.
	 * @throws UndefinedTransformerMethodException 
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLanguageCache(DescriptionElementBase descriptionElement, PesiExportState state) throws UndefinedTransformerMethodException {
		Language language = getLanguage(descriptionElement);
		return state.getTransformer().getCacheByLanguage(language);
	}

	private static Language getLanguage(DescriptionElementBase descriptionElement) {
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
			logger.debug("Given descriptionElement does not support languages. Hence LanguageCache could not be determined: " + descriptionElement.getUuid());
		}
		
		if (multilanguageText != null) {
			Set<Language> languages = multilanguageText.keySet();

			// TODO: Think of something more sophisticated than this
			if (languages.size() > 0) {
				language = languages.iterator().next();
			}
			if (languages.size() > 1){
				logger.warn("There is more than 1 language for a given description (" + descriptionElement.getClass().getSimpleName() + "):" + descriptionElement.getUuid());
			}
		}
		return language;
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
		DescriptionBase<?> inDescription = descriptionElement.getInDescription();
		
		try {
			// Area information are associated to TaxonDescriptions and Distributions.
			if (descriptionElement.isInstanceOf(Distribution.class)) {
				Distribution distribution = CdmBase.deproxy(descriptionElement, Distribution.class);
				//TODO not working any more after transformer refactoring
				result = new PesiTransformer(null).getCacheByNamedArea(distribution.getArea());
			} else if (inDescription != null && inDescription.isInstanceOf(TaxonDescription.class)) {
				TaxonDescription taxonDescription = CdmBase.deproxy(inDescription, TaxonDescription.class);
				Set<NamedArea> namedAreas = taxonDescription.getGeoScopes();
				if (namedAreas.size() == 1) {
					result = new PesiTransformer(null).getCacheByNamedArea(namedAreas.iterator().next());
				} else if (namedAreas.size() > 1) {
					logger.warn("This TaxonDescription contains more than one NamedArea: " + taxonDescription.getTitleCache());
				}
			}
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UndefinedTransformerMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Returns the <code>TaxonFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>TaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonFk(DescriptionElementBase descriptionElement, DbExportStateBase<?, PesiTransformer> state) {
		Integer result = null;
		DescriptionBase<?> inDescription = descriptionElement.getInDescription();
		if (inDescription != null && inDescription.isInstanceOf(TaxonDescription.class)) {
			TaxonDescription taxonDescription = CdmBase.deproxy(inDescription, TaxonDescription.class);
			Taxon taxon = taxonDescription.getTaxon();
			result = state.getDbId(taxon.getName());
		}
		return result;
	}
	
	/**
	 * Returns the TaxonFk for a given TaxonName.
	 * @param taxonName The {@link TaxonNameBase TaxonName}.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return
	 */
	private static Integer getTaxonFk(TaxonNameBase<?,?> taxonName, DbExportStateBase<?, PesiTransformer> state) {
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
		mapping.addMapper(MethodMapper.NewInstance("LastActionDate", this));

		return mapping;
	}

}
