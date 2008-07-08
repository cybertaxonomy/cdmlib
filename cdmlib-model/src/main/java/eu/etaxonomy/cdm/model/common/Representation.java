/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * workaround for enumerations
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Representation", propOrder = {
    "label",
    "abbreviatedLabel"
})
@XmlRootElement(name = "Representation")
@Entity
public class Representation extends LanguageStringBase {
	static Logger logger = Logger.getLogger(Representation.class);

    @XmlElement(name = "Label")
	private String label;
    
    @XmlElement(name = "AbbreviatedLabel")
	private String abbreviatedLabel;

	/**
	 * @param text
	 * @param label
	 * @param lang
	 * @return
	 */
	public static Representation NewInstance(String text, String label, String abbreviatedLabel, Language lang){
		return new Representation(text, label, abbreviatedLabel, lang);
	}
	
	public Representation() {
		super();
	}	

	/**
	 * text represents an explanation/declaration ('The name is illegitimate according to ICBN'); label a string identifier ('illegitimate name');
	 * abbreviatedLabel a shortened string for the label ('nom. illeg.') 
	 */
	public Representation(String text, String label, String abbreviatedLabel, Language language) {
		super(text, language);
		this.label = label;
		this.abbreviatedLabel = abbreviatedLabel;
	}

	
	public String getLabel(){
		return this.label;
	}
	public void setLabel(String label){
		this.label = label;
	}

	public String getAbbreviatedLabel(){
		return this.abbreviatedLabel;
	}
	public void setAbbreviatedLabel(String abbreviatedLabel){
		this.abbreviatedLabel = abbreviatedLabel;
	}
	
	@Transient
	public String getDescription(){
		return getText();
	}
	protected void setDescription(String text) {
		super.setText(text);
	}
	
	
	/* 
	 * Overrides super.getText() only to document that here the Text attribute
	 * should be used for a larger description of the label.
	 */
	@Override
	@Transient
	public String getText(){
		return super.getText();
	}
	
	
	public String toString(){
		// we dont need the language returned too, do we? 
		return getLabel();
//		if(getLanguage()==null || getLanguage().getLabel()==null){
//			return getLabel();
//		}else{
//			return getLabel()+"("+ getLanguage().getLabel()+")";
//		}
	}
}