/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.UUID;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

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
	
	public void put(Integer id, T cdmBase){
		if (service != null){
			throw new RuntimeException();
		}else{
			internalMap.put(id, cdmBase);
		}
	}

	public void put(Integer id, UUID uuid){
		if (service == null){
			throw new RuntimeException();
		}else{
			//TODO
			//service.save(cdmBase);
			internalMap.put(id, uuid);
		}
	}
	
	public T get(Integer id){
		T result;
		if (service == null){
			result = (T)internalMap.get(id);
		}else{
			result = getObjectFromService(id);
		}
		return result;
	}
	
	public boolean containsId(Integer id){
		return internalMap.containsKey(id);
	}
	
	public Collection<T> objects(){
		//TODO from service
		return (Collection<T>)internalMap.values();
	}
	
	private T getObjectFromService(Integer id){
		if (service == null){
			throw new RuntimeException("no service defined");
		}else{
			T result = null;
			UUID uuid = (UUID)internalMap.get(id);
			if (uuid == null){
				result = null;
			}else{
				//TODO
				//result  = (T)service.getObjectUuid(uuid); //.getCdmObjectByUuid(uuid);//  taxonService.getTaxonByUuid(taxonUuid);
			}
			return result;
		}
	}
	
}
