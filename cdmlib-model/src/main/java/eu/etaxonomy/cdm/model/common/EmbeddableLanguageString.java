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

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:32
 */
@MappedSuperclass
public class EmbeddableLanguageString  extends VersionableEntity{
	static Logger logger = Logger.getLogger(EmbeddableLanguageString.class);
	protected String text;
	protected Language language;

	public static EmbeddableLanguageString NewInstance(String text, Language language){
		return new EmbeddableLanguageString(text, language);
	}
	
	protected EmbeddableLanguageString() {
		super();
	}

	protected EmbeddableLanguageString(String text, Language language) {
		super();
		this.setLanguage(language);
		this.setText(text);
		
	}
	
	@ManyToOne
	//@Cascade({CascadeType.SAVE_UPDATE})
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