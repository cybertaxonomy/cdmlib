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

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbTimePeriodMapper;
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
public class PesiNoteExport extends PesiExportBase<DescriptionBase> {
	private static final Logger logger = Logger.getLogger(PesiTaxonExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = DescriptionBase.class;

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
			logger.info("Start: Make " + pluralString + " ...");
	
			// Stores whether this invoke was successful or not.
			boolean success = true ;
	
			// PESI: Clear the database table Note.
			doDelete(state);
	
			// Start transaction
			TransactionStatus txStatus = startTransaction(true);
	
			// CDM: Get all DescriptionElements
			List<DescriptionBase> list = getDescriptionService().list(null, 100000000, 0, null, null);
	
			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Note
			PesiExportMapping mapping = getMapping();
	
			// Initialize the db mapper
			mapping.initialize(state);
	
			// PESI: Create the Notes
			int count = 0;
			for (DescriptionBase<?> description : list) {
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(description);
			}
	
			// Commit transaction
			commitTransaction(txStatus);
			logger.info("End: Make " + pluralString + " ..." + getSuccessString(success));
	
			return success;
		} catch(SQLException e) {
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
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>Note_1</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNote_1(DescriptionBase<?> description) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>Note_2</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>Note_2</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNote_2(DescriptionBase<?> description) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>NoteCategoryFk</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>NoteCategoryFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNoteCategoryFk(DescriptionBase<?> description) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>NoteCategoryCache</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>NoteCategoryCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNoteCategoryCache(DescriptionBase<?> description) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>LanguageFk</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>LanguageFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLanguageFk(DescriptionBase<?> description) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>LanguageCache</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>LanguageCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLanguageCache(DescriptionBase<?> description) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>Region</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>Region</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRegion(DescriptionBase<?> description) {
		// TODO
		return null;
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
	 * Returns the <code>LastAction</code> attribute.
	 * @param description The {@link DescriptionBase Description}.
	 * @return The <code>LastAction</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLastAction(DescriptionBase<?> description) {
		// TODO
		return null;
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
//		mapping.addMapper(IdMapper.NewInstance("NoteId"));
		mapping.addMapper(MethodMapper.NewInstance("Note_1", this));
		mapping.addMapper(MethodMapper.NewInstance("Note_2", this));
		mapping.addMapper(MethodMapper.NewInstance("NoteCategoryFk", this));
		mapping.addMapper(MethodMapper.NewInstance("NoteCategoryCache", this));
		mapping.addMapper(MethodMapper.NewInstance("LanguageFk", this));
		mapping.addMapper(MethodMapper.NewInstance("LanguageCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Region", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk", this));
		mapping.addMapper(MethodMapper.NewInstance("LastAction", this));
		mapping.addMapper(DbTimePeriodMapper.NewInstance("updated", "LastActionDate"));

		return mapping;
	}

}
