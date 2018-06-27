/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.PreferenceSubject;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;

/**
 * @author a.mueller
 * @since 2013-09-09
 */
@Repository
public class PreferenceDaoImpl extends DaoBase implements IPreferenceDao  {

    private static final String TAXON_NODE_FILTER_START = PreferenceSubject.ROOT + "TaxonNode[";

	@Override
	public CdmPreference get(PrefKey key){
		Session session = getSession();
		return session.get(CdmPreference.class, key);

		//old
//		StatelessSession session = getSessionFactory().openStatelessSession();
//		return (CdmPreference) session.get(CdmPreference.class, key);

	}

	@Override
	public void set(CdmPreference preference){
		CdmPreference pref = get(preference.getKey());
		//maybe
		//TODO maybe there is better way to allow updates without allowing to write CdmPref.value
		if (pref != null){
			getSession().delete(pref);
		}
		getSession().save(preference);

		//old
//		if (pref == null){
//			getSessionFactory().openStatelessSession().insert(preference);
//		}else{
//			getSessionFactory().openStatelessSession().update(preference);
//		}
	}


	@Override
	public CdmPreference find(TaxonNode taxonNode, String predicate){
	    String treeIndex = taxonNode.treeIndex();
	    String[] splits = treeIndex == null ? new String[]{}: treeIndex.split("#");
	    List<String> filterStrings = new ArrayList<String>();
	    filterStrings.add(PreferenceSubject.ROOT);
	    String rootSplit = "";
	    for (String split : splits){
	        if (! "".equals(split)) {
	            rootSplit += "#" + split;
	            filterStrings.add(TAXON_NODE_FILTER_START + rootSplit + "#]");
	        }
	    }


	    //TODO Top1 and ORDER BY treeIndex length and remove for() loop below
	    String hql = "FROM CdmPreference pref "
	            + " WHERE pref.key.predicate = :predicate "
	            + "    AND pref.key.subject IN :subject "
	            ;
	    Query query = getSession().createQuery(hql);
	    query.setParameter("predicate", predicate);
	    query.setParameterList("subject", filterStrings);
        @SuppressWarnings("unchecked")
        List<CdmPreference> allPreferences = query.list();
        CdmPreference result = null;
        for (CdmPreference pref : allPreferences){
            if (result == null || result.getSubject().length() < pref.getSubject().length()){
                result = pref;
            }
        }
        return result;
	}


	@Override
	public long count(){
		Criteria crit = getSession().createCriteria(CdmPreference.class);
        crit.setProjection(Projections.rowCount());
        return (Long)crit.uniqueResult();
	}

    @Override
    public List<CdmPreference> list(){
        Criteria crit = getSession().createCriteria(CdmPreference.class);
        @SuppressWarnings("unchecked")
        List<CdmPreference> result = crit.list();
        return result;
    }

}
