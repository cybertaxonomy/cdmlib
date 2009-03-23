/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.IAnnotatableService;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;

/**
 * based on org.cateproject.controller.common
 * 
 * @author a.kohlbecker
 *
 * @param <T>
 * @param <DAO>
 */
public abstract class AnnotatableController<T extends AnnotatableEntity, SERVICE extends IAnnotatableService<T>> extends BaseController<T,SERVICE> {

	private AnnotatableServiceBase<T, ? extends IAnnotatableDao<T>> annotatableService;
	
	
//	@Autowired
//	public void setAnnotationService(AnnotatableServiceBase<T, ? extends IAnnotatableDao<T>> annotatableService) {
//		this.annotatableService = annotatableService;
//	}


}
