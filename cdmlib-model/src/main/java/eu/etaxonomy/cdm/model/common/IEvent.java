package eu.etaxonomy.cdm.model.common;

import eu.etaxonomy.cdm.model.agent.AgentBase;

public interface IEvent {

	public abstract TimePeriod getTimeperiod();

	public abstract void setTimeperiod(TimePeriod timeperiod);

	public abstract AgentBase getActor();

	public abstract void setActor(AgentBase actor);

}