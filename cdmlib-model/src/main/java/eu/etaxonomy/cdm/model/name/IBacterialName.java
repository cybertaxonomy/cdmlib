/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

/**
 * TaxonName interface for bacteria.
 * <P>
 * This class corresponds to: NameBacterial according to the ABCD schema.
 * @author a.mueller
 * @since 26.01.2017
 *
 */
public interface IBacterialName extends INonViralName {

    /**
     * Returns the string containing the authorship with the year and details
     * of the reference in which the subgenus included in the scientific name
     * of <i>this</i> bacterial taxon name was published.
     * For instance if the bacterial taxon name is
     * 'Bacillus (subgen. Aerobacillus Donker 1926, 128) polymyxa' the subgenus
     * authorship string is 'Donker 1926, 128'.
     *
     * @return  the string containing the complete subgenus' authorship
     *          included in <i>this</i> bacterial taxon name
     */
    public String getSubGenusAuthorship();

    /**
     * @see  #getSubGenusAuthorship()
     */
    public void setSubGenusAuthorship(String subGenusAuthorship);

    /**
     * Returns the string representing the reason for the approbation of <i>this</i>
     * bacterial taxon name. Bacterial taxon names are valid or approved
     * according to:
     * <ul>
     * <li>the approved list, c.f.r. IJSB 1980 (AL)
     * <li>the validation list, in IJSB after 1980 (VL)
     * </ul>
     * or
     * <ul>
     * <li>are validly published as paper in IJSB after 1980 (VP).
     * </ul>
     * IJSB is the acronym for International Journal of Systematic Bacteriology.
     *
     * @return  the string with the source of the approbation for <i>this</i> bacterial taxon name
     */
    public String getNameApprobation();

    /**
     * @see  #getNameApprobation()
     */
    public void setNameApprobation(String nameApprobation);
}
