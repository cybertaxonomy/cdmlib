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
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 02.03.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiOccurrenceExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiOccurrenceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = AnnotatableEntity.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Occurrence";
	private static final String pluralString = "Occurrences";
	private static final String parentPluralString = "Taxa";
	private static Taxon taxon = null;

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
//			doDelete(state);
	
			// Get specific mappings: (CDM) Occurrence -> (PESI) Occurrence
			PesiExportMapping mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			// PESI: Create the Occurrences
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<TaxonBase> list = null;

			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + parentPluralString + " (max: " + limit + ") for starters ...");
			while ((list = getTaxonService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + parentPluralString + ".");
				for (TaxonBase taxonBase : list) {
					if (taxonBase.isInstanceOf(Taxon.class)) {

						// Set the current Taxon
						taxon = CdmBase.deproxy(taxonBase, Taxon.class);

						// Determine the TaxonDescriptions
						Set<TaxonDescription> taxonDescriptions = taxon.getDescriptions();

						// Determine the DescriptionElements (Citations) for the current Taxon
						for (TaxonDescription taxonDescription : taxonDescriptions) {
							Set<DescriptionElementBase> descriptionElements = taxonDescription.getElements();
							for (DescriptionElementBase descriptionElement : descriptionElements) {
								Set<DescriptionElementSource> elementSources = descriptionElement.getSources();
								
								// Differentiate between descriptionElements with and without sources.
								if (elementSources.size() == 0) {
									success &= mapping.invoke(descriptionElement);
								} else {
									for (DescriptionElementSource elementSource : elementSources) {
										ReferenceBase reference = elementSource.getCitation();
										doCount(count++, modCount, pluralString);
										success &= mapping.invoke(reference);
									}
								}
							}
						}
					}
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
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return The <code>TaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonFk(AnnotatableEntity reference, DbExportStateBase<?> state) {
		// ReferenceBase parameter isn't needed, but the DbSingleAttributeExportMapperBase throws a type mismatch exception otherwise
		// since it awaits two parameters if one of them is of instance DbExportStateBase.
//		logger.error("taxon state id: " + state.getDbId(taxon));
		return state.getDbId(taxon);
	}

	/**
	 * Returns the <code>AreaFk</code> attribute.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The <code>AreaFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getAreaFk(AnnotatableEntity reference) {
//		NamedArea area = distribution.getArea();
//		return PesiTransformer.area2AreaId();
		return 1;
	}

	/**
	 * Returns the <code>AreaNameCache</code> attribute.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The <code>AreaNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAreaNameCache(AnnotatableEntity reference) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>OccurrenceStatusFk</code> attribute.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The <code>OccurrenceStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getOccurrenceStatusFk(AnnotatableEntity reference) {
		// TODO
		return 1;
	}

	/**
	 * Returns the <code>OccurrenceStatusCache</code> attribute.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The <code>OccurrenceStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOccurrenceStatusCache(AnnotatableEntity reference) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getSourceFk(AnnotatableEntity reference, DbExportStateBase<?> state) {
		return state.getDbId(reference);
	}

	/**
	 * Returns the <code>SourceCache</code> attribute.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The <code>SourceCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceCache(AnnotatableEntity reference) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param reference The {@link ReferenceBase ReferenceBase}.
	 * @return The <code>Notes</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNotes(AnnotatableEntity reference) {
		// TODO
		return null;
	}


	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
//		mapping.addMapper(IdMapper.NewInstance("OccurrenceId"));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk", this.getClass(), "getTaxonFk", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("AreaFk", this));
		mapping.addMapper(MethodMapper.NewInstance("AreaNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("OccurrenceStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("OccurrenceStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceFk", this.getClass(), "getSourceFk", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));

		return mapping;
	}

}
