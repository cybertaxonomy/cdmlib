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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * Typically, rights information includes a statement about various property
 * rights associated with the resource, including intellectual property rights.
 * http://purl.org/dc/elements/1.1/rights  http://dublincore.org/documents/dcmi-
 * terms/
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@Entity
public class Rights extends VersionableEntity {
	static Logger logger = Logger.getLogger(Rights.class);
	//external location of copyright text
	private String uri;
	private String statement;
	private String abbreviatedStatement;
	private Language language;
	private RightsTerm type;

	public RightsTerm getType(){
		return this.type;
	}

	/**
	 * 
	 * @param type    type
	 */
	public void setType(RightsTerm type){
		this.type = type;
	}

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

	public String getUri(){
		return this.uri;
	}

	/**
	 * 
	 * @param uri    uri
	 */
	public void setUri(String uri){
		this.uri = uri;
	}

	public String getStatement(){
		return this.statement;
	}

	/**
	 * 
	 * @param statement    statement
	 */
	public void setStatement(String statement){
		this.statement = statement;
	}

	public String getAbbreviatedStatement(){
		return this.abbreviatedStatement;
	}

	/**
	 * 
	 * @param abbreviatedStatement    abbreviatedStatement
	 */
	public void setAbbreviatedStatement(String abbreviatedStatement){
		this.abbreviatedStatement = abbreviatedStatement;
	}

}