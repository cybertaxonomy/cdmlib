/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.EntityCollectionSetterAdapter;
import eu.etaxonomy.cdm.model.EntityCollectionSetterAdapter.SetterAdapterException;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;

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
 * @since 08-Nov-2007 13:06:58
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Team", propOrder = {
	"protectedNomenclaturalTitleCache",
	"protectedCollectorTitleCache",
    "teamMembers",
    "hasMoreMembers"
})
@XmlRootElement(name = "Team")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.agent.AgentBase")
@Audited
@Configurable
public class Team extends TeamOrPersonBase<Team> {

    private static final long serialVersionUID = 97640416905934622L;
	public static final Logger logger = Logger.getLogger(Team.class);

    @XmlElement(name = "ProtectedNomenclaturalTitleCache")
	private boolean protectedNomenclaturalTitleCache = false;

    @XmlElement(name = "ProtectedCollectorTitleCache")
	private boolean protectedCollectorTitleCache = false;

	//An abbreviated name for the team (e. g. in case of nomenclatural authorteams).
    //A non abbreviated name for the team (e. g.
	//in case of some bibliographical references)
    @XmlElementWrapper(name = "TeamMembers", nillable = true)
    @XmlElement(name = "TeamMember")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OrderColumn(name="sortIndex")
    @ListIndexBase(value=0)  //not really needed as this is the default
	@ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@Match(MatchMode.MATCH)
	private List<Person> teamMembers;

    @XmlElement(name = "hasMoreMembers")
	private boolean hasMoreMembers;

// ********************************** FACTORY ***************************/

	/**
	 * Creates a new team instance without any concrete {@link Person members}.
	 */
	static public Team NewInstance(){
		return new Team();
	}

	/**
	 * Creates a new team instance with a bibliographic and nomenclatural title
	 * but without any {@link Person members}. The caches are set to protected.
	 */
	static public Team NewTitledInstance(String title, String nomTitle){
		Team result = new Team();
		if (isNotBlank(title)){
		    result.setTitleCache(title, true);
		}
		if (isNotBlank(nomTitle)){
		    result.setNomenclaturalTitleCache(nomTitle, true);
		}
		return result;
	}

	/**
     * Creates a new team instance with a bibliographic, nomenclatural and collector title
     * but without any {@link Person members}. The caches are set to protected if not blank.
     */
    static public Team NewTitledInstance(String title, String nomTitle, String collectorTitle){
        Team result = new Team();
        if (isNotBlank(title)){
            result.setTitleCache(title, true);
        }
        if (isNotBlank(nomTitle)){
            result.setNomenclaturalTitleCache(nomTitle, true);
        }
        if (isNotBlank(collectorTitle)){
            result.setCollectorTitleCache(collectorTitle, true);
        }
        return result;
    }

    public static Team NewInstance(Person... members) {
        Team team = new Team();
        for (Person member : members){
            team.addTeamMember(member);
        }
        return team;
    }

//************************ CONSTRUCTOR *******************************/

	/**
	 * Class constructor (including the cache strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy TeamDefaultCacheStrategy}).
	 */
	public Team() {
		addListenersToMembers();
	}

// ******************************************************************/

    @Override
    protected void initDefaultCacheStrategy() {
        this.cacheStrategy = TeamDefaultCacheStrategy.NewInstance();
    }

    @Override
    public void initListener(){
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if (!ev.getPropertyName().equals("titleCache") &&
                        !ev.getPropertyName().equals("collectorTitleCache") &&
                        !ev.getPropertyName().equals("nomenclaturalTitleCache") &&
                        !ev.getPropertyName().equals("cacheStrategy")){
                    resetCaches();
                }
            }
        };
        addPropertyChangeListener(listener);
    }

	/**
	 * Adds a property change listener to all team members.
	 */
	private void addListenersToMembers() {
		List<Person> members = getTeamMembers();
		for (Person member : members){
			addListenerForTeamMember(member);
		}
	}

	private void addListenerForTeamMember(Person member) {
		PropertyChangeListener listener = new PropertyChangeListener() {
			@Override
            public void propertyChange(PropertyChangeEvent e) {
				resetCaches();
			}
		};
		member.addPropertyChangeListener(listener);
	}

    public void resetCaches() {
        if(!protectedTitleCache){
            titleCache = null;
        }
        if (! protectedNomenclaturalTitleCache){
            nomenclaturalTitleCache = null;
        }
        if (!protectedCollectorTitleCache){
            collectorTitleCache = null;
        }
    }

	/**
	 * Returns the list of {@link Person members} belonging to <i>this</i> team.
	 * A person may be a member of several distinct teams.
	 */
	public List<Person> getTeamMembers(){
		if(teamMembers == null) {
			this.teamMembers = new ArrayList<>();
		}
		return this.teamMembers;
	}

	public void setTeamMembers(List<Person> teamMembers) throws SetterAdapterException {
	    new EntityCollectionSetterAdapter<Team, Person>(Team.class, Person.class, "teamMembers").setCollection(this, teamMembers);
    }

	/**
	 * Adds a new {@link Person person} to <i>this</i> team at the end of the members' list.
	 *
	 * @param  person  the person who should be added to the other team members
	 * @see     	   #getTeamMembers()
	 * @see 		   Person
	 */
	public void addTeamMember(Person person){
		if (person != null){
			getTeamMembers().add(person);
			firePropertyChange("teamMember", null, person);
			addListenerForTeamMember(person);
		}
	}

	/**
	 * Adds a new {@link Person person} to <i>this</i> team
	 * at the given index place of the members' list. If the person is already
	 * a member of the list he will be moved to the given index place.
	 * The index must be an integer (>=0). If the index is larger than
	 * the present number of members the person will be added at the end of the list.
	 *
	 * @param  person  the person who should be added to the other team members
	 * @param  index   the position at which the person should be placed within the members' list (starting with 0)
	 * @see     	   #getTeamMembers()
	 * @see 		   Person
	 */
	public void addTeamMember(Person person, int index){
		if (person != null){
			int oldIndex = getTeamMembers().indexOf(person);
			if (oldIndex != -1 ){
				getTeamMembers().remove(person);
			}
			if (index >= getTeamMembers().size()){
				index = getTeamMembers().size();
			}
			getTeamMembers().add(index, person);
			addListenerForTeamMember(person);
			firePropertyChange("teamMember", null, person);
		}
	}

	/**
	 * Removes one person from the list of members of <i>this</i> team.
	 *
	 * @param  person  the person who should be deleted from <i>this</i> team
	 * @see    #getTeamMembers()
	 */
	public void removeTeamMember(Person person){
		boolean wasMember = getTeamMembers().remove(person);
		if (wasMember){
			firePropertyChange("teamMember", person, null);
		}
	}

    public boolean replaceTeamMember(Person newObject, Person oldObject){
        return replaceInList(this.teamMembers, newObject, oldObject);
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
	public String getNomenclaturalTitleCache() {
		if (protectedNomenclaturalTitleCache == PROTECTED){
			return this.nomenclaturalTitleCache;
		}
		if (nomenclaturalTitleCache == null){
			this.nomenclaturalTitleCache = getCacheStrategy().getNomenclaturalTitleCache(this);
		}else{
			//as long as team members do not inform the team about changes the cache must be created new each time
		    nomenclaturalTitleCache = getCacheStrategy().getNomenclaturalTitleCache(this);
		}
		return nomenclaturalTitleCache;
	}

	@Override
	@Deprecated
	public void setNomenclaturalTitle(String nomenclaturalTitle) {
		this.setNomenclaturalTitleCache(nomenclaturalTitle, PROTECTED);
	}

	/**
	 * Assigns a {@link TeamOrPersonBase#nomenclaturalTitle nomenclatural identification} string to <i>this</i> team
	 * and a protection flag status to this string.
	 *
	 * @see  #getNomenclaturalTitleCache()
	 */
	@Override
    public void setNomenclaturalTitleCache(String nomenclaturalTitleCache, boolean protectedNomenclaturalTitleCache) {
		firePropertyChange("nomenclaturalTitleCache", this.nomenclaturalTitleCache, nomenclaturalTitleCache);
		this.nomenclaturalTitleCache = CdmUtils.Nb(nomenclaturalTitleCache);
		this.protectedNomenclaturalTitleCache = protectedNomenclaturalTitleCache;
	}

	@Override
	//@Transient //TODO a.kohlbecker remove??
	public String getTitleCache() {
		String result = "";
		if (isProtectedTitleCache()){
			result = this.titleCache;
		}else{
			result = generateTitle();
			result = replaceEmptyTitleByNomTitle(result);
			result = getTruncatedCache(result);
			this.titleCache = result;
		}
		return result;
	}

    //#4311
    @Override
    public String getCollectorTitleCache() {
        if (protectedCollectorTitleCache == PROTECTED){
            return this.collectorTitleCache;
        }
        if (collectorTitleCache == null){
            this.collectorTitleCache = getCacheStrategy().getCollectorTitleCache(this);
        }else{
            //as long as team members do not inform the team about changes the cache must be created new each time
            collectorTitleCache = getCacheStrategy().getCollectorTitleCache(this);
        }
        return collectorTitleCache;
    }

	/**
	 * Protected nomenclatural title cache flag should be set to true, if
	 * the title cache is to be preferred against the atomized data.
	 * This may be the case if no atomized data exists or if atomization
	 * was incomplete for whatever reason.
	 * @return
	 */
	public boolean isProtectedNomenclaturalTitleCache() {
		return protectedNomenclaturalTitleCache;
	}
	public void setProtectedNomenclaturalTitleCache(boolean protectedNomenclaturalTitleCache) {
		this.protectedNomenclaturalTitleCache = protectedNomenclaturalTitleCache;
	}


	public boolean isProtectedCollectorTitleCache() {
        return protectedCollectorTitleCache;
    }
    public void setProtectedCollectorTitleCache(boolean protectedCollectorTitleCache) {
        this.protectedCollectorTitleCache = protectedCollectorTitleCache;
    }

    public void setCollectorTitleCache(String collectorTitleCache, boolean protectedCache) {
        this.collectorTitleCache = CdmUtils.Nb(collectorTitleCache);
        this.protectedCollectorTitleCache = protectedCache;
    }

    /**
	 * The hasMoreMembers flag is true if this team has more members than
	 * mentioned in the {@link #teamMembers} list. This is usually the case
	 * when "et al." is used in the team representation. Formatters should add
	 * "et al." or an according post fix to the team string representation
	 *  if this flag is set.
	 * @return
	 */
	public boolean isHasMoreMembers() {
		return hasMoreMembers;
	}
	public void setHasMoreMembers(boolean hasMoreMembers) {
		this.hasMoreMembers = hasMoreMembers;
	}

    @Override
    public boolean updateCaches(){
        boolean result = super.updateCaches();
        if (this.protectedNomenclaturalTitleCache == false){
            String oldNomTitleCache = this.nomenclaturalTitleCache;

            String newNomTitleCache = getCacheStrategy().getNomenclaturalTitleCache(this);

            if ( oldNomTitleCache == null   || ! oldNomTitleCache.equals(newNomTitleCache) ){
                this.setNomenclaturalTitleCache(null, false);
                String newCache = this.getNomenclaturalTitleCache();

                if (newCache == null){
                    logger.warn("New nomTitleCache should never be null");
                }
                if (oldNomTitleCache == null){
                    logger.info("Old nomTitleCache should never be null");
                }
                result = true;
            }
        }
        if (this.protectedCollectorTitleCache == false){
            String oldCollTitleCache = this.collectorTitleCache;
            String newCollTitleCache = getCacheStrategy().getCollectorTitleCache(this);

            if ( oldCollTitleCache == null || ! oldCollTitleCache.equals(newCollTitleCache) ){
                 this.setCollectorTitleCache(null, false);
                 String newCache = this.getCollectorTitleCache();

                 if (newCache == null){
                     logger.warn("New collectorTitleCache should never be null");
                 }
                 if (oldCollTitleCache == null){
                     logger.info("Old collectorTitleCache should never be null");
                 }
                 result = true;
             }
         }
        return result;
     }

//*********************** CLONE ********************************************************/

    /**
	 * Clones <i>this</i> Team. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> Team.
	 * The corresponding person is cloned.
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Team clone() {
		try{
			Team result = (Team)super.clone();
			result.teamMembers = new ArrayList<>();
			for (Person teamMember: this.teamMembers){
				result.addTeamMember(teamMember);
			}
			//no changes to protectedNomenclaturalTitleCache
			return result;
		} catch (CloneNotSupportedException e){
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}