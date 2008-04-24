/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.List;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;

/**
 * @author a.mueller
 *
 */
public interface ITaxonDao extends IIdentifiableDao<TaxonBase>, ITitledDao<TaxonBase> {
	
	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec);
	
	public List<Taxon> getRootTaxa(ReferenceBase sec);

	/**
	 * @param pagesize max maximum number of returned taxa
	 * @param page page to start, with 0 being first page 
	 * @return
	 */
	public List<TaxonBase> getAllTaxa(Integer pagesize, Integer page);
}
