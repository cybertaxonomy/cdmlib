/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.reference.ReferenceBase;


/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:30
 */
public interface IReferencedEntity {

	public ReferenceBase getCitation();

}