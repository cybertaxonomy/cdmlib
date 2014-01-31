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

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.database.PermissionDeniedException;
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

        if (isModified(currentState, previousState)) {
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
    private boolean isModified(Object[] currentState, Object[] previousState) {
        for (int i = 0; i<currentState.length; i++){
            if(propertyIsModified(currentState, previousState, i)){
                return true;
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
