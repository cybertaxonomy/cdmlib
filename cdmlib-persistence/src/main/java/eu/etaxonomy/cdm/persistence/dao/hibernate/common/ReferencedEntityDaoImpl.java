/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.persistence.dao.common.IReferencedEntityDao;

/**
 * @author a.babadshanjan
 * @created 05.09.2008
 */

public class ReferencedEntityDaoImpl<T extends ReferencedEntityBase> extends CdmEntityDaoBase<T> 
							implements IReferencedEntityDao<T>{
	
	public ReferencedEntityDaoImpl(Class<T> type) {
		super(type);
	}

}
