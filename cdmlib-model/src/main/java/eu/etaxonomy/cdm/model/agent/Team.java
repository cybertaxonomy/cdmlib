/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents teams of {@link Person persons}. A team exists either for itself
 * or is built with the list of (distinct) persons who belong to it.
 * In the first case the inherited attribute {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} is to be used.
 * In the second case at least all abbreviated names
 * (the inherited attributes {@link TeamOrPersonBase#getNomenclaturalTitle() nomenclaturalTitle})
 * or all full names (the strings returned by Person.generateTitle)
 * of the persons must exist. A team is a {@link java.util.List list} of persons.
 * <P>
 * This class corresponds to: <ul>
 * <li> Team according to the TDWG ontology
 * <li> AgentNames (partially) according to the TCS
 * <li> MicroAgent (partially) according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:58
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Team", propOrder = {
	"protectedNomenclaturalTitleCache",
    "teamMembers"
})
@Entity
public class Team extends TeamOrPersonBase {
	
	static Logger logger = Logger.getLogger(Team.class);
	
    @XmlElement(name = "ProtectedNomenclaturalTitleCache")
	private boolean protectedNomenclaturalTitleCache;

	//An abreviated name for the team (e. g. in case of nomenclatural authorteams). A non abreviated name for the team (e. g.
	//in case of some bibliographical references)
    @XmlElementWrapper(name = "TeamMembers")
    @XmlElement(name = "Member")
    //@XmlIDREF
	private List<Person> teamMembers = new ArrayList<Person>();
	
	
	/** 
	 * Creates a new team instance without any concrete {@link Person members}.
	 */
	static public Team NewInstance(){
		return new Team();
	}
	
	/** 
	 * Class constructor (including the cache strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy TeamDefaultCacheStrategy}).
	 */
	public Team() {
		super();
		this.cacheStrategy = TeamDefaultCacheStrategy.NewInstance();
	}

	/** 
	 * Returns the list of {@link Person members} belonging to <i>this</i> team. 
	 * A person may be a member of several distinct teams. 
	 */
	@ManyToMany
	public List<Person> getTeamMembers(){
		return this.teamMembers;
	}
	/** 
	 * @see     #getTeamMembers()
	 */
	protected void setTeamMembers(List<Person> teamMembers){
		this.teamMembers = teamMembers;
	}
	
	/** 
	 * Adds a new {@link Person person} to <i>this</i> team at the end of the members' list. 
	 *
	 * @param  person  the person who should be added to the other team members
	 * @see     	   #getTeamMembers()
	 * @see 		   Person
	 */
	public void addTeamMember(Person person){
		this.teamMembers.add(person);
	}
	
	/** 
	 * Adds a new {@link Person person} to <i>this</i> team
	 * at the given index place of the members' list. If the person is already
	 * a member of the list he will be moved to the given index place. 
	 * The index must be a positive integer. If the index is bigger than
	 * the present number of members the person will be added at the end of the list.
	 *
	 * @param  person  the person who should be added to the other team members
	 * @param  index   the position at which the new person should be placed within the members' list
	 * @see     	   #getTeamMembers()
	 * @see 		   Person
	 */
	public void addTeamMember(Person person, int index){
		// TODO is still not fully implemented (range for index!)
		logger.warn("not yet fully implemented (range for index!)");
		int oldIndex = teamMembers.indexOf(person);
		if (oldIndex != -1 ){
			teamMembers.remove(person);
		}
		this.teamMembers.add(index, person);
	}
	
	/** 
	 * Removes one person from the list of members of <i>this</i> team.
	 *
	 * @param  person  the person who should be deleted from <i>this</i> team
	 * @see            #getTeamMembers()
	 */
	public void removeTeamMember(Person person){
		this.teamMembers.remove(person);
	}

	/**
	 * Generates an identification string for <i>this</i> team according to the strategy
	 * defined in {@link eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy TeamDefaultCacheStrategy}. This string is built
	 * with the full names of all persons belonging to its (ordered) members' list.
	 * This method overrides {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle() generateTitle}.
	 * The result might be kept as {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#setTitleCache(String) titleCache} if the
	 * flag {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#protectedTitleCache protectedTitleCache} is not set.
	 * 
	 * @return  a string which identifies <i>this</i> team
	 */
	@Override
	public String generateTitle() {
		return cacheStrategy.getTitleCache(this);
	}
	
	
	/**
	 * Generates or returns the {@link TeamOrPersonBase#getnomenclaturalTitle() nomenclatural identification} string for <i>this</i> team.
	 * This method overrides {@link TeamOrPersonBase#getNomenclaturalTitle() getNomenclaturalTitle}.
	 * This string is built with the {@link TeamOrPersonBase#getNomenclaturalTitle() abbreviated names}
	 * of all persons belonging to its (ordered) members' list if the flag
	 * {@link #protectedNomenclaturalTitleCache protectedNomenclaturalTitleCache} is not set.
	 * Otherwise this method returns the present nomenclatural abbreviation.
	 * In case the string is generated the cache strategy used is defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy TeamDefaultCacheStrategy}.
	 * The result might be kept as nomenclatural abbreviation
	 * by using the {@link #setNomenclaturalTitle(String) setNomenclaturalTitle} method.
	 * 
	 * @return  a string which identifies <i>this</i> team for nomenclature
	 */
	@Override
	@Transient
	public String getNomenclaturalTitle() {
		if (protectedNomenclaturalTitleCache == PROTECTED){
			return this.nomenclaturalTitle;
		}
		if (nomenclaturalTitle == null){
			this.nomenclaturalTitle = cacheStrategy.getNomenclaturalTitle(this);
		}
		return nomenclaturalTitle;	
	}
	
	/**
	 * Assigns a {@link TeamOrPersonBase#nomenclaturalTitle nomenclatural identification} string to <i>this</i> team
	 * and protects it from overwriting.
	 * This method overrides {@link TeamOrPersonBase#setNomenclaturalTitle(String) setNomenclaturalTitle}.
	 * 
	 * @see  #getNomenclaturalTitle()
	 * @see  #setNomenclaturalTitle(String, boolean)
	 */
	@Override
	public void setNomenclaturalTitle(String nomenclaturalTitle) {
		setNomenclaturalTitle(nomenclaturalTitle, PROTECTED);
		this.nomenclaturalTitle = nomenclaturalTitle;
	}

	/**
	 * Assigns a {@link TeamOrPersonBase#nomenclaturalTitle nomenclatural identification} string to <i>this</i> team
	 * and a protection flag status to this string.
	 * 
	 * @see  #getNomenclaturalTitle()
	 */
	public void setNomenclaturalTitle(String nomenclaturalTitle, boolean protectedNomenclaturalTitleCache) {
		this.nomenclaturalTitle = nomenclaturalTitle;
		this.protectedNomenclaturalTitleCache = protectedNomenclaturalTitleCache;
	}

	
	
	

}