/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.agent;


import etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * An author team may exist for itself or may be built with the persons who belong
 * to it.
 * {At least one otf the attributes shortName or fullName must exist.}
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:23
 */
public class Team extends VersionableEntity {
	static Logger logger = Logger.getLogger(Team.class);

	//An abreviated name for the team (e. g. in case of nomenclatural authorteams).
	//A non abreviated name for the team (e. g. in case of some bibliographical references)
	@Description("An abreviated name for the team (e. g. in case of nomenclatural authorteams).
	A non abreviated name for the team (e. g. in case of some bibliographical references)")
	private String originalCitation;
	private ArrayList teamInSource;
	private java.util.ArrayList teamMembers;

	public java.util.ArrayList getTeamMembers(){
		return teamMembers;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTeamMembers(java.util.ArrayList newVal){
		teamMembers = newVal;
	}

	public ArrayList getTeamInSource(){
		return teamInSource;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTeamInSource(ArrayList newVal){
		teamInSource = newVal;
	}

	public String getOriginalCitation(){
		return originalCitation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setOriginalCitation(String newVal){
		originalCitation = newVal;
	}

}