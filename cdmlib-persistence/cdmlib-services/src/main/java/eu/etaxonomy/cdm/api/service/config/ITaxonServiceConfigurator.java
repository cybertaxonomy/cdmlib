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

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 20.01.2009
 * @version 1.0
 */
public interface ITaxonServiceConfigurator<T extends TaxonBase> extends IIdentifiableEntityServiceConfigurator<T>{

	public boolean isDoTaxa();
	
	public void setDoTaxa(boolean doTaxa);

	public boolean isDoSynonyms();
	
	public void setDoSynonyms(boolean doSynonyms);
	
	public boolean isDoNamesWithoutTaxa();
	
	public void setDoNamesWithoutTaxa(boolean doNamesWithoutTaxa);

	public boolean isDoTaxaByCommonNames();
	
	public void setDoTaxaByCommonNames(boolean doTaxaByCommonNames);

	public Classification getClassification();
	
	public void setClassification(Classification classification);
	
	public Set<NamedArea> getNamedAreas();

	public void setNamedAreas(Set<NamedArea> areas);
	
	public List<String> getTaxonPropertyPath();

	public void setTaxonPropertyPath(List<String> taxonPropertyPath);
	
	public List<String> getTaxonNamePropertyPath();

	public void setTaxonNamePropertyPath(List<String> taxonNamePropertyPath);

	public List<String> getCommonNamePropertyPath();

	public void setCommonNamePropertyPath(List<String> commonNamePropertyPath);

	public List<String> getSynonymPropertyPath();
	
	public void setSynonymPropertyPath(List<String> synonymPropertyPath);
}
