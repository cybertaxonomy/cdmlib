package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;
import eu.etaxonomy.cdm.validation.Severity;

public class EntityValidationResultDaoHibernateImpl extends CdmEntityDaoBase<EntityValidationResult> implements IEntityValidationResultDao {

//	public static void main(String[] args)
//	{
//		EntityValidationResultDaoHibernateImpl dao = new EntityValidationResultDaoHibernateImpl();
//		EntityValidationResult entity = EntityValidationResult.newInstance();
//		entity.setUuid(UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2"));
//		entity.set
//		System.out.println("done");
//	}
//
	private static final Logger logger = Logger.getLogger(EntityValidationResultDaoHibernateImpl.class);


	public EntityValidationResultDaoHibernateImpl()
	{
		super(EntityValidationResult.class);
	}


	@Override
	public EntityValidationResult getEntityValidationResult(String validatedEntityClass, int validatedEntityId)
	{
		Query query = getSession().createQuery("from EntityValidationResult where validatedEntityClass = :cls and validatedEntityId = : id");
		query.setString("cls", validatedEntityClass);
		query.setInteger("id", validatedEntityId);
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
		if (result.size() == 0) {
			return null;
		}
		if (result.size() != 1) {
			String msg = String.format("Illegal multiplicity of validation results for %s with id %s", validatedEntityClass, validatedEntityId);
			logger.error(msg);
		}
		return result.iterator().next();
	}


	@Override
	public List<EntityValidationResult> getEntityValidationResults()
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<EntityValidationResult> getEntityValidationResults(Severity severity)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
