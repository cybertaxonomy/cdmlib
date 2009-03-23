/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Person;

/**
 * @author n.hoffmann
 * @created 26.09.2008
 * @version 1.0
 */
public class AnnotationElementSTO extends BaseTO {
	private static final Logger logger = Logger.getLogger(AnnotationElementSTO.class);
	
	private String text;
	private Person commentator;

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @param commentator
	 */
	public void setCommentator(Person commentator) {
		this.commentator = commentator;
	}
	
	/**
	 * @return
	 */
	public Person getCommentator(){
		return commentator;
	}
}
