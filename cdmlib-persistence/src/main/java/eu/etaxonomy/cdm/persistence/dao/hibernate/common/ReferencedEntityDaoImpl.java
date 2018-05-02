/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.persistence.dao.common.IReferencedEntityDao;

/**
 * @author a.babadshanjan
 * @since 05.09.2008
 */
@Repository(value="refEntDao")
public class ReferencedEntityDaoImpl<T extends ReferencedEntityBase> extends CdmEntityDaoBase<T> 
							implements IReferencedEntityDao<T>{
	
	public ReferencedEntityDaoImpl() {
		super((Class<T>)ReferencedEntityBase.class);
	}
	
	public ReferencedEntityDaoImpl(Class<T> type) {
		super(type);
	}

}
