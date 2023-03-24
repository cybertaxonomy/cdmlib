/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv.redlist.demo;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author a.oppermann
 * @since 18.10.2012
 */
@Component
public class CsvDemoExport extends CsvDemoBase {

    private static final long serialVersionUID = 8265935377927091897L;

    private static final Logger logger = LogManager.getLogger();

	private static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/Taxon";
	private static final String fileName = "RedlistCoreTax.csv";

	public CsvDemoExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}


	/** Retrieves data from a CDM DB and serializes them CDM to CSV.
	 * Starts with root taxa and traverses the classification to retrieve
	 * children taxa, synonyms, relationships, descriptive data, red list
	 * status (features).
	 * Taxa that are not part of the classification are not found.
	 *
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected void doInvoke(CsvDemoExportState state){
	    CsvDemoExportConfigurator config = state.getConfig();
	    TransactionStatus txStatus = startTransaction(true);

	    List<NamedArea> selectedAreas = config.getNamedAreas();
	    Set<Classification> classificationSet = assembleClassificationSet(config);

	    IProgressMonitor progressMonitor = null;
	    if(config.getProgressMonitor() != null) {
	        progressMonitor = config.getProgressMonitor();
	    }
	    PrintWriter writer = null;
	    try {
	        //json/xml result list
            List<CsvDemoRecord> recordList = null;
            if(config.getRecordList() != null){
                recordList = config.getRecordList();
                performJsonXMLPagination(state, config, txStatus, classificationSet, recordList);
            }

            try {
                switch(config.getTarget()) {
                case FILE:
                    if(!config.getDestination().isDirectory()){
                        writer = new PrintWriter(config.getDestination());
                    }
                    break;
                case EXPORT_DATA:
                    exportStream = new ByteArrayOutputStream();
                    writer = new PrintWriter(exportStream);
                    break;
                default :
                    break;
                }
                if(writer != null) {
                    performCSVExport(state, config, txStatus, classificationSet, progressMonitor, writer);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

	    } catch (ClassCastException e) {
	        e.printStackTrace();
	    }
	    finally{
	        if (exportStream != null){
	            state.getResult().addExportData(getByteArray());
	        }
	        if(writer != null){
	            writer.close();
	        }
	        this.clearExistingRecordIds();
	    }

	    return;
	}

    private void performJsonXMLPagination(CsvDemoExportState state, CsvDemoExportConfigurator config,
            TransactionStatus txStatus, Set<Classification> classificationSet, List<CsvDemoRecord> recordList) {

        Classification classification = null;
        for(Classification c : classificationSet){
            classification = c;
            //this sets the total amount of records for pagination
            config.setTaxonNodeListSize(getTaxonNodeService().countAllNodesForClassification(c));
        }
        //calculate pagination
        int start = config.getPageSize() * config.getPageNumber();
        List<TaxonNode> result = getTaxonNodeService().listAllNodesForClassification(classification, start, config.getPageSize());

        for (TaxonNode node : result){
            Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
            CsvDemoRecord record = assembleRecord(state);
            INonViralName name = taxon.getName();
            config.setClassificationTitleCache(classification.getTitleCache());
            if (! this.recordExists(taxon)){
                handleTaxonBase(record, taxon, name, classification, config, node);
                recordList.add(record);
                this.addExistingRecord(taxon);
            }
        }
        commitTransaction(txStatus);
    }

	private void performCSVExport(CsvDemoExportState state, CsvDemoExportConfigurator config,
	        TransactionStatus txStatus, Set<Classification> classificationSet, IProgressMonitor progressMonitor,
	        PrintWriter writer) {
	    //obtain chuncks of taxonNodes
	    int totalWork = 0;
	    int work = 0;
	    int limit = 500;
	    int end = 500;
	    int start = 0;

	    //TODO: Questionable if this information is really necessary, with respect to memory usage
	    Classification classification = null;
	    for(Classification c : classificationSet){
	    	classification = c;
	    	totalWork = getTaxonNodeService().countAllNodesForClassification(c);


	    	if(progressMonitor != null) {
	    		progressMonitor.beginTask("", totalWork);
	    	}
	    	List<TaxonNode> result = new ArrayList<>();
	    	int totalNodes = getTaxonNodeService().count(TaxonNode.class);

	    	for(int i = 0 ; i < totalNodes; i++){

	    		//geographical Filter
	    		//	     List<TaxonNode> taxonNodes =  handleGeographicalFilter(state, classificationSet, config, limit, start);

	    		result = getTaxonNodeService().listAllNodesForClassification(classification, start, limit);

	    		logger.info(result.size());


	    		for (TaxonNode node : result){
	    			Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
	    			CsvDemoRecord record = assembleRecord(state);
	    			INonViralName name = taxon.getName();
	    			//	                Classification classification = node.getClassification();
	    			config.setClassificationTitleCache(classification.getTitleCache());
	    			if (! this.recordExists(taxon)){

	    				handleTaxonBase(record, taxon, name, classification, config, node);
	    				//if(config.getDestination() != null){
	    					record.write(writer);
	    				//}
	    				this.addExistingRecord(taxon);
	    			}
	    			//misapplied names
	    			//handleMisapplication(taxon, writer, classification, record, config, node);

	    			if(progressMonitor !=null) {
	    				if(work < totalWork-1) {
	    					progressMonitor.worked(1);
	    				}
	    				work++;
	    			}
	    		}
	    		if(writer != null){
	    			writer.flush();
	    			commitTransaction(txStatus);
	    			txStatus = startTransaction(true);
	    		}
	    		//get next 1000 results
	    		if(result.size()%limit == 0){
	    			//increase only once to avoid same row
	    			if(i==0){
	    				start++;
	    			}
	    			start = start + limit;
	    			end = end + limit;
	    			result = null;
	    		}else{
	    			break;
	    		}
	    	}
	    }
	}

    //TODO: Exception handling
	/**
	 *
	 * @param config
	 */
	protected Set<Classification> assembleClassificationSet(CsvDemoExportConfigurator config){
		if(config != null){
			Set<UUID> classificationUuidSet = config.getClassificationUuids();
			List<Classification> classificationList = getClassificationService().find(classificationUuidSet);
			Set<Classification> classificationSet = new HashSet<>();
			classificationSet.addAll(classificationList);
			return classificationSet;
		}
		return null;
	}

	//TODO: Exception handling
	/**
	 * @param state
	 */
	private CsvDemoRecord assembleRecord(CsvDemoExportState state) {
		if(state!=null){
			CsvDemoExportConfigurator config = state.getConfig();
			CsvDemoMetaDataRecord metaRecord = new CsvDemoMetaDataRecord(true, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);
			CsvDemoRecord record = new CsvDemoRecord(metaRecord, config);
			return record;
		}
		return null;
	}

	/**
	 * handles the information record for the actual {@link Taxon} including {@link Classification classification}, Taxon Name, Taxon ID,
	 * Taxon Status, Synonyms, {@link Feature features} data
	 *
	 * @param record the concrete information record
	 */
	private void handleTaxonBase(CsvDemoRecord record, TaxonBase<?> taxonBase,
			INonViralName name, Classification classification,
			CsvDemoExportConfigurator config, TaxonNode node) {

		Taxon taxon = (Taxon) taxonBase;
		List<Feature> features = config.getFeatures();
		if(config.getDestination() != null){
		    record.setHeadLinePrinted(config.isHasHeaderLines());
		    if(config.isRedlistFeatures()){
		        if(features != null){
		            record.setPrintFeatures(features);
		        }
		    }
		    config.setHasHeaderLines(false);
		}
		if(config.isClassification()){
			record.setDatasetName(classification.getTitleCache());
		}
		if(config.isTaxonName()){
			record.setScientificName(name.getNameCache());
		}
		if(config.isTaxonNameID()){
			record.setScientificNameId(name.getUuid().toString());
		}
		if(config.isAuthor()){
			String authorshipCache = name.getAuthorshipCache();
			if(authorshipCache == null){
				authorshipCache = "";
			}
			record.setAuthorName(authorshipCache);
		}
		if(config.isRank()){
			String rank;
			if(taxon.getName().getRank() == null){
				rank = "";
			}else{
				rank = taxon.getName().getRank().toString();
			}
			record.setRank(rank);
		}
		if(config.isTaxonStatus()){
			handleTaxonomicStatus(record, name);
		}
		if(config.isAcceptedName()){
			//TODO write routine for accepted Name
		}
		if(config.isTaxonConceptID()){
			UUID taxonUuid = taxonBase.getUuid();
			if(taxonUuid == null){
				taxonUuid = UUID.fromString("");
			}
			record.setTaxonConceptID(taxonUuid.toString());
		}
		if(config.isParentID()){
			String parentUUID;
			if(node.getParent().getTaxon() == null){
				parentUUID = "";
			}else{
				parentUUID = node.getParent().getTaxon().getUuid().toString();
			}
			record.setParentUUID(parentUUID);
		}
		if(config.isLastChange()){
			String lastChange;
			if(taxon.getUpdated() == null){
				lastChange = "";
			}else{
				lastChange = taxon.getUpdated().toString();
			}
			record.setLastChange(lastChange);
		}
		if(config.isSynonyms()){
			handleSynonyms(record,taxon);
		}
		if(config.isDistributions()){
			handleDiscriptionData(record, taxon);
		}
		if(config.isRedlistFeatures()){
			if(features!= null) {

				List<List<String>> featureCells = new ArrayList<>(features.size());
				for(int i = 0; i < features.size(); i++) {
					featureCells.add(new ArrayList<>());
				}
				handleRelatedRedlistStatus(record, taxon, false, featureCells, features);
				handleRelatedRedlistStatus(record, taxon, true, featureCells, features);
			}
		}

		if(config.isExternalID()){
			Set<IdentifiableSource> sources = taxonBase.getSources();
			for(IdentifiableSource source:sources){
				Reference citation = source.getCitation();
				/*
				 * TODO: handle this more generic.
				 * see ticket #4040
				 *
				 */
				if(citation.getId() == 22){
					String idInSource = source.getIdInSource();
					if(idInSource == null){
						idInSource = "";
					}
					record.setExternalID(idInSource);

				}
			}
		}
	}

	private void handleTaxonomicStatus(
			CsvDemoRecord record,
			INonViralName name) {
		if (name.getNameType()!= null && name.getNameType().acceptedTaxonStatusLabel() != null){
			String acceptedTaxonStatusLabel = name.getNameType().acceptedTaxonStatusLabel();
			if(StringUtils.isEmpty(acceptedTaxonStatusLabel)){
				acceptedTaxonStatusLabel="";
			}
			record.setTaxonomicStatus(acceptedTaxonStatusLabel);
		}
	}

	/**
	 * This method concatenates several synonyms in a list.
	 */
	private void handleSynonyms(CsvDemoRecord record, Taxon taxon) {

		Set<Synonym> synonyms = taxon.getSynonyms();
		ArrayList<String> synonymLabels = new ArrayList<>();
		for (Synonym synonym :synonyms ){
			SynonymType type = synonym.getType();
			if (type == null){ // should not happen
				type = SynonymType.SYNONYM_OF;
			}
			INonViralName name = synonym.getName();
			synonymLabels.add(name.getTitleCache());
		}
		record.setSynonyms(synonymLabels);
	}

	private void handleDiscriptionData(CsvDemoRecord record, Taxon taxon) {

		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		ArrayList<String> distributions = new ArrayList<String>();
		for (TaxonDescription description : descriptions){
			for (DescriptionElementBase el : description.getElements()){
				if (el.isInstanceOf(Distribution.class) ){
						Distribution distribution = CdmBase.deproxy(el, Distribution.class);
						NamedArea area = distribution.getArea();
						distributions.add(area.getTitleCache());
				}

			}
		}
		record.setCountryCode(distributions);
	}

	private void handleRedlistStatus(CsvDemoRecord record, Taxon taxon, List<List<String>> featureCells, List<Feature> features){
		Set<TaxonDescription> descriptions = taxon.getDescriptions();

		for (TaxonDescription description : descriptions){
			for (DescriptionElementBase el : description.getElements()){
				if(el.isInstanceOf(CategoricalData.class)){
					CategoricalData categoricalData = CdmBase.deproxy(el, CategoricalData.class);
					for(DefinedTermBase<?> state:categoricalData.getStatesOnly()){
						Feature stateFeature = categoricalData.getFeature();
						// find matching feature and put data into according cell
						for(int i = 0; i < features.size(); i++) {
							if(features.get(i).equals(stateFeature)){
								List<String> cell = featureCells.get(i);
								cell.add(state.toString());
							}
						}
					}
				}else if(el.isInstanceOf(TextData.class)){
					TextData textData = CdmBase.deproxy(el, TextData.class);
					Feature textFeature = textData.getFeature();
					// find matching feature and put data into according cell
					for(int i = 0; i < features.size(); i++) {
						if(features.get(i).equals(textFeature)){
							List<String> cell = featureCells.get(i);
							String text = textData.getText(Language.GERMAN());
							text = text.replaceAll(System.getProperty("line.separator"), "");
							text = text.replaceAll("                            ", " ");
							cell.add(text);

						}
					}
				}
			}
		}
		record.setFeatures(featureCells);
	}

	private void handleRelatedRedlistStatus(CsvDemoRecord record, Taxon taxon, boolean relationFrom, List<List<String>> featureCells, List<Feature> features) {

		if (relationFrom) {
            handleRedlistStatus(record, taxon, featureCells, features);
        }

		Set<TaxonRelationship> taxRels;
		if(relationFrom){
			taxRels = taxon.getRelationsFromThisTaxon();
		}else{
			taxRels = taxon.getRelationsToThisTaxon();
		}
		for (TaxonRelationship taxRel:taxRels){
			if(taxRel.getType().equals(TaxonRelationshipType.CONGRUENT_TO())){
				Taxon relatedTaxon;
				if(relationFrom){
					relatedTaxon = taxRel.getToTaxon();
				}else{
					relatedTaxon = taxRel.getFromTaxon();
				}
				handleRedlistStatus(record, relatedTaxon, featureCells, features);
			}
		}
	}

	private void sortTaxonNodes(List<TaxonNode> taxonNodes) {
		Collections.sort(taxonNodes, new Comparator<TaxonNode>() {

			@Override
			public int compare(TaxonNode tn1, TaxonNode tn2) {
				Taxon taxon1 = tn1.getTaxon();
				Taxon taxon2 = tn2.getTaxon();
				if(taxon1 != null && taxon2 != null){
					return taxon1.getTitleCache().compareTo(taxon2.getTitleCache());
				}
				else{
					return 0;
				}
			}
		});
	}

	@Override
	protected boolean doCheck(CsvDemoExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}

	@Override
	protected boolean isIgnore(CsvDemoExportState state) {
		return ! state.getConfig().isDoTaxa();
	}

    @Override
    public byte[] getByteArray() {
        if (this.exportStream != null){
            return this.exportStream.toByteArray();
        }
        return null;
    }
}