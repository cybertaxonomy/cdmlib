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
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;

/**
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

    public Reference secReference(){
        if(secReference == null){
            Query q = getSession().createQuery("SELECT r FROM Reference r WHERE r.uuid = :uuid");
            q.setParameter("uuid", getSecReferenceUUID());
            secReference = (Reference) q.uniqueResult();
        } else {
            // make sure the entity is still in the current session
            secReference = getSession().load(Reference.class, secReference.getId());
        }
        return secReference;
    }


    /**
     * Create all missing edges from the <code>taxon</code>.
     *
     * @param taxonName
     */
    public void updateEdges(Taxon taxon) throws TaxonGraphException {

        List<TaxonName> relatedHigherNames = relatedHigherNames(taxon.getName());
        Reference conceptReference = conceptReference(taxon.getName().getNomenclaturalReference());
        if(conceptReference != null){
            List<TaxonRelationship> relations = taxonGraphRelationsFrom(taxon, conceptReference);
            List<TaxonName> relatedHigherNamesWithoutRels = new ArrayList<>(relatedHigherNames);
            for(TaxonRelationship rel : relations){
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

    public Taxon assureSingleTaxon(TaxonName taxonName) throws TaxonGraphException {

        UUID secRefUuid = getSecReferenceUUID();
        Session session = getSession();
        TaxonName taxonNamePersisted = session.load(TaxonName.class, taxonName.getId());
        Taxon taxon;
        if(taxonName.getTaxa().size() == 0){
            if(taxonNamePersisted != null){
            Reference secRef = secReference();
                taxon = Taxon.NewInstance(taxonNamePersisted, secRef);
                session.saveOrUpdate(taxon);
                return taxon;
            } else {
                throw new TaxonGraphException("Can't create taxon for deleted name: " + taxonName);
            }
        } else if(taxonName.getTaxa().size() == 1){
            taxon = taxonName.getTaxa().iterator().next();
            if(taxon.getSec() == null){
                taxon = session.load(Taxon.class, taxon.getId());
                taxon.setSec(secReference());
                session.saveOrUpdate(taxon);
                return taxon;
            } else if(!secRefUuid.equals(taxon.getSec().getUuid())){
                throw new TaxonGraphException("The taxon for a name to be used in a taxon graph must have the default sec reference [secRef uuid: "+ secRefUuid.toString() +"]");
            }
        } else {
            for(Taxon t : taxonName.getTaxa()){
                if(secRefUuid.equals(t.getSec().getUuid())){
                    taxon = t;
                }
            }
            throw new TaxonGraphException("A name to be used in a taxon graph must only have one taxon with the default sec reference [secRef uuid: "+ secRefUuid.toString() +"]");
        }
        return session.load(Taxon.class, taxon.getId());
    }

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
                List<TaxonName> names = listNames(Rank.GENUS(), name.getGenusOrUninomial(), null);
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
                List<TaxonName> names = listNames(Rank.SPECIES(), name.getGenusOrUninomial(), name.getSpecificEpithet());
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

    protected List<TaxonName> listNames(Rank rank, String genusOrUninomial, String specificEpithet){
        String hql = "SELECT n FROM TaxonName n WHERE n.rank = :rank AND n.genusOrUninomial = :genusOrUninomial";
        if(specificEpithet != null){
            hql += " AND n.specificEpithet = :specificEpithet";
        }
        Query q = getSession().createQuery(hql);

        q.setParameter("rank", rank);
        q.setParameter("genusOrUninomial", genusOrUninomial);
        if(specificEpithet != null){
            q.setParameter("specificEpithet", specificEpithet);
        }

        List<TaxonName> result = q.list();
        return result;
    }

    protected List<TaxonRelationship> getTaxonRelationships(Taxon relatedTaxon, TaxonRelationshipType type, Reference citation, Direction direction){
        String hql = "SELECT rel FROM TaxonRelationship rel WHERE rel."+direction+" = :relatedTaxon AND rel.type = :type AND rel.citation = :citation";
        Query q = getSession().createQuery(hql);
        q.setParameter("relatedTaxon", relatedTaxon);
        q.setParameter("type", type);
        q.setParameter("citation", citation);
        List<TaxonRelationship> rels = q.list();
        return rels;
    }


}
