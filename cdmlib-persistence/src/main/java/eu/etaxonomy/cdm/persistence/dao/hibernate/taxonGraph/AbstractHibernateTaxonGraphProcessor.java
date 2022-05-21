/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmPreferenceLookup;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.hibernate.TaxonGraphHibernateListener;

/**
 * Provides the business logic to manage multiple classifications as
 * classification fragments in a graph of
 * {@link eu.etaxonomy.cdm.model.taxon.Taxon Taxa} and
 * {@link eu.etaxonomy.cdm.model.taxon.TaxonRelationship TaxonRelationships}.
 * <p>
 * This abstract class provides the base for
 * {@link eu.etaxonomy.cdm.api.service.taxonGraph.TaxonGraphBeforeTransactionCompleteProcess}
 * and {@link TaxonGraphDaoHibernateImpl} which both are operating on the persisted
 * graph structures and thus are sharing this business logic in common:
 * <ul>
 * <li><code>TaxonGraphBeforeTransactionCompleteProcess</code>: Manages the
 * graph and is the only class allowed to modify it.</li>
 * <li><code>TaxonGraphDaoHibernateImpl</code>: Provides read only access to the
 * graph structure.</li>
 * <ul>
 * <p>
 * The conceptual idea for the resulting graph is described in <a href=
 * "https://dev.e-taxonomy.eu/redmine/issues/6173#6-N1T-Higher-taxon-graphs-with-includedIn-relations-taxon-relationships">#6173
 * 6) [N1T] Higher taxon-graphs with includedIn relations taxon
 * relationships}</a> The
 * <code>TaxonGraphBeforeTransactionCompleteProcess</code> is instantiated and
 * used in the {@link TaxonGraphHibernateListener}
 *
 * @author a.kohlbecker
 * @since Oct 4, 2018
 *
 */
public abstract class AbstractHibernateTaxonGraphProcessor {

    private static final Logger logger = Logger.getLogger(AbstractHibernateTaxonGraphProcessor.class);

    EnumSet<ReferenceType> referenceSectionTypes = EnumSet.of(ReferenceType.Section, ReferenceType.BookSection);

    private Reference secReference = null;

    private TaxonRelationshipType relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();

    protected TaxonRelationshipType relType() {
        if(relType == null){
            relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();
        }
        return relType;
    }


    /**
     * MUST ONLY BE USED IN TESTS
     */
    @Deprecated
    protected UUID secReferenceUUID;


    /**
     * MUST ONLY BE USED IN TESTS
     */
    @Deprecated
    protected void setSecReferenceUUID(UUID uuid){
        secReferenceUUID = uuid;
    }

    public UUID getSecReferenceUUID(){
        if(secReferenceUUID != null){
            return secReferenceUUID;
        } else {
            CdmPreference pref = CdmPreferenceLookup.instance().get(TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID);
            UUID uuid = null;
            if(pref != null && pref.getValue() != null){
                try {
                    uuid = UUID.fromString(pref.getValue());
                } catch (Exception e) {
                    // TODO is logging only ok?
                    logger.error(e);
                }
            }
            if(uuid == null){
                logger.error("missing cdm property: " + TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID.getSubject() + TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID.getPredicate());
            }
            return uuid;
        }
    }

    /**
     * Provides the sec reference for all taxa in the graph. The Reference uuid
     * is expected to be stored in the CDM perferences under the preference key
     * {@link TaxonGraphDaoHibernateImpl#CDM_PREF_KEY_SEC_REF_UUID}
     *
     * @return The reference for all taxa in the graph
     */
    public Reference secReference(){
        if(secReference == null){
            Query<Reference> q = getSession().createQuery("SELECT r FROM Reference r WHERE r.uuid = :uuid", Reference.class);
            q.setParameter("uuid", getSecReferenceUUID());
            secReference = q.uniqueResult();
            if(secReference == null){
                Reference missingRef = ReferenceFactory.newGeneric();
                UUID uuid = getSecReferenceUUID();
                if(uuid != null){
                    missingRef.setUuid(uuid);
                } else {
                    throw new RuntimeException("cdm preference " + TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID.getSubject() + TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID.getPredicate() + " missing, can not recover");
                }
                missingRef.setTitle("Autocreated missing reference for cdm property" + TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID.getSubject() + TaxonGraphDaoHibernateImpl.CDM_PREF_KEY_SEC_REF_UUID.getPredicate());
                logger.warn("A reference with " + getSecReferenceUUID() + " does not exist in the database, and thus will be created now with the title "
                        + "\"" + missingRef.getTitle() + "\"");
                getSession().merge(missingRef);
            }
        } else {
            // make sure the entity is still in the current session
            secReference = getSession().load(Reference.class, secReference.getId());
        }
        return secReference;
    }


    /**
     * Create all missing edges from the <code>taxon</code> to names with higher
     * rank and edges from names with lower rank to this taxon. No longer needed
     * relations (edges) are removed.
     * <p>
     * {@link #conceptReference(Reference) concept references} which are null are ignored.
     * This means no edges are created.
     *
     *
     * @param taxon
     *            The taxon to update the edges for.
     */
    public void updateEdges(Taxon taxon) throws TaxonGraphException {

        Reference nomenclaturalReference = taxon.getName().getNomenclaturalReference();

        updateEdges(taxon, nomenclaturalReference);
    }

    /**
     * Create all missing edges from the <code>taxon</code> to names with higher
     * rank and edges from names with lower rank to this taxon. No longer needed
     * relations (edges) are removed.
     * <p>
     * {@link #conceptReference(Reference) concept references} which are null are ignored.
     * This means no edges are created.
     *
     *
     * @param taxon
     *            The taxon to update the edges for.
     * @param nomenclaturalReference
     *           The nomenclatural reference to update the edged with.
     */
    protected void updateEdges(Taxon taxon, Reference nomenclaturalReference) throws TaxonGraphException {

        Reference conceptReference = conceptReference(nomenclaturalReference);

        if(conceptReference != null){
            // update edges to higher names
            List<TaxonName> relatedHigherNames = relatedHigherNames(taxon.getName());
            List<TaxonRelationship> relationsFrom = taxonGraphRelationsFrom(taxon, conceptReference);
            List<TaxonName> relatedHigherNamesWithoutRels = new ArrayList<>(relatedHigherNames);
            for(TaxonRelationship rel : relationsFrom){
                boolean isRelToHigherName = relatedHigherNames.contains(rel.getToTaxon().getName());
                if(isRelToHigherName){
                    relatedHigherNamesWithoutRels.remove(rel.getToTaxon().getName());
                } else {
                    taxon.removeTaxonRelation(rel);
                }
            }
            for(TaxonName name : relatedHigherNamesWithoutRels){
                Taxon toTaxon = assureSingleTaxon(name);
                taxon.addTaxonRelation(toTaxon, relType(), conceptReference, null);
            }

            // update edges from lower names
            List<TaxonName> relatedLowerNames = relatedLowerNames(taxon.getName());
            List<TaxonRelationship> relationsTo = taxonGraphRelationsTo(taxon, null);
            List<TaxonName> relatedLowerNamesWithoutRels = new ArrayList<>(relatedLowerNames);
            for(TaxonRelationship rel : relationsTo){
                boolean isRelFromLowerName = relatedLowerNames.contains(rel.getFromTaxon().getName());
                if(isRelFromLowerName){
                    relatedLowerNamesWithoutRels.remove(rel.getFromTaxon().getName());
                } else {
                    taxon.removeTaxonRelation(rel);
                }
            }
            for(TaxonName name : relatedLowerNamesWithoutRels){
                Taxon fromTaxon = assureSingleTaxon(name);
                fromTaxon.addTaxonRelation(taxon, relType(), conceptReference, null);
            }
        }
    }

    /**
     * Remove all edges from the <code>taxon</code> having the <code>conceptReference</code>
     *
     * @param taxon
     * @param oldConceptReference
     */
    public void removeEdges(Taxon taxon, Reference conceptReference) {
        List<TaxonRelationship> relations = taxonGraphRelationsFrom(taxon, conceptReference);
        List<TaxonName> relatedHigherNames = relatedHigherNames(taxon.getName());
        for(TaxonRelationship rel : relations){
            boolean isRelToHigherName = relatedHigherNames.contains(rel.getToTaxon().getName());
            if(isRelToHigherName){
                taxon.removeTaxonRelation(rel);
            }
        }
    }

    /**
     * @param taxon
     */
    public void updateConceptReferenceInEdges(Taxon taxon, Reference oldNomReference) throws TaxonGraphException {

        Reference conceptReference = conceptReference(taxon.getName().getNomenclaturalReference());
        Reference oldConceptReference = conceptReference(oldNomReference);

        if(conceptReference != null && oldConceptReference != null){
            // update old with new ref
            updateReferenceInEdges(taxon, conceptReference, oldConceptReference);
        } else if(conceptReference != null && oldConceptReference == null) {
            // create new relations for the name as there are none so far
            updateEdges(taxon);
        } else if(conceptReference == null && oldConceptReference != null){
            // remove all relations
            removeEdges(taxon, oldConceptReference);
        }
    }

    /**
     * @param taxon
     * @param conceptReference
     * @param oldConceptReference
     */
    protected void updateReferenceInEdges(Taxon taxon, Reference conceptReference, Reference oldConceptReference) {
        List<TaxonRelationship> relations = taxonGraphRelationsFrom(taxon, oldConceptReference);
        List<TaxonName> relatedHigherNames = relatedHigherNames(taxon.getName());
        for(TaxonRelationship rel : relations){
            boolean isRelToHigherName = relatedHigherNames.contains(rel.getToTaxon().getName());
            if(isRelToHigherName){
                rel.setCitation(conceptReference);
                getSession().saveOrUpdate(rel);
            }
        }
    }

    abstract public Session getSession();

    /**
     * Same as {@link #assureSingleTaxon(TaxonName, boolean)} with
     * <code>createMissing = true</code>
     */
    public Taxon assureSingleTaxon(TaxonName taxonName) throws TaxonGraphException {
        return assureSingleTaxon(taxonName, true);
    }

    /**
     * Assurers that there is only one {@link Taxon} for the given name
     * (<code>taxonName</code>) having the the default sec reference
     * ({@link #getSecReferenceUUID()}).
     * <p>
     * If there is no such taxon it will be created when
     * <code>createMissing=true</code>. A <code>TaxonGraphException</code> is
     * thrown when more than one taxa with the default sec reference
     * ({@link #getSecReferenceUUID()}) are found for the given name
     * (<code>taxonName</code>)
     *
     * @param taxonName
     *            The name to check
     * @param createMissing
     *            A missing taxon is created when this is <code>true</code>.
     * @return
     * @throws TaxonGraphException
     *             A <code>TaxonGraphException</code> is thrown when more than
     *             one taxa with the default sec reference
     *             ({@link #getSecReferenceUUID()}) are found for the given name
     *             (<code>taxonName</code>)
     */
    public Taxon assureSingleTaxon(TaxonName taxonName, boolean createMissing) throws TaxonGraphException {

        UUID secRefUuid = getSecReferenceUUID();
        Session session = getSession();
        TaxonName taxonNamePersisted = session.load(TaxonName.class, taxonName.getId());

        // filter by secRefUuid
        Taxon taxon = null;
        Set<Taxon> taxa = new HashSet<>();
        for(Taxon t : taxonName.getTaxa()){
            if(t.getSec() != null && t.getSec().getUuid().equals(secRefUuid)){
                taxa.add(t);
            }
        }

        if(taxa.size() == 0){
            if(createMissing){
                if(taxonNamePersisted != null){
                    Reference secRef = secReference();
                    taxon = Taxon.NewInstance(taxonNamePersisted, secRef);
                    session.saveOrUpdate(taxon);
                } else {
                    throw new TaxonGraphException("Can't create taxon for deleted name: " + taxonName);
                }
            } else {
                if(logger.isDebugEnabled()){
                    logger.debug("No taxon found for " + taxonName);
                }
            }
        } else if(taxa.size() == 1){
            taxon = taxa.iterator().next();
        } else {
            throw new TaxonGraphException("A name to be used in a taxon graph must only have one taxon with the default sec reference [secRef uuid: "+ secRefUuid.toString() +"]");
        }
        return taxon != null ? session.load(Taxon.class, taxon.getId()) : null;
    }

    /**
     * Provides the concept reference for a given <code>nomenclaturalReference</code>.
     * For references which are {@link ReferenceType#Section} or {@link ReferenceType#BookSection} the in-reference is returned,
     * otherwise the passed  <code>nomenclaturalReference</code> itself.
     */
    protected Reference conceptReference(Reference nomenclaturalReference) {

        Reference conceptRef = nomenclaturalReference;
        if(conceptRef != null){
            while(referenceSectionTypes.contains(conceptRef.getType()) && conceptRef.getInReference() != null){
                conceptRef = conceptRef.getInReference();
            }
        }
        return conceptRef;
    }

    /**
     * @param name
     * @return
     */
    protected List<TaxonName> relatedHigherNames(TaxonName name) {

        List<TaxonName> relatedNames = new ArrayList<>();

        if(name.getRank().isSpecies() || name.getRank().isInfraSpecific()){
            if(name.getGenusOrUninomial() != null){
                List<TaxonName> names = listNamesAtRank(Rank.GENUS(), name.getGenusOrUninomial(), null);
                if(names.size() == 0){
                    logger.warn("Genus entity with \"" + name.getGenusOrUninomial() + "\" missing");
                } else {
                    if(names.size() > 1){
                        logger.warn("Duplicate genus entities found for \"" + name.getGenusOrUninomial() + "\", will create taxon graph relation to all of them!");
                    }
                    relatedNames.addAll(names);
                }
            }
        }
        if(name.getRank().isInfraSpecific()){
            if(name.getGenusOrUninomial() != null && name.getSpecificEpithet() != null){
                List<TaxonName> names = listNamesAtRank(Rank.SPECIES(), name.getGenusOrUninomial(), name.getSpecificEpithet());
                if(names.size() == 0){
                    logger.warn("Species entity with \"" + name.getGenusOrUninomial() + " " + name.getSpecificEpithet() + "\" missing");
                } else {
                    if(names.size() > 1){
                        logger.warn("Duplicate species entities found for \"" + name.getGenusOrUninomial() + " " + name.getSpecificEpithet() + "\", will create taxon graph relation to all of them!");
                    }
                    relatedNames.addAll(names);
                }
            }
         }

        return relatedNames;
    }

    /**
     * @param name
     * @return
     */
    protected List<TaxonName> relatedLowerNames(TaxonName name) {

        List<TaxonName> relatedNames = new ArrayList<>();

        if(name.getRank().isGenus()){
            if(name.getGenusOrUninomial() != null){
                List<TaxonName> names = listNamesAtRank(Rank.SPECIES(), name.getGenusOrUninomial(), null);
                if(names.size() == 0){
                    logger.debug("No species entity with \"" + name.getGenusOrUninomial() + " *\" found");
                } else {
                    logger.debug(names.size() + " species entities found with \"" + name.getGenusOrUninomial() + " *\"");
                    relatedNames.addAll(names);
                }
            }
        }
        if(name.getRank().isSpecies()){
            if(name.getGenusOrUninomial() != null && name.getSpecificEpithet() != null){
                List<TaxonName> names = listNamesBelowRank(Rank.SPECIES(), name.getGenusOrUninomial(), name.getSpecificEpithet());
                if(names.size() == 0){
                    logger.warn("No infraspecific entity with \"" + name.getGenusOrUninomial() + " " + name.getSpecificEpithet() + "*\" found");
                } else {
                    if(names.size() > 1){
                        logger.warn(names.size() + " infraspecific entities found with \"" + name.getGenusOrUninomial() + " " + name.getSpecificEpithet() + "*\"found");
                    }
                    relatedNames.addAll(names);
                }
            }
         }

        return relatedNames;
    }


    /**
     * @param taxon
     */
    protected List<TaxonRelationship> taxonGraphRelationsFrom(Taxon taxon, Reference citation) {
        List<TaxonRelationship> relations = getTaxonRelationships(taxon, relType(), citation, TaxonRelationship.Direction.relatedFrom);
        return relations;
    }

    /**
     * @param taxon
     */
    protected List<TaxonRelationship> taxonGraphRelationsTo(Taxon taxon, Reference citation) {
        List<TaxonRelationship> relations = getTaxonRelationships(taxon, relType(), citation, TaxonRelationship.Direction.relatedTo);
        return relations;
    }

    protected List<TaxonName> listNamesAtRank(Rank rank, String genusOrUninomial, String specificEpithet){
        String hql = "SELECT n FROM TaxonName n WHERE n.rank = :rank AND n.genusOrUninomial = :genusOrUninomial";
        if(specificEpithet != null){
            hql += " AND n.specificEpithet = :specificEpithet";
        }
        Query<TaxonName> q = getSession().createQuery(hql, TaxonName.class);

        q.setParameter("rank", rank);
        q.setParameter("genusOrUninomial", genusOrUninomial);
        if(specificEpithet != null){
            q.setParameter("specificEpithet", specificEpithet);
        }

        List<TaxonName> result = q.list();
        return result;
    }

    protected List<TaxonName> listNamesBelowRank(Rank rank, String genusOrUninomial, String specificEpithet){
        String hql = "SELECT n FROM TaxonName n WHERE n.rank.orderIndex > :rankOrderIndex AND n.genusOrUninomial = :genusOrUninomial";
        if(specificEpithet != null){
            hql += " AND n.specificEpithet = :specificEpithet";
        }
        Query<TaxonName> q = getSession().createQuery(hql, TaxonName.class);

        q.setParameter("rankOrderIndex", rank.getOrderIndex());
        q.setParameter("genusOrUninomial", genusOrUninomial);
        if(specificEpithet != null){
            q.setParameter("specificEpithet", specificEpithet);
        }

        List<TaxonName> result = q.list();
        return result;
    }

    /**
     *
     * @param relatedTaxon required
     * @param type required
     * @param citation can be null
     * @param direction required
     * @return
     */
    protected List<TaxonRelationship> getTaxonRelationships(Taxon relatedTaxon, TaxonRelationshipType type, Reference citation, Direction direction){

        getSession().flush();
        String hql = "SELECT rel FROM TaxonRelationship rel WHERE rel." + direction + " = :relatedTaxon AND rel.type = :type";
        if(citation != null){
            hql += " AND rel.source.citation = :citation";
        }
        Query<TaxonRelationship> q = getSession().createQuery(hql, TaxonRelationship.class);
        q.setParameter("relatedTaxon", relatedTaxon);
        q.setParameter("type", type);
        if(citation != null){
            q.setParameter("citation", citation);
        }
        List<TaxonRelationship> rels = q.list();
        return rels;
    }


}
