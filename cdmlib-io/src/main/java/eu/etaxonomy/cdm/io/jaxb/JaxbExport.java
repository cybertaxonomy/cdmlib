/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.jaxb;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 25.09.2008
 * @version 1.0
 */
@Component
public class JaxbExport extends CdmExportBase<JaxbExportConfigurator, JaxbExportState> implements ICdmExport<JaxbExportConfigurator, JaxbExportState> {

	private static final Logger logger = Logger.getLogger(JaxbExport.class);
	private CdmDocumentBuilder cdmDocumentBuilder = null;

//	/**
//	 * 
//	 */
//	public JaxbExport() {
//		super();
//		this.ioName = this.getClass().getSimpleName();
//	}

	/** Retrieves data from a CDM DB and serializes them CDM to XML.
	 * Starts with root taxa and traverses the taxonomic tree to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the taxonomic tree are not found.
	 * 
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
//	@Override
//	protected boolean doInvoke(IExportConfigurator config,
//			Map<String, MapWrapper<? extends CdmBase>> stores) {
	@Override
	protected boolean doInvoke(JaxbExportState state) {
		
		JaxbExportConfigurator jaxbExpConfig = (JaxbExportConfigurator)state.getConfig();
		String dbname = jaxbExpConfig.getSource().getName();
    	String fileName = jaxbExpConfig.getDestination();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		logger.debug("DbSchemaValidation = " + jaxbExpConfig.getDbSchemaValidation());

		TransactionStatus txStatus = startTransaction(true);
		DataSet dataSet = new DataSet();

		// get data from DB

		try {
			logger.info("Retrieving data from DB");

			retrieveData(jaxbExpConfig, dataSet);

		} catch (Exception e) {
			logger.error("Error retrieving data");
			e.printStackTrace();
		}

		logger.info("All data retrieved");

		try {
			cdmDocumentBuilder = new CdmDocumentBuilder();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"), true);
			cdmDocumentBuilder.marshal(dataSet, writer);

			// TODO: Split into one file per data set member to see whether performance improves?

			logger.info("XML file written");
			logger.info("Filename is: " + fileName);

		} catch (Exception e) {
			logger.error("Marshalling error");
			e.printStackTrace();
		} 
		commitTransaction(txStatus);
		
		return true;

	}


	private void retrieveData (IExportConfigurator config, DataSet dataSet) {

		JaxbExportConfigurator jaxbExpConfig = (JaxbExportConfigurator)config;
		final int MAX_ROWS = 50000;
		int numberOfRows = jaxbExpConfig.getMaxRows();

		int agentRows = numberOfRows;
		int definedTermBaseRows = numberOfRows;
		int referenceBaseRows = numberOfRows;
		int taxonNameBaseRows = numberOfRows;
		int taxonBaseRows = numberOfRows;
		int relationshipRows = numberOfRows;
		int occurrencesRows = numberOfRows;
		int mediaRows = numberOfRows;
		int featureDataRows = numberOfRows;
		int languageDataRows = numberOfRows;
		int termVocabularyRows = numberOfRows;
		int homotypicalGroupRows = numberOfRows;

		if (jaxbExpConfig.isDoTermVocabularies() == true) {
			if (termVocabularyRows == 0) { termVocabularyRows = MAX_ROWS; }
			logger.info("# TermVocabulary");
			dataSet.setTermVocabularies(getTermService().getAllTermVocabularies(MAX_ROWS, 0));;
		}

//		if (jaxbExpConfig.isDoLanguageData() == true) {
//			if (languageDataRows == 0) { languageDataRows = MAX_ROWS; }
//			logger.info("# Representation, Language String");
//			dataSet.setLanguageData(getTermService().getAllRepresentations(MAX_ROWS, 0));
//			dataSet.addLanguageData(getTermService().getAllLanguageStrings(MAX_ROWS, 0));
//		}

		if (jaxbExpConfig.isDoTerms() == true) {
			if (definedTermBaseRows == 0) { definedTermBaseRows = getTermService().count(DefinedTermBase.class); }
			logger.info("# DefinedTermBase: " + definedTermBaseRows);
			dataSet.setTerms(getTermService().getAllDefinedTerms(definedTermBaseRows, 0));
		}

		if (jaxbExpConfig.isDoAuthors() == true) {
			if (agentRows == 0) { agentRows = getAgentService().count(AgentBase.class); }
			logger.info("# Agents: " + agentRows);
			//logger.info("    # Team: " + appCtr.getAgentService().count(Team.class));
			dataSet.setAgents(getAgentService().getAllAgents(agentRows, 0));
		}

		if (jaxbExpConfig.getDoReferences() != IImportConfigurator.DO_REFERENCES.NONE) {
			if (referenceBaseRows == 0) { referenceBaseRows = getReferenceService().count(ReferenceBase.class); }
			logger.info("# ReferenceBase: " + referenceBaseRows);
			dataSet.setReferences(getReferenceService().getAllReferences(referenceBaseRows, 0));
		}

		if (jaxbExpConfig.isDoTaxonNames() == true) {
			if (taxonNameBaseRows == 0) { taxonNameBaseRows = getNameService().count(TaxonNameBase.class); }
			logger.info("# TaxonNameBase: " + taxonNameBaseRows);
			//logger.info("    # Taxon: " + getNameService().count(BotanicalName.class));
			dataSet.setTaxonomicNames(getNameService().getAllNames(taxonNameBaseRows, 0));
		}

		if (jaxbExpConfig.isDoHomotypicalGroups() == true) {
			if (homotypicalGroupRows == 0) { homotypicalGroupRows = MAX_ROWS; }
			logger.info("# Homotypical Groups");
			dataSet.setHomotypicalGroups(getNameService().getAllHomotypicalGroups(homotypicalGroupRows, 0));
		}

		if (jaxbExpConfig.isDoTaxa() == true) {
			if (taxonBaseRows == 0) { taxonBaseRows = getTaxonService().count(TaxonBase.class); }
			logger.info("# TaxonBase: " + taxonBaseRows);
//			dataSet.setTaxa(new ArrayList<Taxon>());
//			dataSet.setSynonyms(new ArrayList<Synonym>());
			List<TaxonBase> tb = getTaxonService().getAllTaxonBases(taxonBaseRows, 0);
			for (TaxonBase taxonBase : tb) {
				dataSet.addTaxonBase(taxonBase);
			}
		}

		// TODO: 
		// retrieve taxa and synonyms separately
		// need correct count for taxa and synonyms
//		if (taxonBaseRows == 0) { taxonBaseRows = getTaxonService().count(TaxonBase.class); }
//		logger.info("# Synonym: " + taxonBaseRows);
//		dataSet.setSynonyms(new ArrayList<Synonym>());
//		dataSet.setSynonyms(getTaxonService().getAllSynonyms(taxonBaseRows, 0));

//		if (jaxbExpConfig.isDoRelTaxa() == true) {
//			if (relationshipRows == 0) { relationshipRows = MAX_ROWS; }
//			logger.info("# Relationships");
//			List<RelationshipBase> relationList = getTaxonService().getAllRelationships(relationshipRows, 0);
//			Set<RelationshipBase> relationSet = new HashSet<RelationshipBase>(relationList);
//			dataSet.setRelationships(relationSet);
//		}

		if (jaxbExpConfig.isDoTypeDesignations() == true) {
			logger.info("# TypeDesignations");
			dataSet.addTypeDesignations(getNameService().getAllTypeDesignations(MAX_ROWS, 0));
		}

		if (jaxbExpConfig.isDoOccurrence() == true) {
			if (occurrencesRows == 0) { occurrencesRows = getOccurrenceService().count(SpecimenOrObservationBase.class); }
			logger.info("# SpecimenOrObservationBase: " + occurrencesRows);
			dataSet.setOccurrences(getOccurrenceService().getAllSpecimenOrObservationBases(occurrencesRows, 0));
		}

		if (jaxbExpConfig.isDoMedia() == true) {
			if (mediaRows == 0) { mediaRows = MAX_ROWS; }
			logger.info("# Media");
			dataSet.setMedia(getMediaService().getAllMedia(mediaRows, 0));
//			dataSet.addMedia(getMediaService().getAllMediaRepresentations(mediaRows, 0));
//			dataSet.addMedia(getMediaService().getAllMediaRepresentationParts(mediaRows, 0));
		}

		if (jaxbExpConfig.isDoFeatureData() == true) {
			if (featureDataRows == 0) { featureDataRows = MAX_ROWS; }
			logger.info("# Feature Tree, Feature Node");
			dataSet.setFeatureTrees(getDescriptionService().getFeatureTreesAll(null));
		}
	}


	@Override
	protected boolean doCheck(JaxbExportState state) {
		boolean result = true;
		logger.warn("No check implemented for Jaxb export");
		return result;
	}


	@Override
	protected boolean isIgnore(JaxbExportState state) {
		return false;
	}
	
}
