// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.io.dwca.jaxb.Archive;
import eu.etaxonomy.cdm.io.dwca.jaxb.Core;
import eu.etaxonomy.cdm.io.dwca.out.DwcaMetaDataRecord;

/**
 * This class transforms a Darwin Core Archive zip file into a set of CSVReaderInputStreams.
 * For each data file included in the zip it creates one stream by evaluating the meta file.
 * Ecological metadata handling is still unclear.
 * @author a.mueller
 * @date 17.10.2011
 *
 */
public class DwcaZipToStreamConverter {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DwcaZipToStreamConverter.class);

	private final String META_XML = "meta.xml";
	protected static final boolean IS_CORE = true;
	
	private URI dwcaZip;
	private Map<String, DwcaMetaDataRecord> metaRecords = new HashMap<String, DwcaMetaDataRecord>(); 
	private Archive archive;
	
/// ******************** FACTORY ********************************/	
	
	public static DwcaZipToStreamConverter NewInstance(URI dwcaZip){
		return new DwcaZipToStreamConverter(dwcaZip);
	}
	

//************************ CONSTRUCTOR *********************************/
	
	/**
	 * Constructor
	 * @param dwcaZip
	 */
	public DwcaZipToStreamConverter(URI dwcaZip) {
		this.dwcaZip = dwcaZip;
		initArchive();
	}
	

	protected Archive getArchive(){
			return this.archive;
	}
	
	public CsvStream getCoreStream() throws IOException{
		initArchive();
		Core core = archive.getCore();
		char fieldTerminatedBy = core.getFieldsTerminatedBy().charAt(0);
		char fieldsEnclosedBy = core.getFieldsEnclosedBy().charAt(0);
		boolean ignoreHeader = core.getIgnoreHeaderLines();
		String linesTerminatedBy = core.getLinesTerminatedBy();
		String encoding = core.getEncoding();
		int skipLines = ignoreHeader? 1 : 0;
		
		String fileLocation = core.getFiles().getLocation();
		InputStream coreCsvInputStream = makeInputStream(fileLocation);
		Reader coreReader = new InputStreamReader(coreCsvInputStream, encoding); 
		CSVReader csvReader = new CSVReader(coreReader, fieldTerminatedBy,fieldsEnclosedBy, skipLines);
		CsvStream coreStream = new CsvStream(csvReader, core);
		
		//		InputStream s;
//		s.
		
		return coreStream;
	}


	private void initArchive() {
		if (archive == null){
			try {
				InputStream metaInputStream = makeInputStream(META_XML);
				
				JAXBContext jaxbContext = JAXBContext.newInstance("eu.etaxonomy.cdm.io.dwca.jaxb");
				Unmarshaller unmarshaller =  jaxbContext.createUnmarshaller();
				archive = (Archive)unmarshaller.unmarshal(metaInputStream);
	
				validateArchive(archive);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (JAXBException e) {
				throw new RuntimeException(e);
			}
		}
	}


	private void validateArchive(Archive archive) {
		if (archive.getCore().getFieldsTerminatedBy().length() > 1){
			throw new IllegalStateException("CsvReader does not allow field delimiters with more than 1 character");
		}
		if (archive.getCore().getFieldsEnclosedBy().length() > 1){
			throw new IllegalStateException("CsvReader does not allow field delimiters with more than 1 character");
		}
		
	}


	/**
	 * @return
	 * @throws IOException
	 */
	private InputStream makeInputStream(String name) throws IOException {
		ZipFile zip = new ZipFile(new File(dwcaZip), ZipFile.OPEN_READ);
		ZipEntry metaEntry = zip.getEntry(name);
		InputStream metaInputStream = zip.getInputStream(metaEntry);
		return metaInputStream;
	}


	
	
}
