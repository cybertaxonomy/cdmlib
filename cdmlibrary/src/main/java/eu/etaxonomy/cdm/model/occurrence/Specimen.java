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
 * @created 02-Nov-2007 19:18:39
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
	 * @param preservation
	 */
	public void setPreservation(PreservationMethod preservation){
		;
	}

	public Specimen getDerivedFrom(){
		return derivedFrom;
	}

	/**
	 * 
	 * @param derivedFrom
	 */
	public void setDerivedFrom(Specimen derivedFrom){
		;
	}

}