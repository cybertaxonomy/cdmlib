/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.List;

import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.description.IPolytomousKeyNodeDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.VersionableDaoBase;

/**
 * @author a.mueller
 * @since 08.11.2010
 */
@Repository
public class PolytomousKeyNodeDaoImpl extends VersionableDaoBase<PolytomousKeyNode> implements IPolytomousKeyNodeDao {

	public PolytomousKeyNodeDaoImpl() {
		super(PolytomousKeyNode.class); 
	}

	public List<PolytomousKeyNode> list() {
		Criteria crit = getSession().createCriteria(type); 
		return crit.list(); 
	}
	
}
