/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;

/**
 * UserHelper interface.
 *
 * @author a.kohlbecker
 * @since May 23, 2017
 *
 */
public interface UserHelper {

    boolean userHasPermission(Class<? extends CdmBase> cdmType, Integer entitiyId, Object ... args);

    boolean userHasPermission(Class<? extends CdmBase> cdmType, UUID entitiyUUID, Object ... args);

    boolean userHasPermission(Class<? extends CdmBase> cdmType, Object ... args);

    boolean userHasPermission(CdmBase entity, Object ... args);

    User user();

    String userName();

    boolean userIsAnnonymous();

    boolean userIsAutheticated();

    boolean userIsAdmin();

    boolean userIs(RoleProbe roleProbe);

    /**
     *
     * @param username
     * @param cdmEntity
     * @param crud
     * @param property
     * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
     * <code>null</code> in case the operation failed of if the user was already granted with this authority.
     */
    public CdmAuthority createAuthorityFor(String username, CdmBase cdmEntity, EnumSet<CRUD> crud, String property);

    /**
     *
     * @param username
     * @param cdmType
     * @param entitiyId
     * @param crud
     * @param property
     * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
     * <code>null</code> in case the operation failed of if the user was already granted with this authority.
     */
    public CdmAuthority createAuthorityFor(String username, Class<? extends CdmBase> cdmType, Integer entitiyId, EnumSet<CRUD> crud, String property);


    /**
    *
    * @param username
    * @param cdmType
    * @param entitiyUuid
    * @param crud
    * @param property
    * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
    * <code>null</code> in case the operation failed of if the user was already granted with this authority.
    */
   public CdmAuthority createAuthorityFor(String username, Class<? extends CdmBase> cdmType, UUID entitiyUuid, EnumSet<CRUD> crud, String property);


    /**
     * @param cdmType
     * @param entitiyId
     * @param crud
     * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
     * <code>null</code> in case the operation failed of if the user was already granted with this authority.
     */
    public CdmAuthority createAuthorityForCurrentUser(Class<? extends CdmBase> cdmType, Integer entitiyId, EnumSet<CRUD> crud, String property);

    /**
     * @param cdmType
     * @param entitiyUuid
     * @param crud
     * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
     * <code>null</code> in case the operation failed of if the user was already granted with this authority.
     */
    public CdmAuthority createAuthorityForCurrentUser(Class<? extends CdmBase> cdmType, UUID entitiyUuid, EnumSet<CRUD> crud, String property);

    /**
     * @param cdmType
     * @param entitiyId
     * @param crud
     * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
     * <code>null</code> in case the operation failed of if the user was already granted with this authority.
     */
    public CdmAuthority createAuthorityForCurrentUser(CdmBase cdmEntity, EnumSet<CRUD> crud, String property);

    /**
     * Scans the currently authenticated user for CdmAuthorities which match the given parameters
     *
     * @param cdmEntity
     * @param crud
     * @return
     */
    public Collection<CdmAuthority> findUserPermissions(CdmBase cdmEntity, EnumSet<CRUD> crud);

    /**
     * @param newAuthority
     */
    public void removeAuthorityForCurrentUser(CdmAuthority newAuthority);

    /**
     * @param username
     * @param newAuthority
     */
    public void removeAuthorityForCurrentUser(String username, CdmAuthority newAuthority);

    /**
     *
     */
    public void logout();

    void setSecurityContextAccess(SecurityContextAccess securityContextAccess);

    /**
     * @return
     */
    public Authentication getAuthentication();


}
