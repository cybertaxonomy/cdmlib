// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class DwcaMetaRecord  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaMetaRecord.class);
	
	private String fileLocation;
	private String rowType;
	
	private boolean isCore;
	private int currentIndex = 0;
	boolean isId;
	
	private List<FieldEntry> fieldList = new ArrayList<DwcaMetaRecord.FieldEntry>();
	
	public DwcaMetaRecord(boolean isCore, String fileLocation, String rowType){
		FieldEntry idEntry = new FieldEntry();
		idEntry.index = currentIndex++;
		idEntry.elementName = isCore ? "id" : "coreid";
		fieldList.add(idEntry);
		this.isCore = isCore;
		this.fileLocation = fileLocation;
		this.setRowType(rowType);
	}


	protected class FieldEntry{
		int index;
		String term = "";
		String elementName = "field";
	}
	
	public void addFieldEntry(String term){
		FieldEntry fieldEntry = new FieldEntry();
		fieldEntry.index = currentIndex++;
		fieldEntry.term = term;
		this.fieldList.add(fieldEntry);
	}
	
	public List<FieldEntry> getEntries(){
		return fieldList;
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

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.fileLocation;
	}
	
	

}
