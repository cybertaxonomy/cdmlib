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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
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


/**
 * @author a.oppermann
 * @created 18.10.2012
 */

@Component
public class CsvDemoExport extends CsvDemoBase {

    private static final long serialVersionUID = 8265935377927091897L;

    private static final Logger logger = Logger.getLogger(CsvDemoExport.class);

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
                    exportStream = new ByteArrayOutputStream();;
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

//	    commitTransaction(txStatus);
	    return;
	}


    /**
     * @param state
     * @param config
     * @param txStatus
     * @param classificationSet
     * @param recordList
     */
    private void performJsonXMLPagination(CsvDemoExportState state, CsvDemoExportConfigurator config,
            TransactionStatus txStatus, Set<Classification> classificationSet, List<CsvDemoRecord> recordList) {
        // TODO Auto-generated method stub
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
                handleTaxonBase(record, taxon, name, classification, null, false, false, config, node);
                recordList.add(record);
                this.addExistingRecord(taxon);
            }
        }
        commitTransaction(txStatus);
    }


    /**
     * @param state
     * @param config
     * @param txStatus
     * @param classificationSet
     * @param progressMonitor
     * @param writer
     * @return
     */
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
	    	List<TaxonNode> result = new ArrayList<TaxonNode>();
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

	    				handleTaxonBase(record, taxon, name, classification, null, false, false, config, node);
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
	 * @return
	 */
	protected Set<Classification> assembleClassificationSet(CsvDemoExportConfigurator config){
		if(config != null){
			Set<UUID> classificationUuidSet = config.getClassificationUuids();
			List<Classification> classificationList = getClassificationService().find(classificationUuidSet);
			Set<Classification> classificationSet = new HashSet<Classification>();
			classificationSet.addAll(classificationList);
			return classificationSet;
		}
		return null;
	}

	//TODO: Exception handling
	/**
	 *
	 * @param state
	 * @return
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
	 * Takes positive List of areas and iterates over a given classification
	 * and their {@link Taxon} to return all {@link Taxon} with the desired
	 * geographical attribute.
	 *
	 * <p><p>
	 *
	 * If selectedAreas is null all {@link TaxonNode}s of the given {@link Classification} will be returned.
	 *
	 * @param selectedAreas
	 * @param classificationSet
	 * @param limit
	 * @param start
	 * @return
	 */
//	protected List<TaxonNode> handleGeographicalFilter(CsvDemoExportState state,
//			Set<Classification> classificationSet, CsvDemoExportConfigurator config, int limit, int start) {
//		List<TaxonNode> filteredNodes = new ArrayList<TaxonNode>();
//		List<TaxonNode> allNodes = new ArrayList<TaxonNode>();
//		//Check if json/XML export
//		if(config.getRecordList() != null){
//		    if(config.getProgressMonitor() != null) {
//                config.getProgressMonitor().subTask("Calculate size of export...");
//            }
//		    //FIXME does not filter for classifications
//		    allNodes = getTaxonNodeService().list(TaxonNode.class, config.getPageSize(), config.getPageNumber(), null, null);
//		    config.setTaxonNodeListSize(getAllNodes(classificationSet).size());
//		    //getTaxonNodeService().page(TaxonNode.class, config.getPageSize(), config.getPageNumber(), null, null).getRecords();
//		}else{
//
//		    //do your own pagination
//		        allNodes =  getAllNodes(classificationSet);
//
//		}
//		//Geographical filter
//		if(state.getConfig().isDoGeographicalFilter()){
//			List<NamedArea> selectedAreas = state.getConfig().getNamedAreas();
//			logger.info(selectedAreas.size());
//			if(selectedAreas != null && !selectedAreas.isEmpty() && selectedAreas.size() < 16){
//				//				if(selectedAreas.size() == 16){
//				//					//Germany TDWG Level 3
//				//					String germany="uu7b7c2db5-aa44-4302-bdec-6556fd74b0b9id";
//				//					selectedAreas.add((NamedArea) getTermService().find(UUID.fromString(germany)));
//				//				}
//				for (TaxonNode node : allNodes){
//					Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
//					Set<TaxonDescription> descriptions = taxon.getDescriptions();
//					for (TaxonDescription description : descriptions){
//						for (DescriptionElementBase el : description.getElements()){
//							if (el.isInstanceOf(Distribution.class) ){
//								Distribution distribution = CdmBase.deproxy(el, Distribution.class);
//								NamedArea area = distribution.getArea();
//								for(NamedArea selectedArea:selectedAreas){
//									if(selectedArea.getUuid().equals(area.getUuid())){
//										filteredNodes.add(node);
//									}
//								}
//							}
//						}
//					}
//				}
//			}else{
//				filteredNodes = allNodes;
//			}
//		}
//		return filteredNodes;
//	}

	/**
	 * handles misapplied {@link Taxon}
	 * @param taxon
	 * @param writer
	 * @param classification
	 * @param metaRecord
	 * @param config
	 * @param node
	 */
//	private void handleMisapplication(Taxon taxon, PrintWriter writer, Classification classification, CsvDemoRecord record, CsvDemoExportConfigurator config, TaxonNode node) {
//		Set<Taxon> misappliedNames = taxon.getMisappliedNames();
//		for (Taxon misappliedName : misappliedNames ){
////			CsvTaxRecordRedlist record = new CsvTaxRecordRedlist(metaRecord, config);
//			TaxonRelationshipType relType = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
//			INonViralName name = misappliedName.getName();
//
//			if (! this.recordExists(misappliedName)){
//				handleTaxonBase(record, misappliedName, name, taxon, classification, relType, false, false, config, node);
//				if(writer != null){
//				    record.write(writer);
//				}
//				this.addExistingRecord(misappliedName);
//			}
//		}
//	}

	/**
	 * handles the information record for the actual {@link Taxon} including {@link Classification classification}, Taxon Name, Taxon ID,
	 * Taxon Status, Synonyms, {@link Feature features} data
	 * @param record the concrete information record
	 * @param taxonBase {@link Taxon}
	 * @param name
	 * @param acceptedTaxon
	 * @param parent
	 * @param basionym
	 * @param isPartial
	 * @param isProParte
	 * @param config
	 * @param node
	 * @param node
	 * @param type
	 */
	private void handleTaxonBase(CsvDemoRecord record, TaxonBase<?> taxonBase,
			INonViralName name, Classification classification,
			RelationshipTermBase<?> relType, boolean isProParte, boolean isPartial,
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
			handleTaxonomicStatus(record, name, relType, isProParte, isPartial);
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

				List<List<String>> featureCells = new ArrayList<List<String>>(features.size());
				for(int i = 0; i < features.size(); i++) {
					featureCells.add(new ArrayList<String>());
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

	/**
	 * @param record
	 * @param name
	 * @param type
	 * @param isPartial
	 * @param isProParte
	 */
	private void handleTaxonomicStatus(
			CsvDemoRecord record,
			INonViralName name,
			RelationshipTermBase<?> type,
			boolean isProParte,
			boolean isPartial) {
		if (type == null && name.getNomenclaturalCode()!= null && name.getNomenclaturalCode().acceptedTaxonStatusLabel() != null){
			String acceptedTaxonStatusLabel = name.getNomenclaturalCode().acceptedTaxonStatusLabel();
			if(StringUtils.isEmpty(acceptedTaxonStatusLabel)){
				acceptedTaxonStatusLabel="";
			}
			record.setTaxonomicStatus(acceptedTaxonStatusLabel);
		}else if(name.getNomenclaturalCode() != null && name.getNomenclaturalCode().synonymStatusLabel() != null){
			String status = name.getNomenclaturalCode().synonymStatusLabel();
			if (type.equals(SynonymType.HETEROTYPIC_SYNONYM_OF())){
				status = "heterotypicSynonym";
			}else if(type.equals(SynonymType.HOMOTYPIC_SYNONYM_OF())){
				status = "homotypicSynonym";
			}else if(type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
				status = "misapplied";
			}else if(type.equals(TaxonRelationshipType.PRO_PARTE_MISAPPLIED_NAME_FOR())){
                status = "proParteMisapplied";
            }
			if (isProParte){
				status = "proParteSynonym";
			}else if (isPartial){
				String message = "Partial synonym is not part of the gbif toxonomic status vocabulary";
				logger.warn(message);
				status = "partialSynonym";
			}

			record.setTaxonomicStatus(status);
		}
	}

	/**
	 *
	 * This method concatenates several synonyms in a list.
	 *
	 * @param record
	 * @param taxon
	 */
	private void handleSynonyms(CsvDemoRecord record, Taxon taxon) {

		Set<Synonym> synonyms = taxon.getSynonyms();
		ArrayList<String> synonymLabels = new ArrayList<>();
		for (Synonym synonym :synonyms ){
			SynonymType type = synonym.getType();
			if (type == null){ // should not happen
				type = SynonymType.SYNONYM_OF();
			}
			INonViralName name = synonym.getName();
			synonymLabels.add(name.getTitleCache());
		}
		record.setSynonyms(synonymLabels);
	}

	/**
	 *
	 * @param record
	 * @param taxon
	 */
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
	/**
	 *
	 * @param record
	 * @param taxon
	 * @param featureCells
	 * @param features
	 */
	private void handleRedlistStatus(CsvDemoRecord record, Taxon taxon, List<List<String>> featureCells, List<Feature> features){
		Set<TaxonDescription> descriptions = taxon.getDescriptions();

		for (TaxonDescription description : descriptions){
			for (DescriptionElementBase el : description.getElements()){
				if(el.isInstanceOf(CategoricalData.class)){
					CategoricalData categoricalData = CdmBase.deproxy(el, CategoricalData.class);
					for(State state:categoricalData.getStatesOnly()){
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

	/**
	 *
	 * @param record
	 * @param taxon
	 * @param relationFrom
	 * @param featureCells
	 * @param features
	 */
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



	/**
	 *
	 * @param taxonNodes
	 */
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


    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteArray() {
        if (this.exportStream != null){
            return this.exportStream.toByteArray();
        }
        return null;
    }

}
