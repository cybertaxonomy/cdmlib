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
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiAdditionalTaxonSourceExport extends
		PesiExportBase<DescriptionBase> {
	private static final Logger logger = Logger.getLogger(PesiAdditionalTaxonSourceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = DescriptionBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "AdditionalTaxonSource";
	private static final String pluralString = "AdditionalTaxonSources";

	public PesiAdditionalTaxonSourceExport() {
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
			logger.info("Start: Make " + pluralString + " ...");

			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();

			// Stores whether this invoke was successful or not.
			boolean success = true;
	
			// PESI: Clear the database table Note.
			doDelete(state);
	
			// CDM: Get the number of all available description elements.
			int maxCount = getDescriptionService().count(null);
			logger.error("Total amount of " + maxCount + " " + pluralString + " will be exported.");

			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Note
			PesiExportMapping mapping = getMapping();
	
			// Initialize the db mapper
			mapping.initialize(state);
	
			// PESI: Create the Notes
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<DescriptionBase> list = null;
			while (count < maxCount) {
				// Start transaction
				txStatus = startTransaction(true);
				logger.error("Started new transaction. Writing " + pluralString + "...");

				// CDM: Get a number of additional taxon sources specified by 'limit'.
				list = getDescriptionService().list(null, limit, count, null, null);

				for (DescriptionBase<?> description : list) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(description);
				}

				// Commit transaction
				commitTransaction(txStatus);
				logger.error("Committed transaction.");
				logger.error("Wrote " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;
			}
	
			logger.error("Finished Making " + pluralString + " ..." + getSuccessString(success));
			
			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes all entries of database tables related to <code>AdditionalTaxonSource</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear AdditionalTaxonSource
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
	 * Returns the <code>TaxonFk</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>TaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonFk(DescriptionBase<?> description) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceFk(DescriptionBase<?> description) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>SourceUseFk</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>SourceUseFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceUseFk(DescriptionBase<?> description) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>SourceUseCache</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>SourceUseCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceUseCache(DescriptionBase<?> description) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>SourceNameCache</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>SourceNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceNameCache(DescriptionBase<?> description) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>SourceDetail</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>SourceDetail</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceDetail(DescriptionBase<?> description) {
		// TODO
		return null;
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceFk", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceUseFk", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceUseCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceDetail", this));
		
		return mapping;
	}

}
