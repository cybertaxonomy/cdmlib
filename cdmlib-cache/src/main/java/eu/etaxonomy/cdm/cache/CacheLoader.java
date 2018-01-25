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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.ICdmCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * @author cmathew
 * @date 19 Feb 2015
 *
 */
public class CacheLoader {
    private static final Logger logger = Logger.getLogger(CacheLoader.class);

    private static boolean isRecursiveEnabled = true;

    protected final ICdmCacher cdmCacher;

    private final Cache cdmlibModelCache;



    public CacheLoader(ICdmCacher cdmCacher) {
        this.cdmCacher = cdmCacher;
        this.cdmlibModelCache = CdmRemoteCacheManager.getInstance().getCdmModelGetMethodsCache();

    }


    public CdmModelFieldPropertyFromClass getFromCdmlibModelCache(String className) {
        Element e = cdmlibModelCache.get(className);
        if (e == null) {
            return null;
        } else {
            return (CdmModelFieldPropertyFromClass) e.getObjectValue();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T load(T obj, boolean recursive, boolean update) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof CdmBase) {
            return (T) load((CdmBase)obj, recursive, update);
        } else if (obj instanceof Map) {
            return (T) load((Map<T,T>)obj, recursive, update);
        } else if (obj instanceof Collection) {
            return (T) load((Collection<T>)obj, recursive, update);
        } else if(obj instanceof Pager) {
        	load(((Pager)obj).getRecords(), recursive, update);
            return obj;
        } else if(obj instanceof MergeResult) {
            return (T) load((MergeResult<CdmBase>)obj, recursive, update);
        }

        return obj;
    }

    @SuppressWarnings("unchecked")
    private <T extends Object> T loadRecursive(T obj, List<Object> alreadyVisitedEntities, boolean update) {
        if(obj == null) {
            return null;
        }
        if(obj instanceof CdmBase) {
            return (T) loadRecursive((CdmBase)obj, alreadyVisitedEntities, update);
        } else if (obj instanceof Map) {
            return (T) load((Map<T,T>)obj, alreadyVisitedEntities, update);
        } else if (obj instanceof Collection) {
            return (T) load((Collection<T>)obj, alreadyVisitedEntities, update);
        } else if (obj instanceof MergeResult) {
            return (T) loadRecursive((MergeResult)obj, alreadyVisitedEntities, update);
        }


        logger.info("No caching yet for type " + obj.getClass().getName());

        return obj;
    }

    public <T extends Object> Map<T,T> load(Map<T,T> map, boolean recursive, boolean update){


        if(isRecursiveEnabled && recursive) {
            logger.debug("---- starting recursive load for cdm entity map");
            List<Object> alreadyVisitedEntities = new ArrayList<Object>();
            Map<T,T> cachedMap = load(map, alreadyVisitedEntities, update);
            alreadyVisitedEntities.clear();
            logger.debug("---- ending recursive load for cdm entity map \n");
            return cachedMap;
        } else {
            return load(map, null, update);
        }
    }


    private <T extends Object> Map<T,T> load(Map<T,T> map, List<Object> alreadyVisitedEntities, boolean update){
        //map = (Map<T,T>)deproxy(map);

        if(map == null || map.isEmpty()) {
            return map;
        }

        int originalMapSize = map.size();
        Object[] result = new Object[ map.size() * 2 ];
        Iterator<Map.Entry<T,T>> iter = map.entrySet().iterator();
        int i=0;
        // to avoid ConcurrentModificationException
        alreadyVisitedEntities.add(map);
        while ( iter.hasNext() ) {
            Map.Entry<T,T> e = iter.next();
            result[i++] = e.getKey();
            result[i++] = e.getValue();
        }

        for(i=0; i<result.length;i++) {
            if(alreadyVisitedEntities == null) {
                result[i] = load(result[i], false, update);
            } else {
                result[i] = loadRecursive(result[i], alreadyVisitedEntities, update);
            }
        }
        map.clear();
        for(i = 0; i < originalMapSize; i+=2 ) {
            map.put(
                    (T)result[i],
                    (T)result[i+1]
                    );
        }
        return map;
    }

    public <T extends Object> Collection<T> load(Collection<T> collection,  boolean recursive, boolean update){

        Collection<T> loadedCollection;
        if(isRecursiveEnabled && recursive) {
            logger.debug("---- starting recursive load for cdm entity collection");
            List<Object> alreadyVisitedEntities = new ArrayList<Object>();
            Collection<T> cachedCollection = load(collection, alreadyVisitedEntities, update);
            alreadyVisitedEntities.clear();
            logger.debug("---- ending recursive load for cdm entity collection \n");
            loadedCollection = cachedCollection;
        } else {
            loadedCollection = load(collection, null, update);
        }
        return loadedCollection;
    }

    @SuppressWarnings("unchecked")
    private <T extends Object> Collection<T> load(Collection<T> collection, List<Object> alreadyVisitedEntities, boolean update) {



        if(collection == null || collection.isEmpty()) {
            return collection;
        }
        int length = collection.size();
        Object[] result = new Object[length];
        Iterator<T> collectionItr = collection.iterator();
        int count = 0;
        // to avoid ConcurrentModificationException
        alreadyVisitedEntities.add(collection);
        while(collectionItr.hasNext()) {
            Object obj = collectionItr.next();
            if(alreadyVisitedEntities == null) {
                result[count] = load(obj, false, update);
            } else {
                result[count] = loadRecursive(obj, alreadyVisitedEntities, update);
            }

            count++;
        }

        collection.clear();

        for ( int i = 0; i < length; i++ ) {
            collection.add((T)result[i]);
        }

        return collection;
    }


    public MergeResult<CdmBase> load(MergeResult<CdmBase> mergeResult, boolean recursive, boolean update) {
        CdmBase cdmBase = load(mergeResult.getMergedEntity(), recursive, update);
        load(mergeResult.getNewEntities(), recursive, update);
        return new MergeResult(cdmBase, mergeResult.getNewEntities());
    }

    public MergeResult<CdmBase> loadRecursive(MergeResult<CdmBase> mergeResult,List<Object> alreadyVisitedEntities, boolean update) {
        CdmBase cdmBase = loadRecursive(mergeResult.getMergedEntity(), alreadyVisitedEntities, update);
        loadRecursive(mergeResult.getNewEntities(), alreadyVisitedEntities, update);
        return new MergeResult(cdmBase, mergeResult.getNewEntities());
    }

    /**
     * Loads the {@link eu.etaxonomy.cdm.model.common.CdmBase cdmEntity}) in the
     * cache.
     * <p>
     * <b>WARNING: Recursive updating of the cached entity will not take place
     * in case there is a cached entity which is the same object as
     * <code>cdmEntity</code>.</b>
     *
     * For in depth details on the mechanism see
     * {@link #loadRecursive(CdmBase, List, boolean)} and
     * {@link #getCdmBaseTypeFieldValue(CdmBase, CdmBase, String, List, boolean)}
     *
     * @param cdmEntity
     *            the entity to be put into the cache
     * @param recursive
     *            if <code>true</code>, the cache loader will load the whole
     *            entity graph recursively into the cache
     * @param update
     *            all fields of the cached entity will be overwritten by setting
     *            them to the value of the cdm entity being loaded
     */
    public <T extends CdmBase> T load(T cdmEntity, boolean recursive, boolean update) {
        if(cdmEntity == null) {
            return null;
        }

        // start by looking up the cdm entity in the cache
        T cachedCdmEntity = cdmCacher.getFromCache(cdmEntity);

        if(cachedCdmEntity != null) {
            // if cdm entity was found in cache then
            logger.debug(" - object of type " + cdmEntity.getClass().getName() + " with id " + cdmEntity.getId() + " already exists");
            // .. return if the cached and input objects are identical, else (this is a newly loaded object so) continue
            if(cachedCdmEntity == cdmEntity) {
                return cachedCdmEntity;
            }
        }

        CdmBase loadedCdmBase;
        if(isRecursiveEnabled && recursive) {
            logger.debug("---- starting recursive load for cdm entity " + cdmEntity.getClass().getName() + " with id " + cdmEntity.getId());
            List<Object> alreadyVisitedEntities = new ArrayList<Object>();
            CdmBase cb =  loadRecursive(cdmEntity, alreadyVisitedEntities, update);
            alreadyVisitedEntities.clear();
            logger.debug("---- ending recursive load for cdm entity " + cdmEntity.getClass().getName() + " with id " + cdmEntity.getId() + "\n");
            loadedCdmBase =  cb;
        } else {
            loadedCdmBase = load(cdmEntity);
        }
        return (T) loadedCdmBase;

    }


    protected CdmBase load(CdmBase cdmEntity) {
        logger.debug("loading object of type " + cdmEntity.getClass().getName() + " with id " + cdmEntity.getId());
        cdmCacher.put((CdmBase)ProxyUtils.deproxy(cdmEntity));
        return cdmCacher.getFromCache(cdmEntity);
    }


    /**
     * Load the <code>cdmEntity</code> graph recursively into the cache and
     * updates entity which are already in the cache depending on the value of
     * <code>update</code>, for more in depth details on this mechanism see
     * {@link #getCdmBaseTypeFieldValue(CdmBase, CdmBase, String, List, boolean)}.
     *
     *
     * @param cdmEntity
     *            the entity to be loaded into the cache
     * @param alreadyVisitedEntities
     *            protocol list of entities already visited during loading an
     *            entity graph recursively into the cache.
     *
     * @param update
     *            all fields of the cached entity will be overwritten by setting
     *            them to the value of the cdm entity being loaded
     * @return
     */
    private CdmBase loadRecursive(CdmBase cdmEntity,  List<Object> alreadyVisitedEntities, boolean update) {

        CdmBase cdmEntityDeproxied = (CdmBase)ProxyUtils.deproxyOrNull(cdmEntity);
        if(cdmEntityDeproxied == null){
            return cdmEntity;
        } else {
            cdmEntity = cdmEntityDeproxied;
        }

        CdmBase cachedCdmEntity = load(cdmEntity);

        // we want to recursive through the cdmEntity (and not the cachedCdmEntity)
        // since there could be new or deleted objects in the cdmEntity sub-graph

        // start by getting the fields from the cdm entity
        String className = cdmEntity.getClass().getName();
        CdmModelFieldPropertyFromClass cmgmfc = getFromCdmlibModelCache(className);
        if(cmgmfc != null) {
            alreadyVisitedEntities.add(cdmEntity);
            List<String> fields = cmgmfc.getFields();
            for(String field : fields) {
                // retrieve the actual object corresponding to the field.
                // this object will be either a CdmBase or a Collection / Map
                // with CdmBase as the generic type

                CdmBase cdmEntityInSubGraph = getCdmBaseTypeFieldValue(cdmEntity, cachedCdmEntity, field, alreadyVisitedEntities, update);
                if(cdmEntityInSubGraph != null) {
                    //checkForIdenticalCdmEntity(alreadyVisitedEntities, cdmEntityInSubGraph);
                    if(!checkForIdenticalCdmEntity(alreadyVisitedEntities, cdmEntityInSubGraph)) {
                        logger.debug("recursive loading object of type " + cdmEntityInSubGraph.getClass().getName() + " with id " + cdmEntityInSubGraph.getId());
                        loadRecursive(cdmEntityInSubGraph, alreadyVisitedEntities, update);
                    } else {
                        logger.debug("object of type " + cdmEntityInSubGraph.getClass().getName() + " with id " + cdmEntityInSubGraph.getId() + " already visited");
                    }
                }
            }
        } else {
            throw new CdmClientCacheException("CdmEntity with class " + cdmEntity.getClass().getName() + " is not found in the cdmlib model cache. " +
                    "The cache may be corrupted or not in sync with the latest model version" );
        }

        return cachedCdmEntity;
    }

    /**
     * All field of the <code>cdmEntity</code> containing proxy objects will be
     * set to the un-proxied field value. If <code>update</code> is enabled the
     * value of the cached entity will be overwritten by the value of the
     * <code>cdmEntity</code>. In case the cached field value contains a proxy
     * object the value will aways be overwritten (Q: This might only occur in
     * case of uninitialized proxies, since initialized proxies are expected to
     * be replaces by the target entity.)
     *
     * @param cdmEntity
     *            the entity to be loaded into the cache
     * @param cachedCdmEntity
     *            the entity which resides in the cache
     * @param fieldName
     *            the field name to operate on
     * @param alreadyVisitedEntities
     *            protocol list of entities already visited during loading an
     *            entity graph recursively into the cache.
     * @param update
     *            all fields of the cached entity will be overwritten by setting
     *            them to the value of the cdm entity being loaded
     * @return
     */
    private CdmBase getCdmBaseTypeFieldValue(CdmBase cdmEntity,
            CdmBase cachedCdmEntity,
            String fieldName,
            List<Object> alreadyVisitedEntities,
            boolean update) {

        // this method attempts to make sure that for any two objects found in
        // the object graph, if they are equal then they should also be the same,
        // which is crucial for the merge to work
        if(cachedCdmEntity == null) {
            throw new CdmClientCacheException("When trying to set field value, the cached cdm entity cannot be null");
        }

        Class<?> clazz = cdmEntity.getClass();
        try {
            // this call will search in the provided class as well as
            // the super classes until it finds the field
            Field field = ReflectionUtils.findField(clazz, fieldName);

            if(field == null) {
                throw new CdmClientCacheException("Field '" + fieldName
                        + "' not found when searching in class '" + clazz.getName() + "' and its supercalsses");
            }
            field.setAccessible(true);
            Object o = field.get(cdmEntity);
            // resetting the value in cdm entity to the deproxied object
            o = ProxyUtils.deproxy(o);
            field.set(cdmEntity, o);
            Object cachedo = field.get(cachedCdmEntity);
            CdmBase cdmEntityInSubGraph = null;

            if(!ProxyUtils.isUninitializedProxy(o) && (update || ProxyUtils.isUninitializedProxy(cachedo))) {
                // if we are in update mode we have to make the field of the cached entity
                // up-to-date by setting it to the value of the cdm entity being loaded
                //
                // if the cdm entity is a proxy then we always update to make sure that
                // newly created entities are always up-to-date
                //
                // NOTE : the field is overridden in the case of the exception
                // found below
                field.set(cachedCdmEntity, o);

            }

            if(o != null && !ProxyUtils.isUninitializedProxy(o)) {
                if(CdmBase.class.isAssignableFrom(o.getClass())) {
                    logger.debug("found initialised cdm entity '" + fieldName + "' in object of type " + clazz.getName() + " with id " + cdmEntity.getId());

                    cdmEntityInSubGraph  = (CdmBase)o;
                    CdmBase cachedCdmEntityInSubGraph = cdmCacher.getFromCache(cdmEntityInSubGraph);

                    if(cachedCdmEntityInSubGraph != null) {
                        if(cachedCdmEntityInSubGraph != cdmEntityInSubGraph) {
                            // exception : is the case where
                            // the field has been already initialised, cached and
                            // is not the same as the one in the cache, in which case we set the value
                            // of the field to the one found in the cache
                            logger.debug("setting cached + real value to '" + fieldName + "' in object of type " + clazz.getName() + " with id " + cdmEntity.getId());
                            field.set(cachedCdmEntity, cachedCdmEntityInSubGraph);
                            field.set(cdmEntity, cachedCdmEntityInSubGraph);
                        } else {
                            // since the field value object in cdmEntity
                            // is the same as the field value object in cachedCdmEntity
                            // we are sure that the its subgraph is also correctly loaded,
                            // so we can exit the recursion
                            return null;
                        }
                    }
                } else if(o instanceof Map && !checkForIdenticalCdmEntity(alreadyVisitedEntities, o)) {
                    loadRecursive((Map)o, alreadyVisitedEntities, update);
                } else if(o instanceof Collection && !checkForIdenticalCdmEntity(alreadyVisitedEntities, o)) {
                    loadRecursive((Collection)o, alreadyVisitedEntities, update);
                }
            }
            // we return the original cdm entity in the sub graph because we
            // want to continue to recurse on the input cdm entity graph
            // and not the one in the cache
            return cdmEntityInSubGraph;
        } catch (SecurityException e) {
            throw new CdmClientCacheException(e);
        } catch (IllegalArgumentException e) {
            throw new CdmClientCacheException(e);
        } catch (IllegalAccessException e) {
            throw new CdmClientCacheException(e);
        }
    }

    private boolean checkForIdenticalCdmEntity(List<Object> objList, Object objToCompare) {
        if(objToCompare != null) {
            for(Object obj : objList) {
                if(obj == objToCompare) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean isRecursiveEnabled() {
        return isRecursiveEnabled;
    }

    public static void  setRecursiveEnabled(boolean ire) {
        isRecursiveEnabled = ire;
    }
}
