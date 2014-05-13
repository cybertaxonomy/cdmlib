package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityConstraintViolationDao;
import eu.etaxonomy.cdm.validation.Severity;

@Repository
public class EntityConstraintViolationHibernateImpl extends CdmEntityDaoBase<EntityConstraintViolation> implements IEntityConstraintViolationDao {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EntityConstraintViolationHibernateImpl.class);


	public EntityConstraintViolationHibernateImpl()
	{
		super(EntityConstraintViolation.class);
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
		query.setString("cls", validatedEntityClass);
		query.setString("severity", severity.toString());
		@SuppressWarnings("unchecked")
		List<EntityConstraintViolation> result = (List<EntityConstraintViolation>) query.list();
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
		query.setString("severity", severity.toString());
		@SuppressWarnings("unchecked")
		List<EntityConstraintViolation> result = (List<EntityConstraintViolation>) query.list();
		return result;
	}

}
