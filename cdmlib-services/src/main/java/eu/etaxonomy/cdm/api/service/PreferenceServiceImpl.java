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
    @Transactional(readOnly = false)
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

    /**
     * @param taxonNodeRelatedCdmBase
     * @return
     */
    private TaxonNode mapToTaxonNode(CdmBase taxonNodeRelatedCdmBase) {
        if (taxonNodeRelatedCdmBase == null){
            return null;
        }else if (taxonNodeRelatedCdmBase.isInstanceOf(TaxonNode.class)){
            return CdmBase.deproxy(taxonNodeRelatedCdmBase, TaxonNode.class);
        }else{
            throw new RuntimeException("mapToTaxonNode not yet implemented for " + taxonNodeRelatedCdmBase.getClass().getSimpleName());
        }
    }


//    @Override
//    public String setCdmPrefs(CdmBase cdmBase, String predicate, String value) {
//        // TODO Auto-generated method stub
//        return null;
//    }


}
