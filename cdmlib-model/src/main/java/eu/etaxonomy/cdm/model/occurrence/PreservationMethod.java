/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * http://rs.tdwg.org/ontology/voc/Collection.rdf#SpecimenPreservationMethodTypeTerm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PreservationMethod")
@Entity
public class PreservationMethod extends DefinedTermBase {
	static Logger logger = Logger.getLogger(PreservationMethod.class);
	
	/**
	 * Factory method
	 * @return
	 */
	public static PreservationMethod NewInstance(){
		return new PreservationMethod();
	}
	
	/**
	 * Constructor
	 */
	private PreservationMethod() {
		super();
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static PreservationMethod NewInstance(String term, String label, String labelAbbrev) {
		return new PreservationMethod(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	protected PreservationMethod(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
}