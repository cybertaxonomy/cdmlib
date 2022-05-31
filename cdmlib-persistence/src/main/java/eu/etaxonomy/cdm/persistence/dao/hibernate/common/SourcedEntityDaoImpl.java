/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import eu.etaxonomy.cdm.model.common.SourcedEntityBase;
import eu.etaxonomy.cdm.persistence.dao.common.ISourcedEntityDao;

/**
 * @author a.mueller
 * @since 24.01.2019
 */
public abstract class SourcedEntityDaoImpl<T extends SourcedEntityBase>
        extends CdmEntityDaoBase<T>
		implements ISourcedEntityDao<T>{

	@SuppressWarnings("unchecked")
    public SourcedEntityDaoImpl() {
		super((Class<T>)SourcedEntityBase.class);
	}

	public SourcedEntityDaoImpl(Class<T> type) {
		super(type);
	}

}
