/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.taxonGraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PreDeleteEvent;

import eu.etaxonomy.cdm.api.application.IRunAs;
import eu.etaxonomy.cdm.config.CdmHibernateListenerConfiguration;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph.AbstractHibernateTaxonGraphProcessor;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.hibernate.TaxonGraphHibernateListener;

/**
 * A {@link BeforeTransactionCompletionProcess} implementation which manages
 * graphs of taxon relationships automatically. The graph consists of nodes
 * ({@link Taxon}) and edges ({@link TaxonRelationship}) where the taxa all have
 * the same {@link Taxon#getSec() sec reference}. The concept reference of a
 * classification is being projected onto the edge
 * ({@link TaxonRelationship}) having that reference as
 * {@link TaxonRelationship#getSource() TaxonRelationship.source.citation}.
 * <p>
 * The conceptual idea for the resulting graph is described in <a href=
 * "https://dev.e-taxonomy.eu/redmine/issues/6173#6-N1T-Higher-taxon-graphs-with-includedIn-relations-taxon-relationships">#6173
 * 6) [N1T] Higher taxon-graphs with includedIn relations taxon
 * relationships}</a> The
 * <code>TaxonGraphBeforeTransactionCompleteProcess</code> is instantiated and
 * used in the {@link TaxonGraphHibernateListener}
 * <p>
 * To activate this <code>BeforeTransactionCompletionProcess</code> class it needs to be registered at the
 * {@link TaxonGraphHibernateListener} like:
 * {@code taxonGraphHibernateListener.registerProcessClass(TaxonGraphBeforeTransactionCompleteProcess.class); }
 * <p>
 * On insert, update and delete events a new temporary session is being created
 * ({@link {@link #createTempSession(SessionImplementor)} to create, remove or
 * modify the nodes ({@link Taxon}) and edges ({@link TaxonRelationship}) of the
 * graph. The events on which the graph is modified are (see method descriptions
 * for more details on the processes being performed):
 * <ul>
 * <li>Change of a name or a names rank
 * ({@link #onNameOrRankChange(TaxonName)})</li>
 * <li>Creation of a new name ({@link #onNewTaxonName(TaxonName)})</li>
 * <li>Change of a nomenclatural reference
 * ({@link #onNomReferenceChange(TaxonName, Reference)})</li>
 *
 *
 * @see {@link TaxonGraphHibernateListener}
 * @see {@link CdmHibernateListenerConfiguration}
 * @see <a href=
 *      "https://dev.e-taxonomy.eu/redmine/issues/7648">https://dev.e-taxonomy.eu/redmine/issues/7648</a>
 *
 * @author a.kohlbecker
 * @since Sep 26, 2018
 */
public class TaxonGraphBeforeTransactionCompleteProcess
        extends AbstractHibernateTaxonGraphProcessor
        implements BeforeTransactionCompletionProcess {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TaxonGraphBeforeTransactionCompleteProcess.class);

    private static final String[] TAXONNAME_NAMEPARTS_OR_RANK_PROPS = new String[]{"genusOrUninomial", "specificEpithet", "rank"};
    private static final String[] TAXONNAME_NOMENCLATURALSOURCE = new String[]{"nomenclaturalSource"};
    private static final String NOMENCLATURALSOURCE_CITATION = "citation";
    private static final String NOMENCLATURALSOURCE_SOURCEDNAME = "sourcedName";
    private static final String[] CITATION_OR_SOURCEDNAME = new String[] {NOMENCLATURALSOURCE_CITATION, NOMENCLATURALSOURCE_SOURCEDNAME};

    private static boolean failOnMissingNomRef = false;

    /**
     * @return the failOnMissingNomRef
     */
    public static boolean isFailOnMissingNomRef() {
        return failOnMissingNomRef;
    }

    /**
     * @param failOnMissingNomRef the failOnMissingNomRef to set
     */
    public static void setFailOnMissingNomRef(boolean failOnMissingNomRef) {
        TaxonGraphBeforeTransactionCompleteProcess.failOnMissingNomRef = failOnMissingNomRef;
    }

    private Session temporarySession;

    private Level origLoggerLevel;

    private Session parentSession;

    private TaxonName taxonName;

    private NomenclaturalSource nomenclaturalSource;

    private String[] propertyNames;

    private Object[] oldState;

    private Object[] state;

    private int[] dirtyProperties;

    private EventType eventType;

    enum EventType {
        INSERT, UPDATE, DELETE,
    }

    private IRunAs runAs = null;

    public TaxonGraphBeforeTransactionCompleteProcess(PostUpdateEvent event, IRunAs runAs){
        eventType = EventType.UPDATE;
        if(event.getEntity() instanceof TaxonName) {
            taxonName = (TaxonName) event.getEntity();
        } else
        if(event.getEntity() instanceof NomenclaturalSource) {
            nomenclaturalSource = (NomenclaturalSource) event.getEntity();
        } else {
            throw new RuntimeException("Either " + TaxonName.class.getName() + " or " + NomenclaturalSource.class.getName() + " are accepted");
        }
        propertyNames = event.getPersister().getPropertyNames();
        dirtyProperties = event.getDirtyProperties();
        oldState = event.getOldState();
        state = event.getState();
        this.runAs = runAs;
    }

    public TaxonGraphBeforeTransactionCompleteProcess(PostInsertEvent event, IRunAs runAs) {
        eventType = EventType.INSERT;
        if(event.getEntity() instanceof TaxonName) {
            taxonName = (TaxonName) event.getEntity();
        } else
        if(event.getEntity() instanceof NomenclaturalSource) {
            nomenclaturalSource = (NomenclaturalSource) event.getEntity();
        } else {
            throw new RuntimeException("Either " + TaxonName.class.getName() + " or " + NomenclaturalSource.class.getName() + " are accepted");
        }
        propertyNames = event.getPersister().getPropertyNames();
        dirtyProperties = null;
        oldState = null;
        state = event.getState();
        this.runAs = runAs;
    }

    public TaxonGraphBeforeTransactionCompleteProcess(PreDeleteEvent event, IRunAs runAs) {
        eventType = EventType.DELETE;
        if(event.getEntity() instanceof TaxonName) {
            taxonName = (TaxonName) event.getEntity();
        } else
        if(event.getEntity() instanceof NomenclaturalSource) {
            nomenclaturalSource = (NomenclaturalSource) event.getEntity();
        } else {
            throw new RuntimeException("Either " + TaxonName.class.getName() + " or " + NomenclaturalSource.class.getName() + " are accepted");
        }
        propertyNames = event.getPersister().getPropertyNames();
        dirtyProperties = null;
        oldState = event.getDeletedState();
        this.runAs = runAs;
    }

    @Override
    public void doBeforeTransactionCompletion(SessionImplementor session) {

        if(logger.isDebugEnabled()) {
            String message = eventType.name() + " for ";
            message += taxonName != null ? taxonName.toString() : "";
            message += nomenclaturalSource != null ? nomenclaturalSource.toString() : "";
            if(eventType.equals(EventType.UPDATE)){
                message += " with dirty properties: " + Arrays.stream(dirtyProperties).mapToObj(i -> propertyNames[i]).collect(Collectors.joining(", "));
            }
            logger.debug(message);
        }

        try {
            if(eventType.equals(EventType.INSERT)){
                // ---- INSERT ----
                if(taxonName != null) {
                    // 1. do the sanity checks first
                    if(taxonName.getNomenclaturalSource() == null || taxonName.getNomenclaturalSource().getCitation() == null) {
                        if(failOnMissingNomRef) {
                            throw new TaxonGraphException("TaxonName.nomenclaturalSource or TaxonName.nomenclaturalSource.citation must never be null.");
                        } else {
                            logger.warn("TaxonName.nomenclaturalSource or TaxonName.nomenclaturalSource.citation must never be null. (" + taxonName.toString() + ")");
                        }
                    }
                    createTempSession(session);
                    onNewTaxonName(taxonName);
                    getSession().flush();
                } else if(nomenclaturalSource != null) {
                    TaxonName taxonName =  (TaxonName)findValueByName(state, NOMENCLATURALSOURCE_SOURCEDNAME);
                    Reference reference =  (Reference)findValueByName(state, NOMENCLATURALSOURCE_CITATION);
                    if(taxonName != null && reference != null) {
                        createTempSession(session);
                        // load name and reference also into this session
                        taxonName = getSession().load(TaxonName.class, taxonName.getId());
                        reference = getSession().load(Reference.class, reference.getId());
                        onNewNomenClaturalSource(taxonName, reference);
                        getSession().flush();
                    }
                }
            } else if(eventType.equals(EventType.DELETE)) {
                if(taxonName != null) {
                    // handling this case explicitly should not be needed as this is expectd to be done by orphan removal in
                    // hibernate
                    Reference reference =  (Reference)oldState[Arrays.binarySearch(propertyNames, TAXONNAME_NOMENCLATURALSOURCE)];
                    if(reference != null) {
                        createTempSession(session);
                        onTaxonNameDeleted(taxonName, reference);
                        getSession().flush();
                    }
                } else if(nomenclaturalSource != null) {
                   TaxonName taxonName =  (TaxonName)findValueByName(oldState, NOMENCLATURALSOURCE_SOURCEDNAME);
                   Reference reference =  (Reference)findValueByName(oldState, NOMENCLATURALSOURCE_CITATION);
                   if(taxonName != null && reference != null) {
                       createTempSession(session);
                       onNomReferenceRemoved(taxonName, reference);
                       getSession().flush();
                   }
                }

            } else {
                // ---- UPDATE ----
                // either taxonName or nomenclaturalSource not null, never both!
                if(taxonName != null) {
                    // 1. do the sanity checks first
                    Map<String, PropertyStateChange> changedNomenclaturalSourceProp = checkStateChange(TAXONNAME_NOMENCLATURALSOURCE);
                    if(!changedNomenclaturalSourceProp.isEmpty()) {
                         if(changedNomenclaturalSourceProp.get(TAXONNAME_NOMENCLATURALSOURCE[0]).newState == null) {
                             throw new TaxonGraphException("TaxonName.nomenclaturalSource must never be reverted to null.");
                         }
                         if(((NomenclaturalSource)changedNomenclaturalSourceProp.get(TAXONNAME_NOMENCLATURALSOURCE[0]).newState).getCitation() == null){
                             throw new TaxonGraphException("TaxonName.nomenclaturalSource.citation must never be reverted to null.");
                         }
                         createTempSession(session);
                         NomenclaturalSource oldNomenclaturalSource = (NomenclaturalSource)changedNomenclaturalSourceProp.get(TAXONNAME_NOMENCLATURALSOURCE[0]).oldState;
                         onNomReferenceChange(taxonName, oldNomenclaturalSource.getCitation());
                         getSession().flush();
                    }
                    // 2. update the graph
                    Map<String, PropertyStateChange> changedProps = checkStateChange(TAXONNAME_NAMEPARTS_OR_RANK_PROPS);
                    if(!changedProps.isEmpty()){
                        createTempSession(session);
                        onNameOrRankChange(taxonName);
                        getSession().flush();
                    }
                } else
                if(nomenclaturalSource != null) {
                    Map<String, PropertyStateChange> changedProps = checkStateChange(CITATION_OR_SOURCEDNAME);
                    if(!changedProps.isEmpty()){
                        TaxonName newTaxonNameState = null;
                        Reference newCitationState = null;
                        TaxonName oldTaxonNameState = null;
                        Reference oldCitationState = null;
                        if(changedProps.containsKey(NOMENCLATURALSOURCE_SOURCEDNAME)) {
                            newTaxonNameState = (TaxonName) changedProps.get(NOMENCLATURALSOURCE_SOURCEDNAME).newState;
                            oldTaxonNameState = (TaxonName) changedProps.get(NOMENCLATURALSOURCE_SOURCEDNAME).oldState;
                        }
                        if(changedProps.containsKey(NOMENCLATURALSOURCE_CITATION)) {
                            newCitationState = (Reference) changedProps.get(NOMENCLATURALSOURCE_CITATION).newState;
                            oldCitationState = (Reference) changedProps.get(NOMENCLATURALSOURCE_CITATION).oldState;
                        }
                        // 1. do the sanity checks first
                        if(oldTaxonNameState != null && oldTaxonNameState.getNomenclaturalSource() == null) {
                            createTempSession(session);
                            onNomReferenceChange(oldTaxonNameState, null);
                            getSession().flush();
                        }
                        // 2. update the graph
                        if(newTaxonNameState != null && newCitationState == null) {
                            createTempSession(session);
                            onNomReferenceChange(newTaxonNameState, nomenclaturalSource.getCitation());
                            getSession().flush();
                        }
                        if(newTaxonNameState == null && newCitationState != null) {
                            createTempSession(session);
                            onNomReferenceChange(nomenclaturalSource.getSourcedName(), oldCitationState);
                            getSession().flush();
                        }
                    }
                }
            }
      } catch (TaxonGraphException e) {
          throw new HibernateException(e);
      }
      finally {
          if (getSession() != null ) {
              // temporarySession.close(); // no need to close the session since the session is configured for auto close, see createTempSession()
              if(origLoggerLevel != null){
                  Logger.getLogger("org.hibernate.SQL").setLevel(origLoggerLevel);
              }
          }
      }
    }

    private Map<String, PropertyStateChange> checkStateChange(String[] propertyNamesToCheck){

        Map<String, PropertyStateChange> changedProps = new HashMap<>();

        if(dirtyProperties == null){
            return changedProps;
        }
        for(int i : dirtyProperties){
            if(ArrayUtils.contains(propertyNamesToCheck, propertyNames[i])){

                if(!Objects.equals(oldState[i], state[i])){
                    // here we check again, this should not be needed as we should
                    // be able to rely on that this check is true for all
                    // indices in dirtyProperties
                    changedProps.put(propertyNames[i], (new PropertyStateChange(oldState[i], state[i], i)));
                }
            }
        }
        return changedProps;
    }

    private Object findValueByName(Object[] states, String propertyName) {
        int key = -1;
        for(int i = 0; i < propertyNames.length; i++) {
            if(propertyNames[i].equals(propertyName)) {
                key = i;
                break;
            }
        }
        if(key > -1) {
            return states[key];
        }
        return null;
    }

    /**
     * Concept of creation of sub-sessions found in
     * AuditProcess.doBeforeTransactionCompletion(SessionImplementor session)
     * and adapted to make it work for this case.
     *
     * @param session
     */
    protected void createTempSession(SessionImplementor session) {
        if(getSession() == null){
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

    /**
     * Same as {@link #onNameOrRankChange(TaxonName)}
     */
    public void onNewTaxonName(TaxonName taxonName) throws TaxonGraphException {
        onNameOrRankChange(taxonName);
    }

    /**
     * Create a taxon with graph sec reference {@link #secReference()) for the
     * <code>taxonName</code> if not yet existing and updates the edges
     * from and to this taxon by creating missing and removing obsolete ones.
     *
     * @throws TaxonGraphException
     *             A <code>TaxonGraphException</code> is thrown when more than
     *             one taxa with the default sec reference
     *             ({@link #getSecReferenceUUID()}) are found for the given name
     *             (<code>taxonName</code>). Originates from
     *             {@link #assureSingleTaxon(TaxonName, boolean)}
     */
    public void onNameOrRankChange(TaxonName taxonName) throws TaxonGraphException {
        boolean isNotDeleted = parentSession.contains(taxonName) && taxonName.isPersited();
        // TODO use audit event to check for deletion?
        if(isNotDeleted){
            try{
                if(runAs != null){
                    runAs.apply();
                }
                Taxon taxon = assureSingleTaxon(taxonName);
                updateEdges(taxon);
                getSession().saveOrUpdate(taxon);
            } finally {
                if(runAs != null){
                    runAs.restore();
                }
            }
        }
    }

    /**
     * This method manages updates to edges from and to a taxon which
     * reflects the concept reference of the original classification. The
     * concept reference of each classification is being projected onto the
     * graph as edge ({@link TaxonRelationship}) having that reference as
     * {@link TaxonRelationship#getSource() TaxonRelationship.source.citation}.
     * <p>
     * Delegates to {@link #onNewTaxonName(TaxonName)} in case the
     * <code>oldNomReference</code> is <code>null</code>. Otherwise the
     * {@link #assureSingleTaxon(TaxonName)} check is performed to create the
     * taxon if missing and the concept reference in the edges
     * <code>source</code> field is finally updated.
     *
     * @param taxonName
     *   The updated taxon name having a new nomenclatural reference
     * @param oldNomReference
     *   The nomenclatural reference as before the update of the <code>taxonName</code>
     * @throws TaxonGraphException
     *             A <code>TaxonGraphException</code> is thrown when more than
     *             one taxa with the default sec reference
     *             ({@link #getSecReferenceUUID()}) are found for the given name
     *             (<code>taxonName</code>). Originates from
     *             {@link #assureSingleTaxon(TaxonName, boolean)}
     */
    public void onNomReferenceChange(TaxonName taxonName, Reference oldNomReference) throws TaxonGraphException {
        if(oldNomReference == null){
            onNewTaxonName(taxonName);
        }
        boolean isNotDeleted = parentSession.contains(taxonName) && taxonName.isPersited();
        // TODO use audit event to check for deletion?
        if(isNotDeleted){
            try {
                if(runAs != null){
                    runAs.apply();
                }
                Taxon taxon = assureSingleTaxon(taxonName);
                updateConceptReferenceInEdges(taxon, oldNomReference);
                getSession().saveOrUpdate(taxon);
            } finally {
                if(runAs != null){
                    runAs.restore();
                }
            }
        }
    }

    /**
     * This method manages updates to edges from and to a taxon which
     * reflects the concept reference of the original classification. The
     * concept reference of each classification is being projected onto the
     * graph as edge ({@link TaxonRelationship}) having that reference as
     * {@link TaxonRelationship#getSource() TaxonRelationship.source.citation}.
     *
     *
     * @param taxonName
     *   The updated taxon name having a new nomenclatural reference
     * @param nomReference
     *   The new nomenclatural reference to be assigned to the <code>taxonName</code>
     * @throws TaxonGraphException
     *             A <code>TaxonGraphException</code> is thrown when more than
     *             one taxa with the default sec reference
     *             ({@link #getSecReferenceUUID()}) are found for the given name
     *             (<code>taxonName</code>). Originates from
     *             {@link #assureSingleTaxon(TaxonName, boolean)}
     */
    public void onNewNomenClaturalSource(TaxonName taxonName, Reference nomReference) throws TaxonGraphException {

        try {
            if(runAs != null){
                runAs.apply();
            }
            Taxon taxon = assureSingleTaxon(taxonName);
            updateEdges(taxon, nomReference);
            getSession().saveOrUpdate(taxon);
        } finally {
            if(runAs != null){
                runAs.restore();
            }
        }
    }

    /**
     * Manages removals of references which require the deletion of edges from and to
     * the taxon which reflects the concept reference of the original classification.
     *
     * @param taxonName
     *   The taxon name which formerly had the the nomenclatural reference
     * @param oldNomReference
     *   The nomenclatural reference which was removed from the <code>taxonName</code>
     * @throws TaxonGraphException
     */
    public void onNomReferenceRemoved(TaxonName taxonName, Reference oldNomReference) throws TaxonGraphException {

        boolean isNotDeleted = parentSession.contains(taxonName) && taxonName.isPersited();
        // TODO use audit event to check for deletion?
        if(isNotDeleted){
            try {
                if(runAs != null){
                    runAs.apply();
                }
                Taxon taxon = assureSingleTaxon(taxonName, false);
                if(taxon != null) {
                    removeEdges(taxon, oldNomReference);
                }
                getSession().saveOrUpdate(taxon);
            } finally {
                if(runAs != null){
                    runAs.restore();
                }
            }
        }
    }

    /**
     * Manages deletions of taxonNames which requires the deletion of edges and the
     * taxon which reflects the concept reference of the original classification.
     *
     * @param taxonName
     *   The taxon name which formerly had the the nomenclatural reference
     * @param oldNomReference
     *   The nomenclatural reference which was removed from the <code>taxonName</code>
     * @throws TaxonGraphException
     */
    private void onTaxonNameDeleted(TaxonName taxonName, Reference reference) throws TaxonGraphException {

        try {
            if(runAs != null){
                runAs.apply();
            }
            Taxon taxon = assureSingleTaxon(taxonName, false);
            if(taxon != null) {
                removeEdges(taxon, reference);
                getSession().delete(taxon);
            }
            getSession().saveOrUpdate(taxon);
        } finally {
            if(runAs != null){
                runAs.restore();
            }
        }
    }


    /**
     * @return
     */
    @Override
    public Session getSession() {
        return temporarySession;
    }

    public static class PropertyStateChange {

        Object oldState;
        Object newState;
        int index;
        /**
         * @param oldState
         * @param newState
         * @param index
         */
        public PropertyStateChange(Object oldState, Object newState, int index) {
            super();
            this.oldState = oldState;
            this.newState = newState;
            this.index = index;
        }



    }


}
