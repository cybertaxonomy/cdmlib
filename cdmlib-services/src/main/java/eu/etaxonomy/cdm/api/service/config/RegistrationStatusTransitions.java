/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.name.RegistrationStatus;

/**
 *
 * @author a.kohlbecker
 * @since Jul 4, 2018
 *
 */
public class RegistrationStatusTransitions {

    static private Map<RegistrationStatus, Set<RegistrationStatus>> allowedTransitions = new HashMap<>();

    static {
        allowedTransitions.put(RegistrationStatus.PREPARATION, new HashSet<>(Arrays.asList(RegistrationStatus.CURATION)));
        allowedTransitions.put(RegistrationStatus.CURATION, new HashSet<>(Arrays.asList(RegistrationStatus.PREPARATION, RegistrationStatus.READY)));
        allowedTransitions.put(RegistrationStatus.READY, new HashSet<>(Arrays.asList(RegistrationStatus.PUBLISHED, RegistrationStatus.REJECTED)));
        allowedTransitions.put(RegistrationStatus.PUBLISHED, new HashSet<>()); // no further transition
        allowedTransitions.put(RegistrationStatus.REJECTED, new HashSet<>()); // no further transition
    }


    /**
     * @param currentStatus
     */
    public static Set<RegistrationStatus> possibleTransitions(RegistrationStatus currentStatus) {
        return allowedTransitions.get(currentStatus);
    }



}
