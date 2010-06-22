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

public interface IArticle extends IVolumeReference, IReferenceBase,  INomenclaturalReference{
	
	public void setSeries(String series);
	
	public String getSeries();

	
	/**
	 * Same as {@link #getJournal()}.
	 * @deprecated use {@link #getJournal()} instead
	 * @return
	 */
	@Deprecated
	public IJournal getInJournal();
	
	/**
	 * Same as {@link #setJournal(IJournal)}
	 * @deprecated use {@link #setJournal(IJournal)} instead
	 * @param journal
	 */
	@Deprecated()
	public void setInJournal(IJournal journal);

	
	/**
	 * Returns the articles journal.
	 * @return
	 */
	public IJournal getJournal();
	
	
	/**
	 * Sets the articles journal 
	 * @param journal
	 */
	public void setJournal(IJournal journal);

	
	void setCacheStrategy(ArticleDefaultCacheStrategy cacheStrategy);
}
