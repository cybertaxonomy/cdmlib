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
 * @since 19 Feb 2015
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
            load(((Pager<?>)obj).getRecords(), recursive, update);
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
            return (T) loadRecursive((MergeResult<CdmBase>)obj, alreadyVisitedEntities, update);
        }

        if (logger.isInfoEnabled()){logger.info("No caching yet for type " + obj.getClass().getName());}

        return obj;
    }

    public <T extends Object> Map<T,T> load(Map<T,T> map, boolean recursive, boolean update){

        if(isRecursiveEnabled && recursive) {
            if (logger.isDebugEnabled()){logger.debug("---- starting recursive load for cdm entity map");}
            List<Object> alreadyVisitedEntities = new ArrayList<>();
            Map<T,T> cachedMap = load(map, alreadyVisitedEntities, update);
            alreadyVisitedEntities.clear();
            if (logger.isDebugEnabled()){logger.debug("---- ending recursive load for cdm entity map \n");}
            return cachedMap;
        } else {
            return load(map, null, update);
        }
    }

    private <T extends Object> Map<T,T> load(Map<T,T> map, List<Object> alreadyVisitedEntities, boolean update){

        if(map == null || map.isEmpty()) {
            return map;
        }

        Object[] result = new Object[ map.size() * 2 ];
        Iterator<Map.Entry<T,T>> iter = map.entrySet().iterator();
        int i=0;
        // to avoid ConcurrentModificationException
        if (alreadyVisitedEntities != null){
            alreadyVisitedEntities.add(map);
        }
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
        for(i = 0; i < result.length; i+=2 ) {
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
            if (logger.isDebugEnabled()){logger.debug("---- starting recursive load for cdm entity collection");}
            List<Object> alreadyVisitedEntities = new ArrayList<>();
            Collection<T> cachedCollection = load(collection, alreadyVisitedEntities, update);
            alreadyVisitedEntities.clear();
            if (logger.isDebugEnabled()){logger.debug("---- ending recursive load for cdm entity collection \n");}
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
        if (alreadyVisitedEntities != null){
            alreadyVisitedEntities.add(collection);
        }
        while(collectionItr.hasNext()) {
            Object obj = collectionItr.next();
            if(alreadyVisitedEntities == null) {
                //AM: does this really ever happen?
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
        return new MergeResult<>(cdmBase, mergeResult.getNewEntities());
    }

    public MergeResult<CdmBase> loadRecursive(MergeResult<CdmBase> mergeResult, List<Object> alreadyVisitedEntities, boolean update) {
        CdmBase cdmBase = loadRecursive(mergeResult.getMergedEntity(), alreadyVisitedEntities, update);
        loadRecursive(mergeResult.getNewEntities(), alreadyVisitedEntities, update);
        return new MergeResult<>(cdmBase, mergeResult.getNewEntities());
    }

    /**
     * Loads the {@link eu.etaxonomy.cdm.model.common.CdmBase cdmEntity}) in the
     * cache.
     * <p>
     * <b>WARNING: Recursive updating of the cached entity will not take place
     * in case there is a cached entity which is the same/identical object as
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
            if (logger.isDebugEnabled()){logger.debug(" - object of type " + cdmEntity.getClass().getSimpleName() + " with id " + cdmEntity.getId() + " already exists");}
            // .. return if the cached and input objects are identical, else (this is a newly loaded object so) continue
            if(cachedCdmEntity == cdmEntity) {
                return cachedCdmEntity;
            }
        }

        T loadedCdmBase;
        if(isRecursiveEnabled && recursive) {
            if (logger.isDebugEnabled()){logger.debug("---- starting recursive load for cdm entity " + cdmEntity.getClass().getSimpleName() + " with id " + cdmEntity.getId());}
            List<Object> alreadyVisitedEntities = new ArrayList<>();
            T cb =  loadRecursive(cdmEntity, alreadyVisitedEntities, update);
            alreadyVisitedEntities.clear();
            if (logger.isDebugEnabled()){logger.debug("---- ending recursive load for cdm entity " + cdmEntity.getClass().getSimpleName() + " with id " + cdmEntity.getId() + "\n");}
            loadedCdmBase =  cb;
        } else {
            loadedCdmBase = putToCache(cdmEntity);
        }
        return loadedCdmBase;

    }

    /**
     * Puts the entity to the cache if it does not yet exist and returns the cached entity.
     */
    protected <T extends CdmBase> T putToCache(T cdmEntity) {
        if (logger.isDebugEnabled()){logger.debug("put object of type " + cdmEntity.getClass().getSimpleName() + " with id " + cdmEntity.getId() + " to cache ");}
        cdmCacher.putToCache(ProxyUtils.deproxy(cdmEntity));
        return cdmCacher.getFromCache(cdmEntity);
    }

    /**
     * Load the <code>cdmEntity</code> graph recursively into the cache and
     * updates entities which are already in the cache depending on the value of
     * <code>update</code>, for more in depth details on this mechanism see
     * {@link #getCdmBaseTypeFieldValue(CdmBase, CdmBase, String, List, boolean)}.
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
     *            The cached object which is identical with the input entity in case
     *            the object did not yet exist in the cache
     */
    private <T extends CdmBase> T loadRecursive(T cdmEntity,  List<Object> alreadyVisitedEntities, boolean update) {
        if (cdmCacher.ignoreRecursiveLoad(cdmEntity)){
            if (logger.isDebugEnabled()){logger.debug("ignore recursive load for " + cdmEntity.getClass() + "#" + cdmEntity.getId());}
            return cdmEntity;
        }
        T cachedCdmEntity = putToCache(cdmEntity);

        // we want to recurse through the cdmEntity (and not the cachedCdmEntity)
        // since there could be new or deleted objects in the cdmEntity sub-graph

        // start by getting the fields from the cdm entity
        //TODO improve generics for deproxyOrNull, probably need to split the method
        @SuppressWarnings("unchecked")
        T deproxiedEntity = (T)ProxyUtils.deproxyOrNull(cdmEntity);
        if(deproxiedEntity == null){
            if (logger.isDebugEnabled()){logger.debug("ignoring uninitlialized proxy " + cdmEntity.getClass() + "#" + cdmEntity.getId());}
        }else{
            String className = deproxiedEntity.getClass().getName();
            CdmModelFieldPropertyFromClass classFields = getFromCdmlibModelCache(className);
            if(classFields != null) {
                alreadyVisitedEntities.add(cdmEntity);
                List<String> fields = classFields.getFields();
                for(String field : fields) {
                    handleField(alreadyVisitedEntities, update, cachedCdmEntity, deproxiedEntity, field);
                }
            } else {
                throw new CdmClientCacheException("CdmEntity with class " + cdmEntity.getClass().getName() + " is not found in the cdmlib model cache. " +
                        "The cache may be corrupted or not in sync with the latest model version" );
            }
        }

        return cachedCdmEntity;
    }

    private <T extends CdmBase> void handleField(List<Object> alreadyVisitedEntities, boolean update, T cachedCdmEntity,
            T deproxiedEntity, String field) {
        // retrieve the actual object corresponding to the field.
        // this object will be either a CdmBase or a Collection / Map
        // with CdmBase as the generic type
        CdmBase cdmEntityInSubGraph = getCdmBaseTypeFieldValue(deproxiedEntity, cachedCdmEntity, field, alreadyVisitedEntities, update);
        if(cdmEntityInSubGraph != null) {
            //checkForIdenticalCdmEntity(alreadyVisitedEntities, cdmEntityInSubGraph);
            if(!entityAlreadyVisisted(alreadyVisitedEntities, cdmEntityInSubGraph)) {
                if (logger.isDebugEnabled()){logger.debug("recursive loading object of type " + cdmEntityInSubGraph.getClass().getSimpleName() + " with id " + cdmEntityInSubGraph.getId());}
                loadRecursive(cdmEntityInSubGraph, alreadyVisitedEntities, update);
            } else {
                if (logger.isDebugEnabled()){logger.debug("object of type " + cdmEntityInSubGraph.getClass().getSimpleName() + " with id " + cdmEntityInSubGraph.getId() + " already visited");}
            }
        }
    }

    /**
     * All fields of the <code>cdmEntity</code> containing proxy objects will be
     * set to the un-proxied field value. If <code>update</code> is enabled the
     * value of the cached entity will be overwritten by the value of the
     * <code>cdmEntity</code>. In case the cached field value contains a proxy
     * object the value will always be overwritten (Q: This might only occur in
     * case of uninitialized proxies, since initialized proxies are expected to
     * be replaced by the target entity.)
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
                        + "' not found when searching in class '" + clazz.getName() + "' and its superclasses");
            }
            field.setAccessible(true);
            Object obj = field.get(cdmEntity);
            // resetting the value in cdm entity to the deproxied object
            obj = ProxyUtils.deproxy(obj);
            field.set(cdmEntity, obj);
            Object cachedObj = field.get(cachedCdmEntity);
            CdmBase cdmEntityInSubGraph = null;

            if(!ProxyUtils.isUninitializedProxy(obj) && (update || ProxyUtils.isUninitializedProxy(cachedObj))) {
                // if we are in update mode we have to make the field of the cached entity
                // up-to-date by setting it to the value of the cdm entity being loaded
                //
                // if the cdm entity is a proxy then we always update to make sure that
                // newly created entities are always up-to-date
                //
                // NOTE : the field is overridden in the case of the exception
                // found below
                field.set(cachedCdmEntity, obj);
            }

            if(obj != null && !ProxyUtils.isUninitializedProxy(obj)) {
                if(CdmBase.class.isAssignableFrom(obj.getClass())) {
                    if (logger.isDebugEnabled()){logger.debug("found initialised cdm entity '" + fieldName + "' in object of type " + clazz.getSimpleName() + " with id " + cdmEntity.getId());}

                    cdmEntityInSubGraph = (CdmBase)obj;
                    CdmBase cachedCdmEntityInSubGraph = cdmCacher.getFromCache(cdmEntityInSubGraph);

                    if(cachedCdmEntityInSubGraph != null) {
                        if(cachedCdmEntityInSubGraph != cdmEntityInSubGraph) {
                            // exception : is the case where
                            // the field has been already initialized, cached and
                            // is not the same as the one in the cache, in which case we set the value
                            // of the field to the one found in the cache
                            if (logger.isDebugEnabled()){logger.debug("setting cached + real value to '" + fieldName + "' in object of type " + clazz.getSimpleName() + " with id " + cdmEntity.getId());}
                            field.set(cachedCdmEntity, cachedCdmEntityInSubGraph);
                            field.set(cdmEntity, cachedCdmEntityInSubGraph);
                        } else {
                            // since the field value object in cdmEntity
                            // is the same as the field value object in cachedCdmEntity
                            // we are sure that the subgraph is also correctly loaded,
                            // so we can exit the recursion
                            return null;
                        }
                    }
                } else if(obj instanceof Map && !entityAlreadyVisisted(alreadyVisitedEntities, obj)) {
                    loadRecursive((Map<?,?>)obj, alreadyVisitedEntities, update);
                } else if(obj instanceof Collection && !entityAlreadyVisisted(alreadyVisitedEntities, obj)) {
                    loadRecursive((Collection<?>)obj, alreadyVisitedEntities, update);
                }
            }
            // we return the original cdm entity in the sub graph because we
            // want to continue to recurse on the input cdm entity graph
            // and not the one in the cache
            return cdmEntityInSubGraph;
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new CdmClientCacheException(e);
        }
    }

    private boolean entityAlreadyVisisted(List<Object> objList, Object objToCompare) {
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

    public static void  setRecursiveEnabled(boolean recursiveEnabled) {
        isRecursiveEnabled = recursiveEnabled;
    }
}
