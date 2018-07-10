/**
 * Copyright (C) 2011 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import java.beans.Introspector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.database.PermissionDeniedException;
import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.ICdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;
import eu.etaxonomy.cdm.persistence.hibernate.permission.TargetEntityStates;

/**
 * @author k.luther
 * @author a.kohlbecker
 *
 */
@Component
public class CdmSecurityHibernateInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 8477758472369568074L;

    public static final Logger logger = Logger.getLogger(CdmSecurityHibernateInterceptor.class);


    private ICdmPermissionEvaluator permissionEvaluator;

    public ICdmPermissionEvaluator getPermissionEvaluator() {
        return permissionEvaluator;
    }

    public void setPermissionEvaluator(ICdmPermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    /**
     * The exculdeMap must map every property to the CdmBase type !!!
     */
    public static final Map<Class<? extends CdmBase>, Set<String>> exculdeMap = new HashMap<Class<? extends CdmBase>, Set<String>>();

    static{
//        disabled since no longer needed, see https://dev.e-taxonomy.eu/trac/ticket/4111#comment:8
//        exculdeMap.put(TaxonName.class, new HashSet<String>());

        Set<String> defaultExculdes = new HashSet<String>();
        defaultExculdes.add("createdBy");  //created by is changed by CdmPreDataChangeListener after save. This is handled as a change and therefore throws a security exception during first insert if only CREATE rights exist
        defaultExculdes.add("created");  // same behavior was not yet observed for "created", but to be on the save side we also exclude "created"
        defaultExculdes.add("updatedBy");
        defaultExculdes.add("updated");

        for ( CdmBaseType type: CdmBaseType.values()){
            exculdeMap.put(type.getBaseClass(), new HashSet<String>());
            exculdeMap.get(type.getBaseClass()).addAll(defaultExculdes);
        }
        exculdeMap.put(CdmBase.class, new HashSet<String>());
        exculdeMap.get(CdmBase.class).addAll(defaultExculdes);


        /*
         * default fields required for each type for which excludes are defined
         */
//        exculdeMap.get(TaxonName.class).add("updatedBy");
//        exculdeMap.get(TaxonName.class).add("created");
//        exculdeMap.get(TaxonName.class).add("updated");

        /*
         * the specific excludes
         */
//        exculdeMap.get(TaxonName.class).add("taxonBases");
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] type) {

        if (SecurityContextHolder.getContext().getAuthentication() == null || !(entity instanceof CdmBase)) {
            return true;
        }
        // evaluate throws EvaluationFailedException
        TargetEntityStates cdmEntityStates = new TargetEntityStates((CdmBase)entity, state, null, propertyNames, type);
        checkPermissions(cdmEntityStates, Operation.CREATE);
        logger.debug("permission check suceeded - object creation granted");
        return true;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {

        if (SecurityContextHolder.getContext().getAuthentication() == null || !(entity instanceof CdmBase)) {
            return true;
        }
        CdmBase cdmEntity = (CdmBase) entity;
        if (previousState == null){
            return onSave(cdmEntity, id, currentState, propertyNames, null);
        }


        Set<String> excludes = exculdeMap.get(baseType(cdmEntity));
        excludes.addAll(unprotectedCacheFields(currentState, previousState, propertyNames));
        if (isModified(currentState, previousState, propertyNames, excludes)) {
            // evaluate throws EvaluationFailedException
            //if(cdmEntity.getCreated())
            TargetEntityStates cdmEntityStates = new TargetEntityStates(cdmEntity, currentState, previousState, propertyNames, types);
            checkPermissions(cdmEntityStates, Operation.UPDATE);
            logger.debug("Operation.UPDATE permission check suceeded - object update granted");

            if(IPublishable.class.isAssignableFrom(entity.getClass())){
                if(namedPropertyIsModified(currentState, previousState, propertyNames, "publish")){
                    checkRoles(Role.ROLE_PUBLISH, Role.ROLE_ADMIN);
                    logger.debug("Role.ROLE_PUBLISH permission check suceeded - object update granted");
                }
            }
        }
        return true;
    }

    /**
     * Detects all cache fields and the according protection flags. For cache fields which are not
     * protected the name of the cache field and of the protection flag are returned.
     * <p>
     * This method relies on  the convention that the protection flag for cache fields are named like
     * {@code protected{CacheFieldName} } whereas the cache fields a always ending with "Cache"
     *
     * @param currentState
     * @param previousState
     * @param propertyNames
     * @return
     */
    protected Collection<? extends String> unprotectedCacheFields(Object[] currentState, Object[] previousState,
            String[] propertyNames) {

        List<String> excludes = new ArrayList<>();
        for(int i = 0; i < propertyNames.length; i ++){
            if(propertyNames[i].matches("^protected.*Cache$")){
                if(currentState[i] instanceof Boolean && ((Boolean)currentState[i]) == false && currentState[i].equals(previousState[i])){
                    excludes.add(propertyNames[i]);
                    String cacheFieldName = propertyNames[i].replace("protected", "");
                    cacheFieldName = Introspector.decapitalize(cacheFieldName);
                    excludes.add(cacheFieldName);
                }
            }
        }

        return excludes;
    }

    private Class<? extends CdmBase> baseType(CdmBase cdmEntity) {
        Class<? extends CdmBase> basetype = CdmBaseType.baseTypeFor(cdmEntity.getClass());
        return basetype == null ? CdmBase.class : basetype;
    }


    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

        if (SecurityContextHolder.getContext().getAuthentication() == null || !(entity instanceof CdmBase)) {
            return;
        }
        CdmBase cdmEntity = (CdmBase) entity;
        // evaluate throws EvaluationFailedException
        TargetEntityStates cdmEntityStates = new TargetEntityStates(cdmEntity, state, null, propertyNames, types);
        checkPermissions(cdmEntityStates, Operation.DELETE);
        logger.debug("permission check suceeded - object update granted");
        return;
    }

    /**
     * checks if the current authentication has the <code>expectedPermission</code> on the supplied <code>entity</code>.
     * Throws an {@link PermissionDeniedException} if the evaluation fails.
     *
     * @param entity
     * @param expectedOperation
     */
    private void checkPermissions(CdmBase entity, EnumSet<CRUD> expectedOperation) {
        checkPermissions(new TargetEntityStates(entity), expectedOperation);
    }

    // TargetEntityStates
    private void checkPermissions(TargetEntityStates entityStates, EnumSet<CRUD> expectedOperation) {

        if (!permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entityStates, expectedOperation)){
            throw new PermissionDeniedException(SecurityContextHolder.getContext().getAuthentication(), entityStates.getEntity(), expectedOperation);
        }
    }

    /**
     * checks if the current authentication has at least one of the <code>roles</code>.
     * Throws an {@link PermissionDeniedException} if the evaluation fails.
     * @param roles
     */
    private void checkRoles(Role ... roles) {

        if (!permissionEvaluator.hasOneOfRoles(SecurityContextHolder.getContext().getAuthentication(), roles)){
            throw new PermissionDeniedException(SecurityContextHolder.getContext().getAuthentication(), roles);
        }
    }

    /**
     * Checks if the CDM entity as been modified by comparing the current with the previous state.
     *
     * @param currentState
     * @param previousState
     * @return true if the currentState and previousState differ.
     */
    private boolean isModified(Object[] currentState, Object[] previousState, String[] propertyNames, Set<String> excludes) {

        Set<Integer> excludeIds = null;

        if(excludes != null && excludes.size() > 0) {
            excludeIds = new HashSet<Integer>(excludes.size());
            int i = 0;
            for(String prop : propertyNames){
                if(excludes.contains(prop)){
                    excludeIds.add(i);
                }
                if(excludeIds.size() == excludes.size()){
                    // all ids found
                    break;
                }
                i++;
            }
        }

        for (int i = 0; i<currentState.length; i++){
            if((excludeIds == null || !excludeIds.contains(i))){
                if(propertyIsModified(currentState, previousState, i)){
                    if(logger.isDebugEnabled()){
                        logger.debug("modified property found: " + propertyNames[i] + ", previousState: " + previousState[i] + ", currentState: " + currentState[i] );
                    }
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Compares the object states at the property denoted by the key parameter and returns true if they differ in this property
     *
     * @param currentState
     * @param previousState
     * @param isModified
     * @param key
     * @return
     */
    private boolean propertyIsModified(Object[] currentState, Object[] previousState, int key) {
        if (currentState[key]== null ) {
            if ( previousState[key]!= null) {
                return true;
            }
        }
        if (currentState[key]!= null ){
            if (previousState[key] == null){
                return true;
            }
        }
        if (currentState[key]!= null && previousState[key] != null){
            if (!currentState[key].equals(previousState[key])) {
                return true;
            }
        }
        return false;
    }

    private boolean namedPropertyIsModified(Object[] currentState, Object[] previousState, String[] propertyNames, String propertyNameToTest) {

        int key = ArrayUtils.indexOf(propertyNames, propertyNameToTest);
        return propertyIsModified(currentState, previousState, key);
    }

}
