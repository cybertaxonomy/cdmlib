/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.mueller
 * @since 03.05.2011
 *
 */
public class TermMapping {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TermMapping.class);

	private static final String COMMENT = "//";

	private UUID uuidCdmVoc;
	private URI externalVoc;
	private char separator = '\t'; //'\u0009';  //horizontal tab

	private Map<UUID, String> mappingMap = new HashMap<>();


	public TermMapping(String filename) throws IOException{
		readMapping(filename);
	}


	private void readMapping(String filename) throws IOException {
		String strResourceFileName = "mapping" + CdmUtils.getFolderSeperator() + filename;
		InputStreamReader isr = CdmUtils.getUtf8ResourceReader(strResourceFileName);
		CSVReader reader = new CSVReader(isr, separator);

		String [] nextLine = reader.readNext();
		uuidCdmVoc = UUID.fromString(nextLine[0]);
		externalVoc = URI.create(nextLine[1]);

		while ((nextLine = reader.readNext()) != null) {
			// nextLine[] is an array of values from the line
			if (nextLine.length == 0){
				continue;
			}
			readMappingLine(nextLine);

		}
	}

	private void readMappingLine(String[] mappingLine) {
		if (! mappingLine[0].startsWith(COMMENT)){
			UUID uuidCdm = UUID.fromString(mappingLine[0]);
			String externalTerm = mappingLine[1].trim();
			mappingMap.put(uuidCdm, externalTerm);
		}
	}

	public String getTerm(UUID key){
		return mappingMap.get(key);
	}

	public URI getExternalVoc(){
		return externalVoc;
	}

	public UUID getCdmVoc(){
		return uuidCdmVoc;
	}
}
