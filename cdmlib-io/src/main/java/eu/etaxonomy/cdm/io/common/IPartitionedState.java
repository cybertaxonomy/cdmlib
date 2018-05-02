/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.sql.ResultSet;
import java.util.Map;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This class represents all (import(?)) states used with an ResultSetPartitioner
 * @author a.mueller
 * @since 02.03.2010
 */
public interface IPartitionedState {

	public void addRelatedObject(Object namespace, String id, CdmBase relatedObject);
	
	/**
	 * Sets the related objects map. This usually does not need to be handled by the user
	 * but is called by the {@link ResultSetPartitioner result set partitioner}
	 * @param relatedObjects
	 */
	public void setRelatedObjects(Map<Object, Map<String, ? extends CdmBase>> relatedObjects);
	
	/**
	 * Returns a related object defined by the namespace and the id.
	 * The related objects are computed in {@link IPartitionedIO#getRelatedObjectsForPartition(ResultSet)}
	 * @param namespace
	 * @param id
	 * @return
	 */
	public CdmBase getRelatedObject(Object namespace, String id);
	
	public Reference getTransactionalSourceReference();
	
	public void resetTransactionalSourceReference();
	
	public void makeTransactionalSourceReference(IReferenceService service);
	
}
