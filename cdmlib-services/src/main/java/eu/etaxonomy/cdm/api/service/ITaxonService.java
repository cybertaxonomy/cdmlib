/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


public interface ITaxonService extends IIdentifiableEntityService<TaxonBase>{
	
	/** */
	public abstract TaxonBase getTaxonByUuid(UUID uuid);

	/** save a taxon and return its UUID**/
	public abstract UUID saveTaxon(TaxonBase taxon);

	/** save a collection of  taxa and return its UUID**/
	public abstract Map<UUID, TaxonBase> saveTaxonAll(Collection<TaxonBase> taxonCollection);

	
	/** delete a taxon and return its UUID**/
	public abstract UUID removeTaxon(TaxonBase taxon);
	
	/** */
	public abstract List<Taxon> getRootTaxa(ReferenceBase sec);
	
	/** */
	public abstract List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec);
		
	
}
