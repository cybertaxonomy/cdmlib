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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.io.common.IoStateBase;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.dwca.jaxb.Archive;
import eu.etaxonomy.cdm.io.dwca.jaxb.ArchiveEntryBase;
import eu.etaxonomy.cdm.io.dwca.jaxb.Extension;
import eu.etaxonomy.cdm.io.dwca.out.DwcaMetaDataRecord;

/**
 * This class transforms a Darwin Core Archive zip file into a set of CSVReaderInputStreams.
 * For each data file included in the zip it creates one stream by evaluating the meta file.
 * Ecological metadata handling is still unclear.
 * @author a.mueller
 * @date 17.10.2011
 *
 */
public class DwcaZipToStreamConverter<STATE extends IoStateBase> {
	private static Logger logger = Logger.getLogger(DwcaZipToStreamConverter.class);

	private final String META_XML = "meta.xml";
	protected static final boolean IS_CORE = true;
	
	private List<TermUri> extensionList = Arrays.asList(
			TermUri.DWC_RESOURCE_RELATIONSHIP,
			TermUri.GBIF_TYPES_AND_SPECIMEN,
			TermUri.GBIF_VERNACULAR_NAMES,
			TermUri.GBIF_IDENTIFIER,
			TermUri.GBIF_SPECIES_PROFILE,
			TermUri.GBIF_REFERENCE,
			TermUri.GBIF_DESCRIPTION,
			TermUri.GBIF_DISTRIBUTION,
			TermUri.GBIF_IMAGE
	);
			
	
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
		ArchiveEntryBase core = archive.getCore();
		return makeStream(core);
	}
	
	public CsvStream getStream(String rowType) throws IOException{
		initArchive();
		
		ArchiveEntryBase archiveEntry = null; 
		List<Extension> extensions = archive.getExtension();
		for (Extension extension : extensions){
			if (rowType.equalsIgnoreCase(extension.getRowType())){
				archiveEntry = extension;
				break;
			}
		}
		return makeStream(archiveEntry);
	}
	
	public CsvStream getStream(TermUri rowType) throws IOException{
		return getStream(rowType.getUriString());
	}

	public IReader<CsvStream> getStreamStream(STATE state){
		List<CsvStream> streamList = new ArrayList<CsvStream>();
		try {
			streamList.add(getCoreStream()); //for taxa and names
		} catch (IOException e) {
			String message = "Core stream not available for %s: %s";
			logger.warn(String.format(message, "taxa", e.getMessage()));
			state.setSuccess(false);
		} 
		try {
			streamList.add(getCoreStream());//for taxon and name relations
		} catch (IOException e) {
			String message = "Core stream not available for %s: %s";
			logger.warn(String.format(message, "taxon relations", e.getMessage()));
			state.setSuccess(false);
		}  
		for (TermUri extension : extensionList){
			CsvStream extensionStream;
			try {
				extensionStream = getStream(extension);
				if (extensionStream != null){
					streamList.add(extensionStream);
				}
			} catch (IOException e) {
				String message = "Extension stream not available for extension %s: %s";
				logger.warn(String.format(message, extension.getUriString(), e.getMessage()));
				state.setSuccess(false);
			}
		}
		IReader<CsvStream> result = new ListReader<CsvStream>(streamList);
		return result;
	}


	/**
	 * Creates the CsvStream for an archive entry. Returns null if archive entry is null.
	 * @param archiveEntry
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private CsvStream makeStream(ArchiveEntryBase archiveEntry) throws IOException, UnsupportedEncodingException {
		if (archiveEntry == null){
			return null;
		}
		char fieldTerminatedBy = archiveEntry.getFieldsTerminatedBy().isEmpty()? CSVReader.DEFAULT_SEPARATOR : archiveEntry.getFieldsTerminatedBy().charAt(0);
		char fieldsEnclosedBy = (archiveEntry.getFieldsEnclosedBy().isEmpty()) ? CSVReader.DEFAULT_QUOTE_CHARACTER: archiveEntry.getFieldsEnclosedBy().charAt(0);
		boolean ignoreHeader = archiveEntry.getIgnoreHeaderLines();
		String linesTerminatedBy = archiveEntry.getLinesTerminatedBy();
		String encoding = archiveEntry.getEncoding();
		int skipLines = ignoreHeader? 1 : 0;
		
		String fileLocation = archiveEntry.getFiles().getLocation();
		InputStream coreCsvInputStream = makeInputStream(fileLocation);
		Reader coreReader = new InputStreamReader(coreCsvInputStream, encoding); 
		CSVReader csvReader = new CSVReader(coreReader, fieldTerminatedBy,fieldsEnclosedBy, skipLines);
		CsvStream csvStream = new CsvStream(csvReader, archiveEntry);
		
		//		InputStream s;
//		s.
		
		return csvStream;
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
			if (archive.getCore().getFieldsTerminatedBy().equals("\\t") ){
				//TODO handle, TODO also handle other \xxx delimiter
			}else{
				throw new IllegalStateException("CsvReader does not allow field delimiters with more than 1 character. ");
			}
		}
		if (archive.getCore().getFieldsEnclosedBy().length() > 1){
			throw new IllegalStateException("CsvReader does not allow field delimiters with more than 1 character");
		}
		
	}

//
//	/**
//	 * @return
//	 * @throws IOException
//	 */
//	private InputStream makeInputStream(String name) throws IOException {
//		
//		ZipInputStream zin = new ZipInputStream(dwcaZip.toURL().openStream());
//		ZipEntry ze = zin.getNextEntry();
//		while (!ze.getName().equals(name)) {
//		    zin.closeEntry(); // not sure whether this is necessary
//		    ze = zin.getNextEntry();
//		}
//		
//		CheckedInputStream cis = new CheckedInputStream(in, cksum)
//		
//		InputStream metaInputStream = zip.getInputStream(ze);
//		return metaInputStream;
//	
//		InputStream metaInputStream = zip.getInputStream(metaEntry);
//		return metaInputStream;
//	}
//	

	/**
	 * @return
	 * @throws IOException
	 */
	private InputStream makeInputStream(String name) throws IOException {
		ZipFile zip = new ZipFile(new File(dwcaZip), ZipFile.OPEN_READ);
		ZipEntry metaEntry = zip.getEntry(name);
		if (metaEntry == null){
			String message = "Zip entry for %s not available";
			throw new IOException(String.format(message, name));
		}
		InputStream metaInputStream = zip.getInputStream(metaEntry);
		return metaInputStream;
	}


	
	
}
