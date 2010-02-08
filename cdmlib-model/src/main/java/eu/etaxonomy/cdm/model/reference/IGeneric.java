/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.strategy.cache.reference.GenericDefaultCacheStrategy;

public interface IGeneric extends IPublicationBase, INomenclaturalReference{

	public String getEditor();
	
	public void setEditor(String editor);
	
	public String getSeries();
	
	public void setSeries(String series);
	
	public String getPages();
	
	public void setPages(String pages);
	/**
	 * Returns the volume of a reference.
	 */
	public String getVolume();
	
	/**
	 * Sets the volume of the reference.
	 */
	public void setVolume(String volume);
	
	void setCacheStrategy(GenericDefaultCacheStrategy cacheStratefy);
	
}
