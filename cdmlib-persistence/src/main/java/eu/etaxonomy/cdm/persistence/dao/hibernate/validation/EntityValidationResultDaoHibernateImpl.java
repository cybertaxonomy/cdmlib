package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;

public class EntityValidationResultDaoHibernateImpl extends CdmEntityDaoBase<EntityValidationResult> implements IEntityValidationResultDao {

	public EntityValidationResultDaoHibernateImpl()
	{
		super(EntityValidationResult.class);
	}

}
