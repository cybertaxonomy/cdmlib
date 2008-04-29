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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@Entity
public class TextData extends DescriptionElementBase {
	static Logger logger = Logger.getLogger(TextData.class);
	
	private MultilanguageSet multilanguageStringMap;
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
	@CollectionOfElements(targetElement = LanguageString.class)
	//@OneToMany(fetch= FetchType.EAGER)
	@MapKey(name="language")
    @Cascade({CascadeType.SAVE_UPDATE})
	public MultilanguageSet getMultilanguageText() {
		initTextSet();
		return multilanguageStringMap;
	}
	@Transient 
	public String getText(Language language) {
		initTextSet();
		return multilanguageStringMap.getText(language);
	}
	protected void setMultilanguageText(MultilanguageSet texts) {
		this.multilanguageStringMap = texts;
	}
	public String putText(String text, Language language) {
		initTextSet();
		String result = this.multilanguageStringMap.put(language , text);
		return (result == null ? null : result);
	}
	public String putText(LanguageString languageString) {
		initTextSet();
		return this.multilanguageStringMap.add(languageString);
	}
	public String removeText(Language language) {
		initTextSet();
		return this.multilanguageStringMap.remove(language);
	}
	
	private void initTextSet(){
		if (multilanguageStringMap == null){
			multilanguageStringMap = MultilanguageSet.NewInstance();
		}
	}
	
	public int countLanguages(){
		initTextSet();
		return multilanguageStringMap.size();
	}
	

	@ManyToOne
	public TextFormat getFormat() {
		return format;
	}
	public void setFormat(TextFormat format) {
		this.format = format;
	}

}