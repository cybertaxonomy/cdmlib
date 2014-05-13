package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityConstraintViolationDao;
import eu.etaxonomy.cdm.validation.Severity;

@Service
@Transactional(readOnly = true)
public class EntityConstraintViolationService extends ServiceBase<EntityConstraintViolation, IEntityConstraintViolationDao> implements
		IEntityConstraintViolationService {

	IEntityConstraintViolationDao dao;


	@Override
	protected void setDao(IEntityConstraintViolationDao dao)
	{
		this.dao = dao;
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations()
	{
		return dao.getConstraintViolations();
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass)
	{
		return dao.getConstraintViolations(validatedEntityClass);
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass, Severity severity)
	{
		return dao.getConstraintViolations(validatedEntityClass, severity);
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(Severity severity)
	{
		return dao.getConstraintViolations(severity);
	}

}
