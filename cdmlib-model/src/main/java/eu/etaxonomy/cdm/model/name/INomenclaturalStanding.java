/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

/**
 * @author a.mueller
 * @since 18.11.2020
 */
public interface INomenclaturalStanding {

    //Explicit status

    /**
     * Corresponds to {@link NomenclaturalStanding#OTHER_DESIGNATION}
     */
    public boolean isDesignationOnly();

    /**
     * Corresponds to {@link NomenclaturalStanding#INVALID}
     */
    public boolean isInvalidExplicit();

    /**
     * Corresponds to {@link NomenclaturalStanding#ILLEGITIMATE}
     */
    public boolean isIllegitimate();

    /**
     * Corresponds to {@link NomenclaturalStanding#VALID}
     */
    public boolean isValidExplicit();

    /**
     * Corresponds to {@link NomenclaturalStanding#NONE}
     */
    public boolean isNoStatus();

    //combined status

    /**
     * Returns <code>true</code> if this does not represent a
     * name but only a designation. So it is <code>true</code> for
     * {@link NomenclaturalStanding#INVALID} and {@link NomenclaturalStanding#OTHER_DESIGNATION}
     */
    public boolean isInvalid();

    /**
     * Returns <code>true</code> if the status indicates a valid and not
     * illegitimate name or if no explicit status is mentioned.
     * So it is <code>true</code> for
     * {@link NomenclaturalStanding#VALID} and {@link NomenclaturalStanding#NONE}
     */
    public boolean isLegitimate();

    /**
     * Returns <code>true</code> if the status indicates a valid name (legitimate or not)
     * or if no explicit status is mentioned.
     * So it is <code>true</code> for
     * {@link NomenclaturalStanding#VALID}, {@link NomenclaturalStanding#ILLEGITIMATE}
     * and {@link NomenclaturalStanding#NONE}
     */
    public boolean isValid();

}
