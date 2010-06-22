/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.strategy.cache.reference.BookSectionDefaultCacheStrategy;


public interface IBookSection extends ISectionBase, INomenclaturalReference{

	/**
	 * Same as {@link #getBook()}.
	 * @deprecated use {@link #getBook()} instead
	 * @return
	 */
	@Deprecated
	public IBook getInBook();
	
	/**
	 * Same as {@link #setBook(IBook)}
	 * @deprecated use {@link #setBook(IBook)} instead
	 * @param book
	 */
	@Deprecated
	public void setInBook (IBook book);
	
	/**
	 * Returns the book sections book
	 * @return
	 */
	public IBook getBook();
	
	/**
	 * Sets the book sections book.
	 * @param book
	 */
	public void setBook (IBook book);

	
	
	void setCacheStrategy(BookSectionDefaultCacheStrategy cacheStratefy);

}
