/**
 * 
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
		if (service == null){
			internalMap = new HashMap<Integer, CdmBase>();
		}else{
			this.service = service;
			internalMap =  new HashMap<Integer, UUID>();
		}
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
	 * Returns all values that are either stored in the wrapper or the the database.
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
	
}
