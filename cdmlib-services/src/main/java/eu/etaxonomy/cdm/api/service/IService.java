/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;


import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 *
 */
@Transactional(propagation=Propagation.SUPPORTS)
public interface IService<T extends CdmBase>{


}