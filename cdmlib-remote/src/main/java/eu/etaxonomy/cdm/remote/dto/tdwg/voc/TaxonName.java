/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.Name;

/**
 * TaxonName class for TDWG Ontology mapping.
 *
 * @author ben.clark
 * @date 2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonName", propOrder = {
	    "authorship",
	    "nameComplete"
})
@XmlRootElement(name = "TaxonName", namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
public class TaxonName extends Name {

	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
	private String authorship;

	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
	private String nameComplete;

	public String getAuthorship() {
		return authorship;
	}

	public void setAuthorship(String authorship) {
		this.authorship = authorship;
	}

	public String getNameComplete() {
		return nameComplete;
	}

	public void setNameComplete(String nameComplete) {
		this.nameComplete = nameComplete;
	}
}