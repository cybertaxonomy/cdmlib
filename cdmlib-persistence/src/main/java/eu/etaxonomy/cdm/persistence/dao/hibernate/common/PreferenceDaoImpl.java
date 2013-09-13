/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.hibernate.StatelessSession;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmPreference;
import eu.etaxonomy.cdm.persistence.dao.common.IPreferenceDao;

/**
 * @author a.mueller
 * @created 2013-09-09
 */
@Repository
public class PreferenceDaoImpl extends DaoBase implements IPreferenceDao  {
	
	@Override
	public CdmPreference get(CdmPreference.PrefKey key){
		//TODO check if stateless is ok here
		StatelessSession session = getSessionFactory().openStatelessSession();
		return (CdmPreference) session.get(CdmPreference.class, key);
	}
	
	@Override
	public void set(CdmPreference preference){
		CdmPreference pref = get(preference.getKey());
		if (pref == null){
			getSessionFactory().openStatelessSession().insert(preference);
		}else{
			getSessionFactory().openStatelessSession().update(preference);
		}
	}
	
	
}
