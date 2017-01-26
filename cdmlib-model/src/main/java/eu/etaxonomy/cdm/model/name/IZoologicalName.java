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
 * TaxonName interface for animals.
 * <P>
 * This class corresponds to: NameZoological according to the ABCD schema.
 *
 * @author a.mueller
 * @date 26.01.2017
 *
 */
public interface IZoologicalName extends INonViralName {

    /**
     * Returns the breed name string for <i>this</i> animal (zoological taxon name).
     *
     * @return  the string containing the breed name for <i>this</i> zoological taxon name
     */
    public String getBreed();

    /**
     * @see  #getBreed()
     */
    public void setBreed(String breed);

    /**
     * Returns the publication year (as an integer) of the original validly
     * published species epithet for <i>this</i> zoological taxon name. This only
     * applies for zoological taxon names that are no {@link TaxonNameBase#isOriginalCombination() original combinations}.
     * If the originalPublicationYear attribute is null the year could be taken
     * from the publication year of the corresponding original name (basionym)
     * or from the {@link eu.etaxonomy.cdm.reference.INomenclaturalReference nomenclatural reference} of the basionym
     * if it exists.
     *
     * @return  the integer representing the publication year of the original
     *          species epithet corresponding to <i>this</i> zoological taxon name
     * @see     #getPublicationYear()
     */
    public Integer getOriginalPublicationYear();

    /**
     * @see  #getOriginalPublicationYear()
     */
    public void setOriginalPublicationYear(Integer originalPublicationYear);

    /**
     * Returns the publication year (as an integer) for <i>this</i> zoological taxon
     * name. If the publicationYear attribute is null and a nomenclatural
     * reference exists the year could be computed from the
     * {@link eu.etaxonomy.cdm.reference.INomenclaturalReference nomenclatural reference}.
     *
     * @return  the integer representing the publication year for <i>this</i> zoological taxon name
     * @see     #getOriginalPublicationYear()
     */
    public Integer getPublicationYear();
    /**
     * @see  #getPublicationYear()
     */
    public void setPublicationYear(Integer publicationYear);
}
