/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.registration.messages.redmine;

/**
 * @author a.kohlbecker
 * @since Feb 15, 2018
 *
 */
public enum RedminePreferenceKey {


    ADMIN_USER_API_KEY("adminUserApiKey"),
    REDMINE_URL("redmineURL"),
    PROJECT_ID("projectId"),

    CUSTOM_FIELD_IDENTIFIER_ID("customFieldId.identifier"),
    CUSTOM_FIELD_CURATOR_ID("customFieldId.curator"),
    CUSTOM_FIELD_SUBMITTER_ID("customFieldId.submitter"),

    ISSUE_PRIORITIY_INACTIVE_ID("issuePriorityId.inactive"),
    ISSUE_PRIORITIY_ACTIVE_ID("issuePriorityId.active"),

    ISSUE_STATUS_PREPARATION_ID("issueStatusId.preparation"),
    ISSUE_STATUS_CURATION_ID("issueStatusId.curation"),
    ISSUE_STATUS_READY_ID("issueStatusId.ready"),
    ISSUE_STATUS_PUBLISHED_ID("issueStatusId.published"),
    ISSUE_STATUS_REJECTED_ID("issueStatusId.rejected"),

    ROLE_ID_CONTRIBUTOR("roleID.contributor")
    ;

    String key;

    RedminePreferenceKey(String key){
        this.key = key;
    }

    /**
     * Returns the key as string representation.
     */
    @Override
    public String toString(){
        return key;
    }


}
