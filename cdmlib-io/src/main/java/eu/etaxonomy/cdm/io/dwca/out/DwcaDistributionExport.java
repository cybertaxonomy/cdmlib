/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaDistributionExport extends DwcaExportBase {

    private static final long serialVersionUID = -3274468345456407430L;

    private static final Logger logger = Logger.getLogger(DwcaDistributionExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Distribution";
	private static final String fileName = "distribution.txt";

	/**
	 * Constructor
	 */
	public DwcaDistributionExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
		this.exportData = ExportDataWrapper.NewByteArrayInstance();
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
	protected void doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);

		PrintWriter writer = null;
		try {
			writer = createPrintWriter(fileName, state);
			DwcaMetaDataRecord metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);

			Set<UUID> classificationUuidSet = config.getClassificationUuids();
            List<Classification> classificationList;
            if (classificationUuidSet.isEmpty()){
                classificationList = getClassificationService().list(Classification.class, null, 0, null, null);
            }else{
                classificationList = getClassificationService().find(classificationUuidSet);
            }

            Set<Classification> classificationSet = new HashSet<>();
            classificationSet.addAll(classificationList);
            List<TaxonNode> allNodes;

            if (state.getAllNodes().isEmpty()){
                getAllNodes(state, classificationSet);
            }
            allNodes = state.getAllNodes();
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);

				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(Distribution.class) ){
							if (! recordExists(el)){
								DwcaDistributionRecord record = new DwcaDistributionRecord(metaRecord, config);
								Distribution distribution = CdmBase.deproxy(el, Distribution.class);
								handleDistribution(record, distribution, taxon, config);
								record.write(writer);
								this.addExistingRecord(distribution);
							}
						}else if (el.getFeature().equals(Feature.DISTRIBUTION())){
							//TODO
							String message = "Distribution export for TextData not yet implemented";
							logger.warn(message);
						}
					}
				}

				writer.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			closeWriter(writer, state);
		}
		commitTransaction(txStatus);
		return;
	}




	private void handleDistribution(DwcaDistributionRecord record, Distribution distribution, Taxon taxon, DwcaTaxExportConfigurator config) {
		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());
		handleArea(record, distribution.getArea(), taxon, true);
		//TODO missing
		record.setLifeStage(null);
		record.setOccurrenceStatus(distribution.getStatus());
		//TODO missing
		record.setThreadStatus(null);
		record.setEstablishmentMeans(distribution.getStatus());
		//TODO missing
		record.setAppendixCITES(null);
		//TODO missing
		record.setEventDate(null);
		//TODO missing
		record.setSeasonalDate(null);
		//FIXME
		record.setSource(getSources(distribution, config));
		//FIXME
		record.setOccurrenceRemarks(null);

	}

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoDistributions();
	}


}
