// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission.voter;

import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.hibernate.permission.AuthorityPermission;

/**
 * @author andreas kohlbecker
 * @date Sep 4, 2012
 *
 */
public class TaxonNodeVoter extends CdmPermissionVoter {

    public static final Logger logger = Logger.getLogger(TaxonNodeVoter.class);

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return TaxonNode.class;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.access.AccessDecisionVoter#vote(org.springframework.security.core.Authentication, java.lang.Object, java.util.Collection)
     */
    /*
    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        if(!(object instanceof TaxonNode)){
            logger.debug("class missmatch => ACCESS_ABSTAIN");
            return ACCESS_ABSTAIN;
        }
        TaxonNode node = (TaxonNode)object;

        if (logger.isDebugEnabled()){
            logger.debug("authentication: " + authentication.getName() + ", object : " + object.toString() + ", attribute[0]:" + ((AuthorityPermission)attributes.iterator().next()).getAttribute());
        }

        for(ConfigAttribute attribute : attributes){
            if(!(attribute instanceof AuthorityPermission)){
                throw new RuntimeException("attributes must contain only AuthorityPermission");
            }
            AuthorityPermission evalPermission = (AuthorityPermission)attribute;

            for (GrantedAuthority authority: authentication.getAuthorities()){
                AuthorityPermission authorityPermission= new AuthorityPermission(authority.getAuthority());

                boolean isALL = authorityPermission.getClassName().equals(CdmPermissionClass.ALL);
                boolean isClassMatch = isALL || authorityPermission.getClassName().equals(evalPermission.getClassName());

                boolean isADMIN = authorityPermission.getPermission().equals(CdmPermission.ADMIN);
                boolean isPermissionMatch = isADMIN || authorityPermission.getPermission().equals(evalPermission.getPermission());

                boolean hasTargetUuid = authorityPermission.getTargetUUID() != null;

                boolean isUuidMatch = hasTargetUuid && authorityPermission.getTargetUUID().equals(((CdmBase)object).getUuid());

                if ( !hasTargetUuid && isClassMatch && isPermissionMatch){
                    logger.debug("no tragetUuid, class & permission match => ACCESS_GRANTED");
                    return ACCESS_GRANTED;
                }
                if ( isUuidMatch  && isClassMatch && isPermissionMatch){
                    logger.debug("permission, class and uuid are matching => ACCESS_GRANTED");
                    return ACCESS_GRANTED;
                }
                if ( isUuidMatchInParentNodes  && isClassMatch && isPermissionMatch){
                    logger.debug("permission, class and uuid in parent nodes are matching => ACCESS_GRANTED");
                    return ACCESS_GRANTED;
                }
            } // END Authorities loop
        } // END attributes loop

        logger.debug("ACCESS_DENIED");
        return ACCESS_DENIED; // or Abstain???
    }
    */

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#furtherVotingDescisions(org.springframework.security.core.Authentication, java.lang.Object, java.util.Collection, eu.etaxonomy.cdm.persistence.hibernate.permission.voter.TaxonBaseVoter.ValidationResult)
     */
    @Override
    protected Integer furtherVotingDescisions(AuthorityPermission authorityPermission, Object object, Collection<ConfigAttribute> attributes,
            ValidationResult validationResult) {

        boolean isUuidMatchInParentNodes = validationResult.hasTargetUuid && findTargetUuidInParentNodes(authorityPermission.getTargetUUID(), (TaxonNode)object);
        if ( isUuidMatchInParentNodes  && validationResult.isClassMatch && validationResult.isPermissionMatch){
            logger.debug("permission, class and uuid in parent nodes are matching => ACCESS_GRANTED");
            return ACCESS_GRANTED;
        }
        return null;
    }

    /**
     * @param targetUuid
     * @param node
     * @return
     */
    private boolean findTargetUuidInParentNodes(UUID targetUuid, TaxonNode node){
        if (targetUuid.equals(node.getUuid()))
            return true;
        else if (node.getParent()!= null){
             return findTargetUuidInParentNodes(targetUuid, node.getParent());
        }
        return false;
    }

}
