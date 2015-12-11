/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv.redlist.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.csv.redlist.out.CsvTaxExportConfiguratorRedlist;
import eu.etaxonomy.cdm.io.csv.redlist.out.CsvTaxExportStateRedlist;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller, a.oppermann
 * @date 18.10.2012
 *
 */
public abstract class CsvExportBaseRedlist extends CdmExportBase<CsvTaxExportConfiguratorRedlist, CsvTaxExportStateRedlist, IExportTransformer> implements ICdmExport<CsvTaxExportConfiguratorRedlist, CsvTaxExportStateRedlist>{
	private static final Logger logger = Logger.getLogger(CsvExportBaseRedlist.class);
	
	protected static final boolean IS_CORE = true;
	
	
	protected Set<Integer> existingRecordIds = new HashSet<Integer>();
	protected Set<UUID> existingRecordUuids = new HashSet<UUID>();
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#countSteps()
	 */
	@Override
	public int countSteps() {
		List<TaxonNode> allNodes =  getClassificationService().getAllNodes();
		return allNodes.size();
	}

	
	
	/**
	 * Returns the list of {@link TaxonNode taxon nodes} that are part in one of the given {@link Classification classifications} 
	 * and do have a {@link Taxon} attached (empty taxon nodes should not but do exist in CDM databases).
	 * If <code>classificationList</code> is <code>null</code> or empty then all {@link TaxonNode taxon nodes} of all 
	 * {@link Classification classifications} are returned.<BR>
	 * Preliminary implementation. Better implement API method for this.
	 * @return
	 */
	protected List<TaxonNode> getAllNodes(Set<Classification> classificationList) {
		//handle empty list as no filter defined
		if (classificationList != null && classificationList.isEmpty()){
			classificationList = null;
		}
		
		List<TaxonNode> allNodes =  getClassificationService().getAllNodes();
		List<TaxonNode> result = new ArrayList<TaxonNode>();
		for (TaxonNode node : allNodes){
			if (node.getClassification() == null ){
				continue;
			}else if (classificationList != null && ! classificationList.contains(node.getClassification())){
				continue;
			}else{
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				if (taxon == null){
					String message = "There is a taxon node without taxon: " + node.getId();
					logger.warn(message);
					continue;
				}
				result.add(node);
			}
		}
		return result;
	}
	
	
	/**
	 * Creates the locationId, locality, countryCode triple
	 * @param record
	 * @param area
	 */
	protected void handleArea(ICsvAreaRecord record, NamedArea area, TaxonBase<?> taxon, boolean required) {
		if (area != null){
			record.setLocationId(area);
			record.setLocality(area.getLabel());
			if (area.isInstanceOf(Country.class)){
				Country country = CdmBase.deproxy(area, Country.class);
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
	

	protected String getSources(ISourceable<?> sourceable, CsvTaxExportConfiguratorRedlist config) {
		String result = "";
		for (IOriginalSource source: sourceable.getSources()){
			if (StringUtils.isBlank(source.getIdInSource())){//idInSource indicates that this source is only data provenance, may be changed in future
				if (source.getCitation() != null){
					String ref = source.getCitation().getTitleCache();
					result = CdmUtils.concat(config.getSetSeparator(), result, ref);
				}
			}
		}
		return result;
	}
	

	/**
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected FileOutputStream createFileOutputStream(CsvTaxExportConfiguratorRedlist config, String thisFileName) throws IOException, FileNotFoundException {
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
	 * @param config
	 * @param factory
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	protected XMLStreamWriter createXmlStreamWriter(CsvTaxExportStateRedlist state, String fileName)
			throws IOException, FileNotFoundException, XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance(); 
		OutputStream os;
		boolean useZip = state.isZip();
		if (useZip){
			os = state.getZipStream(fileName);
		}else{
			os = createFileOutputStream(state.getConfig(), fileName);
		}
		XMLStreamWriter  writer = factory.createXMLStreamWriter(os);
		return writer;
	}
	

	/**
	 * @param coreTaxFileName
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	protected PrintWriter createPrintWriter(final String fileName, CsvTaxExportStateRedlist state) 
					throws IOException, FileNotFoundException, UnsupportedEncodingException {
		
		OutputStream os;
		boolean useZip = state.isZip();
		if (useZip){
			os = state.getZipStream(fileName);
		}else{
			os = createFileOutputStream(state.getConfig(), fileName);
		}
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF8"), true);
		
		return writer;
	}
	


	
	/**
	 * Closes the writer
	 * @param writer
	 * @param state
	 */
	protected void closeWriter(PrintWriter writer, CsvTaxExportStateRedlist state) {
		if (writer != null && state.isZip() == false){
			writer.close();
		}
	}
	

	
	/**
	 * Closes the writer.
	 * Note: XMLStreamWriter does not close the underlying stream.
	 * @param writer
	 * @param state
	 */
	protected void closeWriter(XMLStreamWriter writer, CsvTaxExportStateRedlist state) {
		if (writer != null && state.isZip() == false){
			try {
				writer.close();
			} catch (XMLStreamException e) {
				throw new RuntimeException(e);
			}
		}
	}
	protected void clearExistingRecordIds(){
		existingRecordIds.clear();
	}
}
