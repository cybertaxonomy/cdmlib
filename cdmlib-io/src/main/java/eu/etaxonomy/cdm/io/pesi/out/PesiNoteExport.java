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

import java.sql.SQLException;
import java.util.List;
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
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
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
//			int pageSize = state.getConfig().getLimitSave();
			int pageSize = 1000;

			// Set the first pageNumber
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
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + pageSize + ") ...");
			while ((list = getDescriptionService().listDescriptionElements(null, null, null, pageSize, pageNumber, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (DescriptionElementBase description : list) {
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(description);
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

			logger.error("*** Finished Making " + pluralString + " ..." + getSuccessString(success));
	
			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
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
		} else {
//			logger.warn("DescriptionElement is of instance: " + descriptionElement.getClass());
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
		return null;
	}

	/**
	 * Returns the <code>NoteCategoryFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>NoteCategoryFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getNoteCategoryFk(DescriptionElementBase descriptionElement) {
		Integer result = null;
		if (descriptionElement.isInstanceOf(TextData.class)) {
			result = PesiTransformer.textData2NodeCategoryFk(descriptionElement.getFeature());
	}
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
		return PesiTransformer.language2LanguageId(Language.DEFAULT());
	}

	/**
	 * Returns the <code>LanguageCache</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>LanguageCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLanguageCache(DescriptionElementBase descriptionElement) {
		return PesiTransformer.language2LanguageCache(Language.DEFAULT());
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
		DescriptionBase description = descriptionElement.getInDescription();

		if (description != null) {
		// Area information are associated to TaxonDescriptions and Distributions.
			if (description.isInstanceOf(Distribution.class)) {
			Distribution distribution = CdmBase.deproxy(descriptionElement, Distribution.class);
				if (distribution != null) {
			result = PesiTransformer.area2AreaCache(distribution.getArea());
				} else {
					logger.warn("Distribution has no area information: " + distribution.getUuid());
				}
		} else if (description.isInstanceOf(TaxonDescription.class)) {
			TaxonDescription taxonDescription = CdmBase.deproxy(description, TaxonDescription.class);
				if (taxonDescription != null) {
			Set<NamedArea> namedAreas = taxonDescription.getGeoScopes();
					if (namedAreas != null && namedAreas.size() == 1) {
				result = PesiTransformer.area2AreaCache(namedAreas.iterator().next());
					} else if (namedAreas != null && namedAreas.size() > 1) {
						logger.warn("This TaxonDescription contains more than one NamedArea: " + taxonDescription.getUuid() + " (" + taxonDescription.getTitleCache() + ")");
	}
				} else {
					logger.warn("TaxonDescription is NULL for the following Description: " + descriptionElement.getUuid());
		}
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
		if (descriptionElement != null) {
		DescriptionBase description = descriptionElement.getInDescription();
			if (description != null) {
		if (description.isInstanceOf(TaxonDescription.class)) {
			TaxonDescription taxonDescription = CdmBase.deproxy(description, TaxonDescription.class);
					if (taxonDescription != null) {
			Taxon taxon = taxonDescription.getTaxon();
						if (taxon != null) {
			result = state.getDbId(taxon);
						} else {
							logger.warn("TaxonDescription has no Taxon it belongs to: " + taxonDescription.getUuid() + " (" + taxonDescription.getTitleCache() + ")");
	}
					} else {
						logger.warn("TaxonDescription is NULL for the following ");
					}
				}
			} else {
				logger.warn("InDescription of the following descriptionElement is NULL: " + descriptionElement.getUuid());
			}
		}
		return result;
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
	private static String getLastActionDate(DescriptionElementBase descriptionElement) {
		String result = null;
//		if (descriptionElement != null) {
//			DateTime updated = descriptionElement.getUpdated(); // maybe not the right attribute!
//			if (updated != null) {
//				result = new DateTime(updated.toDate()); // Unfortunately the time information gets lost here.
//			}
//		}
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
