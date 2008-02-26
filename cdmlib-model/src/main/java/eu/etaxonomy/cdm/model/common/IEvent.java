package eu.etaxonomy.cdm.model.common;

import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Agent;

public interface IEvent {

	public abstract TimePeriod getTimeperiod();

	public abstract void setTimeperiod(TimePeriod timeperiod);

	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public abstract Agent getActor();

	public abstract void setActor(Agent actor);

}