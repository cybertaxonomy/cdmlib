/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 */

package eu.etaxonomy.cdm.io.jaxb;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.io.common.ICdmIoExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 25.09.2008
 */
public class CdmExporter implements ICdmIoExport {
// public class CdmExporter extends CdmIoBase implements ICdmIoExport {
// TODO: public class CdmExporter extends CdmIoBase implements ICdmIO {

	private static final Logger logger = Logger.getLogger(CdmExporter.class);
	private CdmDocumentBuilder cdmDocumentBuilder = null;

	private String ioName = null;

	
	/**
	 * 
	 */
	public CdmExporter() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/** Retrieves data from a CDM DB and serializes them CDM to XML.
	 * Starts with root taxa and traverses the taxonomic tree to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the taxonomic tree are not found.
	 * 
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
//	@Override
	protected boolean doInvoke(IExportConfigurator config) {
//		protected boolean doInvoke(IExportConfigurator config,
//				Map<String, MapWrapper<? extends CdmBase>> stores) {

		JaxbExportConfigurator jaxbExpConfig = (JaxbExportConfigurator)config;
		String dbname = jaxbExpConfig.getSource().getName();
    	String fileName = jaxbExpConfig.getDestination();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		logger.debug("DbSchemaValidation = " + jaxbExpConfig.getDbSchemaValidation());

		CdmApplicationController appCtr = config.getCdmAppController();
		// TODO: 
		//CdmApplicationController appCtr = config.getCdmAppController(false, true);

		TransactionStatus txStatus = appCtr.startTransaction(true);
		DataSet dataSet = new DataSet();
		List<Taxon> taxa = null;
		List<DefinedTermBase> terms = null;

		// get data from DB

		try {
			logger.info("Retrieving data from DB");

			retrieveData(config, dataSet);

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
		appCtr.commitTransaction(txStatus);
		appCtr.close();
		
		return true;

	}


	private void retrieveData (IExportConfigurator config, DataSet dataSet) {

		JaxbExportConfigurator jaxbExpConfig = (JaxbExportConfigurator)config;
		final int MAX_ROWS = 50000;
		int numberOfRows = jaxbExpConfig.getMaxRows();
		CdmApplicationController appCtr = config.getCdmAppController();
		// TODO: 
		//CdmApplicationController appCtr = config.getCdmAppController(false, true);

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
			dataSet.setTermVocabularies(appCtr.getTermService().getAllTermVocabularies(MAX_ROWS, 0));;
		}

		if (jaxbExpConfig.isDoLanguageData() == true) {
			if (languageDataRows == 0) { languageDataRows = MAX_ROWS; }
			logger.info("# Representation, Language String");
			dataSet.setLanguageData(appCtr.getTermService().getAllRepresentations(MAX_ROWS, 0));
			dataSet.addLanguageData(appCtr.getTermService().getAllLanguageStrings(MAX_ROWS, 0));
		}

		if (jaxbExpConfig.isDoTerms() == true) {
			if (definedTermBaseRows == 0) { definedTermBaseRows = appCtr.getTermService().count(DefinedTermBase.class); }
			logger.info("# DefinedTermBase: " + definedTermBaseRows);
			dataSet.setTerms(appCtr.getTermService().getAllDefinedTerms(definedTermBaseRows, 0));
		}

		if (jaxbExpConfig.isDoAuthors() == true) {
			if (agentRows == 0) { agentRows = appCtr.getAgentService().count(Agent.class); }
			logger.info("# Agents: " + agentRows);
			//logger.info("    # Team: " + appCtr.getAgentService().count(Team.class));
			dataSet.setAgents(appCtr.getAgentService().getAllAgents(agentRows, 0));
		}

		if (jaxbExpConfig.getDoReferences() != IImportConfigurator.DO_REFERENCES.NONE) {
			if (referenceBaseRows == 0) { referenceBaseRows = appCtr.getReferenceService().count(ReferenceBase.class); }
			logger.info("# ReferenceBase: " + referenceBaseRows);
			dataSet.setReferences(appCtr.getReferenceService().getAllReferences(referenceBaseRows, 0));
		}

		if (jaxbExpConfig.isDoTaxonNames() == true) {
			if (taxonNameBaseRows == 0) { taxonNameBaseRows = appCtr.getNameService().count(TaxonNameBase.class); }
			logger.info("# TaxonNameBase: " + taxonNameBaseRows);
			//logger.info("    # Taxon: " + appCtr.getNameService().count(BotanicalName.class));
			dataSet.setTaxonomicNames(appCtr.getNameService().getAllNames(taxonNameBaseRows, 0));
		}

		if (jaxbExpConfig.isDoHomotypicalGroups() == true) {
			if (homotypicalGroupRows == 0) { homotypicalGroupRows = MAX_ROWS; }
			logger.info("# Homotypical Groups");
			dataSet.setHomotypicalGroups(appCtr.getNameService().getAllHomotypicalGroups(homotypicalGroupRows, 0));
		}

		if (jaxbExpConfig.isDoTaxa() == true) {
			if (taxonBaseRows == 0) { taxonBaseRows = appCtr.getTaxonService().count(TaxonBase.class); }
			logger.info("# TaxonBase: " + taxonBaseRows);
//			dataSet.setTaxa(new ArrayList<Taxon>());
//			dataSet.setSynonyms(new ArrayList<Synonym>());
			List<TaxonBase> tb = appCtr.getTaxonService().getAllTaxonBases(taxonBaseRows, 0);
			for (TaxonBase taxonBase : tb) {
				if (taxonBase instanceof Taxon) {
					dataSet.addTaxon((Taxon)taxonBase);
				} else if (taxonBase instanceof Synonym) {
					dataSet.addSynonym((Synonym)taxonBase);
				} else {
					logger.error("entry of wrong type: " + taxonBase.toString());
				}
			}
		}

		// TODO: 
		// retrieve taxa and synonyms separately
		// need correct count for taxa and synonyms
//		if (taxonBaseRows == 0) { taxonBaseRows = appCtr.getTaxonService().count(TaxonBase.class); }
//		logger.info("# Synonym: " + taxonBaseRows);
//		dataSet.setSynonyms(new ArrayList<Synonym>());
//		dataSet.setSynonyms(appCtr.getTaxonService().getAllSynonyms(taxonBaseRows, 0));

		if (jaxbExpConfig.isDoRelTaxa() == true) {
			if (relationshipRows == 0) { relationshipRows = MAX_ROWS; }
			logger.info("# Relationships");
			List<RelationshipBase> relationList = appCtr.getTaxonService().getAllRelationships(relationshipRows, 0);
			Set<RelationshipBase> relationSet = new HashSet<RelationshipBase>(relationList);
			dataSet.setRelationships(relationSet);
		}

		if (jaxbExpConfig.isDoReferencedEntities() == true) {
			logger.info("# Referenced Entities");
			dataSet.setReferencedEntities(appCtr.getNameService().getAllNomenclaturalStatus(MAX_ROWS, 0));
			dataSet.addReferencedEntities(appCtr.getNameService().getAllTypeDesignations(MAX_ROWS, 0));
		}

		if (jaxbExpConfig.isDoOccurrence() == true) {
			if (occurrencesRows == 0) { occurrencesRows = appCtr.getOccurrenceService().count(SpecimenOrObservationBase.class); }
			logger.info("# SpecimenOrObservationBase: " + occurrencesRows);
			dataSet.setOccurrences(appCtr.getOccurrenceService().getAllSpecimenOrObservationBases(occurrencesRows, 0));
		}

		if (jaxbExpConfig.isDoMedia() == true) {
			if (mediaRows == 0) { mediaRows = MAX_ROWS; }
			logger.info("# Media");
			dataSet.setMedia(appCtr.getMediaService().getAllMedia(mediaRows, 0));
//			dataSet.addMedia(appCtr.getMediaService().getAllMediaRepresentations(mediaRows, 0));
//			dataSet.addMedia(appCtr.getMediaService().getAllMediaRepresentationParts(mediaRows, 0));
		}

		if (jaxbExpConfig.isDoFeatureData() == true) {
			if (featureDataRows == 0) { featureDataRows = MAX_ROWS; }
			logger.info("# Feature Tree, Feature Node");
			dataSet.setFeatureData(appCtr.getDescriptionService().getFeatureNodesAll());
			dataSet.addFeatureData(appCtr.getDescriptionService().getFeatureTreesAll());
		}
	}


//	@Override
	protected boolean doCheck(IExportConfigurator config) {
		boolean result = true;
		logger.warn("No check implemented for Jaxb export");
		return result;
	}


//	@Override
	protected boolean isIgnore(IExportConfigurator config) {
		return false;
	}
	
	public boolean check(IExportConfigurator config) {
		if (isIgnore(config)){
			logger.warn("No check for " + ioName + " (ignored)");
			return true;
		}else{
			return doCheck(config);
		}
	}
	

	public boolean invoke(IExportConfigurator config) {
		
		if (isIgnore(config)){
			logger.warn("No invoke for " + ioName + " (ignored)");
			return true;
		}else{
			return doInvoke(config);
		}
	}
	
}
