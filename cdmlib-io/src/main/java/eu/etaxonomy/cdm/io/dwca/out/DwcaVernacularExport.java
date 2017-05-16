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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component
public class DwcaVernacularExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaVernacularExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/VernacularName";
	private static final String fileName = "vernacular.txt";


	/**
	 * Constructor
	 */
	public DwcaVernacularExport() {
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

            Set<Classification> classificationSet = new HashSet<Classification>();
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
						if (el.isInstanceOf(CommonTaxonName.class)){
							DwcaVernacularRecord record = new DwcaVernacularRecord(metaRecord, config);
							CommonTaxonName commonTaxonName = CdmBase.deproxy(el, CommonTaxonName.class);
							if (! this.recordExists(commonTaxonName)){
								handleCommonTaxonName(record, commonTaxonName, taxon, config);
								record.write(writer);
								this.addExistingRecord(commonTaxonName);
							}
						}else if (el.getFeature().equals(Feature.COMMON_NAME())){
							//TODO
							String message = "Vernacular name export for TextData not yet implemented";
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
		} finally{
			closeWriter(writer, state);
		}
		commitTransaction(txStatus);
		return;
	}




	private void handleCommonTaxonName(DwcaVernacularRecord record, CommonTaxonName commonTaxonName, Taxon taxon, DwcaTaxExportConfigurator config) {
		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());
		if (StringUtils.isBlank(commonTaxonName.getName())){
			String message = "'Name' is required field for vernacular name but does not exist for taxon " + getTaxonLogString(taxon);
			logger.warn(message);
		}else{
			record.setVernacularName(commonTaxonName.getName());
		}
		//sources
		record.setSource(getSources(commonTaxonName, config));
		record.setLanguage(commonTaxonName.getLanguage());
		// does not exist in CDM
		record.setTemporal(null);

		handleArea(record, commonTaxonName.getArea(), taxon, false);
	}


	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoVernacularNames();
	}



}
