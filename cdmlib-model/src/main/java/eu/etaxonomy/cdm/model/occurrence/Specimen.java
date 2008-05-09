/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * A specimen is regarded as derived from an field observation, 
 * so locality and gathering related information is captured as a separate FieldObservation object
 * related to a specimen via a derivation event
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:52
 */
@Entity
public class Specimen extends DerivedUnitBase {
	static Logger logger = Logger.getLogger(Specimen.class);
	
	private PreservationMethod preservation;
	
	/**
	 * Factory method
	 * @return
	 */
	public static Specimen NewInstance(){
		return new Specimen();
	}
	
	/**
	 * Constructor
	 */
	protected Specimen() {
		super();
	}
	
	@ManyToOne
	public PreservationMethod getPreservation(){
		return this.preservation;
	}
	public void setPreservation(PreservationMethod preservation){
		this.preservation = preservation;
	}

}