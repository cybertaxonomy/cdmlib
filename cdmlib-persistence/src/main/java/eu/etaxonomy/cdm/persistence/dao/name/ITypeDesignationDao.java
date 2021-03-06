/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.name;

import java.util.List;

import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.common.ISourcedEntityDao;

/**
 * @author a.mueller
 *
 */
public interface ITypeDesignationDao extends ISourcedEntityDao<TypeDesignationBase<?>> {

	public List<TypeDesignationBase<?>> getAllTypeDesignations(Integer limit, Integer start);


    public List<TypeDesignationStatusBase> getTypeDesignationStatusInUse();


}
