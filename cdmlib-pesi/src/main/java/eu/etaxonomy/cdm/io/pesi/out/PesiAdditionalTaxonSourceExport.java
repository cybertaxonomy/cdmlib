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
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * The export class for additional information linked to {@link eu.etaxonomy.cdm.model.taxon.Taxon Taxa} and {@link eu.etaxonomy.cdm.model.reference.Reference References}.<p>
 * Inserts into DataWarehouse database table <code>AdditionalTaxonSource</code>.
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiAdditionalTaxonSourceExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiAdditionalTaxonSourceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = Reference.class;

	private static int modCount = 1000;
	private static final String dbTableName = "AdditionalTaxonSource";
	private static final String pluralString = "DescriptionElements";
	private static final String parentPluralString = "Taxa";
	private static boolean sourceUse_AdditionalSource = false;
	private static boolean sourceUse_NomenclaturalReference = false;
	private static boolean sourceUse_SourceOfSynonymy = false;
	private static TaxonNameBase currentTaxonName = null;
	private static String citationMicroReference = null;
	
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
			logger.error("*** Started Making " + pluralString + " ...");

			// Get the limit for objects to save within a single transaction.
//			int limit = state.getConfig().getLimitSave();
			int limit = 1000;

			// Stores whether this invoke was successful or not.
			boolean success = true;
	
			// PESI: Clear the database table Note.
			doDelete(state);
	
			// CDM: Get the number of all available description elements.
//			int maxCount = getDescriptionService().count(null);
//			logger.error("Total amount of " + maxCount + " " + pluralString + " will be exported.");

			// Get specific mappings: (CDM) DescriptionElement -> (PESI) Note
			PesiExportMapping mapping = getMapping();
	
			// Initialize the db mapper
			mapping.initialize(state);
	
			// PESI: Create the Notes
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
				
				logger.error("PHASE 2: Check for SourceUse 'Additional Source'");
				sourceUse_AdditionalSource = true;
				for (TaxonBase taxonBase : list) {
					
					// Set the current Taxon
					currentTaxonName = taxonBase.getName();

					if (taxonBase.isInstanceOf(Taxon.class)) {
						
						Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);

						// Determine the TaxonDescriptions
						Set<TaxonDescription> taxonDescriptions = taxon.getDescriptions();

						// Determine the DescriptionElements (Citations) for the current Taxon
						for (TaxonDescription taxonDescription : taxonDescriptions) {
							Set<DescriptionElementBase> descriptionElements = taxonDescription.getElements();
							
							for (DescriptionElementBase descriptionElement : descriptionElements) {
								// According to FaEu Import those DescriptionElementBase elements are of instance TextData
								// There are no other indicators
								if (descriptionElement.isInstanceOf(TextData.class)) {
									Set<DescriptionElementSource> elementSources = descriptionElement.getSources();
									
									for (DescriptionElementSource elementSource : elementSources) {
										
										// Set the CitationMicroReference so it is accessible later in getSourceDetail()
										setCitationMicroReference(elementSource.getCitationMicroReference());
	
										// Get the citation
										Reference reference = elementSource.getCitation();
										
										// Check whether it was a synonym originally
										TaxonNameBase nameUsedInSource = elementSource.getNameUsedInSource();
										if (nameUsedInSource != null) {
											// It was a synonym originally: Set currentTaxonName to synonym's taxonName
											currentTaxonName = nameUsedInSource;
										}
										
										// Citations can be empty (null): Is it wrong data or just a normal case?
										if (reference != null) {
											if (neededValuesNotNull(reference, state)) {
												doCount(count++, modCount, pluralString);
												success &= mapping.invoke(reference);
											}
										}
									}
								}
							}
						}
					}
					
				}
				sourceUse_AdditionalSource = false;
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ".");
				
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
	 * Checks whether needed values for an entity are NULL.
	 * @return
	 */
	private boolean neededValuesNotNull(Reference<?> reference, PesiExportState state) {
		boolean result = true;
		if (getSourceFk(reference, state) == null) {
			logger.error("SourceFk is NULL, but is not allowed to be. Therefore no record was written to export database for this reference: " + reference.getUuid());
			result = false;
		}
		if (getSourceUseFk(reference) == null) {
			logger.error("SourceUseFk is NULL, but is not allowed to be. Therefore no record was written to export database for this reference: " + reference.getUuid());
			result = false;
		}
		return result;
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
		// TODO
//		return state.getConfig().isDoAdditionalTaxonSource()
		return false;
	}

	/**
	 * Returns the <code>TaxonFk</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonFk(Reference<?> reference, PesiExportState state) {
		// Reference parameter isn't needed, but the DbSingleAttributeExportMapperBase throws a type mismatch exception otherwise
		// since it awaits two parameters if one of them is of instance DbExportStateBase.
		Integer result = null;
		if (state != null && currentTaxonName != null) {
			result = state.getDbId(currentTaxonName);
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getSourceFk(Reference<?> reference, PesiExportState state) {
		Integer result = null;
		if (state != null && reference != null) {
			result = state.getDbId(reference);
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceUseFk</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>SourceUseFk</code> attribute.
	 * @see MethodMapper
	 */
	private static Integer getSourceUseFk(Reference<?> reference) {
		// TODO
		Integer result = null;
		if (sourceUse_AdditionalSource) {
			result = PesiTransformer.sourceUseIdSourceUseId(3);
		} else if (sourceUse_SourceOfSynonymy) {
			result = PesiTransformer.sourceUseIdSourceUseId(4);
		} else if (sourceUse_NomenclaturalReference) {
			result = PesiTransformer.sourceUseIdSourceUseId(8);
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceUseCache</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>SourceUseCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceUseCache(Reference<?> reference) {
		// TODO
		String result = null;
		if (sourceUse_AdditionalSource) {
			result = PesiTransformer.sourceUseId2SourceUseCache(3);
		} else if (sourceUse_SourceOfSynonymy) {
			result = PesiTransformer.sourceUseId2SourceUseCache(4);
		} else if (sourceUse_NomenclaturalReference) {
			result = PesiTransformer.sourceUseId2SourceUseCache(8);
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceNameCache</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>SourceNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceNameCache(Reference<?> reference) {
		String result = null;
		if (reference != null) {
			result = reference.getTitle();
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceDetail</code> attribute.
	 * @param reference The {@link Reference Reference}.
	 * @return The <code>SourceDetail</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceDetail(Reference<?> reference) {
		return PesiAdditionalTaxonSourceExport.citationMicroReference;
	}

	/**
	 * @param citationMicroReference the citationMicroReference to set
	 */
	public static void setCitationMicroReference(String citationMicroReference) {
		PesiAdditionalTaxonSourceExport.citationMicroReference = citationMicroReference;
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk", this.getClass(), "getTaxonFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceFk", this.getClass(), "getSourceFk", standardMethodParameter, PesiExportState.class));
		mapping.addMapper(MethodMapper.NewInstance("SourceUseFk", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceUseCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceDetail", this));
		
		return mapping;
	}

}
