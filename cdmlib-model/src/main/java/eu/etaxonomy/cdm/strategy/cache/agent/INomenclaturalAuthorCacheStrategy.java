package eu.etaxonomy.cdm.strategy.cache.agent;


import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public interface INomenclaturalAuthorCacheStrategy<T extends TeamOrPersonBase> extends IIdentifiableEntityCacheStrategy<T> {
	
	/**
	 * returns the composed name string without author or year
	 * @param object
	 * @return
	 */
	public String getNomenclaturalTitle(T object);


}
