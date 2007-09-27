/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * This author team class note explains everything there is to authors and
 * includes explanation why certain attributes have been dropped or are dealt with
 * elsewhere.
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:16
 */
@Entity
public class Team extends VersionableEntity {
	static Logger logger = Logger.getLogger(Team.class);

	private String fullName;
	private String shortName;
	private java.util.ArrayList teamMembers;

	public String getFullName(){
		return fullName;
	}

	public String getShortName(){
		return shortName;
	}

	public java.util.ArrayList getTeamMembers(){
		return teamMembers;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFullName(String newVal){
		fullName = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setShortName(String newVal){
		shortName = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTeamMembers(java.util.ArrayList newVal){
		teamMembers = newVal;
	}

}