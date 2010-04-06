/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.strategy.cache.reference.BookDefaultCacheStrategy;

public interface IBook extends IPrintedUnitBase, INomenclaturalReference{
	
	public void setEdition(String edition);
	
	public String getEdition();
	
	public String getIsbn();
	
	public void setIsbn(String isbn);
	void setCacheStrategy(BookDefaultCacheStrategy cacheStrategy);
	
	


}
