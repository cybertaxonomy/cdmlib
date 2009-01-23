/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service.config;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.babadshanjan
 * @created 20.01.2009
 * @version 1.0
 */
public interface ITaxonServiceConfigurator {

	public boolean isDoTaxa();
	
	public void setDoTaxa(boolean doTaxa);

	public boolean isDoSynonyms();
	
	public void setDoSynonyms(boolean doSynonyms);
	
	public String getSearchString();
	
	public void setSearchString(String searchString);
	
	public ReferenceBase getSec();
	
	public void setReferenceBase(ReferenceBase sec);
	
	public boolean isDoNamesWithoutTaxa();
	
	public void setDoNamesWithoutTaxa(boolean doNamesWithoutTaxa);

	public Integer getPageSize();

	public void setPageSize(Integer pageSize);

	public Integer getPageNumber();
	
	public void setPageNumber(Integer pageNumber);
}
