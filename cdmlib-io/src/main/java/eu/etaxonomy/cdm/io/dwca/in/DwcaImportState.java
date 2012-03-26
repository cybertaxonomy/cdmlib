// $Id$
/**
* Copyright (C) 2007 EDIT
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
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.dwca.in.IImportMapping.CdmKey;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @created 23.11.2011
 */
public class DwcaImportState extends ImportStateBase<DwcaImportConfigurator, DwcaImport>{
	private static final Logger logger = Logger.getLogger(DwcaImportState.class);

	boolean taxaCreated;
	private Map<String, Map<String, IdentifiableEntity>> partitionStore;
	
	private IImportMapping mapping = new InMemoryMapping();
	
	public DwcaImportState(DwcaImportConfigurator config) {
		super(config);
	}

	/**
	 * True, if taxa have been fully created.
	 * @return
	 */
	public boolean isTaxaCreated() {
		return taxaCreated;
		
	}

	/**
	 * @param taxaCreated the taxaCreated to set
	 */
	public void setTaxaCreated(boolean taxaCreated) {
		this.taxaCreated = taxaCreated;
	}
	
//********************* MAPPING ACCESS *********************************
	//TODO this may move to an external class soon
	
	public void putMapping(String namespace, Integer sourceKey, IdentifiableEntity<?> destinationObject){
		mapping.putMapping(namespace, sourceKey, destinationObject);
	}
		
	public void putMapping(String namespace, String sourceKey, IdentifiableEntity<?> destinationObject){
		mapping.putMapping(namespace, sourceKey, destinationObject);
	}
	
	
	public List<IdentifiableEntity> get(String namespace, String sourceKey){
		return get(namespace, sourceKey, null);
	}
	
	public <CLASS extends IdentifiableEntity> List<CLASS> get(String namespace, String sourceKey,Class<CLASS> destinationClass){
		List<CLASS> result = new ArrayList<CLASS>(); 
		if (this.partitionStore != null){
			Map<String, IdentifiableEntity> namespaceMap = this.partitionStore.get(namespace);
			if (namespaceMap != null){
				IdentifiableEntity cdmBase = namespaceMap.get(sourceKey);
				if (cdmBase == null){
					logger.warn("CdmBase is null");
				}else if (cdmBase.isInstanceOf(destinationClass)){
					CLASS typedCdmBase = CdmBase.deproxy(cdmBase, destinationClass);
					result.add(typedCdmBase);
				}
				
			}
		}else{
			Set<CdmKey> keySet = mapping.get(namespace, sourceKey);
			for (CdmKey<CLASS> key: keySet){
				if (destinationClass == null || destinationClass.isAssignableFrom(key.clazz)){
					IIdentifiableEntityService<CLASS> service = getCurrentIO().getServiceByClass(key.clazz);
					CLASS entity = CdmBase.deproxy(service.find(key.id), key.clazz);
					result.add(entity);
				}
			}
			return result;
		}
		return result;
	}

	public boolean exists(String namespace, String sourceKey,Class<?> destinationClass){
		return mapping.exists(namespace, sourceKey, destinationClass);
	}
	
	
	public  void loadRelatedObjects (IImportMapping mapping){
		Map<String, Map<String, IdentifiableEntity>> result = new HashMap<String, Map<String,IdentifiableEntity>>();
		
		List<MappingEntry<String, String, Class, Integer>> mappingEntryList = mapping.getEntryList();
		
		//order ids by destination classes
		Map<Class, Set<Integer>> destinationNamespaceMap = new HashMap<Class, Set<Integer>>(); 
		for (MappingEntry<String, String, Class, Integer> entry : mappingEntryList){
			Set<Integer> idSet = destinationNamespaceMap.get(entry.destinationNamespace);
			if (idSet == null){
				idSet = new HashSet<Integer>();
				destinationNamespaceMap.put(entry.destinationNamespace, idSet);
			}
			idSet.add(entry.destinationId);
		}
		
		//retrieve cdm objects per class
		Map<Class, Map<Integer, IdentifiableEntity>> classMap = new HashMap<Class, Map<Integer,IdentifiableEntity>>();
		for (Class<?> cdmClass :destinationNamespaceMap.keySet()){
			IIdentifiableEntityService<?> classService = getCurrentIO().getServiceByClass(cdmClass);
			Set<Integer> idSet = destinationNamespaceMap.get(cdmClass);
			List<? extends IdentifiableEntity> relatedObjects = classService.findById(idSet);
			
			//put into id map
			Map<Integer, IdentifiableEntity> idMap = new HashMap<Integer, IdentifiableEntity>();
			for (IdentifiableEntity identEnt : relatedObjects){
				idMap.put(identEnt.getId(), identEnt);
			}
			
			//add to class map
			classMap.put(cdmClass, idMap);
		}
		
		//fill related object map
		for (MappingEntry<String, String, Class, Integer> entry : mappingEntryList){
			Map<String, IdentifiableEntity> namespaceMap = getOrMakeNamespaceMap(result, entry.namespace);
			IdentifiableEntity cdmBase = getCdmObject(classMap, entry);
			if (cdmBase != null){
				namespaceMap.put(entry.sourceKey, cdmBase);
			}else{
				logger.info("CdmBase not found for mapping entry.");
			}
		}
		
		//store
		this.partitionStore = result;
		
	}


//	public Map<String, Map<String, IdentifiableEntity>> getPartitionStore() {
//		return partitionStore;
//	}

	public void unloadPartitionStore(Map<String, Map<String, IdentifiableEntity>> partitionStore) {
		this.partitionStore = new HashMap<String, Map<String,IdentifiableEntity>>();
	}

	public IImportMapping getMapping() {
		return this.mapping;
	}

	
	private Map<String, IdentifiableEntity> getOrMakeNamespaceMap(Map<String, Map<String, IdentifiableEntity>> relatedObjectMap2, String namespace) {
		Map<String, IdentifiableEntity> namespaceMap = relatedObjectMap2.get(namespace);
		if (namespaceMap == null){
			namespaceMap = new HashMap<String, IdentifiableEntity>();
			relatedObjectMap2.put(namespace, namespaceMap);
		}
		return namespaceMap;
	}
	

	private IdentifiableEntity getCdmObject(Map<Class, Map<Integer, IdentifiableEntity>> classMap,
			MappingEntry<String, String, Class, Integer> entry) {
		Class<?> cdmClass = entry.destinationNamespace;
		Integer cdmKey = entry.destinationId;
		Map<Integer, IdentifiableEntity> idMap = classMap.get(cdmClass);
		if (idMap != null){
			return idMap.get(cdmKey);
		}else{
			return null;
		}
	}
	
	


}
