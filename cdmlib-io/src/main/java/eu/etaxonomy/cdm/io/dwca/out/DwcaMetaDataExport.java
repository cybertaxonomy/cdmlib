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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.dwca.out.DwcaMetaDataRecord.FieldEntry;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaMetaDataExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaMetaDataExport.class);

	private static final String fileName = "meta.xml";
	
	/**
	 * Constructor
	 */
	public DwcaMetaDataExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/** Retrieves the MetaData for a Darwin Core Archive File.
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected boolean doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();

		DwcaMetaDataRecord metaDataRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, null);
		metaDataRecord.setMetaData(true);
		state.addMetaRecord(metaDataRecord);
    	
		XMLOutputFactory factory = XMLOutputFactory.newInstance(); 
		try {
			FileOutputStream fos = createFileOutputStream(config, fileName);
			XMLStreamWriter writer = factory.createXMLStreamWriter(fos);
			
			String rootNamespace = "http://rs.tdwg.org/dwc/text/";
			String rootName = "archive";
			
			List<DwcaMetaDataRecord> metaRecords = state.getMetaRecords();
			
			// create header 
			writer.writeStartDocument(); 
			writer.setDefaultNamespace(rootNamespace);
			
				// create root element 
				writer.writeStartElement(rootName);
				writer.writeNamespace(null, rootNamespace);
				
				writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
				writer.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "http://rs.tdwg.org/dwc/text/ http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd");
				
				for (DwcaMetaDataRecord metaRecord : metaRecords){
					if (! metaDataRecord.isMetaData()){
						writeMetaDataRecord(writer, config, metaRecord);
					}
				}
				writer.writeEndElement(); 
			writer.writeEndDocument(); 
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			if (e.getNestedException() != null){
				throw new RuntimeException(e.getNestedException());
			}else{
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
		
		return true;
	}


	private void writeMetaDataRecord(XMLStreamWriter writer,
			DwcaTaxExportConfigurator config, DwcaMetaDataRecord metaRecord) throws XMLStreamException {
		if (! metaRecord.hasEntries()){
			return; 
		}
		String encoding = config.getEncoding();
		String linesTerminatedBy = config.getLinesTerminatedBy();
		String fieldsEnclosedBy = config.getFieldsEnclosedBy();
		String ignoreHeaderLines = config.isIgnoreHeaderLines()? "1":"0";
		
		// create core element 
		String elementName = metaRecord.isCore()? "core": "extension";
		String rowType = metaRecord.getRowType();
		writeElementStart(writer, elementName, encoding, linesTerminatedBy,	fieldsEnclosedBy, ignoreHeaderLines, rowType);
			String filename = metaRecord.getFileLocation();
			writeFiles(writer, filename );
			writeId(writer, metaRecord.isCore());
			
			List<FieldEntry> entryList = metaRecord.getEntries();
			for (FieldEntry fieldEntry : entryList){
				if (fieldEntry.index != 0){
					writeFieldLine(writer, fieldEntry.index, fieldEntry.term);
				}
			}
	
		writer.writeEndElement();
	}

	private void writeFieldLine(XMLStreamWriter writer, int index, URI term) throws XMLStreamException {
		writer.writeStartElement("field");
		writer.writeAttribute("index", String.valueOf(index));
		writer.writeAttribute("term", term.toString());
		writer.writeEndElement();
		
	}

	private void writeId(XMLStreamWriter writer, boolean isCore) throws XMLStreamException {
		String strId = isCore? "id" : "coreid";
		writer.writeStartElement(strId);
		writer.writeAttribute("index", "0");
		writer.writeEndElement();
	}


	private void writeFiles(XMLStreamWriter writer, String filename) throws XMLStreamException {
		writer.writeStartElement("files");
			writer.writeStartElement("location");
			writer.writeCharacters(filename);
			writer.writeEndElement();
		writer.writeEndElement();
		
	}

	/**
	 * @param writer
	 * @param encoding
	 * @param linesTerminatedBy
	 * @param fieldsEnclosedBy
	 * @param ignoreHeaderLines
	 * @param rowType
	 * @param elementName
	 * @throws XMLStreamException
	 */
	private void writeElementStart(XMLStreamWriter writer, String elementName, String encoding,
			String linesTerminatedBy, String fieldsEnclosedBy,
			String ignoreHeaderLines, String rowType)
			throws XMLStreamException {
		writer.writeStartElement(elementName); 
		writer.writeAttribute( "encoding", encoding );
		writer.writeAttribute( "linesTerminatedBy", linesTerminatedBy );
		writer.writeAttribute( "fieldsEnclosedBy", fieldsEnclosedBy );
		writer.writeAttribute("ignoreHeaderLines", ignoreHeaderLines);
		writer.writeAttribute("rowType", rowType);
	}
	


	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoMetaData();
	}
	
}
