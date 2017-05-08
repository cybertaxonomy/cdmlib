/**
 * Copyright (C) 2011 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;



import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
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
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;
/**
 * @author k.luther
 * @author a.kohlbecker
 *
 */
@Component
public class CdmSecurityHibernateInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 8477758472369568074L;

    public static final Logger logger = Logger.getLogger(CdmSecurityHibernateInterceptor.class);


    private CdmPermissionEvaluator permissionEvaluator;

    public CdmPermissionEvaluator getPermissionEvaluator() {
        return permissionEvaluator;
    }

    public void setPermissionEvaluator(CdmPermissionEvaluator permissionEvaluator) {
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


    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] type) {

        if (SecurityContextHolder.getContext().getAuthentication() == null || !(entity instanceof CdmBase)) {
            return true;
        }
        // evaluate throws EvaluationFailedException
        checkPermissions((CdmBase) entity, Operation.CREATE);
        logger.debug("permission check suceeded - object creation granted");
        return true;
    }


    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {

        if (SecurityContextHolder.getContext().getAuthentication() == null || !(entity instanceof CdmBase)) {
            return true;
        }
        CdmBase cdmEntity = (CdmBase) entity;
        if (previousState == null){
            return onSave(cdmEntity, id, currentState, propertyNames, null);
        }
        if (isModified(currentState, previousState, propertyNames, exculdeMap.get(baseType(cdmEntity)))) {
            // evaluate throws EvaluationFailedException
            checkPermissions(cdmEntity, Operation.UPDATE);
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

    private Class<? extends CdmBase> baseType(CdmBase cdmEntity) {
        Class<? extends CdmBase> basetype = CdmBaseType.baseTypeFor(cdmEntity.getClass());
        return basetype == null ? CdmBase.class : basetype;
    }



    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onDelete(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

        if (SecurityContextHolder.getContext().getAuthentication() == null || !(entity instanceof CdmBase)) {
            return;
        }
        CdmBase cdmEntity = (CdmBase) entity;
        // evaluate throws EvaluationFailedException
        checkPermissions(cdmEntity, Operation.DELETE);
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

        if (!permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, expectedOperation)){
            throw new PermissionDeniedException(SecurityContextHolder.getContext().getAuthentication(), entity, expectedOperation);
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
