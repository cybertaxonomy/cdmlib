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
	
	private MultilanguageSet multilanguageText;
	private TextFormat format;
	
	public static TextData NewInstance(){
		return new TextData();
	}

	public static TextData NewInstance(String text, Language language, TextFormat format){
		TextData result =  new TextData();
		result.putText(text, language);
		result.setFormat(format);
		return result;
	}
	
	/**
	 * Constructor
	 */
	public TextData(){
		super();
		initTextSet();
	}

	/**
	 * @return
	 */
	public MultilanguageSet getMultilanguageText() {
		initTextSet();
		return multilanguageText;
	}
	@Transient 
	public String getText(Language language) {
		initTextSet();
		return multilanguageText.getText(language);
	}
	protected void setMultilanguageText(MultilanguageSet texts) {
		this.multilanguageText = texts;
	}
	public String putText(String text, Language language) {
		initTextSet();
		LanguageString result = this.multilanguageText.put(text, language);
		return (result == null ? null : result.getText());
	}
	public LanguageString putText(LanguageString languageString) {
		initTextSet();
		return this.multilanguageText.put(languageString);
	}
	public LanguageString removeText(Language language) {
		initTextSet();
		return this.multilanguageText.remove(language);
	}
	
	private void initTextSet(){
		if (multilanguageText == null){
			multilanguageText = MultilanguageSet.NewInstance();
		}
	}
	
	public int countLanguages(){
		initTextSet();
		return multilanguageText.size();
	}
	

	@ManyToOne
	public TextFormat getFormat() {
		return format;
	}
	public void setFormat(TextFormat format) {
		this.format = format;
	}

}