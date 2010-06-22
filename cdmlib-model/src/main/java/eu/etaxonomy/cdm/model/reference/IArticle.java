/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.strategy.cache.reference.ArticleDefaultCacheStrategy;

public interface IArticle extends IVolumeReference, IReferenceBase, ISectionBase, INomenclaturalReference{
	
	public void setSeries(String series);
	
	public String getSeries();

	
	/**
	 * Returns the articles journal.
	 * @return
	 */
	public IJournal getInJournal();
	
	
	/**
	 * Sets the articles journal 
	 * @param journal
	 */
	public void setInJournal(IJournal journal);

	
	void setCacheStrategy(ArticleDefaultCacheStrategy cacheStrategy);
}
