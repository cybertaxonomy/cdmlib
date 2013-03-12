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

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IIoObservable;
import eu.etaxonomy.cdm.io.common.ObservableBase;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.dwca.jaxb.ArchiveEntryBase;
import eu.etaxonomy.cdm.io.dwca.jaxb.Core;
import eu.etaxonomy.cdm.io.dwca.jaxb.Extension;
import eu.etaxonomy.cdm.io.dwca.jaxb.Field;

/**
 * @author a.mueller
 * @date 17.10.2011
 *
 */
public class CsvStream extends ObservableBase implements INamespaceReader<CsvStreamItem>, IIoObservable{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CsvStream.class);

	private CSVReader csvReader;
	private ArchiveEntryBase archiveEntry;
	private TermUri term;
	private int line = 0;
	
	private CsvStreamItem next;
	
	public CsvStream (CSVReader csvReader, ArchiveEntryBase archiveEntry, int startLine){
		this.csvReader = csvReader;
		this.archiveEntry = archiveEntry;
		String rowType = archiveEntry.getRowType();
		term = TermUri.valueOfUriString(rowType);
		line = startLine;
		
		//FIXME what if null?
	}
	
	public boolean hasNext(){
		if (next != null){
			return true;
		}else{
			next = readMe();
			return (next != null);
		}
	}
	
	public CsvStreamItem read(){
//		line++;
		return readMe();
	}
	
	private CsvStreamItem readMe(){
		CsvStreamItem resultItem;
		Map<String, String> resultMap;
		if (next != null){
			resultItem = next;
			next = null;
			return resultItem;
		}else{
			resultMap = new HashMap<String, String>();
			try {
				String[] next = csvReader.readNext();
				line++;
				resultItem = new CsvStreamItem(term, null, this, line);
				if (next == null){
					return null;
				}
				for (Field field : archiveEntry.getField()){
				        int index = field.getIndex();
				        if (index > next.length -1){
			        		int missingFields = archiveEntry.getField().size() -  (next.length -1);
			        		String message = "Missing value for archive entry %s %s in line %d of input %s.";
			                String msgMoreLines = missingFields <= 1? "": "and %d more fields" ;
			        		msgMoreLines = String.format(msgMoreLines,(missingFields - 1) );
			                message = String.format(message, field.getTerm(), msgMoreLines, line, archiveEntry.getFiles().getLocation());

			                if (countObservers() == 0){
			                        throw new RuntimeException(message);
			                } else {
			                        StringBuilder messageSB = new StringBuilder(message);
			                        messageSB.append(" Line is only partly imported. Csv-Array is [");// + next.toString();

			                        for(int i=0;i<next.length;i++) {
			                                messageSB.append(next[i]);
			                                if(i < next.length-1){
			                                        messageSB.append(",");
			                                } else {
			                                        messageSB.append("]");
			                                }
			                        }
			                        message = messageSB.toString();
			                        fireWarningEvent(message, resultItem.getLocation(), 4);
			                        break;
			                }
				        }
					String value = next[index];
					String term = field.getTerm();
					resultMap.put(term, value);
				}
				if (archiveEntry instanceof Core){
					Core core = (Core)archiveEntry;
					resultMap.put("id", next[core.getId().getIndex()]);
				}else if(archiveEntry instanceof Extension){
					Extension extension = (Extension)archiveEntry;
					resultMap.put("coreId", next[extension.getCoreid().getIndex()]);
				}else{
					throw new RuntimeException("Unhandled archiveEntry type");
				}
	
				
			} catch (IOException e) {
				//TODO handle as event
				throw new RuntimeException(e);
			}
			resultItem.map = resultMap;
			if (resultItem.map == null){
				return null;
			}else {
				return resultItem;
			}
			
		}
	}
	
	public int getLine(){
		return line;
	}
	
	
	/**
	 * @return the term
	 */
	public TermUri getTerm() {
		return term;
	}

	public String getFilesLocation() {
		return this.archiveEntry.getFiles().getLocation();
	}
	
//******************************** TO STRING **************************************/
	
	@Override
	public String toString(){
		if (archiveEntry == null){
			return super.toString();
		}else{
			return "CsvStream for " + CdmUtils.Nz(archiveEntry.getRowType() + " at line " + line);
		}
	}

	
	
}
