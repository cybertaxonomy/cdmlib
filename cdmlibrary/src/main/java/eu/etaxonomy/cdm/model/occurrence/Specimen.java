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

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:52
 */
@Entity
public class Specimen extends Occurrence {
	static Logger logger = Logger.getLogger(Specimen.class);
	private PreservationMethod preservation;
	private Specimen derivedFrom;

	@ManyToOne
	public PreservationMethod getPreservation(){
		return this.preservation;
	}
	public void setPreservation(PreservationMethod preservation){
		this.preservation = preservation;
	}

	@ManyToOne
	public Specimen getDerivedFrom(){
		return this.derivedFrom;
	}
	public void setDerivedFrom(Specimen derivedFrom){
		this.derivedFrom = derivedFrom;
	}

}