/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.permission.voter;

import java.util.Collection;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;

/**
 * @author andreas kohlbecker
 * @since Sep 4, 2012
 */
public class TaxonNodeVoter extends CdmPermissionVoter {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Class<? extends CdmBase> getResponsibilityClass() {
        return TaxonNode.class;
    }

    @Override
    protected Integer furtherVotingDescisions(CdmAuthority CdmAuthority, TargetEntityStates targetEntityStates, Collection<ConfigAttribute> attributes,
            ValidationResult validationResult) {

        boolean isUuidMatchInParentNodes = CdmAuthority.hasTargetUuid() && findTargetUuidInParentNodes(CdmAuthority.getTargetUUID(), (TaxonNode)targetEntityStates.getEntity());
        if ( isUuidMatchInParentNodes  && validationResult.isClassMatch && validationResult.isPermissionMatch){
            logger.debug("permission, class and uuid in parent nodes are matching => ACCESS_GRANTED");
            return ACCESS_GRANTED;
        }
        return null;
    }

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

    @Override
    public boolean isOrpahn(CdmBase object) {
        if(object instanceof TaxonNode){
            return ((TaxonNode)object).getParent() == null;
        }
        return false;
    }

}
