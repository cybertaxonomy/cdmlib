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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import eu.etaxonomy.cdm.model.common.Language;


/**
 * @author a.mueller
 * @created 20.09.2009
 * @version 1.0
 */
@XmlEnum
public enum ReferenceType {
	@XmlEnumValue("Article")
	Article("Article"),
	@XmlEnumValue("Book")
	Book("Book"),
	@XmlEnumValue("Book Section")
	BookSection("Book Section"),
	@XmlEnumValue("CD or DVD")
	CdDvd("CD or DVD"),
	@XmlEnumValue("Database")
	Database("Database"),
	@XmlEnumValue("Generic")
	Generic("Generic"),
	@XmlEnumValue("Inproceedings")
	InProceedings("Inproceedings"),
	@XmlEnumValue("Journal")
	Journal("Journal"),
	@XmlEnumValue("Map")
	Map("Map"),
	@XmlEnumValue("Patent")
	Patent("Patent"),
	@XmlEnumValue("Personal Communication")
	PersonalCommunication("Personal Communication"),
	@XmlEnumValue("Print Series")
	PrintSeries("Print Series"),
	@XmlEnumValue("Proceedings")
	Proceedings("Proceedings"),
	@XmlEnumValue("Report")
	Report("Report"),
	@XmlEnumValue("Thesis")
	Thesis("Thesis"),
	@XmlEnumValue("Web Page")
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
