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
	private ArrayList<Paragraph> paragraphs;
	private Language language;


	public Language getLanguage(){
		return this.language;
	}

	/**
	 * 
	 * @param language    language
	 */
	public void setLanguage(Language language){
		this.language = language;
	}

	public ArrayList<Paragraph> getParagraphs() {
		return paragraphs;
	}

	public void addParagraph(Paragraph paragraph) {
		this.paragraphs.add(paragraph);
	}
	public void removeParagraph(Paragraph paragraph) {
		this.paragraphs.remove(paragraph);
	}

}