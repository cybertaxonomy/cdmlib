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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component
public class DwcaVernacularExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaVernacularExport.class);

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
		String dbname = config.getSource() != null ? config.getSource().getName() : "unknown";
    	String fileName = config.getDestinationNameString();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		TransactionStatus txStatus = startTransaction(true);

		try {
			
			final String coreTaxFileName = "vernacular.txt";
			fileName = fileName + File.separatorChar + coreTaxFileName;
			File f = new File(fileName);
			if (!f.exists()){
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);

			
			
			List<TaxonNode> allNodes =  getClassificationService().getAllNodes();
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				Set<? extends DescriptionBase> descriptions = taxon.getDescriptions();
				for (DescriptionBase description : descriptions){
					for (Object o : description.getElements()){
						DescriptionElementBase el = CdmBase.deproxy(o, DescriptionElementBase.class);
						if (el.isInstanceOf(CommonTaxonName.class)){
							DwcaVernacularRecord record = new DwcaVernacularRecord();
							CommonTaxonName commonTaxonName = CdmBase.deproxy(el, CommonTaxonName.class);
							handleCommonTaxonName(record, commonTaxonName, taxon);
							record.write(writer);
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
		record.setVernacularName(commonTaxonName.getName());
		//TODO mulitple sources 
		record.setSource(null);
		record.setLanguage(commonTaxonName.getLanguage());
		// does not exist in CDM
		record.setTemporal(null);
		
		handleArea(record, commonTaxonName.getArea());
	}

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for Jaxb export");
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return false;
	}
	
}
