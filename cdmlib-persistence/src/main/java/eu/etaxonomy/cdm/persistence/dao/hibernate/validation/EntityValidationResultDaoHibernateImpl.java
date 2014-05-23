package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISelfDescriptive;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;
import eu.etaxonomy.cdm.validation.CRUDEventType;
import eu.etaxonomy.cdm.validation.Severity;

@Repository
@Qualifier("EntityValidationResultDaoHibernateImpl")
public class EntityValidationResultDaoHibernateImpl extends CdmEntityDaoBase<EntityValidationResult> implements IEntityValidationResultDao {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EntityValidationResultDaoHibernateImpl.class);


	public EntityValidationResultDaoHibernateImpl()
	{
		super(EntityValidationResult.class);
	}


	@Override
	public void saveValidationResult(Set<ConstraintViolation<CdmBase>> errors, CdmBase entity, CRUDEventType crudEventType)
	{
		EntityValidationResult old = getValidationResult(entity.getClass().getName(), entity.getId());
		if (old != null) {
			getSession().delete(old);
		}
		EntityValidationResult result = EntityValidationResult.newInstance();
		result.setCrudEventType(crudEventType);
		result.setValidatedEntityId(entity.getId());
		result.setValidatedEntityUuid(entity.getUuid());
		/*
		 * Since CdmBase implements ISelfDescriptive, this is a redundant check. However,
		 * until Andreas Mueller decides that it is actually useful and appropriate that
		 * CdmBase should implement this interface, this check should be made, so that
		 * nothing breaks if the "implements ISelfDescriptive" is removed from the class
		 * declaration of CdmBase.
		 */
		if (entity instanceof ISelfDescriptive) {
			ISelfDescriptive isd = (ISelfDescriptive) entity;
			result.setValidatedEntityDescription(isd.getUserFriendlyDescription());
			result.setValidatedEntityClass(isd.getUserFriendlyTypeName());
		}
		else {
			result.setValidatedEntityClass(entity.getClass().getSimpleName());
			result.setValidatedEntityDescription(entity.toString());
		}
		for (ConstraintViolation<CdmBase> error : errors) {
			EntityConstraintViolation violation = EntityConstraintViolation.newInstance();
			violation.setSeverity(Severity.getSeverity(error));
			violation.setInvalidValue(error.getInvalidValue().toString());
			violation.setMessage(error.getMessage());
			String field = error.getPropertyPath().toString();
			if (entity instanceof ISelfDescriptive) {
				ISelfDescriptive isd = (ISelfDescriptive) entity;
				violation.setPropertyPath(isd.getUserFriendlyFieldName(field));
			}
			else {
				violation.setPropertyPath(field);
			}
			violation.setValidator(error.getConstraintDescriptor().getConstraintValidatorClasses().iterator().next().getName());
			result.addEntityConstraintViolation(violation);
			violation.setEntityValidationResult(result);
		}
		getSession().merge(result);
	}


	@Override
	public void deleteValidationResult(String validatedEntityClass, int validatedEntityId)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"DELETE FROM EntityValidationResult vr "
					+ "WHERE vr.validatedEntityClass = :cls "
					+ "AND vr.validatedEntityId = :id"
		);
		//@formatter:on
		query.setString("cls", validatedEntityClass);
		query.setInteger("id", validatedEntityId);
		query.executeUpdate();
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
	public List<EntityValidationResult> getEntitiesViolatingConstraint(String validatorClass)
	{
		//@formatter:off
		Query query = getSession().createQuery(
				"FROM EntityValidationResult vr "
					+ "JOIN FETCH vr.entityConstraintViolations cv "
					+ "WHERE cv.validator = :cls "
					+ "ORDER BY vr.validatedEntityClass, vr.validatedEntityId"
		);
		//@formatter:on
		query.setString("cls", validatorClass);
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
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
		query.setString("severity", severity.toString());
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
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
		query.setString("severity", severity.toString());
		@SuppressWarnings("unchecked")
		List<EntityValidationResult> result = (List<EntityValidationResult>) query.list();
		return result;
	}

}
