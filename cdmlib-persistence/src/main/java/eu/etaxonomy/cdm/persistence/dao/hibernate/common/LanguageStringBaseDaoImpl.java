/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringBaseDao;

/**
 * @author a.babadshanjan
 * @since 10.09.2008
 */
@Repository(value="langStrBaseDao")
public class LanguageStringBaseDaoImpl<T extends LanguageStringBase> extends AnnotatableDaoImpl<T> 
implements ILanguageStringBaseDao<T>{

	public LanguageStringBaseDaoImpl() {
		super((Class<T>)LanguageStringBase.class);
	}

	public LanguageStringBaseDaoImpl(Class<T> type) {
		super(type);
	}
}

