/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 *
 */
public class InMemoryMapping implements IImportMapping {

	/**
	 * Locally stored foreign key mapping, first object defines a namespace in the source, 2nd Object is
	 * a key in the source that is unique within the given namespace, the 
	 */
	private Map<String, Map<String, Set<CdmKey>>> mapping = new HashMap<String, Map<String,Set<CdmKey>>>();
	
	public class CdmKey<CLASS extends IdentifiableEntity>{
		Class<CLASS> clazz;
		int id;
		
		private CdmKey(IdentifiableEntity object){
			this.clazz = (Class)object.getClass();
			this.id = object.getId();
		}
		
		@Override
		public String toString(){
			return id + "@" + clazz.getSimpleName();
		}
	}
	
	@Override
	public void putMapping(String namespace, Integer sourceKey, IdentifiableEntity destinationObject){
		putMapping(namespace, String.valueOf(sourceKey), destinationObject);
	}
		
	@Override
	public void putMapping(String namespace, String sourceKey, IdentifiableEntity destinationObject){
		Map<String, Set<CdmKey>> namespaceMap = mapping.get(namespace);
		if (namespaceMap == null){
			namespaceMap = new HashMap<String, Set<CdmKey>>();
			mapping.put(namespace, namespaceMap);
		}
		Set<CdmKey> keySet = namespaceMap.get(sourceKey);
		if (keySet == null){
			keySet = new HashSet<InMemoryMapping.CdmKey>();
			namespaceMap.put(sourceKey, keySet);
		}
		
		keySet.add(new CdmKey(destinationObject));
	}
	
	@Override
	public Set<CdmKey> get(String namespace, String sourceKey){
		Set<CdmKey> result = new HashSet<InMemoryMapping.CdmKey>();
		Map<String, Set<CdmKey>> namespaceMap = mapping.get(namespace);
		if (namespaceMap != null){
			Set<CdmKey> keySet = namespaceMap.get(sourceKey);
			if (keySet != null){
				result = keySet;
			}
		}
		return result;
	}
	
	public boolean exists(String namespace, String sourceKey,Class<?> destinationClass){
		Set<CdmKey> keySet = this.get(namespace, sourceKey);
		for (CdmKey<?> key: keySet){
			if (destinationClass == null || destinationClass.isAssignableFrom(key.clazz)){
				return true;
			}
		}
		return false;
	}

}
