/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.common;


import etaxonomy.cdm.model.agent.Person;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:34
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
	 * @param newVal
	 */
	public void setCommentator(Person newVal){
		commentator = newVal;
	}

	public Language getLanguage(){
		return language;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLanguage(Language newVal){
		language = newVal;
	}

	public String getNote(){
		return note;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNote(String newVal){
		note = newVal;
	}

}