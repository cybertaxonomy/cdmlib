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

import java.util.*;
import javax.persistence.*;

/**
 * workaround for enumerations
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@Entity
public class Representation extends LanguageString {
	public Representation(String text, String label, Language lang) {
		super(text, lang);
		this.label = label;
	}

	static Logger logger = Logger.getLogger(Representation.class);
	private String label;
	private String abbreviatedLabel;


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
	public String getText(){
		return super.getText();
	}
	protected void setText(String text) {
		super.setText(text);
	}
	public String getDescription(){
		return super.getText();
	}
	protected void setDescription(String text) {
		super.setText(text);
	}
		
}