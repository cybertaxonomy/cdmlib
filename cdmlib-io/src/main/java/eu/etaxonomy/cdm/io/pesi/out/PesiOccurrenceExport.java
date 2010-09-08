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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * The export class for {@link eu.etaxonomy.cdm.model.description.Distribution Distributions}.<p>
 * Inserts into DataWarehouse database table <code>Occurrence</code>.
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
	private static Map<Integer, Integer> sourceId2OccurenceIdMap = new HashMap<Integer, Integer>();
	private static NamedArea namedArea = null;
	private static Distribution distribution = null;

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
			int taxonCount = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<TaxonBase> list = null;

			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + parentPluralString + " first (max: " + limit + ") ...");
			while ((list = getTaxonService().list(null, limit, taxonCount, null, null)).size() > 0) {

				taxonCount += list.size();
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
								
								if (descriptionElement.isInstanceOf(Distribution.class)) {
									Distribution distribution = CdmBase.deproxy(descriptionElement, Distribution.class);
									setNamedArea(distribution.getArea());
									setDistribution(distribution);

									// Differentiate between descriptionElements with and without sources.
									if (elementSources.size() == 0 && state.getDbId(descriptionElement) != null) {
										if (neededValuesNotNull(descriptionElement, state)) {
											doCount(count++, modCount, pluralString);
											success &= mapping.invoke(descriptionElement);
										}
									} else {
										for (DescriptionElementSource elementSource : elementSources) {
											ReferenceBase reference = elementSource.getCitation();
	
											// Citations can be empty (null): Is it wrong data or just a normal case?
											if (reference != null && state.getDbId(reference) != null) {
												if (neededValuesNotNull(reference, state)) {
													doCount(count++, modCount, pluralString);
													success &= mapping.invoke(reference);
												}
											}
										}
										
									}
									
									setDistribution(null);
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
	 * Checks whether needed values for an entity are NULL.
	 * @return
	 */
	private boolean neededValuesNotNull(AnnotatableEntity entity, PesiExportState state) {
		boolean result = true;
		if (getTaxonFk(entity, state) == null) {
			logger.error("TaxonFk is NULL, but is not allowed to be. Therefore no record was written to export database for this entity: " + entity.getUuid());
			result = false;
		}
		if (getAreaFk(entity) == null) {
			logger.error("AreaFk is NULL, but is not allowed to be. Therefore no record was written to export database for this entity: " + entity.getUuid());
			result = false;
		}
		if (getOccurrenceStatusFk(entity) == null) {
			logger.error("OccurrenceStatusFk is NULL, but is not allowed to be. Therefore no record was written to export database for this entity: " + entity.getUuid());
			result = false;
		}
		return result;
	}

	/**
	 * Creates the entries for the database table 'OccurrenceSource'.
	 * @param entity
	 * @param state
	 * @return
	 */
	protected boolean invokeOccurrenceSource(AnnotatableEntity entity, PesiExportState state) {
		if (entity == null) {
			return true;
		} else {
			// Create OccurrenceSource database table entry
			String lastStoredRecordSql = "Insert Into OccurrenceSource (OccurrenceFk, SourceFk, SourceNameCache, OldTaxonName) " +
					"values(?, ?, ?, ?)";
			Connection con = state.getConfig().getDestination().getConnection();

			try {
				PreparedStatement stmt = con.prepareStatement(lastStoredRecordSql);
				Integer sourceFk = getSourceFk(entity, state);
				stmt.setInt(1, sourceId2OccurenceIdMap.get(sourceFk));
				stmt.setInt(2, sourceFk);
				stmt.setString(3, getSourceCache(entity));
				stmt.setString(4, null); // Which taxon are we talking about?
				stmt.executeUpdate();
				return true;
			} catch (SQLException e) {
				logger.error("SQLException during getOccurrenceId invoke...");
				e.printStackTrace();
				return false;
			}
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
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>TaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getTaxonFk(AnnotatableEntity entity, PesiExportState state) {
		// AnnotatableEntity parameter isn't needed, but the DbSingleAttributeExportMapperBase throws a type mismatch exception otherwise
		// since it awaits two parameters if one of them is of instance DbExportStateBase.
		Integer result = null;
		if (state != null && taxon != null) {
			result = state.getDbId(taxon.getName());
		}
		return result;
	}

	/**
	 * Returns the <code>TaxonFullNameCache</code> attribute.
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @return The <code>TaxonFullNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonFullNameCache(AnnotatableEntity entity) {
		String result = null;
		result = taxon.getName().getTitleCache();
		return result;
	}

	/**
	 * Returns the <code>AreaFk</code> attribute.
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @return The <code>AreaFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getAreaFk(AnnotatableEntity entity) {
		Integer result = null;
		if (getNamedArea() != null) {
			result = PesiTransformer.area2AreaId(namedArea);
		} else {
			logger.warn("This should never happen, but a NamedArea could not be found for entity: " + entity.getUuid());
		}
		return result;
	}

	/**
	 * Returns the <code>AreaNameCache</code> attribute.
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @return The <code>AreaNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAreaNameCache(AnnotatableEntity entity) {
		String result = null;
		if (getNamedArea() != null) {
			result = PesiTransformer.area2AreaCache(namedArea);
		} else {
			logger.warn("This should never happen, but a NamedArea could not be found for entity: " + entity.getUuid());
		}
		return result;
	}

	/**
	 * @return the distribution
	 */
	public static Distribution getDistribution() {
		return distribution;
	}

	/**
	 * @param distribution the distribution to set
	 */
	public static void setDistribution(Distribution distribution) {
		PesiOccurrenceExport.distribution = distribution;
	}

	/**
	 * @return the namedArea
	 */
	public static NamedArea getNamedArea() {
		return namedArea;
	}

	/**
	 * @param namedArea the namedArea to set
	 */
	public static void setNamedArea(NamedArea namedArea) {
		PesiOccurrenceExport.namedArea = namedArea;
	}

	/**
	 * Returns the <code>OccurrenceStatusFk</code> attribute.
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @return The <code>OccurrenceStatusFk</code> attribute.
	 * @throws UnknownCdmTypeException 
	 * @see MethodMapper
	 */
	private static Integer getOccurrenceStatusFk(AnnotatableEntity entity) {
		Integer result = null;
		if (getDistribution() != null) {
			result = PesiTransformer.presenceAbsenceTerm2OccurrenceStatusId(getDistribution().getStatus());
		}
		return result;
	}

	/**
	 * Returns the <code>OccurrenceStatusCache</code> attribute.
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @return The <code>OccurrenceStatusCache</code> attribute.
	 * @throws UnknownCdmTypeException 
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOccurrenceStatusCache(AnnotatableEntity entity) {
		String result = null;
		if (getDistribution() != null) {
			result = PesiTransformer.presenceAbsenceTerm2OccurrenceStatusCache(getDistribution().getStatus());
		}
		return result;
	}

	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getSourceFk(AnnotatableEntity entity, PesiExportState state) {
		Integer result = null;
		if (state != null && entity != null && entity.isInstanceOf(Distribution.class)) {
			Distribution distribution = CdmBase.deproxy(entity, Distribution.class);
			Set<DescriptionElementSource> sources = distribution.getSources();
			if (sources.size() == 1) {
				DescriptionElementSource source = sources.iterator().next();
				result = state.getDbId(source.getCitation());
			} else if (sources.size() > 1) {
				logger.warn("Found Distribution with " + sources.size() + " sources.");
			}
		}
		return result;
	}

	/**
	 * Returns the <code>SourceCache</code> attribute.
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @return The <code>SourceCache</code> attribute.
	 * @see MethodMapper
	 */
	private static String getSourceCache(AnnotatableEntity entity) {
		String result = null;
		ReferenceBase reference;
		if (entity != null && entity.isInstanceOf(Distribution.class)) {
			Distribution distribution = CdmBase.deproxy(entity, Distribution.class);
			Set<DescriptionElementSource> sources = distribution.getSources();
			if (sources.size() == 1) {
				DescriptionElementSource source = sources.iterator().next();
				reference = source.getCitation();
				if (reference != null) {
					result = reference.getTitle();
				}
			} else if (sources.size() > 1) {
				logger.warn("Found Distribution with " + sources.size() + " sources.");
			}
		}
		return result;
	}

	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param entity An {@link AnnotatableEntity AnnotatableEntity}.
	 * @return The <code>Notes</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNotes(AnnotatableEntity entity) {
		// TODO
		return null;
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk", this.getClass(), "getTaxonFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("AreaFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFullNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("AreaNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("OccurrenceStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("OccurrenceStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceFk", this.getClass(), "getSourceFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));

		return mapping;
	}

}
