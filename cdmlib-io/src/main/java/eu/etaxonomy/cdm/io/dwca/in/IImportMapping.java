/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 *
 */
public interface IImportMapping {
	
	/**
	 * Put the destination object with for the given namespaced identifier into the mapping.
	 * @param namespace
	 * @param sourceKey
	 * @param destinationObject
	 */
	public void putMapping(String namespace, String sourceKey, IdentifiableEntity destinationObject);

	/**
	 * Put the destination object with for the given namespaced identifier into the mapping.
	 * @param namespace
	 * @param sourceKey
	 * @param destinationObject
	 * @see #putMapping(String, String, IdentifiableEntity)
	 */
	public void putMapping(String namespace, Integer sourceKey, IdentifiableEntity destinationObject);

	
	/**
	 * Retrieve the CdmKey set for the given namespaced identifier.
	 * @param namespace
	 * @param sourceKey
	 * @return
	 */
	public Set<CdmKey> get(String namespace, String sourceKey);

	/**
	 * Checks if a mapping exists for a given namespaced identifier.
	 * If destinationClass is not <code>null</code> the mapping is limited to 
	 * result objects of the given class or a subclass of the given class.
	 * @param namespace
	 * @param sourceKey
	 * @param destinationClass
	 * @return
	 */
	public boolean exists(String namespace, String sourceKey,Class<?> destinationClass);

	/**
	 * Returns the mapping for only those obejcts addressed by the namespacedSourceKeys parameter
	 * @param namespacedKeys
	 * @return
	 */
	public InMemoryMapping getPartialMapping(Map<String, Set<String>> namespacedSourceKeys);

	/**
	 * Returns a list for all mapping entries.
	 * @return
	 */
	public List<MappingEntry<String, String, Class, Integer>> getEntryList();
	
	
	public class CdmKey<CLASS extends IdentifiableEntity>{
		Class<CLASS> clazz;
		int id;
		
		public CdmKey(IdentifiableEntity<?> object){
			this.clazz = (Class)object.getClass();
			this.id = object.getId();
		}
		
		@Override
		public String toString(){
			return id + "@" + clazz.getSimpleName();
		}
	}
	
}
