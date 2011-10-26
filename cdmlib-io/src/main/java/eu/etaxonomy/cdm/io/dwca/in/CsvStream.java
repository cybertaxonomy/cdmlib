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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.jaxb.ArchiveEntryBase;
import eu.etaxonomy.cdm.io.dwca.jaxb.Core;
import eu.etaxonomy.cdm.io.dwca.jaxb.Extension;
import eu.etaxonomy.cdm.io.dwca.jaxb.Field;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author a.mueller
 * @date 17.10.2011
 *
 */
public class CsvStream {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CsvStream.class);

	private CSVReader csvReader;
	private ArchiveEntryBase archiveEntry;
	
	public CsvStream (CSVReader csvReader, ArchiveEntryBase archiveEntry){
		this.csvReader = csvReader;
		this.archiveEntry = archiveEntry;
	}
	
	public Map<String, String> read(){
		Map<String, String> result = new HashMap<String, String>();
		try {
			String[] next = csvReader.readNext();
			if (next == null){
				return null;
			}
			for (Field field : archiveEntry.getField()){
				int index = field.getIndex();
				if (index > next.length -1){
					throw new RuntimeException("Missing value for archive entry " + field.getTerm());
				}
				String value = next[index];
				String term = field.getTerm();
				result.put(term, value);
			}
			if (archiveEntry instanceof Core){
				Core core = (Core)archiveEntry;
				result.put("id", next[core.getId().getIndex()]);
			}else if(archiveEntry instanceof Extension){
				Extension extension = (Extension)archiveEntry;
				result.put("coreId", next[extension.getCoreid().getIndex()]);
			}else{
				throw new RuntimeException("Unhandled achiveEntry type");
			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = null;
		}
		return result;
		
	}
	
}
