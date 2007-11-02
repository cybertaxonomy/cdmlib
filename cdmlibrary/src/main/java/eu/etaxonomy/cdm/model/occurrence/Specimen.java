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

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:44
 */
public class Specimen extends Occurrence {
	static Logger logger = Logger.getLogger(Specimen.class);

	private PreservationMethod preservation;
	private Specimen derivedFrom;

	public PreservationMethod getPreservation(){
		return preservation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPreservation(PreservationMethod newVal){
		preservation = newVal;
	}

	public Specimen getDerivedFrom(){
		return derivedFrom;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDerivedFrom(Specimen newVal){
		derivedFrom = newVal;
	}

}