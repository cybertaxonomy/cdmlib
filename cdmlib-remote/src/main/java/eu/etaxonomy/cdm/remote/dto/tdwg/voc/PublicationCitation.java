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
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * PublicationCitation class for TDWG Ontology mapping.
 *
 * @author ben.clark
 * @date 2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicationCitation", propOrder = {
})
@XmlRootElement(name = "PublicationCitation", namespace = "http://rs.tdwg.org/ontology/voc/PublicationCitation#")
public class PublicationCitation extends eu.etaxonomy.cdm.remote.dto.tdwg.PublicationCitation {

}