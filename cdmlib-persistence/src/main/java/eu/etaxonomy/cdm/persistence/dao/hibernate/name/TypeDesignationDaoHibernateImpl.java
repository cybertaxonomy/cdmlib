/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;

/**
 * @author a.mueller
 * @since 11.01.2025
 */
@Repository
public class TypeDesignationDaoHibernateImpl
        extends CdmEntityDaoBase<TypeDesignationBase>
        implements ITypeDesignationDao {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public TypeDesignationDaoHibernateImpl() {
		super(TypeDesignationBase.class);
	}
}