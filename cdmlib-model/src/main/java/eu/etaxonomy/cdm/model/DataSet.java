package eu.etaxonomy.cdm.model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"agents",
		"agentData",
	    "terms",
	    "taxa"
})
@XmlRootElement(name = "DataSet", namespace = "http://etaxonomy.eu/cdm/model/1.0")
public class DataSet {

    @XmlElementWrapper(name = "Agents")
    @XmlElements({
        @XmlElement(name = "Team", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Team.class),
        @XmlElement(name = "Institution", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Institution.class),
        @XmlElement(name = "Person", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Person.class)
    })
    protected List<Agent> agents;
    
    @XmlElementWrapper(name = "AgentData")
    @XmlElements({
    @XmlElement(name = "Address", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Address.class),
    @XmlElement(name = "Contact", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Contact.class),
    @XmlElement(name = "Membership", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = InstitutionalMembership.class)
    })
    protected List<VersionableEntity> agentData;

    @XmlElementWrapper(name = "Terms")
    @XmlElements({
        @XmlElement(name = "Rank", namespace = "http://etaxonomy.eu/cdm/model/name/1.0", type = Rank.class)
    })
    protected List<TermBase> terms;

    @XmlElementWrapper(name = "Taxa")
    @XmlElement(name = "Taxon", namespace = "http://etaxonomy.eu/cdm/model/taxon/1.0")
    protected List<Taxon> taxa;
	
    public DataSet () {
    	
    }

    /**
     * Gets the value of the agents property.
     * 
     * @return
     *     possible object is
     *     {@link List<Agent> }
     *     
     */
    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Sets the value of the agents property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<Agent> }
     *     
     */
    public void setAgents(List<Agent> value) {
        this.agents = value;
    }

    /**
     * Gets the value of the agentData property.
     * 
     * @return
     *     possible object is
     *     {@link List<VersionableEntity> }
     *     
     */
    public List<VersionableEntity> getAgentData() {
        return agentData;
    }

    /**
     * Sets the value of the agentData property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<VersionableEntity> }
     *     
     */
    public void setAgentData(List<VersionableEntity> value) {
        this.agentData = value;
    }

    /**
     * Gets the value of the terms property.
     * 
     * @return
     *     possible object is
     *     {@link List<TermBase> }
     *     
     */
    
    public List<TermBase> getTerms() {
        return terms;
    }

    /**
     * Sets the value of the terms property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<TermBase> }
     *     
     */
    public void setTerms(List<TermBase> value) {
        this.terms = value;
    }

    /**
     * Gets the value of the taxa property.
     * 
     * @return
     *     possible object is
     *     {@link List<Taxon> }
     *     
     */
    public List<Taxon> getTaxa() {
        return taxa;
    }

    /**
     * Sets the value of the taxa property.
     * 
     * @param value
     *     allowed object is
     *     {@link List<Taxon> }
     *     
     */
    public void setTaxa(List<Taxon> value) {
        this.taxa = value;
    }
}
