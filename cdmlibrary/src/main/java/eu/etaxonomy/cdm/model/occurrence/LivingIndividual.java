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
 * @created 02-Nov-2007 19:18:21
 */
public class LivingIndividual extends Occurrence {
	static Logger logger = Logger.getLogger(LivingIndividual.class);

	private ArrayList parents;
	private ArrayList offspring;

	public ArrayList getParents(){
		return parents;
	}

	/**
	 * 
	 * @param parents
	 */
	public void setParents(ArrayList parents){
		;
	}

	public ArrayList getOffspring(){
		return offspring;
	}

	/**
	 * 
	 * @param offspring
	 */
	public void setOffspring(ArrayList offspring){
		;
	}

}