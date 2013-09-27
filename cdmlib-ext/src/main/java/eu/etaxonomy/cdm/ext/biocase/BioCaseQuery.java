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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessionNumber == null) ? 0 : accessionNumber.hashCode());
        result = prime * result + ((collector == null) ? 0 : collector.hashCode());
        result = prime * result + ((collectorsNumber == null) ? 0 : collectorsNumber.hashCode());
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((herbarium == null) ? 0 : herbarium.hashCode());
        result = prime * result + ((locality == null) ? 0 : locality.hashCode());
        result = prime * result + ((taxonName == null) ? 0 : taxonName.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BioCaseQuery other = (BioCaseQuery) obj;
        if (accessionNumber == null) {
            if (other.accessionNumber != null) {
                return false;
            }
        } else if (!accessionNumber.equals(other.accessionNumber)) {
            return false;
        }
        if (collector == null) {
            if (other.collector != null) {
                return false;
            }
        } else if (!collector.equals(other.collector)) {
            return false;
        }
        if (collectorsNumber == null) {
            if (other.collectorsNumber != null) {
                return false;
            }
        } else if (!collectorsNumber.equals(other.collectorsNumber)) {
            return false;
        }
        if (country == null) {
            if (other.country != null) {
                return false;
            }
        } else if (!country.equals(other.country)) {
            return false;
        }
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (herbarium == null) {
            if (other.herbarium != null) {
                return false;
            }
        } else if (!herbarium.equals(other.herbarium)) {
            return false;
        }
        if (locality == null) {
            if (other.locality != null) {
                return false;
            }
        } else if (!locality.equals(other.locality)) {
            return false;
        }
        if (taxonName == null) {
            if (other.taxonName != null) {
                return false;
            }
        } else if (!taxonName.equals(other.taxonName)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String string = "BioCaseQuery ";
        if(taxonName!=null && taxonName.trim().isEmpty()){
            string += " taxonName=" + taxonName;
        }
        if(collector!=null && collector.trim().isEmpty()){
            string += " collector=" + collector;
        }
        if(collectorsNumber!=null && collectorsNumber.trim().isEmpty()){
            string += " collectorsNumber=" + collectorsNumber;
        }
        if(accessionNumber!=null && accessionNumber.trim().isEmpty()){
            string += " accessionNumber=" + accessionNumber;
        }
        if(herbarium!=null && herbarium.trim().isEmpty()){
            string += " herbarium=" + herbarium;
        }
        if(country!=null && country.trim().isEmpty()){
            string += " country=" + country;
        }
        if(locality!=null && locality.trim().isEmpty()){
            string += " locality=" + locality;
        }
        if(date!=null){
            string += " date=" + date;
        }
        return string;
    }




}
