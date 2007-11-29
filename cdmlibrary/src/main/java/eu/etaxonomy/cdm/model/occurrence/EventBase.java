package eu.etaxonomy.cdm.model.occurrence;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

@Entity
public abstract class EventBase extends VersionableEntity {
	static Logger logger = Logger.getLogger(EventBase.class);

	private TimePeriod timeperiod;
	private Agent actor;
	private String description;
	
	
	public TimePeriod getTimeperiod() {
		return timeperiod;
	}
	public void setTimeperiod(TimePeriod timeperiod) {
		this.timeperiod = timeperiod;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getActor() {
		return actor;
	}
	public void setActor(Agent actor) {
		this.actor = actor;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
