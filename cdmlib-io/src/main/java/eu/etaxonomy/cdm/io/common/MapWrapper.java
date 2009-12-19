/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 *
 */
public class MapWrapper<T extends CdmBase> {
	private static Logger logger = Logger.getLogger(MapWrapper.class);

	private Map internalMap;
	private IService<CdmBase> service = null; 
	
	public MapWrapper(IService<CdmBase> service){
		makeNewMap(service);
	}
	
	public void put(Object id, T cdmBase){
		if (service != null){
			throw new RuntimeException();
		}else{
			internalMap.put(id, cdmBase);
		}
	}

	public void put(Object id, UUID uuid){
		if (service == null){
			throw new RuntimeException();
		}else{
			//TODO
			//service.save(cdmBase);
			internalMap.put(id, uuid);
		}
	}
	
	public T get(Object id){
		T result;
		if (service == null){
			result = (T)internalMap.get(id);
		}else{
			result = getObjectFromService(id);
		}
		return result;
	}
	
	/**
	 * Returns all values that are either stored in the wrapper or the database.
	 * If <code>service</code> is null then only the elements stored in the wrapper are returned. 
	 * @return
	 */
	public Set<T> getAllValues(){
		Set<T> result = new HashSet<T>();
		if (service == null){
			result.addAll(internalMap.values());
		}else{
			result.addAll(internalMap.values());
			logger.warn("getAll not yet implemented !!");
			//TODO Set<T> persitentAll = service.getAll();
			//result.addAll(persistentALl);
		}
		return result;
	}
	
	public boolean containsId(Object id){
		return internalMap.containsKey(id);
	}
	
	public Collection<T> objects(){
		//TODO from service
		return (Collection<T>)internalMap.values();
	}
	
	private T getObjectFromService(Object id){
		if (service == null){
			throw new RuntimeException("no service defined");
		}else{
			T result = null;
			UUID uuid = (UUID)internalMap.get(id);
			if (uuid == null){
				result = null;
			}else{
				//logger.warn(uuid);
				//TODO
				//result  = (T)service.getObjectUuid(uuid); //.getCdmObjectByUuid(uuid);//  taxonService.getTaxonByUuid(taxonUuid);
			}
			return result;
		}
	}
	
	public boolean makeEmpty(){
		return makeNewMap(service);
	}
	
	public boolean makeNewMap(IService<CdmBase> service){
			if (service == null){
				internalMap = new HashMap<Integer, CdmBase>();
			}else{
				this.service = service;
				internalMap =  new HashMap<Integer, UUID>();
			}
			return true;
	}
	
	public int size() {
		return internalMap.size();
	}
	
	public Collection<T> objects(int start, int limit) {
		
		Map internalPartMap = new HashMap<Integer, CdmBase>();
		int index = 0;
		
		for (int i = 0; i < limit; i++) {
			
			int j = start + i;
			
			Object object = internalMap.get(j);
			if(object != null) {
				internalPartMap.put(index, internalMap.get(j));
				index++;
			} else {
				if (logger.isDebugEnabled()) { logger.debug("Object (" + j + ") is null"); }
			}
		}
		return (Collection<T>)internalPartMap.values();
	}
	
		
	public Collection<T> removeObjects(int start, int limit) {
		
		for (int i = start; i < start + limit; i++) {
			internalMap.remove(i);
			if (logger.isDebugEnabled()) { logger.debug("Object (" + i + ") removed"); }
		}
		return (Collection<T>)internalMap.values();
	}

	
	public Set<Object> keySet() {
		return internalMap.keySet();
	}
	
}
