/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.taxonGraph;

import java.util.Objects;

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
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph.AbstractHibernateTaxonGraphProcessor;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;

/**
 * see https://dev.e-taxonomy.eu/redmine/issues/7648
 *
 * @author a.kohlbecker
 * @since Sep 26, 2018
 *
 */
public class TaxonGraphBeforeTransactionCompleteProcess
        extends AbstractHibernateTaxonGraphProcessor
        implements BeforeTransactionCompletionProcess {

    private static final Logger logger = Logger.getLogger(TaxonGraphBeforeTransactionCompleteProcess.class);

    private String[] NAMEPARTS_OR_RANK_PROPS = new String[]{"genusOrUninomial", "specificEpithet", "rank"};
    private String[] NOMREF_PROP = new String[]{"nomenclaturalSource.citation"};

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

    /**
     * {@inheritDoc}
     */
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
                int changedNomRefIndex = checkStateChange(NOMREF_PROP);
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

    /**
     * Concept of creation of sub-sessions found in AuditProcess.doBeforeTransactionCompletion(SessionImplementor session)
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

    public void onNewTaxonName(TaxonName taxonName) throws TaxonGraphException {
        onNameOrRankChange(taxonName);
    }

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
