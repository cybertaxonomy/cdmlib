/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv.redlist.demo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class CsvDemoMetaDataRecord  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CsvDemoMetaDataRecord.class);
	
	private String fileLocation;
	private String rowType;
	
	private boolean isCore;
	private int currentIndex = 0;
	private boolean isMetaData = false;  //is this record about meta data (should be true for MetaData and EML)
	
	private int count = 0;
	
	private List<FieldEntry> fieldEntryList = new ArrayList<CsvDemoMetaDataRecord.FieldEntry>();
	protected List<String> fieldList = new ArrayList<String>();
	
	
	public CsvDemoMetaDataRecord(boolean isCore, String fileLocation, String rowType){
		FieldEntry idEntry = new FieldEntry();
		idEntry.index = currentIndex++;
		idEntry.elementName = isCore ? "id" : "coreid";
		fieldEntryList.add(idEntry);
		this.isCore = isCore;
		this.fileLocation = fileLocation;
		this.setRowType(rowType);
	}


	protected class FieldEntry{
		int index;
		URI term = null;
		String defaultValue = null;
		String elementName = "field";
	}
	
	public void addFieldEntry(URI term, String defaultValue){
		FieldEntry fieldEntry = new FieldEntry();
		fieldEntry.index = currentIndex++;
		fieldEntry.term = term;
		fieldEntry.defaultValue = defaultValue;
		this.fieldEntryList.add(fieldEntry);
	}
	
	//TODO needed?
//	public abstract List<String> getHeaderList();
	
//	public List<URI> getTermList(){
//		List<URI> result = new ArrayList<URI>();
//		for (String key : fieldList){
//			URI uri = knownFields.get(key);
//			if (uri != null){
//				result.add(uri);
//			}else{
//				String message = "Unknown 'known field key' " + key;
//				logger.warn(message);
//			}
//		}
//		return result;
//	}
	
	public boolean hasEntries(){
		return fieldEntryList.size() > 1;
	}
	
	public List<FieldEntry> getEntries(){
		return fieldEntryList;
	}
	
	
	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public boolean isCore() {
		return isCore;
	}

	public void setCore(boolean isCore) {
		this.isCore = isCore;
	}

	public void setRowType(String rowType) {
		this.rowType = rowType;
	}

	public String getRowType() {
		return rowType;
	}

	public int inc(){
		return ++count;
	}

	public void setMetaData(boolean isMetaData) {
		this.isMetaData = isMetaData;
	}

	public boolean isMetaData() {
		return isMetaData;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.fileLocation;
	}

	

}
