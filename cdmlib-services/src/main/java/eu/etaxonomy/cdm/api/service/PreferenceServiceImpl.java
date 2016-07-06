// $Id$
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
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;

/**
 * @author a.mueller
 * @created 2013-09-09
 */
@Service
@Transactional(readOnly = true)
public class PreferenceServiceImpl implements IPreferenceService {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PreferenceServiceImpl.class);

    @Autowired
    private IPreferenceDao dao;

	@Override
	public CdmPreference find(PrefKey key) {
		List<PrefKey> keys = new ArrayList<>();
		keys.add(key);
//		while(key.)  TODO

		return dao.get(key);
	}

    /**
     * Retrieve the database wide preference for the given predicate.
     * @param key
     * @return
     */
    @Override
    public CdmPreference findDatabase(PreferencePredicate predicate){
        PrefKey key = CdmPreference.NewKey(PreferenceSubject.NewDatabaseInstance(), predicate);
        return find(key);
    }

	@Override
	public void set(CdmPreference preference) {
		dao.set(preference);
	}

	@Override
	public int count() {
		return dao.count();
	}

	@Override
    public List<CdmPreference> list() {
        return dao.list();
    }

    @Override
    public Object find(TaxonNode taxonNode, String predicate) {
        return dao.find(taxonNode, predicate);
    }

    @Override
    public CdmPreference find(TaxonNode taxonNode, PreferencePredicate predicate){
        return dao.find(taxonNode, predicate.getKey());
    }


// ********************** NOT YET HANDLED *******************/


//    /* (non-Javadoc)
//     * @see eu.etaxonomy.cdm.api.service.IPreferenceService#find(java.lang.String, java.lang.String)
//     */
//    @Override
//    public Object find(String subject, String predicate) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IPreferenceService#findAll(java.lang.String, java.lang.String)
     */
    @Override
    public List<Object> findAll(String subject, String predicate) {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IPreferenceService#find(eu.etaxonomy.cdm.model.common.CdmBase, java.lang.String)
     */
    @Override
    public Object find(CdmBase taxonNode, String predicate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IPreferenceService#findAll()
     */
    @Override
    public List<Object> findAll() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IPreferenceService#setCdmPrefs(eu.etaxonomy.cdm.model.common.CdmBase, java.lang.String, java.lang.String)
     */
    @Override
    public String setCdmPrefs(CdmBase cdmBase, String predicate, String value) {
        // TODO Auto-generated method stub
        return null;
    }


}
