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
public class Rights extends LanguageString {
	static Logger logger = Logger.getLogger(Rights.class);
	//external location of copyright text
	private String uri;
	private String abbreviatedText;
	private RightsTerm type;

	public Rights(String text, Language lang) {
		super(text, lang);
	}

	
	@ManyToOne
	public RightsTerm getType(){
		return this.type;
	}
	public void setType(RightsTerm type){
		this.type = type;
	}


	public String getUri(){
		return this.uri;
	}
	public void setUri(String uri){
		this.uri = uri;
	}


	public String getAbbreviatedText(){
		return this.abbreviatedText;
	}
	public void setAbbreviatedText(String abbreviatedStatement){
		this.abbreviatedText = abbreviatedStatement;
	}

}