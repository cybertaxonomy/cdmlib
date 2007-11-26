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
 * An author team may exist for itself or may be built with the persons who belong
 * to it. {At least one otf the attributes shortName or fullName must exist.}
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:58
 */
@Entity
public class Team extends Agent {
	static Logger logger = Logger.getLogger(Team.class);

	//An abreviated name for the team (e. g. in case of nomenclatural authorteams). A non abreviated name for the team (e. g.
	//in case of some bibliographical references)
	private List<Person> teamMembers = new ArrayList();
	
	public Team() {
		super();
		// TODO Auto-generated constructor stub
	}

	@ManyToMany
	public List<Person> getTeamMembers(){
		return this.teamMembers;
	}
	protected void setTeamMembers(List<Person> teamMembers){
		this.teamMembers = teamMembers;
	}
	public void addTeamMember(Person person){
		this.teamMembers.add(person);
	}
	public void removeTeamMember(Person person){
		this.teamMembers.remove(person);
	}

}