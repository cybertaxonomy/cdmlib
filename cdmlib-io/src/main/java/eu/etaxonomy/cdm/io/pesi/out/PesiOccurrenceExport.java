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
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

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
						setTaxon(CdmBase.deproxy(taxonBase, Taxon.class));

						// Determine the TaxonDescriptions
						Set<TaxonDescription> taxonDescriptions = getTaxon().getDescriptions();

						// Determine the DescriptionElements (Citations) for the current Taxon
						for (TaxonDescription taxonDescription : taxonDescriptions) {
							Set<DescriptionElementBase> descriptionElements = taxonDescription.getElements();
							for (DescriptionElementBase descriptionElement : descriptionElements) {
								
								if (descriptionElement.isInstanceOf(Distribution.class)) {
//									logger.error("Distribution instance found.");
									Distribution distribution = CdmBase.deproxy(descriptionElement, Distribution.class);
									setNamedArea(distribution.getArea());
									setDistribution(distribution);
									Set<DescriptionElementSource> elementSources = distribution.getSources();
									
									// Differentiate between descriptionElements with and without sources.
									if (! hasCitations(elementSources)) {
//										logger.error("Distribution has no sources. Exporting ...");
										doCount(count++, modCount, pluralString);
										success &= mapping.invoke(distribution);
									} else {
//										logger.error("Distribution has " + elementSources.size() + " sources. Exporting ...");
										for (DescriptionElementSource elementSource : elementSources) {
											ReferenceBase reference = elementSource.getCitation();
	
											// Citations can be empty (null): Is it wrong data or just a normal case?
											if (reference != null) {
//												logger.error("Exporting Reference " + reference.getTitleCache() + " ...");
												doCount(count++, modCount, pluralString);
												success &= mapping.invoke(reference);
											} else {
												// This should never be the case. Testing purpose only.
//												logger.error("Citation of ElementSource is NULL. Nothing to export for ElementSource: " + elementSource.getUuid());
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
	 * Returns whether the given Set of DescriptionElementSources have a citation or not.
	 * @param elementSources
	 * @return
	 */
	public static boolean hasCitations(Set<DescriptionElementSource> elementSources) {
		boolean notFound = true;
		if (elementSources.size() == 0) {
			return false;
		} else {
			for (DescriptionElementSource elementSource : elementSources) {
				notFound &= elementSource.getCitation() == null;
			}
			}
		if (notFound) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @return the taxon
	 */
	public static Taxon getTaxon() {
		return taxon;
	}

	/**
	 * @param taxon the taxon to set
	 */
	public static void setTaxon(Taxon taxon) {
		PesiOccurrenceExport.taxon = taxon;
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
				logger.error("SQLException during update into " + dbTableName + ". Entity: " + entity.getUuid());
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
	 * @param entity
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return The <code>TaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonFk(AnnotatableEntity entity, PesiExportState state) {
		// AnnotatableEntity parameter isn't needed, but the DbSingleAttributeExportMapperBase throws a type mismatch exception otherwise
		// since it awaits two parameters if one of them is of instance DbExportStateBase.
		Integer result = null;
		if (state != null && taxon != null) {
			result = state.getDbId(taxon);
		}
		return result;
	}

	/**
	 * Returns the <code>TaxonFullNameCache</code> attribute.
	 * @param entity
	 * @return The <code>TaxonFullNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonFullNameCache(AnnotatableEntity entity) {
		String result = null;
		if (getTaxon() != null) {
			TaxonNameBase taxonName = getTaxon().getName();
			if (taxonName != null) {
				result = taxonName.getTitleCache();
			}
		}
		return result;
	}

	/**
	 * Returns the <code>AreaFk</code> attribute.
	 * @param entity
	 * @return The <code>AreaFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getAreaFk(AnnotatableEntity entity) {
		Integer result = null;
		if (getNamedArea() != null) {
			result = PesiTransformer.area2AreaId(namedArea);
		} else {
			logger.warn("A NamedArea could not be found for entity: " + entity.getUuid());
		}
		return result;
	}

	/**
	 * Returns the <code>AreaNameCache</code> attribute.
	 * @param entity
	 * @return The <code>AreaNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAreaNameCache(AnnotatableEntity entity) {
		String result = null;
		if (getNamedArea() != null) {
			result = PesiTransformer.area2AreaCache(namedArea);
		} else {
			logger.warn("A NamedArea could not be found for entity: " + entity.getUuid());
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
	 * @param entity
	 * @return The <code>OccurrenceStatusFk</code> attribute.
	 * @throws UnknownCdmTypeException 
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getOccurrenceStatusFk(AnnotatableEntity entity) {
		Integer result = null;
		if (getDistribution() != null) {
			result = PesiTransformer.presenceAbsenceTerm2OccurrenceStatusId(getDistribution().getStatus());
		}
		return result;
	}

	/**
	 * Returns the <code>OccurrenceStatusCache</code> attribute.
	 * @param entity
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
	 * @param entity
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getSourceFk(AnnotatableEntity entity, PesiExportState state) {
		Integer result = null;		
		if (state != null && entity != null && entity.isInstanceOf(ReferenceBase.class)) {
			ReferenceBase reference = CdmBase.deproxy(entity, ReferenceBase.class);
			if (reference != null) {
			result = state.getDbId(reference);
		}
		}
		return result;
	}

	/**
	 * Returns the <code>SourceCache</code> attribute.
	 * @param entity
	 * @return The <code>SourceCache</code> attribute.
	 * @see MethodMapper
	 */
	private static String getSourceCache(AnnotatableEntity entity) {
		String result = null;
		if (entity != null && entity.isInstanceOf(ReferenceBase.class)) {
			ReferenceBase reference = CdmBase.deproxy(entity, ReferenceBase.class);
			if (reference != null) {
			result = reference.getTitle();
		}
		}
		return result;
	}

	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param entity
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
//		mapping.addMapper(CreatedAndNotesMapper.NewInstance());

		return mapping;
	}

}
