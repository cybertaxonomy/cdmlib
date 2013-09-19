// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.biocase;

import java.util.Date;

/**
 * Holds query parameters for a query to a BioCase provider.
 * @author pplitzner
 * @date 17.09.2013
 *
 */
public class BioCaseQuery {
    public String taxonName;
    public String collector;
    public String collectorsNumber;
    public String accessionNumber;
    public String herbarium;
    public String country;
    public String locality;
    public Date date;

    /**
     * Default constructor which initializes all parameters with <code>null</code>
     */
    public BioCaseQuery() {
        taxonName = null;
        collector = null;
        collectorsNumber = null;
        accessionNumber = null;
        herbarium = null;
        country = null;
        locality = null;
        date = null;
    }

    /**
     * Constructor to initially set the parameters
     * @param taxonName
     * @param collector
     * @param collectorsNumber
     * @param accessionNumber
     * @param herbarium
     * @param country
     * @param locality
     * @param date
     */
    public BioCaseQuery(String taxonName, String collector, String collectorsNumber, String accessionNumber, String herbarium, String country, String locality, Date date) {
        this.taxonName = taxonName;
        this.collector = collector;
        this.collectorsNumber = collectorsNumber;
        this.accessionNumber = accessionNumber;
        this.herbarium = herbarium;
        this.country = country;
        this.locality = locality;
        this.date = date;
    }


}
