/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.strategy.cache.reference.old.ArticleDefaultCacheStrategy;

/**
 * This interface represents articles in a {@link IJournal journal}. An article is an independent
 * piece of prose written by an {@link TeamOrPersonBase author (team)} which is published among
 * other articles within a particular issue of a journal.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * terms (from PublicationTypeTerm): <ul>
 * <li> "JournalArticle"
 * <li> "NewspaperArticle"
 * <li> "MagazineArticle"
 * </ul>
 */
public interface IArticle extends ISection, IVolumeReference, INomenclaturalReference{
	
	/**
	 * Returns the series information for this article
	 */
	public String getSeriesPart();

	/**
	 * Sets the series information for this article
	 * @param series
	 */
	public void setSeriesPart(String series);
	
	
	
	/**
	 * Returns this articles journal.
	 * @return
	 */
	public IJournal getInJournal();
	
	
	/**
	 * Sets this articles journal 
	 * @param journal
	 */
	public void setInJournal(IJournal journal);

	
	void setCacheStrategy(ArticleDefaultCacheStrategy cacheStrategy);
}
