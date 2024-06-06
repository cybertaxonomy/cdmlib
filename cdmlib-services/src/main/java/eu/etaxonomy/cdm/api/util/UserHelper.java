/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.util;

import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.ICdmEntityUuidCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;

/**
 * UserHelper interface.
 *
 * @author a.kohlbecker
 * @since May 23, 2017
 */
public interface UserHelper {

    public boolean userHasPermission(Class<? extends CdmBase> cdmType, UUID entitiyUUID, Object ... args);

    public boolean userHasPermission(Class<? extends CdmBase> cdmType, Object ... args);

    public boolean userHasPermission(CdmBase entity, Object ... args);

    public User user();

    public String userName();

    public boolean userIsAnnonymous();

    public boolean userIsAutheticated();

    public boolean userIsAdmin();

    public boolean userIs(IRoleProber iRoleProbe);

    /**
     * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
     * <code>null</code> in case the operation failed of if the user was already granted with this authority.
     */
    public CdmAuthority createAuthorityFor(String username, CdmBase cdmEntity, EnumSet<CRUD> crud, String property);

   /**
    * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
    * <code>null</code> in case the operation failed of if the user was already granted with this authority.
    */
   public CdmAuthority createAuthorityFor(String username, Class<? extends CdmBase> cdmType, UUID entitiyUuid, EnumSet<CRUD> crud, String property);

    /**
     * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
     *      <code>null</code> in case the operation failed or if the user was already granted with this authority.
     */
    public CdmAuthority createAuthorityForCurrentUser(Class<? extends CdmBase> cdmType, UUID entitiyUuid, EnumSet<CRUD> crud, String property);

    /**
     * @return the newly created CdmAuthority only if a new CdmAuthority has been added to the user otherwise
     *      <code>null</code> in case the operation failed or if the user was already granted with this authority.
     */
    public CdmAuthority createAuthorityForCurrentUser(CdmBase cdmEntity, EnumSet<CRUD> crud, String property);

    /**
     * Scans the currently authenticated user for CdmAuthorities which match the given parameters
     */
    public Collection<CdmAuthority> findUserPermissions(CdmBase cdmEntity, EnumSet<CRUD> crud);

    /**
     * Scans the currently authenticated user for CdmAuthorities which match the given parameters
     */
    public <T extends CdmBase> Collection<CdmAuthority> findUserPermissions(Class<T> cdmType, EnumSet<CRUD> crud);

    public void removeAuthorityForCurrentUser(CdmAuthority newAuthority);

    public void removeAuthorityForUser(String username, CdmAuthority newAuthority);

    public void logout();

    public void setSecurityContextAccess(SecurityContextAccess securityContextAccess);

    public Authentication getAuthentication();

    public CdmUserHelper withCache(ICdmEntityUuidCacher iCdmCacher);
}
