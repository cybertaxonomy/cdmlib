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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiAdditionalTaxonSourceExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiAdditionalTaxonSourceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = ReferenceBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "AdditionalTaxonSource";
	private static final String pluralString = "DescriptionElements";
	private static final String parentPluralString = "Taxa";
	private static boolean sourceUse_AdditionalSource = false;
	private static boolean sourceUse_NomenclaturalReference = false;
	private static boolean sourceUse_SourceOfSynonymy = false;
	private static TaxonBase currentTaxon = null;
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
				
				logger.error("Looking for " + pluralString + " to export:");
				logger.error("PHASE 1: Check for SourceUse 'NomenclaturalReference'");
				setSourceUse_NomenclaturalReference(true);
				for (TaxonBase taxonBase : list) {
					// Set the current Taxon
					setCurrentTaxon(taxonBase);

					ReferenceBase<?> nomenclaturalReference = (ReferenceBase)taxonBase.getName().getNomenclaturalReference();
					if (nomenclaturalReference != null) {
				doCount(count++, modCount, pluralString);
						success &= mapping.invoke(nomenclaturalReference);
			}
				}
				setSourceUse_NomenclaturalReference(false);
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ".");
	
				logger.error("PHASE 2: Check for SourceUse 'Additional Source'");
				setSourceUse_AdditionalSource(true);
				for (TaxonBase taxonBase : list) {
					// Set the current Taxon
					setCurrentTaxon(taxonBase);

					if (taxonBase.isInstanceOf(Taxon.class)) {
						
						Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);

						// Determine the TaxonDescriptions
						Set<TaxonDescription> taxonDescriptions = taxon.getDescriptions();

						// Determine the DescriptionElements (Citations) for the current Taxon
						for (TaxonDescription taxonDescription : taxonDescriptions) {
							Set<DescriptionElementBase> descriptionElements = taxonDescription.getElements();
							for (DescriptionElementBase descriptionElement : descriptionElements) {
								Set<DescriptionElementSource> elementSources = descriptionElement.getSources();
								
								for (DescriptionElementSource elementSource : elementSources) {
									
									// Set the CitationMicroReference for later use in getSourceDetail()
									setCitationMicroReference(elementSource.getCitationMicroReference());

									// Get the citation
									ReferenceBase reference = elementSource.getCitation();
									
									// Citations can be empty (null): Is it wrong data or just a normal case?
									if (reference != null) {
										doCount(count++, modCount, pluralString);
										success &= mapping.invoke(reference);
									}
								}
							}
						}
					}
				}
				setSourceUse_AdditionalSource(false);
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ".");
				
				logger.error("PHASE 3: Check for SourceUse 'Source of Synonymy'");
				ReferenceBase reference = null;
				setSourceUse_SourceOfSynonymy(true);
				for (TaxonBase taxonBase : list) {
					if (taxonBase.isInstanceOf(Synonym.class)) {
						Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
						Set<SynonymRelationship> synonymRelations = synonym.getSynonymRelations();
						for (SynonymRelationship relation : synonymRelations) {

							// Set the current Taxon
							setCurrentTaxon(relation.getAcceptedTaxon());
							reference = relation.getCitation();

							// Citations can be empty (null): Is it wrong data or just a normal case?
							if (reference != null) {
								doCount(count++, modCount, pluralString);
								success &= mapping.invoke(reference);
							}
						}
					}
				}
				setSourceUse_SourceOfSynonymy(false);
				
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
	 * @return the sourceUse_AdditionalSource
	 */
	public static boolean isSourceUse_AdditionalSource() {
		return sourceUse_AdditionalSource;
	}

	/**
	 * @param sourceUseAdditionalSource the sourceUse_AdditionalSource to set
	 */
	public static void setSourceUse_AdditionalSource(
			boolean sourceUseAdditionalSource) {
		sourceUse_AdditionalSource = sourceUseAdditionalSource;
	}

	/**
	 * @return the sourceUse_NomenclaturalReference
	 */
	public static boolean isSourceUse_NomenclaturalReference() {
		return sourceUse_NomenclaturalReference;
	}

	/**
	 * @param sourceUseNomenclaturalReference the sourceUse_NomenclaturalReference to set
	 */
	public static void setSourceUse_NomenclaturalReference(
			boolean sourceUseNomenclaturalReference) {
		sourceUse_NomenclaturalReference = sourceUseNomenclaturalReference;
	}

	/**
	 * @return the sourceUse_SourceOfSynonymy
	 */
	public static boolean isSourceUse_SourceOfSynonymy() {
		return sourceUse_SourceOfSynonymy;
	}

	/**
	 * @param sourceUseSourceOfSynonymy the sourceUse_SourceOfSynonymy to set
	 */
	public static void setSourceUse_SourceOfSynonymy(
			boolean sourceUseSourceOfSynonymy) {
		sourceUse_SourceOfSynonymy = sourceUseSourceOfSynonymy;
	}

	/**
	 * @return the currentTaxon
	 */
	public static TaxonBase getCurrentTaxon() {
		return currentTaxon;
	}

	/**
	 * @param currentTaxon the currentTaxon to set
	 */
	public static void setCurrentTaxon(TaxonBase currentTaxon) {
		PesiAdditionalTaxonSourceExport.currentTaxon = currentTaxon;
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
	 * @param reference The {@link ReferenceBase reference}.
	 * @return The <code>TaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonFk(ReferenceBase<?> reference, PesiExportState state) {
		// ReferenceBase parameter isn't needed, but the DbSingleAttributeExportMapperBase throws a type mismatch exception otherwise
		// since it awaits two parameters if one of them is of instance DbExportStateBase.
		Integer result = null;
		if (state != null && getCurrentTaxon() != null) {
			result = state.getDbId(getCurrentTaxon());
	}
		return result;
	}
	
	/**
	 * Returns the <code>SourceFk</code> attribute.
	 * @param reference The {@link ReferenceBase reference}.
	 * @return The <code>SourceFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getSourceFk(ReferenceBase<?> reference, PesiExportState state) {
		Integer result = null;
		if (state != null && reference != null) {
			result = state.getDbId(reference);
	}
		return result;

//		Set<TaxonDescription> descs = taxon.getDescriptions();
//		//for each
//		TaxonDescription desc = descs.iterator().next();
//		Set<DescriptionElementBase> elements = desc.getElements();
//		for (DescriptionElementBase element : elements){
//			Set<DescriptionElementSource> sources = element.getSources();
//			for (DescriptionElementSource source : sources){
//				ReferenceBase ref = source.getCitation();
//				result = state.getDbId(ref);
//			}
//		}
//		
//		Integer result = null;
//		Set<DescriptionElementSource> sources = element.getSources();
	}
	
	/**
	 * Returns the <code>SourceUseFk</code> attribute.
	 * @param reference The {@link ReferenceBase reference}.
	 * @return The <code>SourceUseFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getSourceUseFk(ReferenceBase<?> reference) {
		// TODO: CDM sourceUseId and PESI sourceUseId are equal for now.
		Integer result = null;
		if (isSourceUse_AdditionalSource()) {
			result = PesiTransformer.sourceUseIdSourceUseId(3);
		} else if (isSourceUse_SourceOfSynonymy()) {
			result = PesiTransformer.sourceUseIdSourceUseId(4);
		} else if (isSourceUse_NomenclaturalReference()) {
			result = PesiTransformer.sourceUseIdSourceUseId(8);
	}
		return result;
	}
	
	/**
	 * Returns the <code>SourceUseCache</code> attribute.
	 * @param reference The {@link ReferenceBase reference}.
	 * @return The <code>SourceUseCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceUseCache(ReferenceBase<?> reference) {
		// TODO: CDM sourceUseId and PESI sourceUseId are equal for now.
		String result = null;
		if (isSourceUse_AdditionalSource()) {
			result = PesiTransformer.sourceUseId2SourceUseCache(3);
		} else if (isSourceUse_SourceOfSynonymy()) {
			result = PesiTransformer.sourceUseId2SourceUseCache(4);
		} else if (isSourceUse_NomenclaturalReference()) {
			result = PesiTransformer.sourceUseId2SourceUseCache(8);
	}
		return result;
	}
	
	/**
	 * Returns the <code>SourceNameCache</code> attribute.
	 * @param reference The {@link ReferenceBase reference}.
	 * @return The <code>SourceNameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceNameCache(ReferenceBase<?> reference) {
		String result = null;
		if (reference != null) {
			result = reference.getTitle();
	}
		return result;
	}
	
	/**
	 * Returns the <code>SourceDetail</code> attribute.
	 * @param reference The {@link ReferenceBase reference}.
	 * @return The <code>SourceDetail</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceDetail(ReferenceBase<?> reference) {
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
