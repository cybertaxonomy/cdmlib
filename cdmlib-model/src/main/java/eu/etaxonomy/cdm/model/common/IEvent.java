package eu.etaxonomy.cdm.model.common;

import eu.etaxonomy.cdm.model.agent.Agent;

public interface IEvent {

	public abstract TimePeriod getTimeperiod();

	public abstract void setTimeperiod(TimePeriod timeperiod);

	public abstract Agent getActor();

	public abstract void setActor(Agent actor);

}