// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

/**
 * Holds query parameters for a query to an occurrence provider.
 * @author pplitzner
 * @date 17.09.2013
 *
 */
public class OccurenceQuery {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public final Set<String[]> tripleIds;

    public final String taxonName;
    public final String collector;
    public final String collectorsNumber;
    public final String accessionNumber;
    public final String herbarium;
    public final String country;
    public final String locality;
    public final Calendar dateFrom;
    public final Calendar dateTo;



    public OccurenceQuery(Set<String[]> tripleIds) {

        this(tripleIds, null, null, null, null, null, null, null, null, null);
    }

    /**
     * @param taxonName
     * @param collector
     * @param collectorsNumber
     * @param accessionNumber
     * @param herbarium
     * @param country
     * @param locality
     * @param dateFrom
     * @param dateTo
     */
    public OccurenceQuery(String taxonName, String collector, String collectorsNumber, String accessionNumber,
            String herbarium, String country, String locality, Calendar dateFrom, Calendar dateTo) {
        this(null, taxonName, collector, collectorsNumber, accessionNumber, herbarium, country, locality, dateFrom, dateTo);
    }

    /**
     * Constructor to initially set the parameters
     */
    private OccurenceQuery(Set<String[]> tripleIds, String taxonName, String collector, String collectorsNumber, String accessionNumber, String herbarium, String country, String locality, Calendar dateFrom, Calendar dateTo) {
        this.tripleIds = tripleIds;
        this.taxonName = taxonName;
        this.collector = collector;
        this.collectorsNumber = collectorsNumber;
        this.accessionNumber = accessionNumber;
        this.herbarium = herbarium;
        this.country = country;
        this.locality = locality;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
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
        result = prime * result + ((dateFrom == null) ? 0 : dateFrom.hashCode());
        result = prime * result + ((dateTo == null) ? 0 : dateTo.hashCode());
        result = prime * result + ((herbarium == null) ? 0 : herbarium.hashCode());
        result = prime * result + ((locality == null) ? 0 : locality.hashCode());
        result = prime * result + ((taxonName == null) ? 0 : taxonName.hashCode());
        result = prime * result + ((tripleIds == null) ? 0 : tripleIds.hashCode());
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
        OccurenceQuery other = (OccurenceQuery) obj;
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
        if (dateFrom == null) {
            if (other.dateFrom != null) {
                return false;
            }
        } else if (!dateFrom.equals(other.dateFrom)) {
            return false;
        }
        if (dateTo == null) {
            if (other.dateTo != null) {
                return false;
            }
        } else if (!dateTo.equals(other.dateTo)) {
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
        if (tripleIds == null) {
            if (other.tripleIds != null) {
                return false;
            }
        } else if (!tripleIds.equals(other.tripleIds)) {
            return false;
        }
        return true;
    }




    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String string = "";
        if(tripleIds!=null ){
            string += " unitId=" ;
            for (String[] unitId: tripleIds){
                string += unitId + ", ";
            }
        }
        if(taxonName!=null && !taxonName.trim().isEmpty()){
            string += " taxonName=" + taxonName;
        }
        if(collector!=null && !collector.trim().isEmpty()){
            string += " collector=" + collector;
        }
        if(collectorsNumber!=null && !collectorsNumber.trim().isEmpty()){
            string += " collectorsNumber=" + collectorsNumber;
        }
        if(accessionNumber!=null && !accessionNumber.trim().isEmpty()){
            string += " accessionNumber=" + accessionNumber;
        }
        if(herbarium!=null && !herbarium.trim().isEmpty()){
            string += " herbarium=" + herbarium;
        }
        if(country!=null && !country.trim().isEmpty()){
            string += " country=" + country;
        }
        if(locality!=null && !locality.trim().isEmpty()){
            string += " locality=" + locality;
        }
        if(dateFrom!=null){
            string += " dateFrom=" + DATE_FORMAT.format(dateFrom.getTime());
        }
        if(dateTo!=null){
            string += " dateTo=" + DATE_FORMAT.format(dateTo.getTime());
        }
        return string;
    }
}