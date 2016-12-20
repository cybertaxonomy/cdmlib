/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * The role of the descriptive system of type {@link FeatureTree}.<BR>
 * A descriptive systeme may play different roles within a working set. 
 * The roles may be 
 * <li>
 * <ul>Default</ul>
 * <ul>Interactive Identification</ul>
 * <ul>Natural Language Reporting</ul>
 * <ul>Filtering</ul>
 * <ul>Description Editing</ul>
 * <ul>Terminology Reporting</ul>
 * <ul>Management</ul>
 * </li> 
 * 
 * Compare SDD: .../CharacterTree/DesignedFor/Role
 * 
 * @author a.mueller
 * @created 04-Mar-2011
 */
@XmlEnum
public enum DescriptiveSystemRole {
	//0 ; if default is set, this descriptive system is taken for all purpose where no more specific 
	//system is defined
	@XmlEnumValue("Default")  
	Default("Default"),
	//1 ; used for interactive keys
	@XmlEnumValue("InteractiveIdentification")  
	InteractiveIdentification("Interactive Identification"),
	//2 ; used for natural language reporting
	@XmlEnumValue("NaturalLanguageReporting")  
	NaturalLanguageReporting("Natural Language Reporting"),
	//3 ; used for fitering
	@XmlEnumValue("Filtering")  
	Filtering("Filtering"),
	//4 ; used for editing descriptions
	@XmlEnumValue("DescriptionEditing")  
	DescriptionEditing("Description Editing"),
	//5 ; used for reporting of the descriptive terminology
	@XmlEnumValue("TerminologyReporting")  
	TerminologyReporting("Terminology Reporting"),
	//6 ; used for management
	@XmlEnumValue("Management")  
	Management("Management"),
	;
	
	private String readableString;
	
	private DescriptiveSystemRole(String readableString ){
		this.readableString = readableString;
	}

	@Transient
	public String getMessage(){
		return getMessage(Language.DEFAULT());
	}
	public String getMessage(Language language){
		//TODO make multi-lingual
		return readableString;
	}
}
