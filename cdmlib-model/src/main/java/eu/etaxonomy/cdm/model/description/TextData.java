/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;

import org.apache.log4j.Logger;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@Entity
public class TextData extends FeatureBase {
	static Logger logger = Logger.getLogger(TextData.class);
	private MultilanguageSet texts;
	private TextFormat format;
	
	public static TextData NewInstance(){
		return new TextData();
	}
	
	public TextData(){
		super();
		initTextSet();
	}


	public MultilanguageSet getTexts() {
		initTextSet();
		return texts;
	}
	private void setTexts(MultilanguageSet texts) {
		this.texts = texts;
	}
	public void addText(String text, Language lang) {
		initTextSet();
		this.texts.add(text, lang);
	}
	public void addText(LanguageString text) {
		initTextSet();
		this.texts.add(text);
	}
	public void removeText(Language lang) {
		initTextSet();
		this.texts.remove(lang);
	}
	
	private void initTextSet(){
		if (texts == null){
			texts = new MultilanguageSet();
		}
	}
	

	@ManyToOne
	public TextFormat getFormat() {
		return format;
	}
	public void setFormat(TextFormat format) {
		this.format = format;
	}

}