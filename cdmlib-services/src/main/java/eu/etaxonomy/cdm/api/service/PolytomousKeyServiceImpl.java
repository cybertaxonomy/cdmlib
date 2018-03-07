/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteConfiguratorBase;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

@Service
@Transactional(readOnly = false)
public class PolytomousKeyServiceImpl extends IdentifiableServiceBase<PolytomousKey, IPolytomousKeyDao> implements IPolytomousKeyService {

	private IIdentificationKeyDao identificationKeyDao;
	private ITaxonDao taxonDao;
	@Autowired
	private IPolytomousKeyNodeService nodeService;


	@Override
    @Autowired
	protected void setDao(IPolytomousKeyDao dao) {
		this.dao = dao;
	}

	@Autowired
	protected void setDao(IIdentificationKeyDao identificationKeyDao) {
		this.identificationKeyDao = identificationKeyDao;
	}

	@Autowired
	protected void setDao(ITaxonDao taxonDao) {
		this.taxonDao = taxonDao;
	}




	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
	 */
	@Override
	public void updateTitleCache(Class<? extends PolytomousKey> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<PolytomousKey> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = PolytomousKey.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IFeatureTreeService#loadWithNodes(java.util.UUID, java.util.List, java.util.List)
	 */
	@Override
    public PolytomousKey loadWithNodes(UUID uuid, List<String> propertyPaths, List<String> nodePaths) {

		if(nodePaths == null){
			nodePaths = new ArrayList<String>();
		}
		nodePaths.add("children");

		List<String> rootPaths = new ArrayList<String>();
		rootPaths.add("root");
		for(String path : nodePaths) {
			rootPaths.add("root." + path);
		}

		if(propertyPaths != null) {
		    rootPaths.addAll(propertyPaths);
		}

		PolytomousKey polytomousKey = load(uuid, rootPaths);
		dao.loadNodes(polytomousKey.getRoot(),nodePaths);
		return polytomousKey;
	}

	/**
	 * Returns the polytomous key specified by the given <code>uuid</code>.
	 *
	 * @see eu.etaxonomy.cdm.api.service.ServiceBase#load(java.util.UUID, java.util.List)
	 */
	@Override
	public PolytomousKey load(UUID uuid, List<String> propertyPaths) {
		return super.load(uuid, propertyPaths);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IPolytomousKeyService#findByTaxonomicScope(eu.etaxonomy.cdm.model.taxon.TaxonBase, java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List)
	 */
	@Override
	public Pager<PolytomousKey> findByTaxonomicScope(
			TaxonBase taxon, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths, List<String> nodePaths) {

		List<PolytomousKey> list = new ArrayList<PolytomousKey>();
		Long numberOfResults = identificationKeyDao.countByTaxonomicScope(taxon.getUuid(), PolytomousKey.class);
		if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)){
			list = identificationKeyDao.findByTaxonomicScope(taxon.getUuid(), PolytomousKey.class, pageSize, pageNumber, propertyPaths);
		}
		if (nodePaths != null) {
			for (PolytomousKey polytomousKey : list) {
				dao.loadNodes(polytomousKey.getRoot(), nodePaths);
			}
		}
		Pager<PolytomousKey> pager = new DefaultPagerImpl<PolytomousKey>(pageNumber, numberOfResults.intValue(), pageSize, list);

		return pager;
	}

	@Override
	public UpdateResult updateAllNodeNumberings(UUID polytomousKeyUuid) {
	    UpdateResult result = new UpdateResult();
	    PolytomousKey polytomousKey = dao.load(polytomousKeyUuid);
	    polytomousKey.getRoot().refreshNodeNumbering();
	    return result;
	}

	@Override
	public UpdateResult updateAllNodeNumberings() {
	    UpdateResult result = new UpdateResult();
	    List<PolytomousKey> polytomousKeys = dao.list();
	    for(PolytomousKey polytomousKey : polytomousKeys) {
	        polytomousKey.getRoot().refreshNodeNumbering();
	    }
	    return result;
	}

	@Override
	public DeleteResult delete(PolytomousKey key){
	    //DeleteResult result = new DeleteResult();
	    PolytomousKeyNode root = key.getRoot();


	    DeleteResult result = isDeletable(key.getUuid(), null);
	    DeleteResult resultRoot = new DeleteResult();
	    if (result.isOk()){
    	    try{
    	        if (root != null){
    	           // root.setKey(null);
    	            resultRoot = nodeService.delete(root.getUuid(), true);

    	        }
    	    }catch(Exception e){
    	        result.addException(e);
    	        result.setAbort();
    	        return result;
    	    }
    	    try{
    	        if (resultRoot.isOk()){
    	            dao.delete(key);
    	            result.addDeletedObject(key);
    	            result.includeResult(resultRoot);
    	        }
    	    }catch(Exception e){
                result.addException(e);
                result.setAbort();
                return result;
            }
	    }
        return result;
	}

	@Override
    public DeleteResult isDeletable(UUID keyUuid, DeleteConfiguratorBase config){
        DeleteResult result = new DeleteResult();
        PolytomousKey key = this.load(keyUuid);
        Set<CdmBase> references = commonService.getReferencingObjectsForDeletion(key);
        if (references != null){
           Iterator<CdmBase> iterator = references.iterator();
           CdmBase ref;
           while (iterator.hasNext()){
               ref = iterator.next();
               if ((ref instanceof PolytomousKeyNode) ){
                   PolytomousKeyNode node = HibernateProxyHelper.deproxy(ref, PolytomousKeyNode.class);
                   if (!node.getKey().equals(key)){
                       String message = "The key is a subkey of " + node.getKey() + ", referenced in node with id: " + node.getId() + ". Please remove the subkey reference first and then delete the key. " ;
                       result.addException(new ReferencedObjectUndeletableException(message));
                       result.setAbort();
                       result.addRelatedObject(ref);
                   }
               }
           }
        }
        return result;
    }

}
