/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.List;
import java.util.Set;

import javax.persistence.Transient;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * @author a.mueller
 * @date 21.01.2017
 *
 */
public interface INonViralName extends ITaxonNameBase{

    /**
     * Creates a new {@link HybridRelationship#HybridRelationship(TaxonNameBase, TaxonNameBase, HybridRelationshipType, String) hybrid relationship}
     * to <i>this</i> non viral name. A HybridRelationship may be of type
     * "is first/second parent" or "is male/female parent". By invoking this
     * method <i>this</i> non viral name becomes a parent of the hybrid child
     * non viral name.
     *
     * @param childName       the non viral name of the child for this new hybrid name relationship
     * @param type            the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see                   #addHybridParent(TaxonNameBase, HybridRelationshipType,String )
     * @see                   #getRelationsToThisName()
     * @see                   #getNameRelations()
     * @see                   #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     * @see                   #addNameRelationship(NameRelationship)
     */
    public HybridRelationship addHybridChild(INonViralName childName, HybridRelationshipType type, String ruleConsidered);

    /**
     * Creates a new {@link HybridRelationship#HybridRelationship(TaxonNameBase, TaxonNameBase, HybridRelationshipType, String) hybrid relationship}
     * to <i>this</i> non viral name. A HybridRelationship may be of type
     * "is first/second parent" or "is male/female parent". By invoking this
     * method <i>this</i> non viral name becomes a hybrid child of the parent
     * non viral name.
     *
     * @param parentName      the non viral name of the parent for this new hybrid name relationship
     * @param type            the type of this new name relationship
     * @param ruleConsidered  the string which specifies the rule on which this name relationship is based
     * @return
     * @see                   #addHybridChild(TaxonNameBase, HybridRelationshipType,String )
     * @see                   #getRelationsToThisName()
     * @see                   #getNameRelations()
     * @see                   #addRelationshipFromName(TaxonNameBase, NameRelationshipType, String)
     * @see                   #addNameRelationship(NameRelationship)
     */
    public HybridRelationship addHybridParent(INonViralName parentName, HybridRelationshipType type, String ruleConsidered);

    /**
     * Shortcut. Returns the basionym authors title cache. Returns null if no basionym author exists.
     * @return
     */
    public String computeBasionymAuthorNomenclaturalTitle() ;

    /**
     * Shortcut. Returns the combination authors title cache. Returns null if no combination author exists.
     * @return
     */
    public String computeCombinationAuthorNomenclaturalTitle();

    /**
     * Shortcut. Returns the ex-basionym authors title cache. Returns null if no exbasionym author exists.
     * @return
     */
    public String computeExBasionymAuthorNomenclaturalTitle();

    /**
     * Shortcut. Returns the ex-combination authors title cache. Returns null if no ex-combination author exists.
     * @return
     */
    public String computeExCombinationAuthorNomenclaturalTitle();

    /**
     * Generates and returns a concatenated and formatted author teams string
     * including basionym and combination authors of <i>this</i> non viral taxon name
     * according to the strategy defined in
     * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(NonViralName) INonViralNameCacheStrategy}.
     *
     * @return  the string with the concatenated and formated author teams for <i>this</i> non viral taxon name
     * @see     eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(NonViralName)
     */
    public String generateAuthorship();


    /**
     * Returns the concatenated and formatted author teams string including
     * basionym and combination authors of <i>this</i> non viral taxon name.
     * If the protectedAuthorshipCache flag is set this method returns the
     * string stored in the the authorshipCache attribute, otherwise it
     * generates the complete authorship string, returns it and stores it in
     * the authorshipCache attribute.
     *
     * @return  the string with the concatenated and formated author teams for <i>this</i> non viral taxon name
     * @see     #generateAuthorship()
     */
    @Transient
    public String getAuthorshipCache();

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author (team)} that published the original combination
     * on which <i>this</i> non viral taxon name is nomenclaturally based. Such an
     * author (team) can only exist if <i>this</i> non viral taxon name is a new
     * combination due to a taxonomical revision.
     *
     * @return  the nomenclatural basionym author (team) of <i>this</i> non viral taxon name
     * @see     #getCombinationAuthorship()
     * @see     eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see     eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    public TeamOrPersonBase<?> getBasionymAuthorship();

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author (team)} that published <i>this</i> non viral
     * taxon name.
     *
     * @return  the nomenclatural author (team) of <i>this</i> non viral taxon name
     * @see     eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see     eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    public TeamOrPersonBase<?> getCombinationAuthorship();


    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author (team)} that contributed to
     * the publication of the original combination <i>this</i> non viral taxon name is
     * based on. This should have been generally stated by
     * the {@link #getBasionymAuthorship() basionym author (team)} itself.
     * The presence of a basionym author (team) of <i>this</i> non viral taxon name is a
     * condition for the existence of an ex basionym author (team)
     * for <i>this</i> same name.
     *
     * @return  the nomenclatural ex basionym author (team) of <i>this</i> non viral taxon name
     * @see     #getBasionymAuthorship()
     * @see     #getExCombinationAuthorship()
     * @see     #getCombinationAuthorship()
     * @see     eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see     eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    public TeamOrPersonBase<?> getExBasionymAuthorship();

    /**
     * Returns the {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author (team)} that contributed to
     * the publication of <i>this</i> non viral taxon name as generally stated by
     * the {@link #getCombinationAuthorship() combination author (team)} itself.<BR>
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
     * @see     #getCombinationAuthorship()
     * @see     eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor
     * @see     eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getNomenclaturalTitle()
     */
    public TeamOrPersonBase<?> getExCombinationAuthorship();

    /**
     * Returns either the scientific name string (without authorship) for <i>this</i>
     * non viral taxon name if its rank is genus or higher (monomial) or the string for
     * the genus part of it if its {@link Rank rank} is lower than genus (bi- or trinomial).
     * Genus or uninomial strings begin with an upper case letter.
     *
     * @return  the string containing the suprageneric name, the genus name or the genus part of <i>this</i> non viral taxon name
     * @see     #getNameCache()
     */
    public String getGenusOrUninomial();

    /**
     * Returns the set of all {@link HybridRelationship hybrid relationships}
     * in which <i>this</i> taxon name is involved as a {@link common.RelationshipBase#getRelatedTo() child}.
     *
     * @see    #getHybridRelationships()
     * @see    #getParentRelationships()
     * @see    HybridRelationshipType
     */
    public Set<HybridRelationship> getHybridChildRelations();

    /**
     * Returns the set of all {@link HybridRelationship hybrid relationships}
     * in which <i>this</i> taxon name is involved as a {@link common.RelationshipBase#getRelatedFrom() parent}.
     *
     * @see    #getHybridRelationships()
     * @see    #getChildRelationships()
     * @see    HybridRelationshipType
     */
    public Set<HybridRelationship> getHybridParentRelations();

    /**
     * Returns the genus subdivision epithet string (infrageneric part) for
     * <i>this</i> non viral taxon name if its {@link Rank rank} is infrageneric (lower than genus and
     * higher than species aggregate: binomial). Genus subdivision epithet
     * strings begin with an upper case letter.
     *
     * @return  the string containing the infrageneric part of <i>this</i> non viral taxon name
     * @see     #getNameCache()
     */
    public String getInfraGenericEpithet();

    /**
     * Returns the species epithet string for <i>this</i> non viral taxon name if its {@link Rank rank} is
     * species aggregate or lower (bi- or trinomial). Species epithet strings
     * begin with a lower case letter.
     *
     * @return  the string containing the species epithet of <i>this</i> non viral taxon name
     * @see     #getNameCache()
     */
    public String getSpecificEpithet();

    /**
     * Returns the species subdivision epithet string (infraspecific part) for
     * <i>this</i> non viral taxon name if its {@link Rank rank} is infraspecific
     * (lower than species: trinomial). Species subdivision epithet strings
     * begin with a lower case letter.
     *
     * @return  the string containing the infraspecific part of <i>this</i> non viral taxon name
     * @see     #getNameCache()
     */
    public String getInfraSpecificEpithet();


    /**
     * Defines the last part of the name.
     * This is for infraspecific taxa, the infraspecific epithet,
     * for species the specific epithet, for infageneric taxa the infrageneric epithet
     * else the genusOrUninomial.
     * However, the result does not depend on the rank (which may be not correctly set
     * in case of dirty data) but returns the first name part which is not blank
     * considering the above order.
     * @return the first not blank name part in reverse order
     */
    public String getLastNamePart();

    /**
     * Returns or generates the nameCache (scientific name
     * without author strings and year) string for <i>this</i> non viral taxon name. If the
     * {@link #isProtectedNameCache() protectedNameCache} flag is not set (False)
     * the string will be generated according to a defined strategy,
     * otherwise the value of the actual nameCache string will be returned.
     *
     * @return  the string which identifies <i>this</i> non viral taxon name (without authors or year)
     * @see     #generateNameCache()
     */
    @Transient
    public String getNameCache() ;

    /**
     * Returns the hybrid child relationships ordered by relationship type, or if equal
     * by title cache of the related names.
     * @see #getHybridParentRelations()
     */
    @Transient
    public List<HybridRelationship> getOrderedChildRelationships();

    /**
     * Tests if the given name has any authors.
     * @return false if no author ((ex)combination or (ex)basionym) exists, true otherwise
     */
    public boolean hasAuthors();

    /**
     * Needs to be implemented by those classes that handle autonyms (e.g. botanical names).
     **/
   @Transient
   public boolean isAutonym();


   /**
    * Returns the boolean value of the flag indicating whether the name of <i>this</i>
    * botanical taxon name is a hybrid formula (true) or not (false). A hybrid
    * named by a hybrid formula (composed with its parent names by placing the
    * multiplication sign between them) does not have an own published name
    * and therefore has neither an {@link NonViralName#getAuthorshipCache() autorship}
    * nor other name components. If this flag is set no other hybrid flags may
    * be set.
    *
    * @return  the boolean value of the isHybridFormula flag
    * @see     #isMonomHybrid()
    * @see     #isBinomHybrid()
    * @see     #isTrinomHybrid()
    * @see     #isHybridName()
   */
   public boolean isHybridFormula();

   /**
    * Returns the boolean value of the flag indicating whether <i>this</i> botanical
    * taxon name is the name of an intergeneric hybrid (true) or not (false).
    * In this case the multiplication sign is placed before the scientific
    * name. If this flag is set no other hybrid flags may be set.
    *
    * @return  the boolean value of the isMonomHybrid flag
    * @see     #isHybridFormula()
    * @see     #isBinomHybrid()
    * @see     #isTrinomHybrid()
    * @see     #isHybridName()
    */
   public boolean isMonomHybrid();

   /**
    * Returns the boolean value of the flag indicating whether <i>this</i> botanical
    * taxon name is the name of an interspecific hybrid (true) or not (false).
    * In this case the multiplication sign is placed before the species
    * epithet. If this flag is set no other hybrid flags may be set.
    *
    * @return  the boolean value of the isBinomHybrid flag
    * @see     #isHybridFormula()
    * @see     #isMonomHybrid()
    * @see     #isTrinomHybrid()
    * @see     #isHybridName()
    */
   public boolean isBinomHybrid();

   /**
    * Returns the boolean value of the flag indicating whether <i>this</i> botanical
    * taxon name is the name of an infraspecific hybrid (true) or not (false).
    * In this case the term "notho-" (optionally abbreviated "n-") is used as
    * a prefix to the term denoting the infraspecific rank of <i>this</i> botanical
    * taxon name. If this flag is set no other hybrid flags may be set.
    *
    * @return  the boolean value of the isTrinomHybrid flag
    * @see     #isHybridFormula()
    * @see     #isMonomHybrid()
    * @see     #isBinomHybrid()
    * @see     #isHybridName()
    */
   public boolean isTrinomHybrid();

   /**
     * Computes if this name is a hybrid name. <code>true</code> if any of the monon, binom or trinom
     * hybrid flags is set. <code>false</code> if neither of these flags is set.
     * Note: usually <code>false</code> if the hybrid formula flag is set as a name is
     * either a hybrid name or a hybrid formula or none of them, but never both.
     *
     * @return the boolean value indicating if this is a hybrid name
     * @see     #isHybridFormula()
     * @see     #isMonomHybrid()
     * @see     #isBinomHybrid()
     * @see     #isTrinomHybrid()
     */
   @Transient
   @java.beans.Transient
   public boolean isHybridName();


   /**
    * Returns the boolean value of the flag intended to protect (true)
    * or not (false) the {@link #getAuthorshipCache() authorshipCache} (complete authorship string)
    * of <i>this</i> non viral taxon name.
    *
    * @return  the boolean value of the protectedAuthorshipCache flag
    * @see     #getAuthorshipCache()
    */
   public boolean isProtectedAuthorshipCache();


   /**
    * Returns the boolean value of the flag intended to protect (true)
    * or not (false) the {@link #getNameCache() nameCache} (scientific name without author strings and year)
    * string of <i>this</i> non viral taxon name.
    *
    * @return  the boolean value of the protectedNameCache flag
    * @see     #getNameCache()
    */
   public boolean isProtectedNameCache();

   public void removeHybridChild(INonViralName child);

   public void removeHybridParent(INonViralName parent);

   /**
    * Removes one {@link HybridRelationship hybrid relationship} from the set of
    * {@link #getHybridRelationships() hybrid relationships} in which <i>this</i> botanical taxon name
    * is involved. The hybrid relationship will also be removed from the set
    * belonging to the second botanical taxon name involved.
    *
    * @param  relationship  the hybrid relationship which should be deleted from the corresponding sets
    * @see                  #getHybridRelationships()
    */
   public void removeHybridRelationship(HybridRelationship hybridRelation);

   /**
    * Assigns an authorshipCache string to <i>this</i> non viral taxon name. Sets the isProtectedAuthorshipCache
    * flag to <code>true</code>.
    *
    * @param  authorshipCache  the string which identifies the complete authorship of <i>this</i> non viral taxon name
    * @see    #getAuthorshipCache()
    */
   public void setAuthorshipCache(String authorshipCache);


   /**
    * Assigns an authorshipCache string to <i>this</i> non viral taxon name.
    *
    * @param  authorshipCache  the string which identifies the complete authorship of <i>this</i> non viral taxon name
    * @param  protectedAuthorshipCache if true the isProtectedAuthorshipCache flag is set to <code>true</code>, otherwise
    * the flag is set to <code>false</code>.
    * @see    #getAuthorshipCache()
    */
   public void setAuthorshipCache(String authorshipCache, boolean protectedAuthorshipCache);

   /**
    * @see  #getBasionymAuthorship()
    */
   public void setBasionymAuthorship(TeamOrPersonBase<?> basionymAuthorship);

   /**
    * @see  #isBinomHybrid()
    * @see  #isMonomHybrid()
    * @see  #isTrinomHybrid()
    */
   public void setBinomHybrid(boolean binomHybrid);


   /**
    * @see  #getCombinationAuthorship()
    */
   public void setCombinationAuthorship(TeamOrPersonBase<?> combinationAuthorship);

   /**
    * @see  #getExBasionymAuthorship()
    */
   public void setExBasionymAuthorship(TeamOrPersonBase<?> exBasionymAuthorship);


   /**
    * @see  #getExCombinationAuthorship()
    */
   public void setExCombinationAuthorship(TeamOrPersonBase<?> exCombinationAuthorship);

   /**
    * @see  #getGenusOrUninomial()
    */
   public void setGenusOrUninomial(String genusOrUninomial);

   /**
    * @see  #isHybridFormula()
    */
   public void setHybridFormula(boolean hybridFormula);

   /**
    * @see  #getInfraGenericEpithet()
    */
   public void setInfraGenericEpithet(String infraGenericEpithet);

   /**
    * @see  #getInfraSpecificEpithet()
    */
   public void setInfraSpecificEpithet(String infraSpecificEpithet);

   /**
    * @see  #isMonomHybrid()
    * @see  #isBinomHybrid()
    * @see  #isTrinomHybrid()
    */
   public void setMonomHybrid(boolean monomHybrid);

   /**
    * Assigns a nameCache string to <i>this</i> non viral taxon name and protects it from being overwritten.
    * Sets the protectedNameCache flag to <code>true</code>.
    *
    * @param  nameCache  the string which identifies <i>this</i> non viral taxon name (without authors or year)
    * @see    #getNameCache()
    */
   public void setNameCache(String nameCache);

   /**
    * Assigns a nameCache string to <i>this</i> non viral taxon name and protects it from being overwritten.
    * Sets the protectedNameCache flag to <code>true</code>.
    *
    * @param  nameCache  the string which identifies <i>this</i> non viral taxon name (without authors or year)
    * @param  protectedNameCache if true teh protectedNameCache is set to <code>true</code> or otherwise set to
    * <code>false</code>
    * @see    #getNameCache()
    */
   public void setNameCache(String nameCache, boolean protectedNameCache);

   /**
    * @see     #isProtectedAuthorshipCache()
    * @see     #getAuthorshipCache()
    */
   public void setProtectedAuthorshipCache(boolean protectedAuthorshipCache);

   /**
    * @see     #isProtectedNameCache()
    */
   public void setProtectedNameCache(boolean protectedNameCache);

   /**
    * @see  #getSpecificEpithet()
    */
   public void setSpecificEpithet(String specificEpithet);

   /**
    * @see  #isTrinomHybrid()
    * @see  #isBinomHybrid()
    * @see  #isMonomHybrid()
    */
   public void setTrinomHybrid(boolean trinomHybrid);


}
