/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

import java.util.HashSet;
import java.util.Set;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.utility.UserHelper;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;

/**
 * Provides filter functions for entities ({@link TaxonName}, {@link TypeDesignationBase})
 * which can be subject to a {@link Registration}.
 * Entities which are associated with a <code>Registration</code> must not be published as long
 * as the registration status is not {@link RegistrationStatus#PUBLISHED PUBLISHED} and and the
 * publication not released.
 *
 * @author a.kohlbecker
 * @since Nov 17, 2020
 */
@Component
public class RegistrableEntityFilter {

    private static Logger logger = Logger.getLogger(RegistrableEntityFilter.class);

    protected UserHelper userHelper;

    public static RegistrableEntityFilter newInstance(UserHelper userHelper) {
        return new RegistrableEntityFilter(userHelper);
    }

    private RegistrableEntityFilter(UserHelper userHelper) {
        this.userHelper = userHelper;
    }

    /**
     * Filters a set of NameRelationship so that none of the relations contains a unpublished name.
     *
     * @param taxonName
     *  The name to which all the nameRelations belong to. It being is assumed that this
     *  name can be publicly visible.
     *
     * @param nameRelations
     *  The set of name relations to be filtered.
     * @return
     *  A Set of name relations which can be publicly visible.
     */
    public Set<NameRelationship> filterPublishedOnly(TaxonName taxonName, Set<NameRelationship> nameRelations) {
        Set<NameRelationship> nameRelationsFiltered = new HashSet<>(nameRelations.size());
        if(!currentUserMaySeeUnpublished()){
        // need to filter out unpublished related names in this case
            for(NameRelationship rel : nameRelations){
                // check if the name has been published by any registration
                Set<Registration> regsToCheck = new HashSet<>();
                if(rel.getToName().equals(taxonName) && rel.getFromName().getRegistrations() != null) {
                    regsToCheck.addAll(rel.getFromName().getRegistrations());
                }
                if(rel.getFromName().equals(taxonName) && rel.getToName().getRegistrations() != null) {
                    regsToCheck.addAll(rel.getToName().getRegistrations());
                }
                // if there is no registration for this name we assume that it is published
                boolean nameIsPublished = regsToCheck.size() == 0;
                nameIsPublished |= regsToCheck.stream().anyMatch(reg -> reg.getStatus().equals(RegistrationStatus.PUBLISHED));
                if(nameIsPublished){
                    nameRelationsFiltered.add(rel);
                } else {
                    logger.debug("Hiding NameRelationship " + rel);
                }
            }
        }  else {
            // no filtering needed
            nameRelationsFiltered = nameRelations;
        }
        return nameRelationsFiltered;
    }

    /**
     * Check if the currently authenticated user is allowed to see unpublished entities wich are
     * subject to a registration.
     *
     * @return
     */
    protected boolean currentUserMaySeeUnpublished() {
        return !(!userHelper.userIsAutheticated() || userHelper.userIsAnnonymous());
    }


}
