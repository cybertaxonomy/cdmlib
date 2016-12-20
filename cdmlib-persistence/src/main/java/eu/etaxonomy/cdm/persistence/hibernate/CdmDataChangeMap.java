/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.persistence.hibernate.CdmDataChangeEvent.EventType;

/**
 * @author n.hoffman
 * @created 09.04.2009
 * @version 1.0
 */
public class CdmDataChangeMap  implements Map<EventType, Vector<CdmDataChangeEvent>>{
	private static final Logger logger = Logger
			.getLogger(CdmDataChangeMap.class);
	
	private Map<EventType, Vector<CdmDataChangeEvent>> dataChangeMap;

	public CdmDataChangeMap(){
		dataChangeMap = new HashMap<EventType, Vector<CdmDataChangeEvent>>();
	}
	
	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		dataChangeMap.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return dataChangeMap.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		return dataChangeMap.containsValue(value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet() {
		return dataChangeMap.entrySet();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Vector<CdmDataChangeEvent> get(Object key) {
		return dataChangeMap.get(key);
	}
	
	/**
	 * Returns events by type
	 * 
	 * @param type
	 * @return
	 */
	public Vector<CdmDataChangeEvent> getEvents(EventType type){
		return dataChangeMap.get(type);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return dataChangeMap.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	public Set keySet() {
		return dataChangeMap.keySet();
	}
	
	public void add(EventType type, CdmDataChangeEvent event){
		Vector<CdmDataChangeEvent> vector = getEvents(type);
		
		// lazy initialising the vectors
		if(vector == null){
			vector = new Vector<CdmDataChangeEvent>();
		}
		
		vector.add(event);
		
		dataChangeMap.put(type, vector);
	}


	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Vector<CdmDataChangeEvent> remove(Object key) {
		return dataChangeMap.remove(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	public int size() {
		return dataChangeMap.size();
	}
	
	public int sizeByEventType(EventType type){
		Vector<CdmDataChangeEvent> vector = getEvents(type);
		return vector == null ? 0 : vector.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	public Collection<Vector<CdmDataChangeEvent>> values() {
		return dataChangeMap.values();
	}
	
	/**
	 * Returns all change events stored in this change map
	 * 
	 * @return
	 */
	public Collection<CdmDataChangeEvent> getAllEvents(){
		Collection<CdmDataChangeEvent> values = new HashSet<CdmDataChangeEvent>();
		for (EventType type : EventType.values()){
			if(dataChangeMap.get(type) != null){
				for(CdmDataChangeEvent event : dataChangeMap.get(type)){
					values.add(event);
				}
			}
		}
		return values;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Vector<CdmDataChangeEvent> put(EventType key,
			Vector<CdmDataChangeEvent> value) {
		return dataChangeMap.put(key, value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(
			Map<? extends EventType, ? extends Vector<CdmDataChangeEvent>> t) {
		dataChangeMap.putAll(t);
	}
}
