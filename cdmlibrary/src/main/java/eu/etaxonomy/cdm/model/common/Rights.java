/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * Typically, rights information includes a statement about various property
 * rights associated with the resource, including intellectual property rights.
 * http://purl.org/dc/elements/1.1/rights
 * 
 * http://dublincore.org/documents/dcmi-terms/
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:15
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
	private RightsTerm type;
	private Language language;

	public RightsTerm getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(RightsTerm newVal){
		type = newVal;
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

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUri(String newVal){
		uri = newVal;
	}

	public String getStatement(){
		return statement;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setStatement(String newVal){
		statement = newVal;
	}

	public String getAbbreviatedStatement(){
		return abbreviatedStatement;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAbbreviatedStatement(String newVal){
		abbreviatedStatement = newVal;
	}

}