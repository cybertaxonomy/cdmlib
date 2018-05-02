/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pilotOutputHtml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.sdd.out.SDDDataSet;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author h.fradin (from a.babadshanjan)
 * @since 10.12.2008
 */
@Component("pilotOutputExporter")
public class PilotOutputExporter
        extends CdmExportBase<PilotOutputExportConfigurator, PilotOutputExportState, IExportTransformer, File>
        implements ICdmExport<PilotOutputExportConfigurator, PilotOutputExportState> {
// public class JaxbExport extends CdmIoBase implements ICdmIoExport {
// TODO: public class JaxbExport extends CdmIoBase implements ICdmIO {

    private static final long serialVersionUID = -5556376038773540067L;
    private static final Logger logger = Logger.getLogger(PilotOutputExporter.class);
	private PilotOutputDocumentBuilder pilotOutputDocumentBuilder = null;

	private boolean doOccurrence = true;


	public PilotOutputExporter() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/** Retrieves data from a CDM DB and serializes them CDM to XML.
	 * Starts with root taxa and traverses the classification to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the classification are not found.
	 *
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected void doInvoke(PilotOutputExportState state){
//		protected boolean doInvoke(IExportConfigurator config,
//		Map<String, MapWrapper<? extends CdmBase>> stores) {

		PilotOutputExportConfigurator pilotOutputExpConfig = state.getConfig();
		String dbname = pilotOutputExpConfig.getSource().getName();
    	String fileName = pilotOutputExpConfig.getDestinationNameString();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		logger.debug("DbSchemaValidation = " + pilotOutputExpConfig.getDbSchemaValidation());

		TransactionStatus txStatus = startTransaction(true);
		SDDDataSet dataSet = new SDDDataSet();
		List<Taxon> taxa = null;
		List<DefinedTermBase> terms = null;

		// get data from DB

		try {
			logger.info("Retrieving data from DB");

			retrieveData(pilotOutputExpConfig, dataSet);

		} catch (Exception e) {
			logger.error("Error retrieving data");
			e.printStackTrace();
		}

		logger.info("All data retrieved");

		try {
			pilotOutputDocumentBuilder = new PilotOutputDocumentBuilder();
			File f = new File(fileName);
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);
			pilotOutputDocumentBuilder.marshal(dataSet, fileName);

			// TODO: Split into one file per data set member to see whether performance improves?

			logger.info("XML file written");
			logger.info("Filename is: " + fileName);

		} catch (Exception e) {
			logger.error("Marshalling error");
			e.printStackTrace();
		}
		commitTransaction(txStatus);

		return;

	}


	private void retrieveData (IExportConfigurator config, SDDDataSet sddDataSet) {

		PilotOutputExportConfigurator pilotOutputExpConfig = (PilotOutputExportConfigurator)config;
		final int MAX_ROWS = 50000;
		int numberOfRows = pilotOutputExpConfig.getMaxRows();
		// TODO:
		//CdmApplicationController appCtr = config.getCdmAppController(false, true);

		int agentRows = numberOfRows;
		int definedTermBaseRows = numberOfRows;
		int referenceBaseRows = numberOfRows;
		int taxonNameRows = numberOfRows;
		int taxonBaseRows = numberOfRows;
		int relationshipRows = numberOfRows;
		int occurrencesRows = numberOfRows;
		int mediaRows = numberOfRows;
		int featureDataRows = numberOfRows;
		int languageDataRows = numberOfRows;
		int termVocabularyRows = numberOfRows;
		int homotypicalGroupRows = numberOfRows;

		if (pilotOutputExpConfig.isDoTermVocabularies() == true) {
			if (termVocabularyRows == 0) { termVocabularyRows = MAX_ROWS; }
			logger.info("# TermVocabulary");
			sddDataSet.setTermVocabularies((List)getVocabularyService().list(null,MAX_ROWS, 0,null,null));;
		}

		if (pilotOutputExpConfig.isDoLanguageData() == true) {
			if (languageDataRows == 0) { languageDataRows = MAX_ROWS; }
			logger.info("# Representation, Language String");
			sddDataSet.setLanguageData(getTermService().getAllRepresentations(MAX_ROWS, 0));
			sddDataSet.addLanguageData(getTermService().getAllLanguageStrings(MAX_ROWS, 0));
		}

		if (pilotOutputExpConfig.isDoTerms() == true) {
			if (definedTermBaseRows == 0) { definedTermBaseRows = getTermService().count(DefinedTermBase.class); }
			logger.info("# DefinedTermBase: " + definedTermBaseRows);
			sddDataSet.setTerms(getTermService().list(null,definedTermBaseRows, 0,null,null));
		}

		if (pilotOutputExpConfig.isDoAuthors() == true) {
			if (agentRows == 0) { agentRows = getAgentService().count(AgentBase.class); }
			logger.info("# Agents: " + agentRows);
			//logger.info("    # Team: " + getAgentService().count(Team.class));
			sddDataSet.setAgents(getAgentService().list(null,agentRows, 0,null,null));
		}

		if (pilotOutputExpConfig.getDoReferences() != IExportConfigurator.DO_REFERENCES.NONE) {
			if (referenceBaseRows == 0) { referenceBaseRows = getReferenceService().count(Reference.class); }
			logger.info("# Reference: " + referenceBaseRows);
			sddDataSet.setReferences(getReferenceService().list(null,referenceBaseRows, 0,null,null));
		}

		if (pilotOutputExpConfig.isDoTaxonNames() == true) {
			if (taxonNameRows == 0) { taxonNameRows = getNameService().count(TaxonName.class); }
			logger.info("# TaxonName: " + taxonNameRows);
			//logger.info("    # Taxon: " + getNameService().count(BotanicalName.class));
			sddDataSet.setTaxonomicNames(getNameService().list(null,taxonNameRows, 0,null,null));
		}

		if (pilotOutputExpConfig.isDoHomotypicalGroups() == true) {
			if (homotypicalGroupRows == 0) { homotypicalGroupRows = MAX_ROWS; }
			logger.info("# Homotypical Groups");
			sddDataSet.setHomotypicalGroups(getNameService().getAllHomotypicalGroups(homotypicalGroupRows, 0));
		}

		if (pilotOutputExpConfig.isDoTaxa() == true) {
			if (taxonBaseRows == 0) { taxonBaseRows = getTaxonService().count(TaxonBase.class); }
			logger.info("# TaxonBase: " + taxonBaseRows);
//			dataSet.setTaxa(new ArrayList<Taxon>());
//			dataSet.setSynonyms(new ArrayList<Synonym>());
			List<TaxonBase> tb = getTaxonService().list(null,taxonBaseRows, 0,null,null);
			for (TaxonBase taxonBase : tb) {
				if (taxonBase instanceof Taxon) {
					sddDataSet.addTaxon((Taxon)taxonBase);
				} else if (taxonBase instanceof Synonym) {
					sddDataSet.addSynonym((Synonym)taxonBase);
				} else {
					logger.error("entry of wrong type: " + taxonBase.toString());
				}
			}
		}

		// TODO:
		// retrieve taxa and synonyms separately
		// need correct count for taxa and synonyms
//		if (taxonBaseRows == 0) { taxonBaseRows = getTaxonService().count(TaxonBase.class); }
//		logger.info("# Synonym: " + taxonBaseRows);
//		dataSet.setSynonyms(new ArrayList<Synonym>());
//		dataSet.setSynonyms(getTaxonService().getAllSynonyms(taxonBaseRows, 0));
//
//		if (pilotOutputExpConfig.isDoRelTaxa() == true) {
//			if (relationshipRows == 0) { relationshipRows = MAX_ROWS; }
//			logger.info("# Relationships");
//			List<RelationshipBase> relationList = getTaxonService().getAllRelationships(relationshipRows, 0);
//			Set<RelationshipBase> relationSet = new HashSet<RelationshipBase>(relationList);
//			sddDataSet.setRelationships(relationSet);
//		}

		if (pilotOutputExpConfig.isDoReferencedEntities() == true) {
			logger.info("# Referenced Entities");
			sddDataSet.setReferencedEntities(getNameService().getAllNomenclaturalStatus(MAX_ROWS, 0));
			sddDataSet.addReferencedEntities(getNameService().getAllTypeDesignations(MAX_ROWS, 0));
		}

		if (pilotOutputExpConfig.isDoOccurrence() == true) {
			if (occurrencesRows == 0) { occurrencesRows = getOccurrenceService().count(SpecimenOrObservationBase.class); }
			logger.info("# SpecimenOrObservationBase: " + occurrencesRows);
			sddDataSet.setOccurrences(getOccurrenceService().list(null,occurrencesRows, 0,null,null));
		}

		if (pilotOutputExpConfig.isDoMedia() == true) {
			if (mediaRows == 0) { mediaRows = MAX_ROWS; }
			logger.info("# Media");
			sddDataSet.setMedia(getMediaService().list(null,mediaRows, 0,null,null));
//			dataSet.addMedia(getMediaService().getAllMediaRepresentations(mediaRows, 0));
//			dataSet.addMedia(getMediaService().getAllMediaRepresentationParts(mediaRows, 0));
		}

		if (pilotOutputExpConfig.isDoFeatureData() == true) {
			if (featureDataRows == 0) { featureDataRows = MAX_ROWS; }
			logger.info("# Feature Tree, Feature Node");
			sddDataSet.setFeatureData(getFeatureTreeService().getFeatureNodesAll());
			sddDataSet.addFeatureData(getFeatureTreeService().list(null,null,null,null,null));
		}
	}


	@Override
	protected boolean doCheck(PilotOutputExportState state) {
		boolean result = true;
		logger.warn("No check implemented for Jaxb export");
		return result;
	}


	@Override
	protected boolean isIgnore(PilotOutputExportState state) {
		return false;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteArray() {
        // TODO Auto-generated method stub
        return null;
    }



}
