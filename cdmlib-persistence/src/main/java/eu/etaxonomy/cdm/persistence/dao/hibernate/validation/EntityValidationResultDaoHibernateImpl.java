package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;
import eu.etaxonomy.cdm.validation.Severity;

@Repository
public class EntityValidationResultDaoHibernateImpl extends CdmEntityDaoBase<EntityValidationResult> implements IEntityValidationResultDao {

	private static final Logger logger = Logger.getLogger(EntityValidationResultDaoHibernateImpl.class);


	public EntityValidationResultDaoHibernateImpl()
	{
		super(EntityValidationResult.class);
	}


	@Override
	public EntityValidationResult getValidationResult(String validatedEntityClass, int validatedEntityId)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityValidationResult vr "
					+ "WHERE vr.validatedEntityClass = :cls "
					+ "AND vr.validatedEntityId = :id"
		);
		//@formatter:on
		query.setString("cls", validatedEntityClass);
		query.setInteger("id", validatedEntityId);
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
		if (result.size() == 0) {
			return null;
		}
		return result.iterator().next();
	}


	@Override
	public List<EntityValidationResult> getValidationResults()
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityValidationResult vr "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId"
		);
		//@formatter:on
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
		return result;
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations()
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityConstraintViolation cv "
					+ "JOIN FETCH cv.entityValidationResult vr "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
		//@formatter:on
		@SuppressWarnings("unchecked")
		List<EntityConstraintViolation> result = (List<EntityConstraintViolation>) query.list();
		return result;
	}


	@Override
	public List<EntityValidationResult> getEntityValidationResults(String validatedEntityClass)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityValidationResult vr "
					+ "WHERE vr.validatedEntityClass = :cls "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
		//@formatter:on
		query.setString("cls", validatedEntityClass);
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
		return result;
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityConstraintViolation cv "
					+ "JOIN FETCH cv.entityValidationResult vr "
					+ "WHERE vr.validatedEntityClass = :cls "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
		//@formatter:on
		query.setString("cls", validatedEntityClass);
		@SuppressWarnings("unchecked")
		List<EntityConstraintViolation> result = (List<EntityConstraintViolation>) query.list();
		return result;
	}


	@Override
	public List<EntityValidationResult> getValidationResults(String validatedEntityClass, Severity severity)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityValidationResult vr "
					+ "JOIN FETCH vr.entityConstraintViolations cv "
					+ "WHERE vr.validatedEntityClass = :cls "
					+ "AND cv.severity = :severity "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId"
		);
		//@formatter:on
		query.setString("cls", validatedEntityClass);
		query.setString("severity", severity.name());
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
		return result;
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass, Severity severity)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityConstraintViolation cv "
					+ "JOIN FETCH cv.entityValidationResult vr "
					+ "WHERE vr.validatedEntityClass = :cls "
					+ "AND cv.severity = :severity "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
		//@formatter:on
		query.setString("severity", severity.name());
		@SuppressWarnings("unchecked")
		List<EntityConstraintViolation> result = (List<EntityConstraintViolation>) query.list();
		return result;
	}

	@Override
	public List<EntityValidationResult> getValidationResults(Severity severity)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityValidationResult vr " 
					+ "JOIN FETCH vr.entityConstraintViolations cv " 
					+ "WHERE cv.severity = :severity "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId"
		);
		//@formatter:on
		query.setString("severity", severity.name());
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
		return result;
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(Severity severity)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityConstraintViolation cv "
					+ "JOIN FETCH cv.entityValidationResult vr "
					+ "WHERE cv.severity = :severity "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
		//@formatter:on
		query.setString("severity", severity.name());
		@SuppressWarnings("unchecked")
		List<EntityConstraintViolation> result = (List<EntityConstraintViolation>) query.list();
		return result;
	}


}
