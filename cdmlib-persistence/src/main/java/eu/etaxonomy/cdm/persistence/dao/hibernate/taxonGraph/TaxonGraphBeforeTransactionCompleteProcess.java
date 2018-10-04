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

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;

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
 * @since Sep 26, 2018
 *
 */
public class TaxonGraphBeforeTransactionCompleteProcess implements BeforeTransactionCompletionProcess {

    private TaxonRelationshipType relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();

    private String[] NAMEPARTS_OR_RANK_PROPS = new String[]{"genusOrUninomial", "specificEpithet", "rank"};
    private String[] NOMREF_PROP = new String[]{"nomenclaturalReference"};

    private EnumSet<ReferenceType> referenceSectionTypes = EnumSet.of(ReferenceType.Section, ReferenceType.BookSection);

    private static final Logger logger = Logger.getLogger(TaxonGraphBeforeTransactionCompleteProcess.class);

    /**
     * MUST ONLY BE USED IN TESTS
     */
    @Deprecated
    private UUID secReferenceUUID;

    private Reference secReference = null;

    private Session temporarySession;

    private Level origLoggerLevel;

    private Session parentSession;

    private TaxonName entity;

    private String[] propertyNames;

    private Object[] oldState;

    private Object[] state;

    private boolean isInsertEvent;

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

    protected TaxonRelationshipType relType() {
        if(relType == null){
            relType = TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN();
        }
        return relType;
    }

    public TaxonGraphBeforeTransactionCompleteProcess(PostUpdateEvent event){
        entity = (TaxonName) event.getEntity();
        propertyNames = event.getPersister().getPropertyNames();
        oldState = event.getOldState();
        state = event.getState();
    }

    public TaxonGraphBeforeTransactionCompleteProcess(PostInsertEvent event){
        isInsertEvent = true;
        entity = (TaxonName) event.getEntity();
        propertyNames = event.getPersister().getPropertyNames();
        oldState = null;
        state = event.getState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doBeforeTransactionCompletion(SessionImplementor session) {

        try {
            if(isInsertEvent){
                createTempSession(session);
                onNewTaxonName(entity);
                temporarySession.flush();
            } else {
                if(checkStateChange(NAMEPARTS_OR_RANK_PROPS) > -1){
                    createTempSession(session);
                    onNameOrRankChange(entity);
                    temporarySession.flush();
                }
                int changedNomRefIndex = checkStateChange(NOMREF_PROP);
                if(changedNomRefIndex > -1){
                    createTempSession(session);
                    onNomReferenceChange(entity, (Reference)oldState[changedNomRefIndex]);
                    temporarySession.flush();
                }
            }
      } catch (TaxonGraphException e) {
          throw new HibernateException(e);
      }
      finally {
          if (temporarySession != null ) {
              // temporarySession.close(); // no need to close the session since the session is configured for auto close, see createTempSession()
              if(origLoggerLevel != null){
                  Logger.getLogger("org.hibernate.SQL").setLevel(origLoggerLevel);
              }
          }
      }

    }

    private int checkStateChange(String[] propertyNamesToCheck){

        if(oldState == null){
            return -1;
        }
        int propsCheckedCnt = 0;
        for(int i = 0; i < propertyNames.length; i++){
            if(ArrayUtils.contains(propertyNamesToCheck, propertyNames[i])){
                propsCheckedCnt++;
                if(!oldState[i].equals(state[i])){
                    return i;
                }
                if(propsCheckedCnt == propertyNamesToCheck.length){
                    return -1;
                }
            }
        }
        // this exception should be raised during the unit tests already and thus will never occur in production
        throw new RuntimeException("TaxonName class misses at least one property of: " + ArrayUtils.toString(propertyNamesToCheck));
    }

    /**
     * Concept of creation of sub-sessions found in AuditProcess.doBeforeTransactionCompletion(SessionImplementor session)
     * and adapted to make it work for this case.
     *
     * @param session
     */
    protected void createTempSession(SessionImplementor session) {
        if(temporarySession == null){
            parentSession = (Session)session;
            temporarySession = ((Session) session)
                      .sessionWithOptions().transactionContext()
                      // in contrast to AuditProcess.doBeforeTransactionCompletion we need the session to close automatically,
                      // otherwise the hibernate search indexer will suffer from LazyInitializationExceptions
                      .autoClose(true)
                      // in contrast to AuditProcess.doBeforeTransactionCompletion, the ConnectionReleaseMode.AFTER_TRANSACTION causes problems for us:
                      //.connectionReleaseMode( ConnectionReleaseMode.AFTER_TRANSACTION )
                      .openSession();
//            origLoggerLevel = Logger.getLogger("org.hibernate.SQL").getLevel();
//            Logger.getLogger("org.hibernate.SQL").setLevel(Level.TRACE);
        }
    }

    public void onNewTaxonName(TaxonName taxonName) throws TaxonGraphException {
        onNameOrRankChange(taxonName);
    }


    public void onNameOrRankChange(TaxonName taxonName) throws TaxonGraphException {
        Taxon taxon = assureSingleTaxon(taxonName);
        boolean isNotDeleted = parentSession.contains(taxonName) && taxonName.isPersited();
        // TODO use audit event to check for deletion?
        if(isNotDeleted){
            updateEdges(taxon);
            temporarySession.saveOrUpdate(taxon);
        }
    }


    public void onNomReferenceChange(TaxonName taxonName, Reference oldNomReference) throws TaxonGraphException {
        if(oldNomReference == null){
            onNewTaxonName(taxonName);
        }
        Taxon taxon = assureSingleTaxon(taxonName);
        boolean isNotDeleted = parentSession.contains(taxonName) && taxonName.isPersited();
        // TODO use audit event to check for deletion?
        if(isNotDeleted){
            updateConceptReferenceInEdges(taxon, oldNomReference);
            temporarySession.saveOrUpdate(taxon);
        }
    }

    /**
     * @param taxonName
     */
    private void updateEdges(Taxon taxon) throws TaxonGraphException {

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
     * @param taxon
     */
    private void updateConceptReferenceInEdges(Taxon taxon, Reference oldNomReference) throws TaxonGraphException {

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
    protected void updateReferenceInEdges(Taxon taxon, Reference conceptReference, Reference oldConceptReference) {
        List<TaxonRelationship> relations = taxonGraphRelationsFrom(taxon, oldConceptReference);
        List<TaxonName> relatedHigherNames = relatedHigherNames(taxon.getName());
        for(TaxonRelationship rel : relations){
            boolean isRelToHigherName = relatedHigherNames.contains(rel.getToTaxon().getName());
            if(isRelToHigherName){
                rel.setCitation(conceptReference);
                temporarySession.saveOrUpdate(rel);
            }
        }
    }

    private List<TaxonName> listNames(Rank rank, String genusOrUninomial, String specificEpithet){
        String hql = "SELECT n FROM TaxonName n WHERE n.rank = :rank AND n.genusOrUninomial = :genusOrUninomial";
        if(specificEpithet != null){
            hql += " AND n.specificEpithet = :specificEpithet";
        }
        Query q = temporarySession.createQuery(hql);

        q.setParameter("rank", rank);
        q.setParameter("genusOrUninomial", genusOrUninomial);
        if(specificEpithet != null){
            q.setParameter("specificEpithet", specificEpithet);
        }

        List<TaxonName> result = q.list();
        return result;
    }

    private List<TaxonRelationship> getTaxonRelationships(Taxon relatedTaxon, TaxonRelationshipType type, Reference citation, Direction direction){
        String hql = "SELECT rel FROM TaxonRelationship rel WHERE rel."+direction+" = :relatedTaxon AND rel.type = :type AND rel.citation = :citation";
        Query q = temporarySession.createQuery(hql);
        q.setParameter("relatedTaxon", relatedTaxon);
        q.setParameter("type", type);
        q.setParameter("citation", citation);
        List<TaxonRelationship> rels = q.list();
        return rels;
    }

    /**
     * @param name
     * @return
     */
    private List<TaxonName> relatedHigherNames(TaxonName name) {

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

    private Reference secReference(){
        if(secReference == null){
            Query q = temporarySession.createQuery("SELECT r FROM Reference r WHERE r.uuid = :uuid");
            q.setParameter("uuid", getSecReferenceUUID());
            secReference = (Reference) q.uniqueResult();
        }
        return secReference;
    }

    /**
     * FIXME: this is a duplicate implementation of the same method as in {@link TaxonGraphDaoHibernateImpl}
     *
     * @param taxonName
     * @return
     * @throws TaxonGraphException
     */
    public Taxon assureSingleTaxon(TaxonName taxonName) throws TaxonGraphException {

        UUID secRefUuid = getSecReferenceUUID();
        TaxonName taxonNamePersisted = temporarySession.load(TaxonName.class, taxonName.getId());
        Taxon taxon;
        if(taxonName.getTaxa().size() == 0){
            if(taxonNamePersisted != null){
            Reference secRef = secReference();
                taxon = Taxon.NewInstance(taxonNamePersisted, secRef);
                temporarySession.saveOrUpdate(taxon);
                return taxon;
            } else {
                throw new TaxonGraphException("Can't create taxon for deleted name: " + taxonName);
            }
        } else if(taxonName.getTaxa().size() == 1){
            taxon = taxonName.getTaxa().iterator().next();
            if(taxon.getSec() == null){
                taxon = temporarySession.load(Taxon.class, taxon.getId());
                taxon.setSec(secReference);
                temporarySession.saveOrUpdate(taxon);
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
        return temporarySession.load(Taxon.class, taxon.getId());
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
}
