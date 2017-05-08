/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.jaxb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.babadshanjan
 * @created 25.09.2008
 * @version 1.0
 */
@Component
public class JaxbExport extends CdmExportBase<JaxbExportConfigurator, JaxbExportState, IExportTransformer> implements ICdmExport<JaxbExportConfigurator, JaxbExportState> {

    private static final Logger logger = Logger.getLogger(JaxbExport.class);

    private DataSet dataSet;

    /**
     *
     */
    public JaxbExport() {
        super();
        this.exportData = ExportDataWrapper.NewByteArrayInstance();
    }


    //	/**
    //	 *
    //	 */
    //	public JaxbExport() {
    //		super();
    //		this.ioName = this.getClass().getSimpleName();
    //	}

    /** Retrieves data from a CDM DB and serializes them CDM to XML.
     * Starts with root taxa and traverses the classification to retrieve children taxa, synonyms and relationships.
     * Taxa that are not part of the classification are not found.
     *
     * @param exImpConfig
     * @param dbname
     * @param filename
     */
    //	@Override
    //	protected boolean doInvoke(IExportConfigurator config,
    //			Map<String, MapWrapper<? extends CdmBase>> stores) {
    @Override
    protected void doInvoke(JaxbExportState state) {

        JaxbExportConfigurator jaxbExpConfig = (JaxbExportConfigurator)state.getConfig();
        //		String dbname = jaxbExpConfig.getSource().getName();
        URI uri = jaxbExpConfig.getDestination();
        //		logger.info("Serializing DB " + dbname + " to file " + fileName);
        //		logger.debug("DbSchemaValidation = " + jaxbExpConfig.getDbSchemaValidation());

        TransactionStatus txStatus = null;

        txStatus = startTransaction(true);

        dataSet = new DataSet();

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
            switch(jaxbExpConfig.getTarget()) {
            case FILE:
                writeToFile(new File(uri), dataSet);
                break;
            case EXPORT_DATA:
                CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
                exportStream = new ByteArrayOutputStream();
                cdmDocumentBuilder.marshal(dataSet, new StreamResult(exportStream));
                ((ExportResult)state.getResult()).addExportData((byte[])this.createExportData().getExportData());

                break;
            default:
                break;
            }
        } catch (Exception e) {
            logger.error("Marshalling error");
            e.printStackTrace();
        }

        commitTransaction(txStatus);

        return;

    }

    public static void writeToFile(File file, DataSet dataSet) throws UnsupportedEncodingException, FileNotFoundException {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"), true);
        cdmDocumentBuilder.marshal(dataSet, writer);

        // TODO: Split into one file per data set member to see whether performance improves?

        logger.info("XML file written");
        logger.info("Filename is: " + file.getAbsolutePath());
    }

    private void retrieveData (IExportConfigurator config, DataSet dataSet) {

        JaxbExportConfigurator jaxbExpConfig = (JaxbExportConfigurator)config;
        final int MAX_ROWS = 50000;
        int numberOfRows = jaxbExpConfig.getMaxRows();

        int agentRows = numberOfRows;
        int definedTermBaseRows = numberOfRows;
        int referenceBaseRows = numberOfRows;
        int taxonNameRows = numberOfRows;
        int taxonBaseRows = numberOfRows;
        int taxonNodeRows = numberOfRows;
        int relationshipRows = numberOfRows;
        int occurrencesRows = numberOfRows;
        int mediaRows = numberOfRows;
        int featureDataRows = numberOfRows;
        int classificationDataRows = numberOfRows;
        int languageDataRows = numberOfRows;
        int termVocabularyRows = numberOfRows;
        int homotypicalGroupRows = numberOfRows;
        int UserRows= numberOfRows;

        if (jaxbExpConfig.isDoUsers() == true) {

            if (UserRows == 0) { UserRows = MAX_ROWS; }
            logger.info("# User");
            List<User> users = getUserService().list(null, UserRows, 0, null, null);


            for (User user: users){
                dataSet.addUser(HibernateProxyHelper.deproxy(user));
            }

        }
        if (jaxbExpConfig.isDoTermVocabularies() == true) {
            if (termVocabularyRows == 0) { termVocabularyRows = MAX_ROWS; }
            logger.info("# TermVocabulary");
            dataSet.setTermVocabularies((List)getVocabularyService().list(null,termVocabularyRows, 0, null, null));
        }



        //		if (jaxbExpConfig.isDoLanguageData() == true) {
        //			if (languageDataRows == 0) { languageDataRows = MAX_ROWS; }
        //			logger.info("# Representation, Language String");
        //			dataSet.setLanguageData(getTermService().getAllRepresentations(MAX_ROWS, 0));
        //TODO!!!
        dataSet.setLanguageStrings(getTermService().getAllLanguageStrings(MAX_ROWS, 0));
        //		}

        if (jaxbExpConfig.isDoTerms() == true) {
            if (definedTermBaseRows == 0) { definedTermBaseRows = getTermService().count(DefinedTermBase.class); }
            logger.info("# DefinedTermBase: " + definedTermBaseRows);
            dataSet.setTerms(getTermService().list(null,definedTermBaseRows, 0,null,null));
        }

        if (jaxbExpConfig.isDoAuthors() == true) {
            if (agentRows == 0) { agentRows = getAgentService().count(AgentBase.class); }
            logger.info("# Agents: " + agentRows);
            //logger.info("    # Team: " + appCtr.getAgentService().count(Team.class));
            dataSet.setAgents(getAgentService().list(null,agentRows, 0,null,null));
        }



        if (jaxbExpConfig.getDoReferences() != IExportConfigurator.DO_REFERENCES.NONE) {
            if (referenceBaseRows == 0) { referenceBaseRows = getReferenceService().count(Reference.class); }
            logger.info("# Reference: " + referenceBaseRows);
            dataSet.setReferences(getReferenceService().list(null,referenceBaseRows, 0,null,null));
        }

        if (jaxbExpConfig.isDoHomotypicalGroups() == true) {
            if (homotypicalGroupRows == 0) { homotypicalGroupRows = MAX_ROWS; }
            logger.info("# Homotypical Groups");
            dataSet.setHomotypicalGroups(getNameService().getAllHomotypicalGroups(homotypicalGroupRows, 0));
        }

        if (jaxbExpConfig.isDoTaxonNames() == true) {
            if (taxonNameRows == 0) { taxonNameRows = getNameService().count(TaxonName.class); }
            logger.info("# TaxonName: " + taxonNameRows);
            //logger.info("    # Taxon: " + getNameService().count(BotanicalName.class));
            dataSet.setTaxonomicNames(getNameService().list(null,taxonNameRows, 0,null,null));
        }



        if (jaxbExpConfig.isDoTaxa() == true) {
            if (taxonBaseRows == 0) { taxonBaseRows = getTaxonService().count(TaxonBase.class); }
            logger.info("# TaxonBase: " + taxonBaseRows);
            //			dataSet.setTaxa(new ArrayList<Taxon>());
            //			dataSet.setSynonyms(new ArrayList<Synonym>());
            List<TaxonBase> tb = getTaxonService().list(null,taxonBaseRows, 0,null,null);
            for (TaxonBase taxonBase : tb) {
                dataSet.addTaxonBase(HibernateProxyHelper.deproxy(taxonBase));
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
        if (jaxbExpConfig.isDoOccurrence() == true) {
            if (occurrencesRows == 0) { occurrencesRows = getOccurrenceService().count(SpecimenOrObservationBase.class); }
            logger.info("# SpecimenOrObservationBase: " + occurrencesRows);
            List<SpecimenOrObservationBase> occurrenceList = getOccurrenceService().list(null,occurrencesRows, 0,null,null);
            /*List<SpecimenOrObservationBase> noProxyList = new ArrayList<SpecimenOrObservationBase>();
			for (SpecimenOrObservationBase specimen : occurrenceList){
				specimen = (SpecimenOrObservationBase)HibernateProxyHelper.deproxy(specimen);
				noProxyList.add(specimen);
			}*/
            dataSet.setOccurrences(occurrenceList);
        }

        if (jaxbExpConfig.isDoTypeDesignations() == true) {
            logger.info("# TypeDesignations");
            dataSet.addTypeDesignations(getNameService().getAllTypeDesignations(MAX_ROWS, 0));
        }



        if (jaxbExpConfig.isDoMedia() == true) {
            if (mediaRows == 0) { mediaRows = MAX_ROWS; }
            logger.info("# Media");
            dataSet.setMedia(getMediaService().list(null,mediaRows, 0,null,null));
            //			dataSet.addMedia(getMediaService().getAllMediaRepresentations(mediaRows, 0));
            //			dataSet.addMedia(getMediaService().getAllMediaRepresentationParts(mediaRows, 0));
        }

        if (jaxbExpConfig.isDoFeatureData() == true) {
            if (featureDataRows == 0) { featureDataRows = MAX_ROWS; }
            logger.info("# Feature Tree, Feature Node");
            List<FeatureTree> featureTrees = new ArrayList<FeatureTree>();
            featureTrees= getFeatureTreeService().list(null,featureDataRows, 0, null, null);
            List<FeatureTree> taxTreesdeproxy = new ArrayList<FeatureTree>();
            for (FeatureTree featureTree : featureTrees){
                HibernateProxyHelper.deproxy(featureTree);
                taxTreesdeproxy.add(featureTree);
            }

            dataSet.setFeatureTrees(getFeatureTreeService().list(null,null,null,null,null));
        }
        if (jaxbExpConfig.isDoClassificationData() == true) {
            if (classificationDataRows == 0) { classificationDataRows = MAX_ROWS; }
            logger.info("# Classification");


            List<Classification> taxTrees = new ArrayList<Classification>();
            taxTrees= getClassificationService().list(null,classificationDataRows, 0, null, null);

            List<Classification> taxTreesdeproxy = new ArrayList<Classification>();
            for (Classification taxTree : taxTrees){
                HibernateProxyHelper.deproxy(taxTree);
                taxTreesdeproxy.add(taxTree);
            }
            List<TaxonNode> taxNodes = new ArrayList<TaxonNode>();
            taxNodes= getClassificationService().getAllNodes();
            List<TaxonNode> taxNodesdeproxy = new ArrayList<TaxonNode>();
            for (TaxonNode taxNode : taxNodes){
                HibernateProxyHelper.deproxy(taxNode);
                taxNodesdeproxy.add(taxNode);
            }

            dataSet.setTaxonNodes(taxNodesdeproxy);
            dataSet.setClassifications(taxTreesdeproxy );
        }
        //TODO: FIXME!!!!!
        dataSet.setLanguageStrings(null);
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
