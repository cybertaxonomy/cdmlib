/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * A team exists for itself or is built with the list of (distinct) persons
 * who belong to it.
 * In the first case the inherited attribute {@link common.IdentifiableEntity#titleCache titleCache} is to be used.
 * In the second case at least all abbreviated names (the attributes Person.titleCache)
 * or all full names (the strings returned by Person.generateTitle)
 * of the persons must exist. A team is an ordered set of persons.
 * 
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
	
	/** 
	 * Class constructor
	 */
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
	/** 
	 * Adds a new person to this team at the end of the members' list. 
	 *
	 * @param  person  the person who should be added to the other team members
	 * @see 		   Person
	 */
	public void addTeamMember(Person person){
		this.teamMembers.add(person);
	}
	/** 
	 * Removes one person from the list of members of this team.
	 *
	 * @param  person  the person who should be deleted from this team
	 * @see            #addTeamMember(Person)
	 */
	public void removeTeamMember(Person person){
		this.teamMembers.remove(person);
	}

	@Override
	/**
	 * Generates an identification string for this team.
	 * This string might be built with the full names or with the abbreviated names
	 * of all persons belonging to its (ordered) members' list.
	 * This method overrides {@link common.IdentifiableEntity#generateTitle() generateTitle}.
	 * The result might be kept as {@link common.IdentifiableEntity#setTitleCache(String) titleCache} if the
	 * flag {@link common.IdentifiableEntity#protectedTitleCache protectedTitleCache} is not set.
	 * 
	 * @return  the string which identifies this team
	 */
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}