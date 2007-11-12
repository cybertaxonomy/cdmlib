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
 * to it. {At least one otf the attributes shortName or fullName must exist.}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:58
 */
@Entity
public class Team extends VersionableEntity {
	public Team() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Logger logger = Logger.getLogger(Team.class);
	//An abreviated name for the team (e. g. in case of nomenclatural authorteams). A non abreviated name for the team (e. g.
	//in case of some bibliographical references)
	private String originalCitation;
	private Set<TeamInSource> teamInSource;
	private ArrayList<Person> teamMembers;

	public ArrayList<Person> getTeamMembers(){
		return this.teamMembers;
	}

	/**
	 * 
	 * @param teamMembers    teamMembers
	 */
	public void setTeamMembers(ArrayList<Person> teamMembers){
		this.teamMembers = teamMembers;
	}

	public Set<TeamInSource> getTeamInSource(){
		return this.teamInSource;
	}
	public void setTeamInSource(Set<TeamInSource> teamInSource){
		this.teamInSource = teamInSource;
	}
	public void addTeamInSource(TeamInSource teamInSource){
		this.teamInSource.add(teamInSource);
	}
	public void removeTeamInSource(TeamInSource teamInSource){
		this.teamInSource.remove(teamInSource);
	}

	
	public String getOriginalCitation(){
		return this.originalCitation;
	}

	/**
	 * 
	 * @param originalCitation    originalCitation
	 */
	public void setOriginalCitation(String originalCitation){
		this.originalCitation = originalCitation;
	}

}