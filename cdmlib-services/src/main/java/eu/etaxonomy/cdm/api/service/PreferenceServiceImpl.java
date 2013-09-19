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

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
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
	public CdmPreference get(PrefKey key) {
		List<PrefKey> keys = new ArrayList<CdmPreference.PrefKey>(){};
		keys.add(key);
//		while(key.)  TODO
		
		return dao.get(key);
	}

	@Override
	public void set(CdmPreference preference) {
		dao.set(preference);
	}

	@Override
	public int count() {
		return dao.count();
	}


}
