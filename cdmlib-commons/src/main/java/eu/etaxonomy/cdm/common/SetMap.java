/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class wraps a {@link Map} with a {@link Set} as value.
 * It offers convenience methods to avoid Set creation for the user.
 *
 * @author a.mueller
 * @since 06.02.2021
 */
public class SetMap<K,V> implements Map<K, Set<V>>{

    final private Map<K,Set<V>> map;

    public SetMap() {
        map = new HashMap<>();
    }

    /**
     * @param size the initial size of the internal map
     */
    public SetMap(int size) {
        this.map = new HashMap<>(size);
    }

    @Override
    public int size() {
        return map.size();
    }

    public int sizeAll() {
        int result = 0;
        for (Set<V> set : map.values()){
            result += set.size();
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsKey(value);
    }

    /**
     * True, if any of the value sets contains the given item
     * @param item
     * @return <code>true</code> if item exists in one of the {@link Set}s
     */
    public boolean containsValueItem(V item) {
        for (Entry<K,Set<V>> entry: map.entrySet()){
            if (entry.getValue() != null && entry.getValue().contains(item)){
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Note: The implementation of this method in this class is,
     * that if no set exists yet for key and if key is instanceof K
     * an empty Set will be returned which will be stored in the internal map
     * and can therefore be used externally to add values if needed.
     */
    @Override
    public Set<V> get(Object key) {
        Set<V> result = map.get(key);
        if (result == null){
            result = new HashSet<>();
            try {
                @SuppressWarnings("unchecked")
                K k = (K)key;
                map.put(k, result);
            } catch (Exception e) {
                //if key is not of type K we return null
                return null;
            }
        }
        return result;
    }

    @Override
    public Set<V> put(K key, Set<V> value) {
        return map.put(key, value);
    }

    /**
     * Convenience method to avoid that the user has to care for creation
     * of the {@link Set} if it is <code>null</code>.
     *
     * @param key the
     * @param value the value to add to the value set
     * @return <code>true</code> if the value already existed in the set.
     */
    public boolean putItem(K key, V value){
        Set<V> set = map.get(key);
        if (set == null){
            set = new HashSet<>();
            this.map.put(key, set);
        }
        return set.add(value);
    }

    @Override
    public Set<V> remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Set<V>> m) {
        map.putAll(m);//TODO

    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Set<V>> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<K, Set<V>>> entrySet() {
        return map.entrySet();
    }

}
