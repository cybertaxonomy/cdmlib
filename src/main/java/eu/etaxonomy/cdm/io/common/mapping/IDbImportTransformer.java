// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;

/**
 * @author a.mueller
 * @created 02.03.2010
 * @version 1.0
 */
public interface IDbImportTransformer {

	public NameTypeDesignationStatus transformNameTypeDesignationStatus(Object statusId);

	
	
	
}
