/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

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
	private static final Logger logger = Logger.getLogger(DwcaVernacularExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/VernacularName";
	private static final String fileName = "vernacular.txt";
	
	
	/**
	 * Constructor
	 */
	public DwcaVernacularExport() {
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
	protected boolean doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);

		try {
			PrintWriter writer = createPrintWriter(fileName, config);
			
			DwcaMetaRecord metaRecord = new DwcaMetaRecord(! IS_CORE, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);
			DwcaVernacularRecord r = new DwcaVernacularRecord();
			List<String> l = r.getHeaderList();
			for (String header : l){
				metaRecord.addFieldEntry("http://rs.tdwg.org/dwc/terms/" + header);
			}
			
			
			List<TaxonNode> allNodes =  getAllNodes(null);
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(CommonTaxonName.class)){
							DwcaVernacularRecord record = new DwcaVernacularRecord();
							CommonTaxonName commonTaxonName = CdmBase.deproxy(el, CommonTaxonName.class);
							if (! this.recordExists(commonTaxonName)){
								handleCommonTaxonName(record, commonTaxonName, taxon);
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
		}
		commitTransaction(txStatus);
		return true;
	}
	



	private void handleCommonTaxonName(DwcaVernacularRecord record, CommonTaxonName commonTaxonName, Taxon taxon) {
		record.setCoreid(taxon.getId());
		if (StringUtils.isBlank(commonTaxonName.getName())){
			String message = "'Name' is required field for vernacular name but does not exist for taxon " + getTaxonLogString(taxon);
			logger.warn(message);
		}else{
			record.setVernacularName(commonTaxonName.getName());
		}
		//TODO mulitple sources 
		record.setSource(null);
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
