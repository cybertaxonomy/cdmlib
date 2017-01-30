/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;

/**
 * @author a.mueller
 * @date 30.01.2017
 *
 */
public class TaxonNameFactory {
 // *************** FACTORY METHODS ********************************/

    /**
     * Creates a new non viral taxon name instance
     * only containing its {@link common.Rank rank} and
      * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     *
     * @param  rank  the rank to be assigned to <i>this</i> non viral taxon name
     * @see    #NewInstance(Rank, HomotypicalGroup)
     * @see    #NonViralName(Rank, HomotypicalGroup)
     * @see    #NonViralName()
     * @see    #NonViralName(Rank, String, String, String, String, TeamOrPersonBase, Reference, String, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    public static NonViralName NewNonViralInstance(Rank rank){
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
     * @see    #NonViralName(Rank, String, String, String, String, TeamOrPersonBase, Reference, String, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    public static NonViralName NewNonViralInstance(Rank rank, HomotypicalGroup homotypicalGroup){
        return new NonViralName(rank, homotypicalGroup);
    }


    /**
     * Creates a new viral taxon name instance only containing its {@link Rank rank}.
     *
     * @param   rank  the rank to be assigned to <i>this</i> viral taxon name
     * @see     #ViralName(Rank)
     */
    public static ViralName NewViralInstance(Rank rank){
        return new ViralName(rank);
    }

    /**
     * Creates a new bacterial taxon name instance
     * only containing its {@link Rank rank} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     *
     * @param  rank  the rank to be assigned to <i>this</i> bacterial taxon name
     * @see    #NewInstance(Rank, HomotypicalGroup)
     * @see    #BacterialName(Rank, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    public static BacterialName NewBacterialInstance(Rank rank){
        return new BacterialName(rank, null);
    }

    /**
     * Creates a new bacterial taxon name instance
     * only containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
     * The new bacterial taxon name instance will be also added to the set of
     * bacterial taxon names belonging to this homotypical group.
     *
     * @param  rank  the rank to be assigned to <i>this</i> bacterial taxon name
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> bacterial taxon name belongs
     * @see    #NewInstance(Rank)
     * @see    #BacterialName(Rank, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
     * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
     */
    public static BacterialName NewBacterialInstance(Rank rank, HomotypicalGroup homotypicalGroup){
        return new BacterialName(rank, homotypicalGroup);
    }


    /**
     * Creates a new zoological taxon name instance
     * only containing its {@link Rank rank} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
     *
     * @param   rank    the rank to be assigned to <i>this</i> zoological taxon name
     * @see             #ZoologicalName(Rank, HomotypicalGroup)
     * @see             #NewInstance(Rank, HomotypicalGroup)
     * @see             #NewZoologicalInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
     * @see             eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
     */
    public static ZoologicalName NewZoologicalInstance(Rank rank){
        return new ZoologicalName(rank, null);
    }

    /**
     * Creates a new zoological taxon name instance
     * only containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
     * The new zoological taxon name instance will be also added to the set of
     * zoological taxon names belonging to the given homotypical group.
     *
     * @param  rank  the rank to be assigned to <i>this</i> zoological taxon name
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> zoological taxon name belongs
     * @see    #NewInstance(Rank)
     * @see    #NewZoologicalInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
     * @see    #ZoologicalName(Rank, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
     */
    public static ZoologicalName NewZoologicalInstance(Rank rank, HomotypicalGroup homotypicalGroup){
        return new ZoologicalName(rank, homotypicalGroup);
    }
    /**
     * Creates a new zoological taxon name instance
     * containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group},
     * its scientific name components, its {@link eu.etaxonomy.cdm.agent.TeamOrPersonBase author(team)},
     * its {@link eu.etaxonomy.cdm.reference.INomenclaturalReference nomenclatural reference} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy default cache strategy}.
     * The new zoological taxon name instance will be also added to the set of
     * zoological taxon names belonging to the given homotypical group.
     *
     * @param   rank  the rank to be assigned to <i>this</i> zoological taxon name
     * @param   genusOrUninomial the string for <i>this</i> zoological taxon name
     *          if its rank is genus or higher or for the genus part
     *          if its rank is lower than genus
     * @param   infraGenericEpithet  the string for the first epithet of
     *          <i>this</i> zoological taxon name if its rank is lower than genus
     *          and higher than species aggregate
     * @param   specificEpithet  the string for the first epithet of
     *          <i>this</i> zoological taxon name if its rank is species aggregate or lower
     * @param   infraSpecificEpithet  the string for the second epithet of
     *          <i>this</i> zoological taxon name if its rank is lower than species
     * @param   combinationAuthorship  the author or the team who published <i>this</i> zoological taxon name
     * @param   nomenclaturalReference  the nomenclatural reference where <i>this</i> zoological taxon name was published
     * @param   nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
     * @param   homotypicalGroup  the homotypical group to which <i>this</i> zoological taxon name belongs
     * @see     #NewInstance(Rank)
     * @see     #NewInstance(Rank, HomotypicalGroup)
     * @see     #ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
     * @see     eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy
     */
    public static ZoologicalName NewZoologicalInstance(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
        return new ZoologicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
    }

    /**
     * Creates a new botanical taxon name instance
     * only containing its {@link Rank rank} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
     *
     * @param   rank    the rank to be assigned to <i>this</i> botanical taxon name
     * @see             #BotanicalName(Rank, HomotypicalGroup)
     * @see             #NewInstance(Rank, HomotypicalGroup)
     * @see             #NewBotanicalInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
     * @see             eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
     */
    public static BotanicalName NewBotanicalInstance(Rank rank){
        return new BotanicalName(rank, null);
    }
    /**
     * Creates a new botanical taxon name instance
     * only containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
     * The new botanical taxon name instance will be also added to the set of
     * botanical taxon names belonging to this homotypical group.
     *
     * @param  rank  the rank to be assigned to <i>this</i> botanical taxon name
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
     * @see    #NewInstance(Rank)
     * @see    #NewBotanicalInstance(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
     * @see    #BotanicalName(Rank, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
     */
    public static BotanicalName NewBotanicalInstance(Rank rank, HomotypicalGroup homotypicalGroup){
        return new BotanicalName(rank, homotypicalGroup);
    }
    /**
     * Creates a new botanical taxon name instance
     * containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group},
     * its scientific name components, its {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author(team)},
     * its {@link eu.etaxonomy.cdm.model.reference.INomenclaturalReference nomenclatural reference} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
     * The new botanical taxon name instance will be also added to the set of
     * botanical taxon names belonging to this homotypical group.
     *
     * @param   rank  the rank to be assigned to <i>this</i> botanical taxon name
     * @param   genusOrUninomial the string for <i>this</i> botanical taxon name
     *          if its rank is genus or higher or for the genus part
     *          if its rank is lower than genus
     * @param   infraGenericEpithet  the string for the first epithet of
     *          <i>this</i> botanical taxon name if its rank is lower than genus
     *          and higher than species aggregate
     * @param   specificEpithet  the string for the first epithet of
     *          <i>this</i> botanical taxon name if its rank is species aggregate or lower
     * @param   infraSpecificEpithet  the string for the second epithet of
     *          <i>this</i> botanical taxon name if its rank is lower than species
     * @param   combinationAuthorship  the author or the team who published <i>this</i> botanical taxon name
     * @param   nomenclaturalReference  the nomenclatural reference where <i>this</i> botanical taxon name was published
     * @param   nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
     * @param   homotypicalGroup  the homotypical group to which <i>this</i> botanical taxon name belongs
     * @see     #NewInstance(Rank)
     * @see     #NewInstance(Rank, HomotypicalGroup)
     * @see     ZoologicalName#ZoologicalName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
     * @see     eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
     */
    public static  BotanicalName NewBotanicalInstance(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorship, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
        return new BotanicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
    }


    /**
     * Creates a new cultivar taxon name instance
     * only containing its {@link Rank rank} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
     *
     * @param   rank    the rank to be assigned to <i>this</i> cultivar taxon name
     * @see             #CultivarPlantName(Rank, HomotypicalGroup)
     * @see             #NewInstance(Rank, HomotypicalGroup)
     * @see             eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
     */
    public static CultivarPlantName NewCultivarInstance(Rank rank){
        return new CultivarPlantName(rank, null);
    }

    /**
     * Creates a new cultivar taxon name instance
     * only containing its {@link Rank rank},
     * its {@link HomotypicalGroup homotypical group} and
     * the {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy default cache strategy}.
     * The new cultivar taxon name instance will be also added to the set of
     * cultivar taxon names belonging to this homotypical group.
     *
     * @param  rank  the rank to be assigned to <i>this</i> cultivar taxon name
     * @param  homotypicalGroup  the homotypical group to which <i>this</i> cultivar taxon name belongs
     * @see    #NewInstance(Rank)
     * @see    #CultivarPlantName(Rank, HomotypicalGroup)
     * @see    eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy
     */
    public static CultivarPlantName NewCultivarInstance(Rank rank, HomotypicalGroup homotypicalGroup){
        return new CultivarPlantName(rank, homotypicalGroup);
    }

}
