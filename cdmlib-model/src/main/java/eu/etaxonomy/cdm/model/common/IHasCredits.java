/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.List;

/**
 * Interface indicating that an entity supports {@link Credit}s
 *
 * @author muellera
 * @since 22.07.2025
 * @see https://dev.e-taxonomy.eu/redmine/issues/10772
 */
public interface IHasCredits {

    public List<Credit> getCredits();

    public Credit getCredits(Integer index);

    public void addCredit(Credit credit);

    public void addCredit(Credit credit, int index);

    public void removeCredit(Credit credit);

    public void removeCredit(int index);

    /**
     * Replaces all occurrences of oldObject in the credits list with newObject
     * @param newObject the replacement object
     * @param oldObject the object to be replaced
     * @return true, if an object was replaced, false otherwise
     */
    public boolean replaceCredit(Credit newObject, Credit oldObject);
}
