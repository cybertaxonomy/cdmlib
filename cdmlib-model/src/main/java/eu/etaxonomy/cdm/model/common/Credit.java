/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.agent.AgentBase;

/**
 * @author a.mueller
 * @created 23.03.2009
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Credit")
@Entity
@Audited
public class Credit extends LanguageStringBase implements Cloneable{
	private static final long serialVersionUID = 5763391127298427701L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Credit.class);

// ********************** FACTORY **********************************************/

    public static Credit NewInstance(AgentBase agent, String text){
        return NewInstance(agent, text, null, Language.DEFAULT());
    }

    public static Credit NewInstance(AgentBase agent, String text, String abbreviatedText, Language language){
        Credit result = new Credit(text, language);
        result.setAgent(agent);
        result.setAbbreviatedText(abbreviatedText);
        return result;
    }

// ********************** FACTORY **********************************************/

	// owner etc as defined by the rightstype
	@XmlElement(name = "Agent")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private AgentBase<?> agent;

	@XmlElement(name = "AbbreviatedText")
	private String abbreviatedText;

// ********************** CONSTRUCTOR **********************************************/

	protected Credit(){
		super();
	}

	protected Credit(String text, Language language){
		super(text, language);
	}

//*********************** GETTER /SETTER *****************************/

	/**
	 * @return the agent
	 */
	public AgentBase getAgent() {
		return agent;
	}
	public void setAgent(AgentBase agent) {
		this.agent = agent;
	}


	/**
	 * @return the abbreviatedText
	 */
	public String getAbbreviatedText() {
		return abbreviatedText;
	}
	public void setAbbreviatedText(String abbreviatedText) {
		this.abbreviatedText = abbreviatedText;
	}

//************************* CLONE **************************/

	@Override
	public Object clone() throws CloneNotSupportedException{
		Credit result = (Credit)super.clone();
		//no changes to: agent
		return result;
	}

    @Override
    public String toString() {
        if (StringUtils.isNotBlank(this.abbreviatedText)){
            return this.abbreviatedText;
        }else if (StringUtils.isNotBlank(this.text)){
            return this.text;
        }else{
            return super.toString();
        }
    }

}
