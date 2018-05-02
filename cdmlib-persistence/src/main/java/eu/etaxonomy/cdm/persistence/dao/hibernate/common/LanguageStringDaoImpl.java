/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringDao;

/**
 * @author a.babadshanjan
 * @since 12.09.2008
 */
@Repository
public class LanguageStringDaoImpl 
extends LanguageStringBaseDaoImpl<LanguageString> implements ILanguageStringDao {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LanguageStringDaoImpl.class);

	public LanguageStringDaoImpl() {
		super(LanguageString.class); 
	}

	public List<LanguageString> getAllLanguageStrings(Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(LanguageString.class);
		List<LanguageString> results = crit.list();
		return results;
	}
}
