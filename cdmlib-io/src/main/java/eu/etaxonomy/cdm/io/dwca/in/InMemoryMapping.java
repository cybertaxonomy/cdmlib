/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import eu.etaxonomy.cdm.io.dwca.in.InMemoryMapping.CdmKey;
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
		
		private CdmKey(IdentifiableEntity<?> object){
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
		CdmKey<IdentifiableEntity<?>> cdmKey = new CdmKey(destinationObject);
		putMapping(namespace, sourceKey, cdmKey);
	}
	
	public void putMapping(String namespace, String sourceKey, CdmKey<IdentifiableEntity<?>> cdmKey){
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
		
		keySet.add(cdmKey);
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

	@Override
	public InMemoryMapping getPartialMapping( Map<String, Set<String>> namespacedSourceKeys) {
		InMemoryMapping partialMapping = new InMemoryMapping();
		for (Entry<String,Set<String>> entry  : namespacedSourceKeys.entrySet()){
			String namespace = entry.getKey();
			for (String sourceKey : entry.getValue() ){
				Set<CdmKey> destObjects = this.get(namespace, sourceKey);
				for (CdmKey cdmKey : destObjects){
					partialMapping.putMapping(namespace, sourceKey, cdmKey);
				}
			}
		}
		return partialMapping;
	}


	@Override
	public List<MappingEntry<String, String, Class, Integer>> getEntryList() {
		List<MappingEntry<String, String, Class, Integer>> result = new ArrayList<MappingEntry<String,String,Class,Integer>>();
		for (Entry<String, Map<String, Set<CdmKey>>> namespaceEntry : mapping.entrySet() ){
			String sourceNamespace = namespaceEntry.getKey();
			for (Entry<String, Set<CdmKey>> idEntry : namespaceEntry.getValue().entrySet() ){
				String sourceId = idEntry.getKey();
				for (CdmKey cdmKey : idEntry.getValue()){
					result.add(new MappingEntry<String, String, Class, Integer>(sourceNamespace, sourceId, cdmKey.clazz, cdmKey.id));
				}
			}
		}
		return result;
	}

}
