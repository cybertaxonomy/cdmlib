/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;

/**
 * @author n.hoffmann
 * @created 24.09.2008
 * @version 1.0
 */
@Repository
public class AnnotatableDaoImpl<T extends AnnotatableEntity> extends CdmEntityDaoBase<T> implements IAnnotatableDao<T> {
	private static Logger logger = Logger.getLogger(AnnotatableDaoImpl.class);
	
	
	public AnnotatableDaoImpl() {
		super((Class<T>)AnnotatableEntity.class);
	}
	/**
	 * @param type
	 */
	public AnnotatableDaoImpl(Class<T> type) {
		super(type);
	}

	
}
