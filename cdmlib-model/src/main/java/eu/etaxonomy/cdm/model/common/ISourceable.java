/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

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