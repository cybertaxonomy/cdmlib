/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Reference systems for coordinates also according to OGC (Open Geographical
 * Consortium) The list should be extensible at runtime through configuration.
 * This needs to be investigated.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:49
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceSystem")
@XmlRootElement(name = "ReferenceSystem")
@Entity
public class ReferenceSystem extends DefinedTermBase {
	static Logger logger = Logger.getLogger(ReferenceSystem.class);

	/**
	 * Factory method
	 * @return
	 */
	public static ReferenceSystem NewInstance(){
		return new ReferenceSystem();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static ReferenceSystem NewInstance(String term, String label, String labelAbbrev){
		return new ReferenceSystem(term, label, labelAbbrev);
	}
	
	/**
	 * Constructor
	 */
	private ReferenceSystem() {
		super();
	}

	/**
	 * Constructor
	 */
	public ReferenceSystem(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	public static final ReferenceSystem WGS84(){
		logger.warn("not yet implemented");
		return null;
	}

}