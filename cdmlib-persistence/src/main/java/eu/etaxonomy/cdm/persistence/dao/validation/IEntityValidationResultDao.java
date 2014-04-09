package eu.etaxonomy.cdm.persistence.dao.validation;

import java.util.List;

import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.validation.Severity;

public interface IEntityValidationResultDao extends ICdmEntityDao<EntityValidationResult> {

	EntityValidationResult getEntityValidationResult(String validatedEntityClass, int validatedEntityId);


	List<EntityValidationResult> getEntityValidationResults();


	List<EntityValidationResult> getEntityValidationResults(Severity severity);



}
