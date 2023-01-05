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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.persistence.hibernate.CdmDataChangeEvent.EventType;

/**
 * @author n.hoffman
 * @since 09.04.2009
 */
public class CdmDataChangeMap  implements Map<EventType, Vector<CdmDataChangeEvent>>{

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private Map<EventType, Vector<CdmDataChangeEvent>> dataChangeMap;

	public CdmDataChangeMap(){
		dataChangeMap = new HashMap<>();
	}

	@Override
    public void clear() {
		dataChangeMap.clear();
	}

	@Override
    public boolean containsKey(Object key) {
		return dataChangeMap.containsKey(key);
	}

	@Override
    public boolean containsValue(Object value) {
		return dataChangeMap.containsValue(value);
	}

	@Override
    public Set entrySet() {
		return dataChangeMap.entrySet();
	}

	@Override
    public Vector<CdmDataChangeEvent> get(Object key) {
		return dataChangeMap.get(key);
	}

	/**
	 * Returns events by type
	 *
	 * @param type
	 * @return never null
	 */
	public Vector<CdmDataChangeEvent> getEvents(EventType type){
		Vector<CdmDataChangeEvent> vector = dataChangeMap.get(type);
		if(vector == null){
		    vector = new Vector<>(0);
		}
        return vector;
	}

	@Override
    public boolean isEmpty() {
		return dataChangeMap.isEmpty();
	}

	@Override
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

	@Override
    public Vector<CdmDataChangeEvent> remove(Object key) {
		return dataChangeMap.remove(key);
	}

	@Override
    public int size() {
		return dataChangeMap.size();
	}

	public int sizeByEventType(EventType type){
		Vector<CdmDataChangeEvent> vector = getEvents(type);
		return vector == null ? 0 : vector.size();
	}

	@Override
    public Collection<Vector<CdmDataChangeEvent>> values() {
		return dataChangeMap.values();
	}

	/**
	 * Returns all change events stored in this change map
	 *
	 * @return
	 */
	public Collection<CdmDataChangeEvent> getAllEvents(){
		Collection<CdmDataChangeEvent> values = new HashSet<>();
		for (EventType type : EventType.values()){
			if(dataChangeMap.get(type) != null){
				for(CdmDataChangeEvent event : dataChangeMap.get(type)){
					values.add(event);
				}
			}
		}
		return values;
	}

	@Override
    public Vector<CdmDataChangeEvent> put(EventType key,
			Vector<CdmDataChangeEvent> value) {
		return dataChangeMap.put(key, value);
	}

	@Override
    public void putAll(
			Map<? extends EventType, ? extends Vector<CdmDataChangeEvent>> t) {
		dataChangeMap.putAll(t);
	}
}