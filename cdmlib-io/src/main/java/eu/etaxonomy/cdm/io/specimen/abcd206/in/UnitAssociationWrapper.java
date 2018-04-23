/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.net.URI;

import org.w3c.dom.NodeList;

/**
 * Wrapper class which hold the list of associated units and
 * the parameters of the association.
 * @author pplitzner
 \* @since Jun 24, 2015
 *
 */
public class UnitAssociationWrapper {

    private NodeList associatedUnits;
    private URI accesPoint;
    private String associationType;
    private String prefix;

    public NodeList getAssociatedUnits() {
        return associatedUnits;
    }

    public void setAssociatedUnits(NodeList associatedUnits) {
        this.associatedUnits = associatedUnits;
    }

    public String getAssociationType() {
        return associationType;
    }

    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the accesPoint
     */
    public URI getAccesPoint() {
        return accesPoint;
    }

    /**
     * @param accesPoint the accesPoint to set
     */
    public void setAccesPoint(URI accesPoint) {
        this.accesPoint = accesPoint;
    }

}
