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

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;

/**
 * @author a.mueller
 *
 */
public interface ITaxonDao extends IIdentifiableDao<TaxonBase>, ITitledDao<TaxonBase> {
	
	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec);
	
	/**
	 * Computes all Taxon instances that do not have a taxonomic parent and has at least one child.
	 * @return The List<Taxon> of root taxa.
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec);

	
	
	/**
	 * Computes all Taxon instances that do not have a taxonomic parent.
	 * @param sec The concept reference that the taxon belongs to
	 * @param cdmFetch TODO
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @return The List<Taxon> of root taxa.
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch,  Boolean onlyWithChildren);

	/**
	 * TODO necessary? 
	 * @param pagesize max maximum number of returned taxa
	 * @param page page to start, with 0 being first page 
	 * @return
	 */
	public List<TaxonBase> getAllTaxa(Integer pagesize, Integer page);
	
	
	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	public List<Synonym> getAllSynonyms(Integer limit, Integer start);

	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	//public List<TaxonRelationship> getAllTaxonRelationships(Integer limit, Integer start);
	
	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	//public List<SynonymRelationship> getAllSynonymRelationships(Integer limit, Integer start);

	public List<RelationshipBase> getAllRelationships(Integer limit, Integer start); 

	/**
	 * Find taxa by searching for @{link NameBase}
	 * @param queryString
	 * @param matchMode
	 * @param page
	 * @param pagesize
	 * @param onlyAcccepted
	 * @return
	 */
	public List<Taxon> findByName(String queryString, ITitledDao.MATCH_MODE matchMode, int page, int pagesize, boolean onlyAcccepted);
	
	/**
	 * @param queryString
	 * @param matchMode
	 * @param onlyAcccepted
	 * @return
	 */
	public int countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted);
	
	/**
	 * @param queryString
	 * @param matchMode
	 * @param onlyAcccepted
	 * @param criteria
	 * @return
	 */
	public int countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted, List<Criterion> criteria);
	
}
