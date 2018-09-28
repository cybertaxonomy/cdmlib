/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.taxonGraph;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * Provides the business logic to manage multiple classifications as
 * classification fragments in a graph of
 * {@link eu.etaxonomy.cdm.model.taxon.Taxon Taxa} and {@link eu.etaxonomy.cdm.model.taxon.TaxonRelationship TaxonRelationships}.
 *
 * For further details on the concept and related discussion see https://dev.e-taxonomy.eu/redmine/issues/6173
 *
 *
 * @author a.kohlbecker
 * @since Sep 26, 2018
 *
 */
@Service()
@Transactional(readOnly = true)
public class TaxonGraphService implements ITaxonGraphService {

    protected static final TaxonRelationshipType RELTYPE = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();

    private EnumSet<ReferenceType> referenceSectionTypes = EnumSet.of(ReferenceType.Section, ReferenceType.BookSection);

    private static final Logger logger = Logger.getLogger(TaxonGraphService.class);

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private ITaxonNameDao nameDao;

    @Autowired
    private IReferenceService referenceService;

    private UUID secReferenceUUID;

    @Override
    public void setSecReferenceUUID(UUID uuid){
        secReferenceUUID = uuid;
    }

    @Override
    public void onNewTaxonName(TaxonName taxonName) throws TaxonGraphException {
        onNameOrRankChange(taxonName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNameOrRankChange(TaxonName taxonName) throws TaxonGraphException {
        Taxon taxon = assureSingleTaxon(taxonName);
        boolean isNotDeleted = taxonService.getSession().contains(taxonName) && taxonName.isPersited();
        // TODO use audit event to check for deletion?
        if(isNotDeleted){
            updateEdges(taxon);
        }
    }

    @Override
    public void onNomReferenceChange(TaxonName taxonName, Reference oldNomReference) throws TaxonGraphException {
        if(oldNomReference == null){
            onNewTaxonName(taxonName);
        }
        Taxon taxon = assureSingleTaxon(taxonName);
        boolean isNotDeleted = taxonService.getSession().contains(taxonName) && taxonName.isPersited();
        // TODO use audit event to check for deletion?
        if(isNotDeleted){
            updateConceptReferenceInEdges(taxon, oldNomReference);
        }
    }

    /**
     * @param taxonName
     */
    private void updateEdges(Taxon taxon) throws TaxonGraphException {

        Session session = taxonService.getSession();
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
                session.merge(taxon.addTaxonRelation(toTaxon, RELTYPE, conceptReference, null));
            }
        }
    }

    /**
     * @param taxon
     */
    private void updateConceptReferenceInEdges(Taxon taxon, Reference oldNomReference) throws TaxonGraphException {

        Reference conceptReference = conceptReference(taxon.getName().getNomenclaturalReference());
        Reference oldConceptReference = conceptReference(oldNomReference);

        if(conceptReference != null && oldConceptReference != null){
            // update old with new ref
            updateReferenceinEdges(taxon, conceptReference, oldConceptReference);
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
     * @param oldConceptReference
     */
    protected void removeEdges(Taxon taxon, Reference oldConceptReference) {
        List<TaxonRelationship> relations = taxonGraphRelationsFrom(taxon, oldConceptReference);
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
     * @param conceptReference
     * @param oldConceptReference
     */
    protected void updateReferenceinEdges(Taxon taxon, Reference conceptReference, Reference oldConceptReference) {
        List<TaxonRelationship> relations = taxonGraphRelationsFrom(taxon, oldConceptReference);
        List<TaxonName> relatedHigherNames = relatedHigherNames(taxon.getName());
        for(TaxonRelationship rel : relations){
            boolean isRelToHigherName = relatedHigherNames.contains(rel.getToTaxon().getName());
            if(isRelToHigherName){
                rel.setCitation(conceptReference);
            }
        }
    }

    /**
     * @param name
     * @return
     */
    private List<TaxonName> relatedHigherNames(TaxonName name) {

        List<TaxonName> relatedNames = new ArrayList<>();

        if(name.getRank().isSpecies() || name.getRank().isInfraSpecific()){
            if(name.getGenusOrUninomial() != null){
                List<Restriction<?>> restrictions = new ArrayList<>();
                restrictions.add(new Restriction<Rank>("rank", MatchMode.EXACT, Rank.GENUS()));
                restrictions.add(new Restriction<String>("genusOrUninomial", MatchMode.EXACT, name.getGenusOrUninomial()));
                List<TaxonName> names = nameDao.list(TaxonName.class, restrictions , 2, 0, null, null);
                if(names.size() == 0){
                    logger.error("Genus entity missing for \"" + name.getGenusOrUninomial() + "\"");
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
                List<Restriction<?>> restrictions = new ArrayList<>();
                restrictions.add(new Restriction<Rank>("rank", MatchMode.EXACT, Rank.SPECIES()));
                restrictions.add(new Restriction<String>("specificEpithet", MatchMode.EXACT, name.getSpecificEpithet()));
                restrictions.add(new Restriction<String>("genusOrUninomial", MatchMode.EXACT, name.getGenusOrUninomial()));
                List<TaxonName> names = nameDao.list(TaxonName.class, restrictions , 2, 0, null, null);
                if(names.size() == 0){
                    logger.error("Genus entity missing for \"" + name.getGenusOrUninomial() + " " + name.getSpecificEpithet() + "\"");
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
     * @param taxonName
     * @return
     * @throws TaxonGraphException
     */
    protected Taxon assureSingleTaxon(TaxonName taxonName) throws TaxonGraphException {

        // TODO check algo regarding deleted names
        TaxonName taxonNamePersisted = taxonService.getSession().load(TaxonName.class, taxonName.getId());
        Taxon taxon;
        if(taxonName.getTaxa().size() == 0){
            Reference secRef = referenceService.load(secReferenceUUID);
            taxon = Taxon.NewInstance(taxonNamePersisted, secRef);
            taxonService.saveOrUpdate(taxon);
        } else if(taxonName.getTaxa().size() == 1){
            taxon = taxonName.getTaxa().iterator().next();
            if(!secReferenceUUID.equals(taxon.getSec().getUuid())){
                throw new TaxonGraphException("The taxon for a name to be used in a taxon graph must have the default sec reference [secRef uuid: "+ secReferenceUUID.toString() +"]");
            }
        } else {
            for(Taxon t : taxonName.getTaxa()){
                if(secReferenceUUID.equals(t.getSec().getUuid())){
                    taxon = t;
                }
            }
            throw new TaxonGraphException("A name to be used in a taxon graph must only have one taxon with the default sec reference [secRef uuid: "+ secReferenceUUID.toString() +"]");
        }
        return taxon;
    }

    /**
     * @return the Reference entity for the publication
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

    @Override
    public List<TaxonGraphEdgeDTO> edges(TaxonName fromName, TaxonName toName, boolean includeUnpublished) throws TaxonGraphException{
        Taxon fromTaxon = assureSingleTaxon(fromName);
        Taxon toTaxon = assureSingleTaxon(toName);
        return taxonDao.getTaxonGraphEdgeDTOs(fromTaxon.getUuid(), toTaxon.getUuid(), RELTYPE, includeUnpublished);
    }

    @Override
    public List<TaxonGraphEdgeDTO> edges(UUID fromtaxonUuid, UUID toTaxonUuid, boolean includeUnpublished) throws TaxonGraphException{
        return taxonDao.getTaxonGraphEdgeDTOs(fromtaxonUuid, toTaxonUuid, RELTYPE, includeUnpublished);
    }

    /**
     * @param taxon
     */
    protected List<TaxonRelationship> taxonGraphRelationsFrom(Taxon taxon, Reference citation) {
        // TODO optimize by creating filterable listToTaxonRelationships method
        List<TaxonRelationship> relations = taxonService.listToTaxonRelationships(taxon, RELTYPE, true, null, null, null, null);
        List<TaxonRelationship> includedInRelations = new ArrayList<>();
        for(TaxonRelationship taxonRel : relations){
            if(citation.equals(taxonRel.getCitation())){
                includedInRelations.add(taxonRel);
            }
        }
        return includedInRelations;
    }

    /**
     * @param taxon
     */
    protected List<TaxonRelationship> taxonGraphRelationsTo(Taxon taxon, Reference citation) {
        // TODO optimize by creating filterable listToTaxonRelationships method
        List<TaxonRelationship> relations = taxonService.listFromTaxonRelationships(taxon, RELTYPE, true, null, null, null, null);
        List<TaxonRelationship> includedInRelations = new ArrayList<>();
        for(TaxonRelationship taxonRel : relations){
            if(citation.equals(taxonRel.getCitation())){
                includedInRelations.add(taxonRel);
            }
        }
        return includedInRelations;
    }
}
