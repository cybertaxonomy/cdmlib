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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public abstract class DwcaExportBase extends CdmExportBase<DwcaTaxExportConfigurator, DwcaTaxExportState> implements ICdmExport<DwcaTaxExportConfigurator, DwcaTaxExportState>{
	private static final Logger logger = Logger.getLogger(DwcaExportBase.class);
	
	protected static final boolean IS_CORE = true;
	
	
	protected Set<Integer> existingRecordIds = new HashSet<Integer>();
	protected Set<UUID> existingRecordUuids = new HashSet<UUID>();
	
	
	/**
	 * Returns the list of taxon nodes that are part in one of the given classifications 
	 * and do have a taxon attached (empty taxon nodes should not but do exist in CDM databases).
	 * Preliminary implementation. Better implement API method for this.
	 * @return
	 */
	protected List<TaxonNode> getAllNodes(Set<Classification> classificationList) {
		List<TaxonNode> allNodes =  getClassificationService().getAllNodes();
		List<TaxonNode> result = new ArrayList<TaxonNode>();
		for (TaxonNode node : allNodes){
			if (node.getClassification() == null ){
				continue;
			}else if (classificationList != null && classificationList.contains(node.getClassification())){
				continue;
			}
			Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
			if (taxon == null){
				String message = "There is a taxon node without taxon: " + node.getId();
				logger.warn(message);
				continue;
			}
			result.add(node);
		}
		return result;
	}
	
	
	/**
	 * Creates the locationId, locality, countryCode triple
	 * @param record
	 * @param area
	 */
	protected void handleArea(IDwcaAreaRecord record, NamedArea area, TaxonBase<?> taxon, boolean required) {
		if (area != null){
			record.setLocationId(area);
			record.setLocality(area.getLabel());
			if (area.isInstanceOf(WaterbodyOrCountry.class)){
				WaterbodyOrCountry country = CdmBase.deproxy(area, WaterbodyOrCountry.class);
				record.setCountryCode(country.getIso3166_A2());
			}
		}else{
			if (required){
				String message = "Description requires area but area does not exist for taxon " + getTaxonLogString(taxon);
				logger.warn(message);
			}
		}
	}


	protected String getTaxonLogString(TaxonBase<?> taxon) {
		return taxon.getTitleCache() + "(" + taxon.getId() + ")";
	}
	

	/**
	 * @param el
	 * @return
	 */
	protected boolean recordExists(CdmBase el) {
		return existingRecordIds.contains(el.getId());
	}
	

	/**
	 * @param sec
	 */
	protected void addExistingRecord(CdmBase cdmBase) {
		existingRecordIds.add(cdmBase.getId());
	}
	
	/**
	 * @param el
	 * @return
	 */
	protected boolean recordExistsUuid(CdmBase el) {
		return existingRecordUuids.contains(el.getUuid());
	}
	
	/**
	 * @param sec
	 */
	protected void addExistingRecordUuid(CdmBase cdmBase) {
		existingRecordUuids.add(cdmBase.getUuid());
	}
	

	/**
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected FileOutputStream createFileOutputStream(DwcaTaxExportConfigurator config, String thisFileName) throws IOException, FileNotFoundException {
		String filePath = config.getDestinationNameString();
		String fileName = filePath + File.separatorChar + thisFileName;
		File f = new File(fileName);
		if (!f.exists()){
			f.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(f);
		return fos;
	}
	

	/**
	 * @param coreTaxFileName
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	protected PrintWriter createPrintWriter(final String fileName, DwcaTaxExportConfigurator config) 
					throws IOException, FileNotFoundException, UnsupportedEncodingException {
		FileOutputStream fos = createFileOutputStream(config, fileName);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);
		return writer;
	}
}
