/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.internal.PersistentSortedMap;
import org.hibernate.collection.internal.PersistentSortedSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.model.common.PersistentMultiLanguageText;

/**
 * @author cmathew
 * @date 17 Feb 2015
 *
 */
public class ProxyUtils {



    public static enum CollectionType {
        SET,
        LIST,
        MAP,
        BAG;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public static Object getCollectionType(Object obj, Class<?> clazz) {
        if(obj != null) {
            if(obj instanceof List) {
            	//the field in PersistentBag is called "bag" although it is an ArrayList -> #
            	if(clazz.equals(PersistentBag.class)){
            		return CollectionType.BAG;
            	}
                return CollectionType.LIST;
            }
            if(obj instanceof Set) {
                return CollectionType.SET;
            }
            if(obj instanceof Map) {
                return CollectionType.MAP;
            }
            throw new ProxyUtilsException("Cannot get Collection Type for " + obj.getClass().getName());
        }
        return null;
    }

    public static Object getObject(PersistentCollection pc) {
        if(pc != null) {
            if(pc instanceof PersistentSet) {
                return new HashSet<>((Set<?>)pc);
            }
            if(pc instanceof PersistentSortedSet) {
                return new TreeSet<>((Set<?>)pc);
            }
            if(pc instanceof PersistentList || pc instanceof PersistentBag) {
                return new ArrayList<>((List<?>)pc);
            }
            if(pc instanceof PersistentMap || pc instanceof PersistentMultiLanguageText) {
                return new HashMap<>((Map<?,?>)pc);
            }
            if(pc instanceof PersistentSortedMap) {
                return new TreeMap<>((Map<?,?>)pc);
            }
            throw new ProxyUtilsException("Cannot get Collection field for type " + pc.getClass().getName());
        }
        return null;
    }

    public static CollectionField getCollectionField(PersistentCollection pc) {
        if(pc != null) {
            if(pc instanceof PersistentSet) {
                return new CollectionField(new HashSet<>((Set<?>)pc), CollectionType.SET);
            }
            if(pc instanceof PersistentSortedSet) {
                return new CollectionField(new TreeSet<>((Set<?>)pc), CollectionType.SET);
            }
            if(pc instanceof PersistentList) {
                return new CollectionField(new ArrayList<>((List<?>)pc), CollectionType.LIST);
            }
            if(pc instanceof PersistentMap || pc instanceof PersistentMultiLanguageText) {
                return new CollectionField(new HashMap<>((Map<?,?>)pc), CollectionType.MAP);
            }
            if(pc instanceof PersistentSortedMap) {
                return new CollectionField(new TreeMap<>((Map<?,?>)pc), CollectionType.MAP);
            }
            throw new ProxyUtilsException("Cannot get Collection field for type " + pc.getClass().getName());
        }
        return null;
    }

    public static class CollectionField {
        private final Object col;
        private final CollectionType type;
        public CollectionField(Object col, CollectionType type) {
            this.col = col;
            this.type = type;
        }

        public Object getCollection() {
            return this.col;
        }

        public CollectionType getType() {
            return this.type;
        }
    }

    /**
     * de-proxies the passed object <code>o</code> if it is an initialized proxy object,
     * otherwise <code>o</code> is returned.
     */
    public static Object deproxy(Object o) {
        if(o != null && o instanceof HibernateProxy) {
            LazyInitializer hli = ((HibernateProxy)o).getHibernateLazyInitializer();
            if(!hli.isUninitialized()) {
                return hli.getImplementation();

            }
        }

        if(o != null && o instanceof PersistentCollection) {
            PersistentCollection pc = ((PersistentCollection)o);
            if(pc.wasInitialized()) {
                return  ProxyUtils.getObject(pc);

            }
        }
        return o;
    }

    /**
     * de-proxies the passed object <code>o</code> if it is an initialized proxy object,
     * otherwise <code>null</code> is returned.
     */
    public static Object deproxyOrNull(Object o) {
        if(o != null && o instanceof HibernateProxy) {
            LazyInitializer hli = ((HibernateProxy)o).getHibernateLazyInitializer();
            if(!hli.isUninitialized()) {
                return hli.getImplementation();
            } else {
                return null;
            }
        }

        if(o != null && o instanceof PersistentCollection) {
            PersistentCollection pc = ((PersistentCollection)o);
            if(pc.wasInitialized()) {
                return  ProxyUtils.getObject(pc);
            } else {
                return null;
            }
        }
        return o;
    }

    public static boolean isUninitializedProxy(Object o) {
        if(o != null && o instanceof HibernateProxy) {
            LazyInitializer hli = ((HibernateProxy)o).getHibernateLazyInitializer();
            if(hli.isUninitialized()) {
                return true;
            }
        }

        if(o != null && o instanceof PersistentCollection) {
            PersistentCollection pc = ((PersistentCollection)o);
            if(!pc.wasInitialized()) {
                return true;
            }
        }

        return false;
    }

    /*
     * ########################################### makes no sense without remoting ###########################################
    @SuppressWarnings("unchecked")
	public static Object remoteLoadPersistentCollectionIfProxy(Object o, UUID ownerUuid, String fieldName) throws ClassNotFoundException {
        if(o != null && o instanceof HibernateProxy) {
            LazyInitializer hli = ((HibernateProxy)o).getHibernateLazyInitializer();
            if(hli.isUninitialized()) {
                return CdmApplicationState.getCachedCommonService().find((Class<CdmBase>)Class.forName(hli.getEntityName()),
                        ((Integer)hli.getIdentifier()).intValue());
            }
        }

        if(o != null && o instanceof PersistentCollection) {
            PersistentCollection pc = ((PersistentCollection)o);
            if(!pc.wasInitialized()) {
                return CdmApplicationState.getCachedCommonService().initializeCollection(ownerUuid, fieldName);
            }
        }

        return o;
    }
    ########################################### ########################################### */




    public static void setRoleValueInOwner(Object owner, String role, Object value) {
        if(role == null || role.isEmpty()) {
            throw new ProxyUtilsException("Role cannot be null or an empty string");
        }

        String fieldName = role.substring(role.lastIndexOf(".") + 1);

        Field field = ReflectionUtils.findField(owner.getClass(), fieldName);

        if(field == null) {
            throw new ProxyUtilsException("Field '" + fieldName
                    + "' not found when searching in class '" + owner.getClass() + "' and its supercalsses");
        }

        field.setAccessible(true);

        try {
            field.set(owner, value);
        } catch (IllegalArgumentException e) {
            throw new ProxyUtilsException(e);
        } catch (IllegalAccessException e) {
            throw new ProxyUtilsException(e);
        }
    }

}
