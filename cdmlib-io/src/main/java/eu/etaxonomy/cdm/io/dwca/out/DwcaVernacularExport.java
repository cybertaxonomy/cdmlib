/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

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
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component
public class DwcaVernacularExport extends DwcaExportBase {
    private static final long serialVersionUID = 3169086545830374918L;

    private static final Logger logger = Logger.getLogger(DwcaVernacularExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/VernacularName";
	protected static final String fileName = "vernacular.txt";


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

		DwcaTaxOutputFile file = DwcaTaxOutputFile.VERNACULAR;
		try {

			DwcaMetaDataRecord metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);

            List<TaxonNode> allNodes = allNodes(state);
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon());
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(CommonTaxonName.class)){
							DwcaVernacularRecord record = new DwcaVernacularRecord(metaRecord, config);
							CommonTaxonName commonTaxonName = CdmBase.deproxy(el, CommonTaxonName.class);
							if (! state.recordExists(file, commonTaxonName)){
								handleCommonTaxonName(record, commonTaxonName, taxon, config);
								PrintWriter writer = createPrintWriter(state, file);
								record.write(state, writer);
								state.addExistingRecord(file, commonTaxonName);
							}
						}else if (el.getFeature().equals(Feature.COMMON_NAME())){
							//TODO
							String message = "Vernacular name export for TextData not yet implemented";
							state.getResult().addError(message, this, "doInvoke()");
							logger.warn(message);
						}
					}
				}

                flushWriter(state, file);
			}
		} catch (Exception e) {
	         String message = "Unexpected exception " + e.getMessage();
	         state.getResult().addException(e, message, "DwcaVernacularExport.doInvoke()");
		} finally{
			closeWriter(file, state);
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
