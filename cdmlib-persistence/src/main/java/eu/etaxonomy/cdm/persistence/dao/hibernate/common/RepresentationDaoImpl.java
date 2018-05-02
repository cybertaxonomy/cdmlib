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

import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.persistence.dao.common.IRepresentationDao;

/**
 * @author a.babadshanjan
 * @since 10.09.2008
 */
@Repository
public class RepresentationDaoImpl 
extends LanguageStringBaseDaoImpl<Representation> implements IRepresentationDao {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RepresentationDaoImpl.class);

	public RepresentationDaoImpl() {
		super(Representation.class); 
	}

	public List<Representation> getAllRepresentations(Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(Representation.class);
		List<Representation> results = crit.list();
		return results;
	}
}

