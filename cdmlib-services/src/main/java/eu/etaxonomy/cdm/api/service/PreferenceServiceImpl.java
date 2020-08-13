/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.IPreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceResolver;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;

/**
 * @author a.mueller
 * @since 2013-09-09
 */
@Service
@Transactional(readOnly = true)
public class PreferenceServiceImpl implements IPreferenceService {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PreferenceServiceImpl.class);

    @Autowired
    private IPreferenceDao dao;

    private Map<String, CdmPreference> cache = new ConcurrentHashMap<>();

    private boolean cacheIsComplete = false;

    private boolean cacheIsLocked = false;

    @Override
	public CdmPreference findExact(PrefKey key) {
		String cacheKey = cacheKey(key);
        return fromCacheGet(key, cacheKey);
	}

    @Override
    public CdmPreference find(PrefKey key) {
        CdmPreference pref = PreferenceResolver.resolve(list(), key);
        return pref;
    }

    @Override
    public CdmPreference findDatabase(IPreferencePredicate<?> predicate){
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), predicate);
        return find(key);
    }

    @Override
    public CdmPreference findVaadin(IPreferencePredicate<?> predicate){
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewVaadinInstance(), predicate);
        return find(key);
    }

    @Override
    public CdmPreference findTaxEditor(IPreferencePredicate<?> predicate){
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewTaxEditorInstance(), predicate);
        return find(key);
    }

	@Override
    @Transactional(readOnly = false)
	public void set(CdmPreference preference) {
		dao.set(preference);
		cachePut(preference);
	}

    @Override
    @Transactional(readOnly = false)
    public void remove(PrefKey key) {
        dao.remove(key);
        removeFromCache(key);
    }

    @Override
	public long count() {
		return dao.count();
	}

	@Override
    public List<CdmPreference> list() {
	    if(!cacheIsComplete) {
	        cacheFullUpdate();
	    }
        return new ArrayList<>(cacheValues());
    }

    @Override
    public List<CdmPreference> list(IPreferencePredicate<?> predicate) {
        // using the cache for this method makes not much sense
        return dao.list(predicate);
    }

    @Override
    public CdmPreference find(TaxonNode taxonNode, String predicate) {
        String cacheKey = cacheKey(taxonNode, predicate);
        return fromCacheOrFind(taxonNode, predicate, cacheKey);
    }

    @Override
    public CdmPreference find(TaxonNode taxonNode, IPreferencePredicate<?> predicate){
        return find(taxonNode, predicate.getKey());
    }

// ********************** NOT YET HANDLED *******************/

    @Override
    public List<CdmPreference> list(String subject, String predicate) {
        //FIXME
        throw new RuntimeException("list(String, String) not yet implemented" );
    }

    @Override
    //this method is only partly implemented
    public CdmPreference find(CdmBase taxonNodeRelatedCdmBase, String predicate) {
        TaxonNode taxonNode = mapToTaxonNode(taxonNodeRelatedCdmBase);
        return dao.find(taxonNode, predicate);
    }

    private TaxonNode mapToTaxonNode(CdmBase taxonNodeRelatedCdmBase) {
        if (taxonNodeRelatedCdmBase == null){
            return null;
        }else if (taxonNodeRelatedCdmBase.isInstanceOf(TaxonNode.class)){
            return CdmBase.deproxy(taxonNodeRelatedCdmBase, TaxonNode.class);
        }else{
            throw new RuntimeException("mapToTaxonNode not yet implemented for " + taxonNodeRelatedCdmBase.getClass().getSimpleName());
        }
    }

    // ====================== Cache methods ======================= //

    /**
     * Concatenates subject and predicate as key for the cache map
     */
    private String cacheKey(PrefKey key) {
        return key.getSubject() + "@" + key.getPredicate();
    }

    private String cacheKey(TaxonNode taxonNode, String predicate) {
        return taxonNode.treeIndex() + predicate;
    }


    // --------------- non locking cache read methods --------------- //

    protected Collection<CdmPreference> cacheValues() {
        waitForCache();
        return cache.values();
    }

    protected CdmPreference fromCacheGet(PrefKey key, String cacheKey) {
        waitForCache();
        return cache.computeIfAbsent(cacheKey, k -> dao.get(key));
    }


    protected CdmPreference fromCacheOrFind(TaxonNode taxonNode, String predicate, String cacheKey) {
        waitForCache();
        return cache.computeIfAbsent(cacheKey, k -> dao.find(taxonNode, predicate));
    }

    // --------------- cache locking methods --------------- //

    protected void cachePut(CdmPreference preference) {
        waitForCache();
        cacheIsLocked = true;
        cache.put(cacheKey(preference.getKey()), preference);
        cacheIsLocked = false;
    }


    protected void removeFromCache(PrefKey key) {
        waitForCache();
        cacheIsLocked = true;
        cache.remove(cacheKey(key));
        cacheIsLocked = false;
    }

    protected void cacheFullUpdate() {
        waitForCache();
        cacheIsLocked = true;
        cache.clear();
        for(CdmPreference pref :  dao.list()){
            cache.put(cacheKey(pref.getKey()), pref);
        }
        cacheIsComplete = true;
        cacheIsLocked = false;
    }

    protected void waitForCache() {
        while(cacheIsLocked) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // just keep on sleeping, we may improve this later on
            }
        }
    }

}
