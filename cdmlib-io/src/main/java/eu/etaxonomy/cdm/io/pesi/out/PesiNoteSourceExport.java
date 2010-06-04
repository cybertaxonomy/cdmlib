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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author e.-m.lee
 * @date 03.03.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiNoteSourceExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiNoteSourceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = DescriptionElementBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "NoteSource";
	private static final String pluralString = "NoteSources";

	public PesiNoteSourceExport() {
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

			// Calculate the pageNumber
			int maxCount = 0;
			int pageNumber = 1;

			// Stores whether this invoke was successful or not.
			boolean success = true;

			// PESI: Clear the database table NoteSource.
			doDelete(state);
	
			// Get specific mappings: (CDM) ? -> (PESI) NoteSource
			PesiExportMapping mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			// PESI: Create the NoteSource
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<DescriptionElementBase> list = null;
			
			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + pageSize + ") ...");
			while ((list = getDescriptionService().listDescriptionElements(null, null, null, pageSize, pageNumber, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (DescriptionElementBase descriptionElement : list) {
					
					if (getNoteCategoryFk(descriptionElement) != null) {
						doCount(count++, modCount, pluralString);
						success &= mapping.invoke(descriptionElement);
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
	
			logger.error("*** Finished Making " + pluralString + " ..." + getSuccessString(success));
			
			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes all entries of database tables related to <code>NoteSource</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear NoteSource
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
		// TODO
//		return ! state.getConfig().isDoNoteSource();
		return false;
	}

	/**
	 * Returns the <code>NoteCategoryFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>NoteCategoryFk</code> attribute.
	 */
	private static Integer getNoteCategoryFk(DescriptionElementBase descriptionElement) {
		Integer result = null;
		result = PesiTransformer.textData2NodeCategoryFk(descriptionElement.getFeature());
		return result;
	}

	/**
	 * Returns the <code>NoteFk</code> attribute.
	 * @param description The {@link TaxonDescription TaxonDescription}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>NoteFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getNoteFk(DescriptionElementBase descriptionElement, PesiExportState state) {
		Integer result = state.getDbId(descriptionElement);
		return result;
	}
	
	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param description The {@link TaxonDescription TaxonDescription}.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getSourceFk(DescriptionElementBase descriptionElement, DbExportStateBase<?> state) {
		Integer result = state.getDbId(descriptionElement);
//		DescriptionBase description = descriptionElement.getInDescription();
//		if (description.isInstanceOf(TaxonDescription.class)) {
//			TaxonDescription taxonDescription = CdmBase.deproxy(description, TaxonDescription.class);
//			Taxon taxon = taxonDescription.getTaxon();
//			result = state.getDbId(taxon.getSec());
//		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceNameCache</code> attribute.
	 * @param description The {@link TaxonDescription TaxonDescription}.
	 * @return The <code>SourceNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceNameCache(DescriptionElementBase descriptionElement) {
		String result = null;
		DescriptionBase description = descriptionElement.getInDescription();
		if (description.isInstanceOf(TaxonDescription.class)) {
			TaxonDescription taxonDescription = CdmBase.deproxy(description, TaxonDescription.class);
			Taxon taxon = taxonDescription.getTaxon();
			result = taxon.getSec().getTitleCache();
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceDetail</code> attribute.
	 * @param description The {@link TaxonDescription TaxonDescription}.
	 * @return The <code>SourceDetail</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceDetail(DescriptionElementBase descriptionElement) {
		return descriptionElement.getCitationMicroReference(); // TODO: What else could be used?
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);

		mapping.addMapper(MethodMapper.NewInstance("NoteFk", this.getClass(), "getNoteFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceFk", this.getClass(), "getSourceFk", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceDetail", this));

		return mapping;
	}

}
