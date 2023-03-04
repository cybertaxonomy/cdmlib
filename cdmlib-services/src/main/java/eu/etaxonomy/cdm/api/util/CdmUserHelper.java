/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmRepository;
import eu.etaxonomy.cdm.api.application.RunAsAuthenticator;
import eu.etaxonomy.cdm.database.PermissionDeniedException;
import eu.etaxonomy.cdm.model.ICdmEntityUuidCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthorityParsingException;
import eu.etaxonomy.cdm.persistence.permission.ICdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.permission.Role;

/**
 * @author a.kohlbecker
 * @since May 19, 2017
 */
public class CdmUserHelper implements UserHelper, Serializable {

    private static final long serialVersionUID = -2521474709047255979L;
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ICdmPermissionEvaluator permissionEvaluator;

    @Autowired
    @Lazy
    @Qualifier("cdmRepository")
    private CdmRepository repo;

    private AuthenticationProvider runAsAuthenticationProvider;

    @Autowired(required=false)
    @Qualifier("runAsAuthenticationProvider")
    public void setRunAsAuthenticationProvider(AuthenticationProvider runAsAuthenticationProvider){
        this.runAsAuthenticationProvider = runAsAuthenticationProvider;
        runAsAutheticator.setRunAsAuthenticationProvider(runAsAuthenticationProvider);
    }

    private RunAsAuthenticator runAsAutheticator = new RunAsAuthenticator();

    private SecurityContextAccess securityContextAccess;

    public CdmUserHelper(){
        super();
    }

    protected ICdmPermissionEvaluator permissionEvaluator() {
        return permissionEvaluator;
    }

    protected CdmRepository repo() {
        return repo;
    }

    @Override
    public boolean userIsAutheticated() {
        Authentication authentication = getAuthentication();
        if(authentication != null && !AnonymousAuthenticationToken.class.equals(authentication.getClass())){
            return authentication.isAuthenticated();
        }
        return false;
    }

    @Override
    public boolean userIsAnnonymous() {
        Authentication authentication = getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication instanceof AnonymousAuthenticationToken;
    }

    @Override
    public User user() {
        Authentication authentication = getAuthentication();
        if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    @Override
    public String userName() {
        Authentication authentication = getAuthentication();
        if(authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    @Override
    public boolean userIsAdmin() {
        Authentication authentication = getAuthentication();
        if(authentication != null) {
            return authentication.getAuthorities().stream().anyMatch(a -> {
                return a.getAuthority().equals(Role.ROLE_ADMIN.getAuthority());
            });
        }
        return false;
    }

    @Override
    public boolean userIs(IRoleProber iRoleProbe) {
        return iRoleProbe.checkForRole(getAuthentication());
    }

    @Override
    public boolean userHasPermission(CdmBase entity, Object ... args){
        EnumSet<CRUD> crudSet = crudSetFromArgs(args);
        try {
            return permissionEvaluator().hasPermission(getAuthentication(), entity, crudSet);
        } catch (PermissionDeniedException e){
            //IGNORE
        }
        return false;
    }

    /**
     * @deprecated not performance optimized by using the cache,
     * use {@link #userHasPermission(Class, UUID, Object...)} instead
     */
    @Override
    @Deprecated
    public boolean userHasPermission(Class<? extends CdmBase> cdmType, Integer entitiyId, Object ... args){
        EnumSet<CRUD> crudSet = crudSetFromArgs(args);
        try {
            CdmBase entity = repo().getCommonService().find(cdmType, entitiyId);
            return permissionEvaluator().hasPermission(getAuthentication(), entity, crudSet);
        } catch (PermissionDeniedException e){
            //IGNORE
        }
        return false;
    }

    @Override
    public boolean userHasPermission(Class<? extends CdmBase> cdmType, UUID entitiyUuid, Object ... args){
        EnumSet<CRUD> crudSet = crudSetFromArgs(args);
        try {
            CdmBase entity = entity(cdmType, entitiyUuid);
            return permissionEvaluator().hasPermission(getAuthentication(), entity, crudSet);
        } catch (PermissionDeniedException e){
            //IGNORE
        }
        return false;
    }

    protected CdmBase entity(Class<? extends CdmBase> cdmType, UUID entitiyUuid) {
        CdmBase entity = entityFromCache(cdmType, entitiyUuid);
        if(entity == null){
            entity = repo().getCommonService().find(cdmType, entitiyUuid);
            if(getCache() != null && entity != null){
                getCache().putToCache(entity);
            }
        }
        return entity;
    }

    @Override
    public boolean userHasPermission(Class<? extends CdmBase> cdmType, Object ... args){
        EnumSet<CRUD> crudSet = crudSetFromArgs(args);
        try {
            return permissionEvaluator().hasPermission(getAuthentication(), cdmType, crudSet);
        } catch (PermissionDeniedException e){
            //IGNORE
        }
        return false;
    }

    @Override
    public void logout() {
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(null);
        SecurityContextHolder.clearContext();
    }

    private EnumSet<CRUD> crudSetFromArgs(Object[] args) {
        EnumSet<CRUD> crudSet = EnumSet.noneOf(CRUD.class);
        for(int i = 0; i < args.length; i++){
            try {
                crudSet.add(CRUD.valueOf(args[i].toString()));
            } catch (Exception e){
                throw new IllegalArgumentException("could not add " + args[i], e);
            }
        }
        return crudSet;
    }

    private SecurityContext currentSecurityContext() {
        if(securityContextAccess != null){
            return securityContextAccess.currentSecurityContext();
        }
        return SecurityContextHolder.getContext();
    }

    @Override
    public Authentication getAuthentication() {
        return currentSecurityContext().getAuthentication();
    }

    @Override
    public CdmAuthority createAuthorityFor(String username, CdmBase cdmEntity, EnumSet<CRUD> crud, String property) {

        TransactionStatus txStatus = repo().startTransaction();
        UserDetails userDetails = repo().getUserService().loadUserByUsername(username);
        boolean newAuthorityAdded = false;
        CdmAuthority authority = null;
        User user = (User)userDetails;
        if(userDetails != null){
            try{
                // flush all pending transactions before changing the authentication,
                // see https://dev.e-taxonomy.eu/redmine/issues/8066 for discussion
                // in case of problems we may want to use another transaction propagation level
                // instead (PROPAGATION_REQUIRES_NEW) which would require to reload the cdm entity.
                repo().getSession().flush();
                getRunAsAutheticator().runAsAuthentication(Role.ROLE_USER_MANAGER);
                authority = new CdmAuthority(cdmEntity, property, crud);
                try {
                    GrantedAuthorityImpl grantedAuthority = repo().getGrantedAuthorityService().findAuthorityString(authority.toString());
                    if(grantedAuthority == null){
                        grantedAuthority = authority.asNewGrantedAuthority();
                    }
                    newAuthorityAdded = user.getGrantedAuthorities().add(grantedAuthority);
                } catch (CdmAuthorityParsingException e) {
                    getRunAsAutheticator().restoreAuthentication();
                    throw new RuntimeException(e);
                }
                repo().getSession().flush();
            } finally {
                // in any case restore the previous authentication
                getRunAsAutheticator().restoreAuthentication();
            }
            if (logger.isDebugEnabled()) {logger.debug("new authority for " + username + ": " + authority.toString());}
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("security context refreshed with user " + username);
        }
        repo().commitTransaction(txStatus);
        return newAuthorityAdded ? authority : null;

    }

    /**
     * @param username
     * @param cdmType
     * @param entitiyId
     * @param crud
     * @return
     * @deprecated not performance optimized by using the cache,
     * use {@link #createAuthorityFor(String, Class, UUID, EnumSet, String)} instead
     */
    @Override
    @Deprecated
    public CdmAuthority createAuthorityFor(String username, Class<? extends CdmBase> cdmType, Integer entitiyId, EnumSet<CRUD> crud, String property) {

        CdmBase cdmEntity = repo().getCommonService().find(cdmType, entitiyId);
        return createAuthorityFor(username, cdmEntity, crud, property);
    }

    @Override
    public CdmAuthority createAuthorityFor(String username, Class<? extends CdmBase> cdmType, UUID entitiyUuid, EnumSet<CRUD> crud, String property) {

        CdmBase cdmEntity = entity(cdmType, entitiyUuid);
        return createAuthorityFor(username, cdmEntity, crud, property);
    }

    @Override
    public CdmAuthority createAuthorityForCurrentUser(CdmBase cdmEntity, EnumSet<CRUD> crud, String property) {
        return createAuthorityFor(userName(), cdmEntity, crud, property);
    }

    @Override
    public CdmAuthority createAuthorityForCurrentUser(Class<? extends CdmBase> cdmType, Integer entitiyId, EnumSet<CRUD> crud, String property) {
        return createAuthorityFor(userName(), cdmType, entitiyId, crud, property);
    }

    @Override
    public CdmAuthority createAuthorityForCurrentUser(Class<? extends CdmBase> cdmType, UUID entitiyUuid, EnumSet<CRUD> crud, String property) {
        return createAuthorityFor(userName(), cdmType, entitiyUuid, crud, property);
    }

    @Override
    public void removeAuthorityForCurrentUser(CdmAuthority cdmAuthority) {
        removeAuthorityForCurrentUser(userName(), cdmAuthority);

    }

    @Override
    public void removeAuthorityForCurrentUser(String username, CdmAuthority cdmAuthority) {

        TransactionStatus txStatus = repo().startTransaction();
        UserDetails userDetails = repo().getUserService().loadUserByUsername(username);
        User user = (User)userDetails;
        if(userDetails != null){
            try {
                getRunAsAutheticator().runAsAuthentication(Role.ROLE_USER_MANAGER);
                user.getGrantedAuthorities().remove(cdmAuthority);
                repo().getSession().flush();
                logger.debug("security context refreshed with user " + username);
            } finally {
                getRunAsAutheticator().restoreAuthentication();
            }
            logger.debug("authority removed from " + username + ": " + cdmAuthority.toString());
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("security context refreshed with user " + username);
        }
        repo().commitTransaction(txStatus);
    }

    @Override
    public Collection<CdmAuthority> findUserPermissions(CdmBase cdmEntity, EnumSet<CRUD> crud) {
        Set<CdmAuthority> matches = new HashSet<>();
        PermissionClass permissionClass = PermissionClass.getValueOf(cdmEntity);
        Collection<? extends GrantedAuthority> authorities = getAuthentication().getAuthorities();
        for(GrantedAuthority ga : authorities){
            try {
                CdmAuthority cdmAuthority = CdmAuthority.fromGrantedAuthority(ga);
                if(cdmAuthority.getPermissionClass().equals(permissionClass)){
                    if(cdmAuthority.getOperation().containsAll(crud)){
                        if(cdmAuthority.hasTargetUuid() && cdmAuthority.getTargetUUID().equals(cdmEntity.getUuid())){
                            matches.add(cdmAuthority);
                        } else {
                            matches.add(cdmAuthority);
                        }
                    }
                }
            } catch (CdmAuthorityParsingException e) {
                continue;
            }
        }
        return matches;
    }

    @Override
    public <T extends CdmBase> Collection<CdmAuthority> findUserPermissions(Class<T> cdmType, EnumSet<CRUD> crud) {
        Set<CdmAuthority> matches = new HashSet<>();
        PermissionClass permissionClass = PermissionClass.getValueOf(cdmType);
        Collection<? extends GrantedAuthority> authorities = getAuthentication().getAuthorities();
        for(GrantedAuthority ga : authorities){
            try {
                CdmAuthority cdmAuthority = CdmAuthority.fromGrantedAuthority(ga);
                if(cdmAuthority.getPermissionClass().equals(permissionClass)){
                    if(cdmAuthority.getOperation().containsAll(crud)){
                        matches.add(cdmAuthority);
                    }
                }
            } catch (CdmAuthorityParsingException e) {
                continue;
            }
        }
        return matches;
    }

    @Override
    public void setSecurityContextAccess(SecurityContextAccess securityContextAccess) {
        this.securityContextAccess = securityContextAccess;
    }

    public RunAsAuthenticator getRunAsAutheticator() {
        if(runAsAutheticator == null){
          throw new RuntimeException("RunAsAuthenticator is missing! The application needs to be configured with security context.");
        }
        return runAsAutheticator;
    }

    public ICdmEntityUuidCacher getCache() {
        return null;
    }

    private CdmBase entityFromCache(Class<? extends CdmBase> cdmType, UUID entitiyUuid) {
        CdmBase entity = null;
        if(getCache() != null){
           entity = getCache().getFromCache(entitiyUuid);
           if(entity != null && !cdmType.isAssignableFrom(entity.getClass())){
               logger.error("Entity with " +  entitiyUuid + " does not match the required type");
               entity = null;
           }
        }
        return entity;
    }

    @Override
    public CdmUserHelper withCache(ICdmEntityUuidCacher cache){
        return new CachingCdmUserHelper(cache);
    }

    class CachingCdmUserHelper extends CdmUserHelper{

        private static final long serialVersionUID = -5010082174809972831L;

        private ICdmEntityUuidCacher cache;

        public CachingCdmUserHelper(ICdmEntityUuidCacher cache){
            this.cache = cache;
        }

        @Override
        public ICdmEntityUuidCacher getCache() {
            return cache;
        }

        @Override
        protected CdmRepository repo() {
            return CdmUserHelper.this.repo;
        }

        @Override
        protected ICdmPermissionEvaluator permissionEvaluator() {
            return CdmUserHelper.this.permissionEvaluator;
        }
    }

}
