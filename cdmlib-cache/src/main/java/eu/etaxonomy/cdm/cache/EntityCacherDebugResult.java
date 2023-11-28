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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.api.cache.CdmPermanentCacheBase;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @author cmathew
 * @since 9 Feb 2015
 */
public class EntityCacherDebugResult {

    private static final Logger logger = LogManager.getLogger();

    private Map<CdmEntityInfo, CdmEntityInfo> duplicateCdmEntityMap;

    private List<CdmEntityInfo> notInCacheList;

    private CdmTransientEntityCacher cacher;

    private List<CdmEntityInfo> rootElements;

    StringBuilder debugOutput = new StringBuilder();

    public EntityCacherDebugResult() {
    }

    public <T extends CdmBase> EntityCacherDebugResult(CdmTransientEntityCacher cacher, Collection<T> rootEntities, boolean includeIgnored) {
        this.cacher = cacher;
        init();

        if(rootEntities != null && !rootEntities.isEmpty()) {
            for(CdmBase rootEntity : rootEntities) {
                debug(rootEntity, true, includeIgnored);
                String out = toString(duplicateCdmEntityMap, notInCacheList, rootEntity);
                //System.out.println(out);
                debugOutput.append(out);
                clear();
            }
        }
    }

    private void init() {
        duplicateCdmEntityMap = new HashMap<>();
        notInCacheList = new ArrayList<>();
        rootElements = new ArrayList<>();
    }

    private void clear() {
        duplicateCdmEntityMap.clear();
        notInCacheList.clear();
    }

    private void addDuplicateInGraphEntity(CdmEntityInfo cei, CdmEntityInfo cachedCei) {
        duplicateCdmEntityMap.put(cei, cachedCei);
    }

    private void addEntityNotInCache(CdmEntityInfo cei) {
        notInCacheList.add(cei);
    }

    public List<CdmEntityInfo> getRootElements() {
        return rootElements;
    }

    private void print(Map<CdmEntityInfo, CdmEntityInfo> duplicateCdmEntityMap,
            List<CdmEntityInfo> notInCacheList,
            CdmBase rootEntity) {
        System.out.println(toString(duplicateCdmEntityMap, notInCacheList, rootEntity));
    }

    @Override
    public String toString() {
        return debugOutput.toString();
    }

    private String toString(Map<CdmEntityInfo, CdmEntityInfo> duplicateCdmEntityMap,
            List<CdmEntityInfo> notInCacheList,
            CdmBase rootEntity) {

        StringBuilder sb = new StringBuilder();
        toStringDuplicatesInGraph(duplicateCdmEntityMap, rootEntity, sb);

        sb.append(System.getProperty("line.separator"));

        toStringNotInCache(notInCacheList, sb);

        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }

    private void toStringNotInCache(List<CdmEntityInfo> notInCacheList, StringBuilder sb) {
        sb.append(System.getProperty("line.separator"));

        if(notInCacheList.isEmpty()) {
            sb.append("No entities found in root graph which are not in Cache.");
        } else {
            sb.append("Entities in root graph which are not in cache (");
            sb.append(NotInCacheType.NOT_FOUND.getLabel() + ": " + NotInCacheType.NOT_FOUND.name() + ", ");
            sb.append(NotInCacheType.COPY_ENTITY.getLabel() + ": " + NotInCacheType.COPY_ENTITY.name() + "):");

            for(CdmEntityInfo cei : notInCacheList) {
                CdmBase cb = (CdmBase) cei.getObject();
                CdmEntityInfo parentCei = cei.getParent();

                sb.append(System.getProperty("line.separator"));

                String fieldName = "";
                if(cei.getField() != null) {
                    fieldName = cei.getField().getName();
                }
                sb.append(" - ");
                if(cei.getNotInCacheDetail() != null){
                    sb.append(cei.getNotInCacheDetail().getLabel());
                }
                sb.append(fieldName + "[" + cb.getUserFriendlyTypeName() + "#" + cb.getId() + "]");

                String parentsPath = "";
                while(parentCei != null){
                    parentsPath += ".";
                    parentsPath += parentCei.getField() != null? parentCei.getField().getName() : "";
                    String id = "";
                    if(parentCei.getObject() instanceof CdmBase){
                        id = "#" + ((CdmBase)parentCei.getObject()).getId();
                    }
                    parentsPath += "[" + classLabel(parentCei.getObject()) + id + "]";
                    parentCei = parentCei.getParent();
                }

                sb.append(parentsPath);
            }
        }
    }

    private void toStringDuplicatesInGraph(Map<CdmEntityInfo, CdmEntityInfo> duplicateCdmEntityMap, CdmBase rootEntity,
            StringBuilder sb) {
        sb.append(System.getProperty("line.separator"));
        sb.append("<<< Root Entity " + rootEntity.getUserFriendlyTypeName() + " with id " + rootEntity.getId() + " >>>");
        sb.append(System.getProperty("line.separator"));
        if(duplicateCdmEntityMap.isEmpty()) {
            sb.append("No Duplicate CDM Entities in root graph.");
        } else {
            sb.append("Duplicate CDM Entities in root graph: ");

            for (Map.Entry<CdmEntityInfo, CdmEntityInfo> entry : duplicateCdmEntityMap.entrySet()){

                sb.append(System.getProperty("line.separator"));
                CdmEntityInfo cei = entry.getKey();
                CdmBase cb = (CdmBase) cei.getObject();

                sb.append(" 1 " + cei.getField().getName() + ":" + cb.getUserFriendlyTypeName() + "/" + cb.getId());
                if(cei.getParent() != null) {
                    Object cbParent = cei.getParent().getObject();
                    sb.append("     in entity " + cbParent.getClass().getCanonicalName());
                    if(cbParent instanceof CdmBase) {
                        sb.append(" with id : " + ((CdmBase)cbParent).getId());
                    }
                }
                sb.append(System.getProperty("line.separator"));
                sb.append("  -- entity belongs to cache(s) : " + getCachesContainingEntity(cb));
                sb.append(System.getProperty("line.separator"));


                CdmEntityInfo dupCei = entry.getValue();
                CdmBase dupCb = (CdmBase) dupCei.getObject();

                String dupCeiFieldName = "";
                if(dupCei.getField() != null) {
                    dupCeiFieldName = dupCei.getField().getName();
                }
                sb.append(" 2 " + dupCeiFieldName + ":" + dupCb.getUserFriendlyTypeName() + "/" + dupCb.getId());
                if(dupCei.getParent() != null) {
                    Object dupCbParent = dupCei.getParent().getObject();
                    sb.append("      in entity " + dupCbParent.getClass().getCanonicalName());
                    if(dupCbParent instanceof CdmBase) {
                        sb.append(" with id : " + ((CdmBase)dupCbParent).getId());
                    }
                }
                sb.append(System.getProperty("line.separator"));
                sb.append("  -- entity belongs to cache(s) : " + getCachesContainingEntity(dupCb));
                sb.append(System.getProperty("line.separator"));
                sb.append("-----------");
            }
        }
    }

    private String classLabel(Object entity){
        if(entity instanceof CdmBase) {
            return ((CdmBase)entity).getUserFriendlyTypeName();
        } else {
            return entity.getClass().getName();
        }
    }

    private String getCachesContainingEntity(CdmBase cdmEntity) {
        String caches = "";

        Cache defaultCache = CacheManager.create().getCache(CdmPermanentCacheBase.PERMANENT_CACHE_NAME);
        Element dce = defaultCache.get(cdmEntity.getUuid());
        if(dce != null && dce.getObjectValue() == cdmEntity) {
            caches = "{DC}";
        }

        CdmBase cte = cacher.getFromCache(CdmTransientEntityCacher.generateKey(cdmEntity));
        if(cte != null && cte == cdmEntity) {
            caches += "{TC}";
        }
        return caches;
    }

    private void debug(CdmBase cdmEntity, boolean recursive, boolean includeIgnored) {
        if(cdmEntity == null) {
            return;
        }
        logger.info("---- starting recursive debug for cdm entity " + cdmEntity.getClass().getName() + " with id " + cdmEntity.getId());
        List<CdmEntityInfo> alreadyVisitedEntities = new ArrayList<>();
        CdmEntityInfo cei = new CdmEntityInfo(ProxyUtils.deproxyIfInitialized(cdmEntity));
        if(recursive){
            debugRecursive(cdmEntity, alreadyVisitedEntities, cei, includeIgnored);
        }
        rootElements.add(cei);
        alreadyVisitedEntities.clear();
        logger.info("---- ending recursive debug for cdm entity " + cdmEntity.getClass().getName() + " with id " + cdmEntity.getId() + "\n");
    }

    private <T extends Object> void debugRecursive(T obj,
            List<CdmEntityInfo> alreadyVisitedEntities,
            CdmEntityInfo cei, boolean includeIgnored) {
        if(obj == null) {
            return;
        }
        if(obj instanceof CdmBase) {
            debugRecursive((CdmBase)obj, alreadyVisitedEntities, cei, includeIgnored);
        } else if (obj instanceof Map) {
            debug((Map<T,T>)obj, alreadyVisitedEntities, cei, includeIgnored);
        } else if (obj instanceof Collection) {
            debug((Collection<T>)obj, alreadyVisitedEntities, cei, includeIgnored);
        }
        logger.info("No caching yet for type " + obj.getClass().getName());
    }

    private <T extends Object> void debug(Map<T,T> map,
            List<CdmEntityInfo> alreadyVisitedEntities,
            CdmEntityInfo cei, boolean includeIgnored) {
        if(map == null || map.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<T,T>> iter = map.entrySet().iterator();
        while ( iter.hasNext() ) {
            Map.Entry<T,T> e = iter.next();
            CdmEntityInfo childCei = new CdmEntityInfo(e);
            cei.addChild(childCei);

            CdmEntityInfo keyCei = new CdmEntityInfo(ProxyUtils.deproxyIfInitialized(e.getKey()));
            childCei.addChild(keyCei);
            CdmEntityInfo valueCei = new CdmEntityInfo(ProxyUtils.deproxyIfInitialized(e.getValue()));
            childCei.addChild(valueCei);

            debugRecursive(e.getKey(), alreadyVisitedEntities, keyCei, includeIgnored);
            debugRecursive(e.getValue(), alreadyVisitedEntities, valueCei, includeIgnored);
        }
    }

    private <T extends Object> void debug(Collection<T> collection,
            List<CdmEntityInfo> alreadyVisitedEntities,
            CdmEntityInfo cei, boolean includeIgnored) {
        Iterator<T> collectionItr = collection.iterator();

        while(collectionItr.hasNext()) {
            Object obj = collectionItr.next();
            boolean alreadyVisited = false;
            for (CdmEntityInfo entityInfo: alreadyVisitedEntities) {
                if(obj.equals(entityInfo.getObject())){
                    alreadyVisited = true;
                    break;
                }
            }
            if(!alreadyVisited){
                CdmEntityInfo childCei = new CdmEntityInfo(ProxyUtils.deproxyIfInitialized(obj));
                cei.addChild(childCei);
                debugRecursive(obj, alreadyVisitedEntities, childCei, includeIgnored);
            }
        }
    }

    private void debugRecursive(CdmBase cdmEntity,
            List<CdmEntityInfo> alreadyVisitedEntities,
            CdmEntityInfo cei, boolean includeIgnored) {

        if(cei.getObject() instanceof CdmBase) {
           CdmBase cb =  (CdmBase)cei.getObject();
           cb = ProxyUtils.deproxyIfInitialized(cb);
           CdmBase cachedCdmEntityInSubGraph = cacher.getFromCache(cb);
           if(cachedCdmEntityInSubGraph == null || !cachedCdmEntityInSubGraph.equals(cb) && (includeIgnored || !cacher.ignoreRecursiveLoad(cb))) {
               cei.setNotInCacheDetail(cachedCdmEntityInSubGraph == null ? NotInCacheType.NOT_FOUND : NotInCacheType.COPY_ENTITY);
               // found a cdm entity which is not in cache - need to record this
               //logger.info("  - found entity not in cache " + fieldName + "' in object of type " + clazz.getName() + " with id " + cdmEntity.getId());
               addEntityNotInCache(cei);
           }
        }

        // we want to recurse through the cdmEntity (and not the cachedCdmEntity)
        // since there could be new or deleted objects in the cdmEntity sub-graph

        // start by getting the fields from the cdm entity
        String className = cdmEntity.getClass().getName();
        CdmModelFieldPropertyFromClass classFields = cacher.getFromCdmlibModelCache(className);
        if(classFields != null) {
            alreadyVisitedEntities.add(cei);
            List<String> fields = classFields.getFields();
            for(String field : fields) {
                debugField(cdmEntity, alreadyVisitedEntities, cei, field, includeIgnored);
            }
        } else {
            throw new CdmClientCacheException("CdmEntity with class " + cdmEntity.getClass().getName() + " is not found in the cdmlib model cache. " +
                    "The cache may be corrupted or not in sync with the latest model version" );
        }

    }

    private void debugField(CdmBase cdmEntity, List<CdmEntityInfo> alreadyVisitedEntities, CdmEntityInfo cei,
            String field, boolean includeIgnored) {
        // retrieve the actual object corresponding to the field.
        // this object will be either a CdmBase or a Collection / Map
        // with CdmBase as the generic type
        CdmEntityInfo childCei = getDebugCdmBaseTypeFieldValue(cdmEntity, field, alreadyVisitedEntities, cei, includeIgnored);
        if(!childCei.isProxy()) {
            Object object = childCei.getObject();
            if(object != null && object instanceof CdmBase) {
                CdmBase cdmEntityInSubGraph = (CdmBase)object;
                if(!containsIdenticalCdmEntity(alreadyVisitedEntities, cdmEntityInSubGraph)) {
                    logger.info("recursive debugging object of type " + cdmEntityInSubGraph.getClass().getName() + " with id " + cdmEntityInSubGraph.getId());
                    debugRecursive(cdmEntityInSubGraph, alreadyVisitedEntities, childCei, includeIgnored);
                } else {
                    logger.info("object of type " + cdmEntityInSubGraph.getClass().getName() + " with id " + cdmEntityInSubGraph.getId() + " already visited");
                }
            }
        }
    }

    private CdmEntityInfo getDebugCdmBaseTypeFieldValue(CdmBase cdmEntity,
            String fieldName,
            List<CdmEntityInfo> alreadyVisitedEntities,
            CdmEntityInfo cei, boolean includeIgnored) {

        CdmEntityInfo childCei = null;
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
            o = ProxyUtils.deproxyIfInitialized(o);
            CdmBase cdmEntityInSubGraph = null;

            childCei = new CdmEntityInfo(o);
            cei.addChild(childCei);
            childCei.setField(field);

            if(o != null) {
                boolean isProxy = ProxyUtils.isUninitializedProxy(o);

                childCei.setProxy(isProxy);
                if(!isProxy) {
                    childCei.setObject(o);
                    if(CdmBase.class.isAssignableFrom(o.getClass())) {
                        logger.info("found initialised cdm entity '" + fieldName + "' in object of type " + clazz.getName() + " with id " + cdmEntity.getId());
                        cdmEntityInSubGraph  = (CdmBase)o;

                        //logger.info("  - found duplicate entity at " + fieldName + "' in object of type " + clazz.getName() + " with id " + cdmEntity.getId());
                        CdmEntityInfo dupCei = getDuplicate(alreadyVisitedEntities, cdmEntityInSubGraph);
                        if(dupCei != null && (includeIgnored || !cacher.ignoreRecursiveLoad(cdmEntityInSubGraph))) {
                            addDuplicateInGraphEntity(childCei, dupCei);
                        }

                    } else if(o instanceof Map) {
                        debugRecursive((Map)o, alreadyVisitedEntities, childCei, includeIgnored);
                    } else if(o instanceof Collection) {
                        debugRecursive((Collection)o, alreadyVisitedEntities, childCei, includeIgnored);
                    }

                }
            }
            // we return the original cdm entity in the sub graph because we
            // want to continue to recurse on the input cdm entity graph
            // and not the one in the cache
            return childCei;
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new CdmClientCacheException(e);
        }
    }

    private CdmEntityInfo getDuplicate(List<CdmEntityInfo> alreadyVisitedEntities, Object objectToCompare) {
        if(objectToCompare != null ) {
            for(CdmEntityInfo cei: alreadyVisitedEntities) {
                if(objectToCompare.equals(cei.getObject()) && objectToCompare != cei.getObject()) {
                    return cei;
                }
            }
        }
        return null;
    }

    private boolean containsIdenticalCdmEntity(List<CdmEntityInfo> ceiSet, Object objectToCompare) {
        boolean foundIdentical = false;
        if(objectToCompare != null) {
            for(CdmEntityInfo cei : ceiSet) {
                if(cei.getObject() == objectToCompare) {
                    foundIdentical = true;
                }
//                } else if(objectToCompare.equals(cei.getObject())) {
//                    return false;
//                }
            }
        }
        return foundIdentical;
    }

    public class CdmEntityInfo {

        private Object object;
        private CdmEntityInfo parent;
        private List<CdmEntityInfo> children;
        private Field field;
        private boolean isProxy;
        private NotInCacheType notInCacheType = null;

        public CdmEntityInfo(Object object) {
            this.object = object;
            isProxy = false;
            children = new ArrayList<>();
        }

        public CdmEntityInfo getParent() {
            return parent;
        }
        public void setParent(CdmEntityInfo parent) {
            this.parent = parent;
        }

        public List<CdmEntityInfo> getChildren() {
            return children;
        }
        public void setChildren(List<CdmEntityInfo> children) {
            this.children = children;
        }

        public void addChild(CdmEntityInfo cei) {
            this.children.add(cei);
            cei.setParent(this);
        }

        public Field getField() {
            return field;
        }
        public void setField(Field field) {
            this.field = field;
        }

        public String getLabel() {
            String label;
            String fieldName = "";
            if(field != null) {
                fieldName = field.getName();
            }

            if(object != null) {
                String className = object.getClass().getName();
                if(object instanceof HibernateProxy) {
                    LazyInitializer hli = ((HibernateProxy)object).getHibernateLazyInitializer();
                    if(hli.isUninitialized()) {
                        className = "HibernateProxy";
                    } else {
                        className = "InitialisedHibernateProxy";
                    }
                    label = fieldName + ": [" + className + "]";
                } else if(object instanceof PersistentCollection) {
                    PersistentCollection pc = ((PersistentCollection)object);
                    if(!pc.wasInitialized()) {
                        className = "PersistentCollection";
                    } else {
                        className = "InitialisedPersistentCollection";
                    }
                    label = fieldName + ": [" + className + "]";
                } else if(object instanceof Collection) {
                    label = fieldName + ": [" + className + "] : " + String.valueOf(((Collection)object).size());
                } else if(object instanceof Map) {
                    label = fieldName + ": [" + className + "] : " + String.valueOf(((Map)object).size());
                } else if(object instanceof CdmBase) {
                    String objectLabel = "-- not fully initialized for toString() --";
                    try {
                        objectLabel = object.toString();
                    } catch(LazyInitializationException e){}

                    label = CdmUtils.concat(": ", fieldName, objectLabel) + ": " + getCachesContainingEntity((CdmBase)object) +  "[" + className + "#" + ((CdmBase)object).getId() + "]";
                } else {
                    label = fieldName + ": [" + className + "] : " + object.toString();
                }
                label += " {"+ System.identityHashCode(object) + "}";
            } else {
                label = fieldName + ": [NULL]";
            }
            return label;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public boolean isProxy() {
            return isProxy;
        }

        public void setProxy(boolean isProxy) {
            this.isProxy = isProxy;
        }

        public NotInCacheType getNotInCacheDetail() {
            return notInCacheType;
        }
        public void setNotInCacheDetail(NotInCacheType notInCacheType) {
            this.notInCacheType = notInCacheType;
        }
    }

    enum NotInCacheType {
        NOT_FOUND("*"),
        COPY_ENTITY("?");

        private String label;

        private NotInCacheType(String label){
            this.label = label;
        }

        public Object getLabel() {
            return label;
        }
    }
}