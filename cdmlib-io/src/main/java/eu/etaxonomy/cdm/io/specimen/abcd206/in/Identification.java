/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

/**
 * @author pplitzner
 * @since Sep 16, 2014
 *
 */
public class Identification {

    private final String scientificName;
    private final String identifier;
    private final String preferred;
    private final String code;
    private final String date;

    public Identification(String taxonName, String preferred, String date) {
        this(taxonName, preferred, null, null, date);
    }

    public Identification(String scientificName, String preferred, String code, String identifier, String date) {
        super();
        this.scientificName = scientificName.trim();
        if (preferred != null){
            this.preferred = preferred.trim();
        }else{
            this.preferred = null;
        }
        this.code = code;
        if (identifier != null){
            this.identifier = identifier.trim();
        } else{
            this.identifier = null;
        }

        this.date = date;
    }

    /**
     * @return the taxonName
     */
    public String getScientificName() {
        return scientificName;
    }

    /**
     * @return the preferred
     */
    public String getPreferred() {
        return preferred;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Identification [taxonName=" + scientificName + ", preferred=" + preferred + ", code=" + code + "]";
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the identifier
     */
    public String getDate() {
        return date;
    }

}
