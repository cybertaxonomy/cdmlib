/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.csv.redlist.out;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
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
public class CsvTaxExportRedlist extends CsvExportBaseRedlist {
    private static final long serialVersionUID = 841703025922543361L;

    private static final Logger logger = Logger.getLogger(CsvTaxExportRedlist.class);

	private static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/Taxon";
	private static final String fileName = "RedlistCoreTax.csv";

	public CsvTaxExportRedlist() {
		super();
		this.ioName = this.getClass().getSimpleName();
		this.exportData = ExportDataWrapper.NewByteArrayInstance();
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
	protected void doInvoke(CsvTaxExportStateRedlist state){
		CsvTaxExportConfiguratorRedlist config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);
		List<NamedArea> selectedAreas = config.getNamedAreas();
		Set<TaxonNode> taxonNodes = assembleTaxonNodeSet(config);

		PrintWriter writer = null;
		ByteArrayOutputStream byteArrayOutputStream;
		try {
			byteArrayOutputStream = config.getByteArrayOutputStream();
			writer = new PrintWriter(byteArrayOutputStream);
			//geographical Filter
			List<TaxonNode> filteredNodes = handleGeographicalFilter(selectedAreas, taxonNodes);

			//sorting List
			Collections.sort(filteredNodes, new Comparator<TaxonNode>() {

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
			for (TaxonNode node : filteredNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				CsvTaxRecordRedlist record = assembleRecord(state);
				INonViralName name = taxon.getName();
				Classification classification = node.getClassification();
				config.setClassificationTitleCache(classification.getTitleCache());
				if (! this.recordExists(taxon)){
					handleTaxonBase(record, taxon, name, taxon, classification, null, false, false, config);
					record.write(writer);
					this.addExistingRecord(taxon);
				}
				//misapplied names
				handleMisapplication(taxon, writer, classification, record, config);
				writer.flush();
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		finally{
		    if(writer!=null){
		        writer.close();
		    }
			this.clearExistingRecordIds();
		}
		commitTransaction(txStatus);
		return;

	}


	//TODO: Exception handling
	protected Set<TaxonNode> assembleTaxonNodeSet(CsvTaxExportConfiguratorRedlist config){
		Set<TaxonNode> taxonNodes = new HashSet<>();
		if(config != null){
			List<UUID> taxonNodeUuidSet = new ArrayList<>(config.getTaxonNodeUuids());
			List<TaxonNode> loadedNodes = getTaxonNodeService().load(taxonNodeUuidSet, null);
			taxonNodes.addAll(loadedNodes);
		}
		return taxonNodes;
	}

	//TODO: Exception handling
	private CsvTaxRecordRedlist assembleRecord(CsvTaxExportStateRedlist state) {
		if(state!=null){
			CsvTaxExportConfiguratorRedlist config = state.getConfig();
			CsvMetaDataRecordRedlist metaRecord = new CsvMetaDataRecordRedlist(true, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);
			CsvTaxRecordRedlist record = new CsvTaxRecordRedlist(metaRecord, config);
			return record;
		}
		return null;
	}

	/**
	 * Takes positive List of areas and iterates over a given taxon node
	 * and their {@link Taxon} to return all {@link Taxon} with the desired
	 * geographical attribute.
	 *
	 * <p><p>
	 *
	 * If selectedAreas is null all child {@link TaxonNode}s of the given taxon node will be returned.
	 *
	 * @param selectedAreas
	 * @param taxonNodes
	 * @return
	 */
	protected List<TaxonNode> handleGeographicalFilter(List<NamedArea> selectedAreas,
	        Set<TaxonNode> taxonNodes) {
	    List<TaxonNode> filteredNodes = new ArrayList<TaxonNode>();
	    List<TaxonNode> allNodes = new ArrayList<TaxonNode>();
	    for (TaxonNode node : taxonNodes){
	        if(node.getTaxon()!=null){
	            allNodes.add(node);
	        }
	        allNodes.addAll(getTaxonNodeService().loadChildNodesOfTaxonNode(node, null, true, null));
	    }
	    //Geographical filter
	    if(selectedAreas != null && !selectedAreas.isEmpty() && selectedAreas.size() < 16){
	        for (TaxonNode node : allNodes){
	            Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
	            Set<TaxonDescription> descriptions = taxon.getDescriptions();
	            for (TaxonDescription description : descriptions){
	                for (DescriptionElementBase el : description.getElements()){
	                    if (el.isInstanceOf(Distribution.class) ){
	                        Distribution distribution = CdmBase.deproxy(el, Distribution.class);
	                        NamedArea area = distribution.getArea();
	                        for(NamedArea selectedArea:selectedAreas){
	                            if(selectedArea.getUuid().equals(area.getUuid())){
	                                filteredNodes.add(node);
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }else{
	        filteredNodes = allNodes;
	    }
	    return filteredNodes;
	}

	/**
	 * handles misapplied {@link Taxon}
	 * @param taxon
	 * @param writer
	 * @param classification
	 * @param metaRecord
	 * @param config
	 */
	private void handleMisapplication(Taxon taxon, PrintWriter writer, Classification classification, CsvTaxRecordRedlist record, CsvTaxExportConfiguratorRedlist config) {
		Set<Taxon> misappliedNames = taxon.getMisappliedNames();
		for (Taxon misappliedName : misappliedNames ){
//			CsvTaxRecordRedlist record = new CsvTaxRecordRedlist(metaRecord, config);
			TaxonRelationshipType relType = TaxonRelationshipType.MISAPPLIED_NAME_FOR();
			INonViralName name = misappliedName.getName();

			if (! this.recordExists(misappliedName)){
				handleTaxonBase(record, misappliedName, name, taxon, classification, relType, false, false, config);
				record.write(writer);
				this.addExistingRecord(misappliedName);
			}
		}
	}

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
	 * @param type
	 */
	private void handleTaxonBase(CsvTaxRecordRedlist record,TaxonBase<?> taxonBase,
			INonViralName name, Taxon acceptedTaxon, Classification classification,
			RelationshipTermBase<?> relType, boolean isProParte, boolean isPartial,
			CsvTaxExportConfiguratorRedlist config) {

		List<Feature> features = config.getFeatures();
		record.setHeadLinePrinted(config.isHasHeaderLines());
		if(features != null) {
            record.setPrintFeatures(features);
        }
		config.setHasHeaderLines(false);

		record.setDatasetName(classification.getTitleCache());
		record.setScientificName(name.getTitleCache());
		record.setScientificNameId(name.getUuid().toString());
		handleTaxonomicStatus(record, name, relType, isProParte, isPartial);
		//synonyms
		handleSynonyms(record,(Taxon) taxonBase);
		//distribution
		handleDiscriptionData(record, (Taxon) taxonBase);
		if(features!= null) {

			List<List<String>> featureCells = new ArrayList<List<String>>(features.size());
			for(int i = 0; i < features.size(); i++) {
				featureCells.add(new ArrayList<String>());
			}
			handleRelatedRedlistStatus(record, (Taxon)taxonBase, false, featureCells, features);
			handleRelatedRedlistStatus(record, (Taxon)taxonBase, true, featureCells, features);

		}
		return;
	}

	private void handleTaxonomicStatus(
			CsvTaxRecordRedlist record,
			INonViralName name,
			RelationshipTermBase<?> type,
			boolean isProParte,
			boolean isPartial) {
		if (type == null){
			record.setTaxonomicStatus(name.getNomenclaturalCode().acceptedTaxonStatusLabel());
		}else{
			String status = name.getNomenclaturalCode().synonymStatusLabel();
			if (type.equals(SynonymType.HETEROTYPIC_SYNONYM_OF())){
				status = "heterotypicSynonym";
			}else if(type.equals(SynonymType.HOMOTYPIC_SYNONYM_OF())){
				status = "homotypicSynonym";
			}else if(type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
				status = "misapplied";
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

	private void handleSynonyms(CsvTaxRecordRedlist record, Taxon taxon) {

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

	private void handleDiscriptionData(CsvTaxRecordRedlist record, Taxon taxon) {

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

	private void handleRedlistStatus(CsvTaxRecordRedlist record, Taxon taxon, List<List<String>> featureCells, List<Feature> features){
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


	private void handleRelatedRedlistStatus(CsvTaxRecordRedlist record, Taxon taxon, boolean relationFrom, List<List<String>> featureCells, List<Feature> features) {

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

	@Override
	protected boolean doCheck(CsvTaxExportStateRedlist state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}

	@Override
	protected boolean isIgnore(CsvTaxExportStateRedlist state) {
		return ! state.getConfig().isDoTaxa();
	}




}
