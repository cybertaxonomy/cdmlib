package eu.etaxonomy.cdm.strategy.cache;


import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

public interface INomenclaturalAuthorCacheStrategy<T extends TeamOrPersonBase> extends IIdentifiableEntityCacheStrategy<T> {
	
	/**
	 * returns the composed name string without author or year
	 * @param object
	 * @return
	 */
	public String getNomenclaturalTitle(T object);


}
