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
import eu.etaxonomy.cdm.model.common.MultilanguageArray;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@Entity
public class TextData extends FeatureBase {
	static Logger logger = Logger.getLogger(TextData.class);
	private MultilanguageArray texts;
	private TextFormat format;


	/**
	 * @return
	 * returns distinct list of languages/translations that exist for this text
	 */
	public ArrayList<Language> getLanguages(){
		// for ls in this.MultilanguageArray
		return null;
	}

	public MultilanguageArray getTexts() {
		return texts;
	}

	public void addText(String text, Language lang) {
		this.texts.addText(text, lang);
	}
	public void removeText(LanguageString ls) {
		this.texts.remove(ls);
	}

}