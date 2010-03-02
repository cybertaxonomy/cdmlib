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

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author e.-m.lee
 * @date 02.03.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiOccurrenceExport extends PesiExportBase<DescriptionBase> {
	private static final Logger logger = Logger.getLogger(PesiOccurrenceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = SpecimenOrObservationBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Occurrence";
	private static final String pluralString = "Occurrences";

	public PesiOccurrenceExport() {
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

			// Stores whether this invoke was successful or not.
			boolean success = true;

			// PESI: Clear the database table Occurrence.
			doDelete(state);
	
			// Get specific mappings: (CDM) Occurrence -> (PESI) Occurrence
			PesiExportMapping mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			// PESI: Create the Occurrences
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<SpecimenOrObservationBase> list = null;

			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getOccurrenceService().list(null, limit, count, null, null)).size() > 0) {
				
				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
				for (SpecimenOrObservationBase<?> relation : list) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(relation);
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
	 * Deletes all entries of database tables related to <code>Occurrence</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear Occurrence
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
		return ! state.getConfig().isDoOccurrence();
	}


	/**
	 * Returns the <code>TaxonFk</code> attribute.
	 * @param specimenOrObservation The {@link SpecimenOrObservationBase SpecimenOrObservation}.
	 * @return The <code>TaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonFk(SpecimenOrObservationBase<?> specimenOrObservation) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>AreaFk</code> attribute.
	 * @param specimenOrObservation The {@link SpecimenOrObservationBase SpecimenOrObservation}.
	 * @return The <code>AreaFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAreaFk(SpecimenOrObservationBase<?> specimenOrObservation) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>AreaNameCache</code> attribute.
	 * @param specimenOrObservation The {@link SpecimenOrObservationBase SpecimenOrObservation}.
	 * @return The <code>AreaNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAreaNameCache(SpecimenOrObservationBase<?> specimenOrObservation) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>OccurrenceStatusFk</code> attribute.
	 * @param specimenOrObservation The {@link SpecimenOrObservationBase SpecimenOrObservation}.
	 * @return The <code>OccurrenceStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOccurrenceStatusFk(SpecimenOrObservationBase<?> specimenOrObservation) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>OccurrenceStatusCache</code> attribute.
	 * @param specimenOrObservation The {@link SpecimenOrObservationBase SpecimenOrObservation}.
	 * @return The <code>OccurrenceStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOccurrenceStatusCache(SpecimenOrObservationBase<?> specimenOrObservation) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param specimenOrObservation The {@link SpecimenOrObservationBase SpecimenOrObservation}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceFk(SpecimenOrObservationBase<?> specimenOrObservation) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>SourceCache</code> attribute.
	 * @param specimenOrObservation The {@link SpecimenOrObservationBase SpecimenOrObservation}.
	 * @return The <code>SourceCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceCache(SpecimenOrObservationBase<?> specimenOrObservation) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param specimenOrObservation The {@link SpecimenOrObservationBase SpecimenOrObservation}.
	 * @return The <code>Notes</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNotes(SpecimenOrObservationBase<?> specimenOrObservation) {
		// TODO
		return null;
	}


	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(IdMapper.NewInstance("OccurrenceId"));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk", this));
		mapping.addMapper(MethodMapper.NewInstance("AreaFk", this));
		mapping.addMapper(MethodMapper.NewInstance("AreaNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("OccurrenceStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("OccurrenceStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceFk", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));

		return mapping;
	}

}
