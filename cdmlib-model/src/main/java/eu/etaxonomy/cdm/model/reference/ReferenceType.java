// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.common.Language;


/**
 * @author a.mueller
 * @created 20.09.2009
 * @version 1.0
 */
public enum ReferenceType {
	Article("Article"),
	Book("Book"),
	BookSection("Book Section"),
	CdDvd("CD or DVD"),
	Database("Database"),
	Generic("Generic"),
	InProceedings("Inproceedings"),
	Journal("Journal"),
	Map("Map"),
	Patent("Patent"),
	PersonalCommunication("Personal Communication"),
	PrintSeries("Print Series"),
	Proceedings("Proceedings"),
	Report("Report"),
	Thesis("Thesis"),
	WebPage("Web Page");
	
	
	private String readableString;
	
	private ReferenceType(String defaultString){
		readableString = defaultString;
	}
	
	public String getMessage(){
		return getMessage(Language.DEFAULT());
	}
	public String getMessage(Language language){
		//TODO make multi-lingual
		return readableString;
	}
	
}
