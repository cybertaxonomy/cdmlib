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
 * The taxon name interface for viral taxa. The scientific name will be stored
 * as a string (consisting eventually of several words even combined also with
 * non alphabetical characters) in the inherited {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#setTitleCache(String) titleCache} attribute.
 * Classification has no influence on the names of viral taxon names and no
 * viral taxon must be taxonomically included in another viral taxon with
 * higher rank. For examples see ICTVdb:
 * "http://www.ncbi.nlm.nih.gov/ICTVdb/Ictv/vn_indxA.htm"
 * <p>
 * For the 2017 version of the ICVCN see https://talk.ictvonline.org/information/w/ictv-information/383/ictv-code
 *
 * <P>
 * This class corresponds to: NameViral according to the ABCD schema.
 *
 * @author a.mueller
 \* @since 26.01.2017
 *
 */
public interface IViralName extends ITaxonNameBase {

    /**
     * Returns the accepted acronym (an assigned abbreviation) string for <i>this</i>
     * viral taxon name. For instance PCV stays for Peanut Clump Virus.
     *
     * @return  the string containing the accepted acronym of <i>this</i> viral taxon name
     */
    public String getAcronym();

    /**
     * @see  #getAcronym()
     */
    public void setAcronym(String acronym);
}
