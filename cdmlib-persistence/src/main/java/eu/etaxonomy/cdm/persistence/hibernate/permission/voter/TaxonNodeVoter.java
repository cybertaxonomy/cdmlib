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

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;

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
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#furtherVotingDescisions(org.springframework.security.core.Authentication, java.lang.Object, java.util.Collection, eu.etaxonomy.cdm.persistence.hibernate.permission.voter.TaxonBaseVoter.ValidationResult)
     */
    @Override
    protected Integer furtherVotingDescisions(CdmAuthority CdmAuthority, Object object, Collection<ConfigAttribute> attributes,
            ValidationResult validationResult) {

        boolean isUuidMatchInParentNodes = CdmAuthority.hasTargetUuid() && findTargetUuidInParentNodes(CdmAuthority.getTargetUUID(), (TaxonNode)object);
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
        if (targetUuid.equals(node.getUuid())) {
            return true;
        } else {
            TaxonNode parentNode = HibernateProxyHelper.deproxy(node, TaxonNode.class).getParent();
            if (parentNode != null){
                 return findTargetUuidInParentNodes(targetUuid, parentNode);
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.hibernate.permission.voter.CdmPermissionVoter#isOrpahn(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public boolean isOrpahn(CdmBase object) {
        if(object instanceof TaxonNode){
            return ((TaxonNode)object).getParent() == null;
        }
        return false;
    }

}
