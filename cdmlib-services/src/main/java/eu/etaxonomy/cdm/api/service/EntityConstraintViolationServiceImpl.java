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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityConstraintViolationDao;

/**
 *
 * @author ayco_holleman
 * @since 15 jan. 2015
 *
 */
@Service
@Transactional(readOnly = true)
public class EntityConstraintViolationServiceImpl extends ServiceBase<EntityConstraintViolation, IEntityConstraintViolationDao> implements
		IEntityConstraintViolationService {

    @Autowired
	private IEntityConstraintViolationDao dao;


	@Override
	protected void setDao(IEntityConstraintViolationDao dao){
		this.dao = dao;
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(){
		return dao.getConstraintViolations();
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass){
		return dao.getConstraintViolations(validatedEntityClass);
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass, Severity severity){
		return dao.getConstraintViolations(validatedEntityClass, severity);
	}


	@Override
	public List<EntityConstraintViolation> getConstraintViolations(Severity severity){
		return dao.getConstraintViolations(severity);
	}

}
