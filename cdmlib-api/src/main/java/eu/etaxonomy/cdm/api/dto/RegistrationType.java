/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

/**
 * @author a.kohlbecker
 * @since Mar 3, 2017
 */
public enum RegistrationType {

    /**
     * A <code>Registration</code> for a new name
     */
    NAME,
    /**
     * A <code>Registration</code> for a new name and one or more according
     * typifications.
     */
    NAME_AND_TYPIFICATION,
    /**
     * A <code>Registration</code> for one or more typifications for an
     * previously published name.
     */
    TYPIFICATION,
    /**
     * A newly created <code>Registration</code> without any name and
     * typification.
     */
    EMPTY;

    public boolean isName() {
        return NAME.equals(this);

    }

    public boolean isTypification() {
        return TYPIFICATION.equals(this);
    }

    public boolean isNameAndTypification() {
        return NAME_AND_TYPIFICATION.equals(this);
    }

    public boolean isEnmpty() {
        return EMPTY.equals(this);
    }
}