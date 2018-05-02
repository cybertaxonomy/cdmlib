/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This class is a helper class for all IOStates implementing IPartionedState to hold and retrieve the 
 * related objects for a partition. If once all import states implement IPartitionedState this class
 * can be integrated into ImportStateBase
 * @author a.mueller
 * @since 02.03.2010
 */
public class RelatedObjectsHelper {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RelatedObjectsHelper.class);
	
	private Map<Object, Map<String, ? extends CdmBase>> relatedObjects;

	/**
	 * @param relatedObjects the relatedObjects to set
	 */
	public void setRelatedObjects(Map<Object, Map<String, ? extends CdmBase>> relatedObjects) {
		this.relatedObjects = relatedObjects;
	}

	/**
	 * Returns the cdmbase object for the key pair (namespace, id).
	 * @param namespace
	 * @param id
	 */
	public CdmBase getRelatedObject(Object namespace, String id) {
		CdmBase cdmBase = null;
		Map<String, ? extends CdmBase> idMap = relatedObjects.get(namespace);
		if (idMap != null){
			cdmBase = idMap.get(id);
		}
		return cdmBase;
	}

	/**
	 * Adds a related object to the underlying map.
	 * @param namespace
	 * @param id
	 * @param relatedObject
	 */
	public void  addRelatedObjet(Object namespace, String id, CdmBase relatedObject){
		Map idMap = relatedObjects.get(namespace);
		if (idMap == null){
			idMap = new HashMap<String, CdmBase>();
			relatedObjects.put(namespace, idMap);
		}
		idMap.put(id, relatedObject);
		
	}
}
