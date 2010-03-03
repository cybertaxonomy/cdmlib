// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.Map;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This class represents all (import(?)) states used with an ResultSetPartitioner
 * @author a.mueller
 * @created 02.03.2010
 * @version 1.0
 */
public interface IPartitionedState {

	//Map<Object, Map<String, ? extends CdmBase>> getRelatedObjects();
	
	public void setRelatedObjects(Map<Object, Map<String, ? extends CdmBase>> relatedObjects);
	
	public CdmBase getRelatedObject(Object namespace, String id);
	
}
