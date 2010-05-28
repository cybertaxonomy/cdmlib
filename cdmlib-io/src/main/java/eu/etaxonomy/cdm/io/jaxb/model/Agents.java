package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Agents", propOrder = {
	    "agents"
})
@XmlRootElement(name = "Agents")
public class Agents extends CdmListWrapper<AgentBase> {
	
	@XmlElements({             
        @XmlElement(name = "Team", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Team.class),
        @XmlElement(name = "Institution", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Institution.class),
        @XmlElement(name = "Person", namespace = "http://etaxonomy.eu/cdm/model/agent/1.0", type = Person.class)
    })
	protected List<AgentBase> agents = new ArrayList<AgentBase>();

	@Override
	public List<AgentBase> getElements() {
		return agents;
	}

	@Override
	public void setElements(List<AgentBase> elements) {
		this.agents = elements;
	}

}
