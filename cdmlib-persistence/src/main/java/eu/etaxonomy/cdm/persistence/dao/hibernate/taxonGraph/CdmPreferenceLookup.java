/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph;

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
 *
 */

public class CdmPreferenceLookup {

    private static CdmPreferenceLookup instance;

    public static CdmPreferenceLookup instance(){
        if(instance == null){
            instance = new CdmPreferenceLookup();
        }
        return instance;
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
