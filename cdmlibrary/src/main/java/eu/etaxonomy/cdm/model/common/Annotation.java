/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.agent.Person;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:10
 */
@Entity
public class Annotation extends LanguageString {
	public Annotation(String text, Language lang) {
		super(text, lang);
	}

	static Logger logger = Logger.getLogger(Annotation.class);
	//Human annotation
	private Person commentator;

	public Person getCommentator(){
		return this.commentator;
	}

	/**
	 * 
	 * @param commentator    commentator
	 */
	public void setCommentator(Person commentator){
		this.commentator = commentator;
	}

}