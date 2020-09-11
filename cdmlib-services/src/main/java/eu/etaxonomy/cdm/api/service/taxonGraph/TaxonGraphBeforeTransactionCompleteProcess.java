/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.taxonGraph;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;

import eu.etaxonomy.cdm.api.application.IRunAs;
import eu.etaxonomy.cdm.config.CdmHibernateListenerConfiguration;
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

    private String[] NAMEPARTS_OR_RANK_PROPS = new String[]{"genusOrUninomial", "specificEpithet", "rank"};
    private String NOMREF_PROP = "nomenclaturalSource.citation";

    private Session temporarySession;

    private Level origLoggerLevel;

    private Session parentSession;

    private TaxonName entity;

    private String[] propertyNames;

    private Object[] oldState;

    private Object[] state;

    private boolean isInsertEvent;

    private IRunAs runAs = null;

    public TaxonGraphBeforeTransactionCompleteProcess(PostUpdateEvent event, IRunAs runAs){
        entity = (TaxonName) event.getEntity();
        propertyNames = event.getPersister().getPropertyNames();
        oldState = event.getOldState();
        state = event.getState();
        this.runAs = runAs;
    }

    public TaxonGraphBeforeTransactionCompleteProcess(PostInsertEvent event, IRunAs runAs){
        isInsertEvent = true;
        entity = (TaxonName) event.getEntity();
        propertyNames = event.getPersister().getPropertyNames();
        oldState = null;
        state = event.getState();
        this.runAs = runAs;
    }

    @Override
    public void doBeforeTransactionCompletion(SessionImplementor session) {

        try {
            if(isInsertEvent){
                createTempSession(session);
                onNewTaxonName(entity);
                getSession().flush();
            } else {
                if(checkStateChange(NAMEPARTS_OR_RANK_PROPS) > -1){
                    createTempSession(session);
                    onNameOrRankChange(entity);
                    getSession().flush();
                }
                int changedNomRefIndex = checkStateChangeNomRef(NOMREF_PROP);
                if(changedNomRefIndex > -1){
                    createTempSession(session);
                    onNomReferenceChange(entity, (Reference)oldState[changedNomRefIndex]);
                    getSession().flush();
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

    private int checkStateChange(String[] propertyNamesToCheck){

        if(oldState == null){
            return -1;
        }
        int propsCheckedCnt = 0;
        for(int i = 0; i < propertyNames.length; i++){
            if(ArrayUtils.contains(propertyNamesToCheck, propertyNames[i])){
                propsCheckedCnt++;
                if(!Objects.equals(oldState[i], state[i])){
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

    private int checkStateChangeNomRef(String propertyPath){

        if(oldState == null){
            return -1;
        }

        try {
            String[] path = propertyPath.split("\\.");
            for (int i=1; i < propertyNames.length; i++) {
                if(propertyNames[i].equals(path[0])){
                    if (oldState[i] == null && state[i] == null){
                        return -1;
                    }else{
                        //TODO make it recursive (until now only a 2 step path is allowed, but should be enough for the given use-case
                        Object oldStatePathObj = (oldState[i]==null) ? null: PropertyUtils.getProperty(oldState[i], path[1]);
                        Object newStatePathObj = (state[i]==null) ? null: PropertyUtils.getProperty(state[i], path[1]);
                        if (oldStatePathObj == null && newStatePathObj == null){
                            return -1;
                        }else{
                            if(!Objects.equals(oldStatePathObj, newStatePathObj)){
                                return i;
                            }else{
                                return -1;
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("The nomenclatural reference path seems to be invalid: " + propertyPath);
        }
        // this exception should be raised during the unit tests already and thus will never occur in production
        throw new RuntimeException("TaxonName class misses at least one property of: " + propertyPath);
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
     * This method manages updates to the edges from and to a taxon which
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
     * @return
     */
    @Override
    public Session getSession() {
        return temporarySession;
    }


}
