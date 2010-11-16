// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

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
	
	public boolean isDoNamesWithoutTaxa();
	
	public void setDoNamesWithoutTaxa(boolean doNamesWithoutTaxa);

	public boolean isDoTaxaByCommonNames();
	
	public void setDoTaxaByCommonNames(boolean doTaxaByCommonNames);

	public String getSearchString();
	
	public void setSearchString(String searchString);
	
	public MatchMode getMatchMode();

	public void setMatchMode(MatchMode matchMode);

	public TaxonomicTree getTaxonomicTree();
	
	public void setTaxonomicTree(TaxonomicTree taxonomicTree);
	
	public Integer getPageSize();

	public void setPageSize(Integer pageSize);

	public Integer getPageNumber();
	
	public void setPageNumber(Integer pageNumber);
	
	public Set<NamedArea> getNamedAreas();

	public void setNamedAreas(Set<NamedArea> areas);
	
	public List<String> getTaxonPropertyPath();

	public void setTaxonPropertyPath(List<String> taxonPropertyPath);
	
	public List<String> getTaxonNamePropertyPath();

	public void setTaxonNamePropertyPath(List<String> taxonNamePropertyPath);

	public List<String> getCommonNamePropertyPath();

	public void setCommonNamePropertyPath(List<String> commonNamePropertyPath);
	
	@Deprecated
	public Reference getSec();
	@Deprecated
	public void setSec(Reference sec);

	public List<String> getSynonymPropertyPath();
	
	public void setSynonymPropertyPath(List<String> synonymPropertyPath);
}
