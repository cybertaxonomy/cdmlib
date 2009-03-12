/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Target;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy;

/**
 * The taxon name class for all non viral taxa. Parenthetical authorship is derived
 * from basionym relationship. The scientific name including author strings and
 * maybe year can be stored as a string in the inherited {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.
 * The year itself is an information obtained from the {@link eu.etaxonomy.cdm.model.reference.ReferenceBase#getYear() nomenclatural reference}.
 * The scientific name string without author strings and year can be stored in the {@link #getNameCache() nameCache} attribute.
 * <P>
 * This class corresponds partially to: <ul>
 * <li> TaxonName according to the TDWG ontology
 * <li> ScientificName and CanonicalName according to the TCS
 * <li> ScientificName according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:39
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonViralName", propOrder = {
    "nameCache",
    "genusOrUninomial",
    "infraGenericEpithet",
    "specificEpithet",
    "infraSpecificEpithet",
    "combinationAuthorTeam",
    "exCombinationAuthorTeam",
    "basionymAuthorTeam",
    "exBasionymAuthorTeam",
    "authorshipCache",
    "protectedAuthorshipCache",
    "protectedNameCache"
})
@XmlRootElement(name = "NonViralName")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.name.TaxonNameBase")
@Audited
public class NonViralName<T extends NonViralName> extends TaxonNameBase<T, INonViralNameCacheStrategy> {
	
	private static final Logger logger = Logger.getLogger(NonViralName.class);
	
	@XmlElement(name = "NameCache")
	@Fields({@Field(index = org.hibernate.search.annotations.Index.TOKENIZED),
    	 @Field(name = "name_forSort", index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
    })
	private String nameCache;
	
	@XmlElement(name = "GenusOrUninomial")
	@Field(index=Index.TOKENIZED)
	private String genusOrUninomial;
	
	@XmlElement(name = "InfraGenericEpithet")
	@Field(index=Index.TOKENIZED)
	private String infraGenericEpithet;
	
	@XmlElement(name = "SpecificEpithet")
	@Field(index=Index.TOKENIZED)
	private String specificEpithet;
	
	@XmlElement(name = "InfraSpecificEpithet")
	@Field(index=Index.TOKENIZED)
	private String infraSpecificEpithet;
	
	@XmlElement(name = "CombinationAuthorTeam", type = TeamOrPersonBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Target(TeamOrPersonBase.class)
	@Cascade(CascadeType.SAVE_UPDATE)
	private INomenclaturalAuthor combinationAuthorTeam;
	
	@XmlElement(name = "ExCombinationAuthorTeam", type = TeamOrPersonBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Target(TeamOrPersonBase.class)
	@Cascade(CascadeType.SAVE_UPDATE)
	private INomenclaturalAuthor exCombinationAuthorTeam;
	
	@XmlElement(name = "BasionymAuthorTeam", type = TeamOrPersonBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Target(TeamOrPersonBase.class)
	@Cascade(CascadeType.SAVE_UPDATE)
	private INomenclaturalAuthor basionymAuthorTeam;
	
	@XmlElement(name = "ExBasionymAuthorTeam", type = TeamOrPersonBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Target(TeamOrPersonBase.class)
	@Cascade(CascadeType.SAVE_UPDATE)
	private INomenclaturalAuthor exBasionymAuthorTeam;
	
	@XmlElement(name = "AuthorshipCache")
	@Field(index=Index.TOKENIZED)
	private String authorshipCache;
	
	@XmlElement(name = "ProtectedAuthorshipCache")
	protected boolean protectedAuthorshipCache;
	
	@XmlElement(name = "ProtectedNameCache")
	protected boolean protectedNameCache;

    @XmlTransient
    @Transient
	protected INonViralNameCacheStrategy cacheStrategy;
	
	// ************* CONSTRUCTORS *************/	
	
	//needed by hibernate
	/** 
	 * Class constructor: creates a new non viral taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #NonViralName(Rank, HomotypicalGroup)
	 * @see #NonViralName(Rank, String, String, String, String, TeamOrPersonBase, ReferenceBase, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected NonViralName(){
		super();
		setNameCacheStrategy();
	}
	
	/** 
	 * Class constructor: creates a new non viral taxon name instance
	 * only containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * The new non viral taxon name instance will be also added to the set of
	 * non viral taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to <i>this</i> non viral taxon name
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> non viral taxon name belongs
	 * @see 	#NonViralName()
	 * @see		#NonViralName(Rank, String, String, String, String, TeamOrPersonBase, ReferenceBase, String, HomotypicalGroup)
	 * @see		#NewInstance(Rank, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected NonViralName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		setNameCacheStrategy();
	}
	/** 
	 * Class constructor: creates a new non viral taxon name instance
	 * containing its {@link Rank rank},
	 * its {@link HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author(team)},
	 * its {@link eu.etaxonomy.cdm.model.reference.ReferenceBase nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * The new non viral taxon name instance will be also added to the set of
	 * non viral taxon names belonging to this homotypical group.
	 * 
	 * @param	rank  the rank to be assigned to <i>this</i> non viral taxon name
	 * @param	genusOrUninomial the string for <i>this</i> non viral taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			<i>this</i> non viral taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			<i>this</i> non viral taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			<i>this</i> non viral taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published <i>this</i> non viral taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where <i>this</i> non viral taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which <i>this</i> non viral taxon name belongs
	 * @see 	#NonViralName()
	 * @see		#NonViralName(Rank, HomotypicalGroup)
	 * @see		#NewInstance(Rank, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected NonViralName(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		setNameCacheStrategy();
		setGenusOrUninomial(genusOrUninomial);
		setInfraGenericEpithet (infraGenericEpithet);
		setSpecificEpithet(specificEpithet);
		setInfraSpecificEpithet(infraSpecificEpithet);
		setCombinationAuthorTeam(combinationAuthorTeam);
		setNomenclaturalReference(nomenclaturalReference);
		this.setNomenclaturalMicroReference(nomenclMicroRef);
	}
	
	//********* METHODS **************************************/
	/** 
	 * Creates a new non viral taxon name instance
	 * only containing its {@link common.Rank rank} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param  rank  the rank to be assigned to <i>this</i> non viral taxon name
	 * @see    #NewInstance(Rank, HomotypicalGroup)
	 * @see    #NonViralName(Rank, HomotypicalGroup)
	 * @see    #NonViralName()
	 * @see    #NonViralName(Rank, String, String, String, String, TeamOrPersonBase, ReferenceBase, String, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	public static NonViralName NewInstance(Rank rank){
		return new NonViralName(rank, null);
	}

	/** 
	 * Creates a new non viral taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * The new non viral taxon name instance will be also added to the set of
	 * non viral taxon names belonging to this homotypical group.
	 * 
	 * @param  rank  the rank to be assigned to <i>this</i> non viral taxon name
	 * @param  homotypicalGroup  the homotypical group to which <i>this</i> non viral taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NonViralName(Rank, HomotypicalGroup)
	 * @see    #NonViralName()
	 * @see    #NonViralName(Rank, String, String, String, String, TeamOrPersonBase, ReferenceBase, String, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	public static NonViralName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new NonViralName(rank, homotypicalGroup);
	}
	
	private void setNameCacheStrategy(){
		if (getClass() == NonViralName.class){
			this.cacheStrategy = NonViralNameDefaultCacheStrategy.NewInstance();
		}
		
	}
	
	//TODO for PROTOTYPE
	/**
	 * Returns the {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy cache strategy} used to generate
	 * several strings corresponding to <i>this</i> non viral taxon name
	 * (in particular taxon name caches and author strings).
	 * 
	 * @return  the cache strategy used for <i>this</i> non viral taxon name
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see     eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	@Override
	public INonViralNameCacheStrategy getCacheStrategy() {
		return cacheStrategy;
	}
	
	/**
	 * @see  #getCacheStrategy()
	 */
	@Override
	public void setCacheStrategy(INonViralNameCacheStrategy cacheStrategy) {
		this.cacheStrategy = cacheStrategy;
	}

	/**
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that published <i>this</i> non viral
	 * taxon name.
	 * 
	 * @return  the nomenclatural author (team) of <i>this</i> non viral taxon name
	 * @see 	eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
	 * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
	 */
	public INomenclaturalAuthor getCombinationAuthorTeam(){
		return this.combinationAuthorTeam;
	}
	
	/**
	 * @see  #getCombinationAuthorTeam()
	 */
	public void setCombinationAuthorTeam(INomenclaturalAuthor combinationAuthorTeam){
		this.combinationAuthorTeam = combinationAuthorTeam;
	}

	/**
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that contributed to
	 * the publication of <i>this</i> non viral taxon name as generally stated by
	 * the {@link #getCombinationAuthorTeam() combination author (team)} itself.<BR>
	 * An ex-author(-team) is an author(-team) to whom a taxon name was ascribed
	 * although it is not the author(-team) of a valid publication (for instance
	 * without the validating description or diagnosis in case of a name for a
	 * new taxon). The name of this ascribed authorship, followed by "ex", may
	 * be inserted before the name(s) of the publishing author(s) of the validly
	 * published name:<BR>
	 * <i>Lilium tianschanicum</i> was described by Grubov (1977) as a new species and
	 * its name was ascribed to Ivanova; since there is no indication that
	 * Ivanova provided the validating description, the name may be cited as
	 * <i>Lilium tianschanicum</i> N. A. Ivanova ex Grubov or <i>Lilium tianschanicum</i> Grubov.
	 * <P>
	 * The presence of an author (team) of <i>this</i> non viral taxon name is a
	 * condition for the existence of an ex author (team) for <i>this</i> same name. 
	 * 
	 * @return  the nomenclatural ex author (team) of <i>this</i> non viral taxon name
	 * @see 	#getCombinationAuthorTeam()
	 * @see 	eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
	 * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
	 */
	public INomenclaturalAuthor getExCombinationAuthorTeam(){
		return this.exCombinationAuthorTeam;
	}
	
	/**
	 * @see  #getExCombinationAuthorTeam()
	 */
	public void setExCombinationAuthorTeam(INomenclaturalAuthor exCombinationAuthorTeam){
		this.exCombinationAuthorTeam = exCombinationAuthorTeam;
	}

	/**
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that published the original combination
	 * on which <i>this</i> non viral taxon name is nomenclaturally based. Such an
	 * author (team) can only exist if <i>this</i> non viral taxon name is a new
	 * combination due to a taxonomical revision.
	 * 
	 * @return  the nomenclatural basionym author (team) of <i>this</i> non viral taxon name
	 * @see 	#getCombinationAuthorTeam()
	 * @see 	eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
	 * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
	 */
	public INomenclaturalAuthor getBasionymAuthorTeam(){
		return basionymAuthorTeam;
	}
	
	/**
	 * @see  #getBasionymAuthorTeam()
	 */
	public void setBasionymAuthorTeam(INomenclaturalAuthor basionymAuthorTeam) {
		this.basionymAuthorTeam = basionymAuthorTeam;
	}

	/**
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor author (team)} that contributed to
	 * the publication of the original combination <i>this</i> non viral taxon name is
	 * based on. This should have been generally stated by
	 * the {@link #getBasionymAuthorTeam() basionym author (team)} itself.
	 * The presence of a basionym author (team) of <i>this</i> non viral taxon name is a
	 * condition for the existence of an ex basionym author (team)
	 * for <i>this</i> same name. 
	 * 
	 * @return  the nomenclatural ex basionym author (team) of <i>this</i> non viral taxon name
	 * @see 	#getBasionymAuthorTeam()
	 * @see 	#getExCombinationAuthorTeam()
	 * @see 	#getCombinationAuthorTeam()
	 * @see 	eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
	 * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
	 */
	public INomenclaturalAuthor getExBasionymAuthorTeam(){
		return exBasionymAuthorTeam;
	}
	
	/**
	 * @see  #getExBasionymAuthorTeam()
	 */
	public void setExBasionymAuthorTeam(INomenclaturalAuthor exBasionymAuthorTeam) {
		this.exBasionymAuthorTeam = exBasionymAuthorTeam;
	}
	/**
	 * Returns either the scientific name string (without authorship) for <i>this</i>
	 * non viral taxon name if its rank is genus or higher (monomial) or the string for
	 * the genus part of it if its {@link Rank rank} is lower than genus (bi- or trinomial).
	 * Genus or uninomial strings begin with an upper case letter.
	 * 
	 * @return  the string containing the suprageneric name, the genus name or the genus part of <i>this</i> non viral taxon name
	 * @see 	#getNameCache()
	 */
	public String getGenusOrUninomial() {
		return genusOrUninomial;
	}
	
	/**
	 * @see  #getGenusOrUninomial()
	 */
	public void setGenusOrUninomial(String genusOrUninomial) {
		this.genusOrUninomial = genusOrUninomial;
	}

	/**
	 * Returns the genus subdivision epithet string (infrageneric part) for
	 * <i>this</i> non viral taxon name if its {@link Rank rank} is infrageneric (lower than genus and
	 * higher than species aggregate: binomial). Genus subdivision epithet
	 * strings begin with an upper case letter.
	 * 
	 * @return  the string containing the infrageneric part of <i>this</i> non viral taxon name
	 * @see 	#getNameCache()
	 */
	public String getInfraGenericEpithet(){
		return this.infraGenericEpithet;
	}

	/**
	 * @see  #getInfraGenericEpithet()
	 */
	public void setInfraGenericEpithet(String infraGenericEpithet){
		this.infraGenericEpithet = infraGenericEpithet;
	}

	/**
	 * Returns the species epithet string for <i>this</i> non viral taxon name if its {@link Rank rank} is
	 * species aggregate or lower (bi- or trinomial). Species epithet strings
	 * begin with a lower case letter.
	 * 
	 * @return  the string containing the species epithet of <i>this</i> non viral taxon name
	 * @see 	#getNameCache()
	 */
	public String getSpecificEpithet(){
		return this.specificEpithet;
	}

	/**
	 * @see  #getSpecificEpithet()
	 */
	public void setSpecificEpithet(String specificEpithet){
		this.specificEpithet = specificEpithet;
	}

	/**
	 * Returns the species subdivision epithet string (infraspecific part) for
	 * <i>this</i> non viral taxon name if its {@link Rank rank} is infraspecific
	 * (lower than species: trinomial). Species subdivision epithet strings
	 * begin with a lower case letter.
	 * 
	 * @return  the string containing the infraspecific part of <i>this</i> non viral taxon name
	 * @see 	#getNameCache()
	 */
	public String getInfraSpecificEpithet(){
		return this.infraSpecificEpithet;
	}

	/**
	 * @see  #getInfraSpecificEpithet()
	 */
	public void setInfraSpecificEpithet(String infraSpecificEpithet){
		this.infraSpecificEpithet = infraSpecificEpithet;
	}

	/**
	 * Generates and returns the string with the scientific name of <i>this</i>
	 * non viral taxon name including author strings and maybe year according to
	 * the strategy defined in
	 *  {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy INonViralNameCacheStrategy}.
	 * This string may be stored in the inherited
	 * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.
	 * This method overrides the generic and inherited
	 * TaxonNameBase#generateTitle() method.
	 *
	 * @return  the string with the composed name of <i>this</i> non viral taxon name with authorship (and maybe year)
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 * @see  	TaxonNameBase#generateTitle()
	 */
	@Override
	public String generateTitle(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
			return null;
		}else{
			return cacheStrategy.getTitleCache(this);
		}
	}
	
	@Override
	public String generateFullTitle(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
			return null;
		}else{
			return cacheStrategy.getFullTitleCache(this);
		}
	}
	
	/**
	 * Generates the composed name string of <i>this</i> non viral taxon name without author
	 * strings or year according to the strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy INonViralNameCacheStrategy}.
	 * The result might be stored in {@link #getNameCache() nameCache} if the
	 * flag {@link #isProtectedNameCache() protectedNameCache} is not set.
	 * 
	 * @return  the string with the composed name of <i>this</i> non viral taxon name without authors or year
	 * @see 	#getNameCache()
	 */
	protected String generateNameCache(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for taxonName: " + this.toString());
			return null;
		}else{
			return cacheStrategy.getNameCache(this);
		}
	}
	
	/**
	 * Returns or generates the nameCache (scientific name
	 * without author strings and year) string for <i>this</i> non viral taxon name. If the
	 * {@link #isProtectedNameCache() protectedNameCache} flag is not set (False)
	 * the string will be generated according to a defined strategy,
	 * otherwise the value of the actual nameCache string will be returned.
	 * 
	 * @return  the string which identifies <i>this</i> non viral taxon name (without authors or year)
	 * @see 	#generateNameCache()
	 */
	public String getNameCache() {
		if (protectedNameCache){
			return this.nameCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (nameCache == null){
			this.nameCache = generateNameCache();
		}
		return nameCache;
	}

	/**
	 * Assigns a nameCache string to <i>this</i> non viral taxon name and protects it from being overwritten.
	 * Sets the protectedNameCache flag to <code>true</code>.
	 *  
	 * @param  nameCache  the string which identifies <i>this</i> non viral taxon name (without authors or year)
	 * @see	   #getNameCache()
	 */
	public void setNameCache(String nameCache){
		setNameCache(nameCache, true);
	}
	
	/**
	 * Assigns a nameCache string to <i>this</i> non viral taxon name and protects it from being overwritten.
	 * Sets the protectedNameCache flag to <code>true</code>.
	 *  
	 * @param  nameCache  the string which identifies <i>this</i> non viral taxon name (without authors or year)
	 * @param  protectedNameCache if true teh protectedNameCache is set to <code>true</code> or otherwise set to
	 * <code>false</code>
	 * @see	   #getNameCache()
	 */
	public void setNameCache(String nameCache, boolean protectedNameCache){
		this.nameCache = nameCache;
		this.setProtectedTitleCache(false);
		this.setProtectedNameCache(protectedNameCache);
	}
	
	/**
	 * Returns the boolean value of the flag intended to protect (true)
	 * or not (false) the {@link #getNameCache() nameCache} (scientific name without author strings and year)
	 * string of <i>this</i> non viral taxon name.
	 *  
	 * @return  the boolean value of the protectedNameCache flag
	 * @see     #getNameCache()
	 */
	public boolean isProtectedNameCache() {
		return protectedNameCache;
	}

	/** 
	 * @see     #isProtectedNameCache()
	 */
	public void setProtectedNameCache(boolean protectedNameCache) {
		this.protectedNameCache = protectedNameCache;
	}

	
	/**
	 * Generates and returns a concatenated and formated authorteams string
	 * including basionym and combination authors of <i>this</i> non viral taxon name
	 * according to the strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(NonViralName) INonViralNameCacheStrategy}.
	 * 
	 * @return  the string with the concatenated and formated authorteams for <i>this</i> non viral taxon name
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(NonViralName)
	 */
	public String generateAuthorship(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
			return null;
		}else{
			return ((INonViralNameCacheStrategy<T>)cacheStrategy).getAuthorshipCache((T)this);
		}
	}

	/**
	 * Returns the concatenated and formated authorteams string including
	 * basionym and combination authors of <i>this</i> non viral taxon name.
	 * If the protectedAuthorshipCache flag is set this method returns the
	 * string stored in the the authorshipCache attribute, otherwise it
	 * generates the complete authorship string, returns it and stores it in
	 * the authorshipCache attribute.
	 * 
	 * @return  the string with the concatenated and formated authorteams for <i>this</i> non viral taxon name
	 * @see 	#generateAuthorship()
	 */
	public String getAuthorshipCache() {
		if (protectedAuthorshipCache){
			return this.authorshipCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (authorshipCache == null){
			this.authorshipCache = generateAuthorship();
		}else{
			//TODO get is Dirty of authors
			this.authorshipCache = generateAuthorship();
		}
		return authorshipCache;
	}

	/**
	 * Assigns an authorshipCache string to <i>this</i> non viral taxon name. Sets the isProtectedAuthorshipCache
	 * flag to <code>true</code>.
	 *  
	 * @param  authorshipCache  the string which identifies the complete authorship of <i>this</i> non viral taxon name
	 * @see	   #getAuthorshipCache()
	 */
	public void setAuthorshipCache(String authorshipCache) {
		setAuthorshipCache(authorshipCache, true);
	}
	
	/**
	 * Assigns an authorshipCache string to <i>this</i> non viral taxon name.
	 *  
	 * @param  authorshipCache  the string which identifies the complete authorship of <i>this</i> non viral taxon name
	 * @param  protectedAuthorshipCache if true the isProtectedAuthorshipCache flag is set to <code>true</code>, otherwise 
	 * the flag is set to <code>false</code>.
	 * @see	   #getAuthorshipCache()
	 */
	public void setAuthorshipCache(String authorshipCache, boolean protectedAuthorshipCache) {
		this.authorshipCache = authorshipCache;
		//TODO hibernate safe?
		if (! this.isProtectedFullTitleCache()){
			this.setFullTitleCache(null, false);
		}
		this.setProtectedAuthorshipCache(protectedAuthorshipCache);
	}

	
	
	/**
	 * Returns the boolean value "false" since the components of <i>this</i> taxon name
	 * cannot follow the rules of a corresponding {@link NomenclaturalCode nomenclatural code}
	 * which is not defined for this class. The nomenclature code depends on
	 * the concrete name subclass ({@link BacterialName BacterialName},
	 * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} or
	 * {@link ZoologicalName ZoologicalName} to which <i>this</i> non viral taxon name belongs.
	 * This method overrides the isCodeCompliant method from the abstract
	 * {@link TaxonNameBase#isCodeCompliant() TaxonNameBase} class.
	 *  
	 * @return  false
	 * @see	   	TaxonNameBase#isCodeCompliant()
	 */
	@Override
	public boolean isCodeCompliant() {
		//FIXME
		logger.warn("is CodeCompliant not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomeclaturalCode()
	 */
	/**
	 * Returns null as {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of <i>this</i> non viral taxon name since there is no specific
	 * nomenclatural code defined. The real implementention takes place in the
	 * subclasses {@link BacterialName BacterialName},
	 * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} and
	 * {@link ZoologicalName ZoologicalName}.
	 * This method overrides the getNomeclaturalCode method from {@link TaxonNameBase TaxonNameBase}.
	 *
	 * @return  null
	 * @see  	#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Override
	public NomenclaturalCode getNomenclaturalCode() {
		logger.warn("Non Viral Name has no specific Code defined. Use subclasses");
		return null;
	}

	/**
	 * Returns the boolean value of the flag intended to protect (true)
	 * or not (false) the {@link #getAuthorshipCache() authorshipCache} (complete authorship string)
	 * of <i>this</i> non viral taxon name.
	 *  
	 * @return  the boolean value of the protectedAuthorshipCache flag
	 * @see     #getAuthorshipCache()
	 */
	public boolean isProtectedAuthorshipCache() {
		return protectedAuthorshipCache;
	}

	/** 
	 * @see     #isProtectedAuthorshipCache()
	 * @see     #getAuthorshipCache()
	 */
	public void setProtectedAuthorshipCache(boolean protectedAuthorshipCache) {
		this.protectedAuthorshipCache = protectedAuthorshipCache;
		if (protectedAuthorshipCache == false){
			if (! this.isProtectedFullTitleCache()){
				this.setFullTitleCache(null, false);
			}
		}
	}

	
	

}
