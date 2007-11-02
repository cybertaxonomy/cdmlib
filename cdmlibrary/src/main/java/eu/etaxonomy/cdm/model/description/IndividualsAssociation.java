/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.MultilanguageString;
import eu.etaxonomy.cdm.model.occurrence.ObservationalUnit;
import org.apache.log4j.Logger;

/**
 * {type is "host" or "hybrid_parent"}
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:22
 */
public class IndividualsAssociation extends FeatureBase {
	static Logger logger = Logger.getLogger(IndividualsAssociation.class);

	@Description("")
	private MultilanguageString description;
	private ObservationalUnit observationalUnit2;

	public ObservationalUnit getObservationalUnit2(){
		return observationalUnit2;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setObservationalUnit2(ObservationalUnit newVal){
		observationalUnit2 = newVal;
	}

	public MultilanguageString getDescription(){
		return description;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(MultilanguageString newVal){
		description = newVal;
	}

}