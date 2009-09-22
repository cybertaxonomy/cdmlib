package eu.etaxonomy.cdm.model.common;

import java.util.Set;

 
public interface ISourceable<T extends IOriginalSource> {

	/**
	 * Returns the set of original sources (citations) for this sourceable object
	 * @return
	 */
	public Set<T> getSources();

	/**
	 * Adds a source to this object
	 * @param source
	 */
	public void addSource(T source);

	/**
	 * Removes a source from this object
	 * @param source
	 */
	public void removeSource(T source);

}