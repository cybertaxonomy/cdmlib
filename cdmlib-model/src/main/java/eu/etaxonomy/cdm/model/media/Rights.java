/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;

/**
 * Typically, rights information includes a statement about various property
 * rights associated with the resource, including intellectual property rights.
 * http://purl.org/dc/elements/1.1/rights  http://dublincore.org/documents/dcmi-
 * terms/
 * @author m.doering
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
@Audited
@Table(name = "RightsInfo")  //to avoid conflicts with reserved database words
public class Rights extends LanguageStringBase implements Cloneable{
	private static final long serialVersionUID = 4920749849951432284L;
	@SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Rights.class);

	//external location of copyright text
	@XmlElement(name = "URI")
	@Field(analyze = Analyze.NO)
	@Type(type="uriUserType")
	private URI uri;

	@XmlElement(name = "AbbreviatedText")
	private String abbreviatedText;

	@XmlElement(name = "Type")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	private RightsType type;

	// owner etc as defined by the rights type
	@XmlElement(name = "Agent")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private AgentBase<?> agent;

// ******************** FACTORY ***********************/

	/**
	 * Factory method
	 * @return
	 */
	public static Rights NewInstance() {
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
     * Factory method
     * @return
     */
    public static Rights NewInstance(String text, Language language, RightsType type) {
        return new Rights(text, language, type);
    }

//*********************** CONSTRUCTOR *************************/

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

	/**
     * Constructor
     */
    protected Rights(String text, Language language, RightsType type) {
        super(text, language);
        this.setType(type);
    }

//*********************** GETTER /SETTER *****************************/

	public RightsType getType(){
		return this.type;
	}

	public void setType(RightsType type){
		this.type = type;
	}

	public URI getUri(){
		return this.uri;
	}

	public void setUri(URI uri){
		this.uri = uri;
	}

	public String getAbbreviatedText(){
		return this.abbreviatedText;
	}

	public void setAbbreviatedText(String abbreviatedStatement){
		this.abbreviatedText = abbreviatedStatement;
	}

	public AgentBase getAgent() {
		return agent;
	}

	public void setAgent(AgentBase agent) {
		this.agent = agent;
	}

//************************* CLONE **************************/

	@Override
	public Object clone() throws CloneNotSupportedException{
		Rights result = (Rights)super.clone();
		//no changes to: type, agent
		return result;
	}
}