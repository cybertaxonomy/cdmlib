/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;

/**
 * @author a.babadshanjan
 * @created 24.09.2008
 */
@Repository
public class HomotypicalGroupDaoHibernateImpl
extends CdmEntityDaoBase<HomotypicalGroup> implements IHomotypicalGroupDao {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(HomotypicalGroupDaoHibernateImpl.class);

	public HomotypicalGroupDaoHibernateImpl() {
		super(HomotypicalGroup.class); 
	}

}
