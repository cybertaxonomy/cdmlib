/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;

/**
 *
 * @author ayco_holleman
 * @date 15 jan. 2015
 *
 */
@Service
@Transactional(readOnly = true)
public class EntityValidationResultServiceImpl extends ServiceBase<EntityValidationResult, IEntityValidationResultDao> implements
		IEntityValidationResultService {

	@Autowired
	IEntityValidationResultDao dao;


	@Override
	protected void setDao(IEntityValidationResultDao dao){
		this.dao = dao;
	}

	@Override
	public EntityValidationResult getValidationResult(String validatedEntityClass, int validatedEntityId){
		return dao.getValidationResult(validatedEntityClass, validatedEntityId);
	}


	@Override
	public List<EntityValidationResult> getValidationResults(){
		return dao.getValidationResults();
	}


	@Override
	public List<EntityValidationResult> getEntityValidationResults(String validatedEntityClass){
		return dao.getEntityValidationResults(validatedEntityClass);
	}


	@Override
	public List<EntityValidationResult> getEntitiesViolatingConstraint(String validatorClass){
		return dao.getEntitiesViolatingConstraint(validatorClass);
	}


	@Override
	public List<EntityValidationResult> getValidationResults(String validatedEntityClass, Severity severity){
		return dao.getValidationResults(validatedEntityClass, severity);
	}


	@Override
	public List<EntityValidationResult> getValidationResults(Severity severity){
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    @Transactional(readOnly = false)
    public <T extends CdmBase> void saveValidationResult(Set<ConstraintViolation<T>> errors, T entity,
            CRUDEventType crudEventType) {
        dao.saveValidationResult(errors, entity, crudEventType);

    }

    @Override
    @Transactional(readOnly = false)
    public void deleteValidationResult(String validatedEntityClass, int validatedEntityId) {
        dao.deleteValidationResult(validatedEntityClass, validatedEntityId);
    }

}
