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

/**
 * Typically, rights information includes a statement about various property
 * rights associated with the resource, including intellectual property rights.
 * http://purl.org/dc/elements/1.1/rights
 * 
 * http://dublincore.org/documents/dcmi-terms/
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:36
 */
public class Rights extends VersionableEntity {
	static Logger logger = Logger.getLogger(Rights.class);

	//external location of copyright text
	@Description("external location of copyright text")
	private String uri;
	@Description("")
	private String statement;
	@Description("")
	private String abbreviatedStatement;
	private Language language;
	private RightsTerm type;

	public RightsTerm getType(){
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(RightsTerm type){
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

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param uri
	 */
	public void setUri(String uri){
		;
	}

	public String getStatement(){
		return statement;
	}

	/**
	 * 
	 * @param statement
	 */
	public void setStatement(String statement){
		;
	}

	public String getAbbreviatedStatement(){
		return abbreviatedStatement;
	}

	/**
	 * 
	 * @param abbreviatedStatement
	 */
	public void setAbbreviatedStatement(String abbreviatedStatement){
		;
	}

}