/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.SingleSourcedEntityBase;
import eu.etaxonomy.cdm.persistence.dao.common.ISingleSourcedEntityDao;

/**
 * @author a.mueller
 * @since 12.06.2020
 */
@Repository(value="singleSourcedEntityDao")
public abstract class SingleSourcedEntityDaoImpl<T extends SingleSourcedEntityBase> extends CdmEntityDaoBase<T>
							implements ISingleSourcedEntityDao<T>{

	@SuppressWarnings("unchecked")
    public SingleSourcedEntityDaoImpl() {
		super((Class<T>)SingleSourcedEntityBase.class);
	}

	public SingleSourcedEntityDaoImpl(Class<T> type) {
		super(type);
	}

}
