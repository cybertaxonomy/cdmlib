/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:03
 */
@Entity
public abstract class FactBase extends VersionableEntity {
	static Logger logger = Logger.getLogger(FactBase.class);

	private String citationMicroReference;
	private String fact;

	public String getCitationMicroReference(){
		return citationMicroReference;
	}

	public String getFact(){
		return fact;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCitationMicroReference(String newVal){
		citationMicroReference = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFact(String newVal){
		fact = newVal;
	}

}