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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * An author team may exist for itself or may be built with the persons who belong
 * to it.
 * {At least one otf the attributes shortName or fullName must exist.}
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:38
 */
@Entity
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
	 * @param teamMembers
	 */
	public void setTeamMembers(java.util.ArrayList teamMembers){
		;
	}

	public ArrayList getTeamInSource(){
		return teamInSource;
	}

	/**
	 * 
	 * @param teamInSource
	 */
	public void setTeamInSource(ArrayList teamInSource){
		;
	}

	public String getOriginalCitation(){
		return originalCitation;
	}

	/**
	 * 
	 * @param originalCitation
	 */
	public void setOriginalCitation(String originalCitation){
		;
	}

}