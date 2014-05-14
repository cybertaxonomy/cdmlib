package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;
import eu.etaxonomy.cdm.validation.Severity;

@Service
@Transactional(readOnly = true)
public class EntityValidationResultServiceImpl extends ServiceBase<EntityValidationResult, IEntityValidationResultDao> implements
		IEntityValidationResultService {

	IEntityValidationResultDao dao;


	@Override
	protected void setDao(IEntityValidationResultDao dao)
	{
		this.dao = dao;
	}


	@Override
	public EntityValidationResult getValidationResult(String validatedEntityClass, int validatedEntityId)
	{
		return dao.getValidationResult(validatedEntityClass, validatedEntityId);
	}


	@Override
	public List<EntityValidationResult> getValidationResults()
	{
		return dao.getValidationResults();
	}


	@Override
	public List<EntityValidationResult> getEntityValidationResults(String validatedEntityClass)
	{
		return dao.getEntityValidationResults(validatedEntityClass);
	}


	@Override
	public List<EntityValidationResult> getEntitiesViolatingConstraint(String validatorClass)
	{
		return dao.getEntitiesViolatingConstraint(validatorClass);
	}


	@Override
	public List<EntityValidationResult> getValidationResults(String validatedEntityClass, Severity severity)
	{
		return dao.getValidationResults(validatedEntityClass, severity);
	}


	@Override
	public List<EntityValidationResult> getValidationResults(Severity severity)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
