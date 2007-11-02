/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.description;


import etaxonomy.cdm.model.location.NamedArea;
import org.apache.log4j.Logger;

/**
 * fact attribute contains the concrete occurrence term like "Extinct"
 * This allows all terms to enter the database and classify them basically
 * according to class hierarchy of distribution.
 * 
 * {validInRegion mandatory}
 * {type is "distribution"}
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:45
 */
public class Distribution extends FeatureBase {
	static Logger logger = Logger.getLogger(Distribution.class);

	private NamedArea area;
	private PresenceAbsenceTermBase status;

	public NamedArea getArea(){
		return area;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setArea(NamedArea newVal){
		area = newVal;
	}

	public PresenceAbsenceTermBase getStatus(){
		return status;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setStatus(PresenceAbsenceTermBase newVal){
		status = newVal;
	}

}