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

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportBase;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportConfigurator;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportMapping;
import eu.etaxonomy.cdm.io.pesi.out.PesiExportState;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The export class for NoteSources.
 * Inserts into DataWarehouse database table <code>NoteSource</code>.
 * @author e.-m.lee
 * @date 03.03.2010
 *
 */
@Component
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
	protected void doInvoke(PesiExportState state) {
		try {
			logger.error("*** Started Making " + pluralString + " ...");
	
			// Get the limit for objects to save within a single transaction.
//			int pageSize = state.getConfig().getLimitSave();
			int pageSize = 1000;

			// pageNumber
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
			logger.info("Started new transaction. Fetching some " + pluralString + " (max: " + pageSize + ") ...");
			while ((list = getDescriptionService().listDescriptionElements(null, null, null, pageSize, pageNumber, null)).size() > 0) {

				logger.info("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (DescriptionElementBase descriptionElement : list) {
					
					if (getNoteCategoryFk(descriptionElement) != null && neededValuesNotNull(descriptionElement, state)) {
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
			
			list = null;
			// Commit transaction
			commitTransaction(txStatus);
			logger.debug("Committed transaction.");
	
			logger.info("*** Finished Making " + pluralString + " ..." + getSuccessString(success));
			
			if (!success){
				state.setUnsuccessfull();
			}
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
			return;
		}
	}

	/**
	 * Checks whether needed values for an entity are NULL.
	 * @return
	 */
	private boolean neededValuesNotNull(DescriptionElementBase descriptionElement, PesiExportState state) {
		boolean result = true;
		if (getSourceFk(descriptionElement, state) == null) {
			logger.error("SourceFk is NULL, but is not allowed to be. Therefore no record was written to export database for this descriptionElement: " + descriptionElement.getUuid());
			result = false;
		}
		return result;
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
		return ! ( state.getConfig().isDoNoteSources() && state.getConfig().isDoNotes() && state.getConfig().getDoReferences().equals(DO_REFERENCES.ALL));
	}

	/**
	 * Returns the <code>NoteCategoryFk</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>NoteCategoryFk</code> attribute.
	 */
	private static Integer getNoteCategoryFk(DescriptionElementBase descriptionElement) {
		Integer result = null;
		result = PesiTransformer.feature2NoteCategoryFk(descriptionElement.getFeature());
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
		Integer result = null;
		result = state.getDbId(descriptionElement);
		return result;
	}
	
	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param description The {@link TaxonDescription TaxonDescription}.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getSourceFk(DescriptionElementBase descriptionElement, PesiExportState state) {
		Integer result = null;
		result = state.getDbId(descriptionElement);
		return result;
	}
	
	/**
	 * Returns the <code>SourceNameCache</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>SourceNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceNameCache(DescriptionElementBase descriptionElement) {
		String result = null;

		DescriptionBase<?> inDescription = descriptionElement.getInDescription();
		if (inDescription != null && inDescription.isInstanceOf(TaxonDescription.class)) {
			TaxonDescription taxonDescription = CdmBase.deproxy(inDescription, TaxonDescription.class);
			Taxon taxon = taxonDescription.getTaxon();
			result = taxon.getSec().getTitleCache();
		}

		return result;
	}
	
	/**
	 * Returns the <code>SourceDetail</code> attribute.
	 * @param descriptionElement The {@link DescriptionElementBase DescriptionElement}.
	 * @return The <code>SourceDetail</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceDetail(DescriptionElementBase descriptionElement) {
		//FIXME this is a replacement for the deprecated descriptionElement.getCitationMicroReference()
		//it needs to be checked what should be done when multiple sources exist
		if (descriptionElement.getSources().size() < 1){
			return null;
		}else{
			if (descriptionElement.getSources().size() > 1){
				logger.warn("Multiple sources exist");
			}
			return descriptionElement.getSources().iterator().next().getCitationMicroReference();
		}
//		return descriptionElement.getCitationMicroReference(); // TODO: What should be used instead?
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);

		mapping.addMapper(MethodMapper.NewInstance("NoteFk", this.getClass(), "getNoteFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceFk", this.getClass(), "getSourceFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceDetail", this));

		return mapping;
	}

}
