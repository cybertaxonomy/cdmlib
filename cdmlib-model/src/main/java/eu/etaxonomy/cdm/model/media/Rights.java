/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Typically, rights information includes a statement about various property
 * rights associated with the resource, including intellectual property rights.
 * http://purl.org/dc/elements/1.1/rights  http://dublincore.org/documents/dcmi-
 * terms/
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rights", propOrder = {
    "uri",
    "abbreviatedText",
    "type",
    "agent"
})
@XmlRootElement(name = "Rights")
@Entity
public class Rights extends LanguageStringBase {
	private static final long serialVersionUID = 4920749849951432284L;
	private static final Logger logger = Logger.getLogger(Rights.class);
	
	//external location of copyright text
	@XmlElement(name = "URI")
	private String uri;
	
	@XmlElement(name = "AbbreviatedText")
	private String abbreviatedText;
	
	@XmlElement(name = "Type")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private RightsTerm type;
	
	// owner etc as defined by the rightstype
	@XmlElement(name = "Agent")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Agent agent;

	
	/**
	 * Factory method
	 * @return
	 */
	public static Rights NewInstance() {
		logger.debug("NewInstance");
		return new Rights();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static Rights NewInstance(String text, Language language) {
		return new Rights(text, language);
	}
	
	/**
	 * Default Constructor
	 */
	protected Rights() {
		super();
	}

	/**
	 * Constructor
	 */
	protected Rights(String text, Language language) {
		super(text, language);
	}

	
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
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


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getAgent() {
		return agent;
	}
	public void setAgent(Agent agent) {
		this.agent = agent;
	}

}