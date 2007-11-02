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
 * @created 02-Nov-2007 19:36:38
 */
@Entity
public class TextData extends FeatureBase {
	static Logger logger = Logger.getLogger(TextData.class);

	private java.util.ArrayList m_Paragraph;
	private Language language;

	public java.util.ArrayList getM_Paragraph(){
		return m_Paragraph;
	}

	/**
	 * 
	 * @param m_Paragraph
	 */
	public void setM_Paragraph(java.util.ArrayList m_Paragraph){
		;
	}

	public Language getLanguage(){
		return language;
	}

	/**
	 * 
	 * @param language
	 */
	public void setLanguage(Language language){
		;
	}

}