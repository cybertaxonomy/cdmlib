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

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import eu.etaxonomy.cdm.database.EvaluationFailedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionEvaluator;
/**
 * @author k.luther
 * @date 2011-07-27
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
    public boolean onSave(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] type) {

        if (SecurityContextHolder.getContext().getAuthentication() == null || !(entity instanceof CdmBase) ) {
            return true;
        }
        // evaluate throws EvaluationFailedException
        checkPermissions((CdmBase)entity, Operation.CREATE);
        logger.debug("permission check suceeded - object creation granted");
        return true;
    }


    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    public boolean onFlushDirty(Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames,
            Type[] types) {

        if (SecurityContextHolder.getContext().getAuthentication() == null || !(entity instanceof CdmBase) ) {
            return true;
        }
        CdmBase cdmEntity = (CdmBase)entity;

        if (isModified(currentState, previousState)){
            // evaluate throws EvaluationFailedException
            checkPermissions(cdmEntity, Operation.UPDATE);
            logger.debug("permission check suceeded - object update granted");
        }
        return true;
    }

    /**
     * checks if the current authentication has the <code>expectedPermission</code> on the supplied <code>entity</code>.
     * Throws an {@link EvaluationFailedException} if the evaluation fails.
     *
     * @param entity
     * @param expectedOperation
     */
    private void checkPermissions(CdmBase entity, Operation expectedOperation) {

        if (!permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), entity, expectedOperation)){
            throw new EvaluationFailedException(SecurityContextHolder.getContext().getAuthentication(), entity, expectedOperation);
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
        boolean equals = true;
        for (int i = 0; i<currentState.length; i++){
            if (currentState[i]== null ) {
                if ( previousState[i]!= null) {
                    equals = false;
                    break;
                }
            }
            if (currentState[i]!= null ){
                if (previousState == null){
                    equals = false;
                    break;
                }
            }
            if (currentState[i]!= null && previousState[i] != null){
                Object a = currentState[i];
                Object b = previousState[i];
                if (!currentState[i].equals(previousState[i])) {
                    equals = false;
                    break;
                }
            }
        }
        return equals;
    }


}
