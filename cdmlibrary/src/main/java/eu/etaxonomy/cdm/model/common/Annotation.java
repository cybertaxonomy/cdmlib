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

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:17:59
 */
public class Annotation extends VersionableEntity {
	static Logger logger = Logger.getLogger(Annotation.class);

	//Human annotation
	@Description("Human annotation")
	private String note;
	private Language language;
	private Person commentator;

	public Person getCommentator(){
		return commentator;
	}

	/**
	 * 
	 * @param commentator
	 */
	public void setCommentator(Person commentator){
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

	public String getNote(){
		return note;
	}

	/**
	 * 
	 * @param note
	 */
	public void setNote(String note){
		;
	}

}