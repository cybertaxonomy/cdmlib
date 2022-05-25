/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;

/**
 * Caching CdmPreference access class which updates the cached values after
 * the milliseconds as defined in <code>updateInterval</code> (currently 10 minutes).
 *
 * @author a.kohlbecker
 * @since Oct 2, 2018
 */
public class CdmPreferenceCache {

    //we register preference access per preference dao (5-2022, before it was purely static),
    //this is to allow running >1 entity managers per virtual machine. It Assumes that
    //each peference dao "belongs" to exactly one entity manager. Needs to be changed
    //if this assumption is not true in future.
    //The change became necessary as e.g. running persistence tests uses >1 entity manager
    //when running in (maven) suite.
    private static Map<IPreferenceDao,CdmPreferenceCache> registry = new HashMap<>();

    public static CdmPreferenceCache instance(IPreferenceDao dao){
        CdmPreferenceCache result = registry.get(dao);
        if(result == null){
            result = new CdmPreferenceCache();
            result.setIPreferenceDao(dao);
            registry.put(dao, result);
        }
        return result;
    }

    long updateInterval = 10 * 60 * 1000l;

    private IPreferenceDao preferenceDao;

    private Map<PrefKey, CachedCdmPreference> map = new HashMap<>();

    public CdmPreference get(PrefKey key){
        CdmPreference cdmPref = null;
        CachedCdmPreference  cached = map.get(key);
        if(cached == null || cached.isOutDated()){
            cdmPref = preferenceDao.get(key);
            if(cdmPref != null){
                map.put(key, new CachedCdmPreference(cdmPref));
            } else {
                map.remove(key);
            }
        } else {
            cdmPref = cached.cdmPreference;
        }
        return cdmPref;
    }

    public void setIPreferenceDao(IPreferenceDao preferenceDao){
        this.preferenceDao = preferenceDao;
    }

    class CachedCdmPreference {
        long time;
        CdmPreference cdmPreference;

        CachedCdmPreference(CdmPreference cdmPreference){
            this.cdmPreference = cdmPreference;
            time = System.currentTimeMillis();
        }

        boolean isOutDated(){
            return System.currentTimeMillis() - updateInterval > time;
        }
    }
}