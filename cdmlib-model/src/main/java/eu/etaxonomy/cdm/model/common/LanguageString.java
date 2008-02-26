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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:32
 */
@MappedSuperclass
public class LanguageString  extends VersionableEntity{
	static Logger logger = Logger.getLogger(LanguageString.class);
	protected String text;
	private Language language;

	public LanguageString() {
		super();
	}
	public LanguageString(String text, Language lang) {
		this.setLanguage(lang);
		this.setText(text);
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Language getLanguage(){
		return this.language;
	}
	public void setLanguage(Language language){
		this.language = language;
	}

	public String getText(){
		return this.text;
	}
	protected void setText(String text) {
		this.text = text;
	}
	
	@Transient
	public String getLanguageLabel(){
		return this.language.getRepresentation(Language.DEFAULT()).getLabel();
	}
	@Transient
	public String getLanguageLabel(Language lang){
		return this.language.getRepresentation(lang).getLabel();
	}
	@Transient
	public String getLanguageText(){
		return this.language.getRepresentation(Language.DEFAULT()).getLabel();
	}
	@Transient
	public String getLanguageText(Language lang){
		return this.language.getRepresentation(lang).getLabel();
	}
}