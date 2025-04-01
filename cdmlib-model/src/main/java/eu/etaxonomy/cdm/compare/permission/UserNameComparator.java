/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.permission;

import java.util.Comparator;

import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author k.luther
 * @since 02.07.2018
 * @since 2025-04-01 moved to cdmlib from TaxEditor
 */
public class UserNameComparator implements Comparator<User> {

    private boolean ignoreCase;

    public UserNameComparator() {
        this.ignoreCase = false;
    }

    public UserNameComparator(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }


    @Override
    public int compare(User user0, User user1) {

        if (user0 == null && user1 == null) {
            return 0;
        }else if (user0 == null) {
            return -1;
        }else if (user1 == null) {
            return 1;
        }

        String userName0 = user0.getUsername();
        String userName1 = user1.getUsername();
        if (userName0 == null && userName1 == null) {
            return 0;
        }
        if (userName0 == null) {
            return -1;
        }
        if (userName1 == null) {
            return 1;
        }

        int result =  ignoreCase ? userName0.compareToIgnoreCase(userName1) :
            userName0.compareTo(userName1);

        if (result == 0){
            result = user0.getUuid().compareTo(user1.getUuid());
        }
        return result;

    }

}