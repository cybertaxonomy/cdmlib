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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.IOriginalSource;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller, a.oppermann
 * @since 18.10.2012
 */
public abstract class CsvExportBaseRedlist
        extends CdmExportBase<CsvTaxExportConfiguratorRedlist, CsvTaxExportStateRedlist, IExportTransformer, File>
        implements ICdmExport<CsvTaxExportConfiguratorRedlist, CsvTaxExportStateRedlist>{

    private static final long serialVersionUID = 2719567114724597599L;

    private static final Logger logger = LogManager.getLogger();

	protected static final boolean IS_CORE = true;


	protected Set<Integer> existingRecordIds = new HashSet<Integer>();
	protected Set<UUID> existingRecordUuids = new HashSet<UUID>();

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

	protected boolean recordExists(CdmBase el) {
		return existingRecordIds.contains(el.getId());
	}

	protected void addExistingRecord(CdmBase cdmBase) {
		existingRecordIds.add(cdmBase.getId());
	}

	protected boolean recordExistsUuid(CdmBase el) {
		return existingRecordUuids.contains(el.getUuid());
	}

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