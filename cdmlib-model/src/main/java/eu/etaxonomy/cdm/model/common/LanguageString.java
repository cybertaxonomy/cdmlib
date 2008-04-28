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
@Entity
public class LanguageString  extends EmbeddableLanguageString{
	static Logger logger = Logger.getLogger(LanguageString.class);

	public static LanguageString NewInstance(String text, Language language){
		return new LanguageString(text, language);
	}
	
	protected LanguageString() {
		super();
	}
	
	protected LanguageString(String text, Language language) {
		super(text, language);
	}
	
}