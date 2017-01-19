/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;


/**
 * @author a.kohlbecker
 * @date Feb 22, 2016
 *
 */
public enum SwaggerGroupsConfig {

    LSID_AUTHORITY_SERVICES("LSID authority services"),
    DATA_EXPORT("Data export"),
    GENERIC_REST_API("Generic REST API"),
    WEB_PORTAL_SERVICES("Web Portal Services"),
    CATALOGUE_SERVICES("Catalogue Services");

    private String groupName;

    SwaggerGroupsConfig(String groupName) {
        this.groupName = groupName;
    }

    public String groupName() {
        return groupName;
    }

    public static SwaggerGroupsConfig byGroupName(String groupName) {
        for(SwaggerGroupsConfig group : SwaggerGroupsConfig.values()) {
            if(group.groupName.equals(groupName)) {
                return group;
            }
        }
        return null;
    }
}
