/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmApplication;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.VocabularyFilter;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author a.mueller
 * @since 17.09.2021
 * IN WORK
 */
@Component
public class Cdm2CdmVocabularyImport
        extends Cdm2CdmImportBase {

    private static final long serialVersionUID = 3995116783196060465L;
    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void doInvoke(Cdm2CdmImportState state) {
        IProgressMonitor monitor = state.getConfig().getProgressMonitor();
        doData(state);
    }

    private void doData(Cdm2CdmImportState state){

        ICdmApplication source = sourceRepo(state);
        //term uuids laden
        //gegen existierende Terme abgleichen
        //fehlende Terme importieren

        List<String> propertyPaths = null;
        int totalCount = getTotalCount();
        int count = 0;

        //vocabularies
        VocabularyFilter vocFilter = state.getConfig().getVocabularyFilter();
        for (UUID vocUuid : source.getVocabularyService().uuidList(vocFilter)){
            TransactionStatus tx = startTransaction();
            doSingleVocabulary(state, vocUuid);
            commitTransaction(tx);
        }

        //graphs
        Collection<UUID> graphUuids = state.getConfig().getGraphFilter();
        state.setGraph(true);
        for (UUID graphUuid : graphUuids){
            TransactionStatus tx = startTransaction();
            doSingleGraph(state, graphUuid);
            commitTransaction(tx);
        }
        state.setGraph(false);
    }

    private int getTotalCount() {
        // TODO to be implemented
        return 100;
    }

    private void doSingleVocabulary(Cdm2CdmImportState state, UUID vocUuid) {
        ICdmApplication source = sourceRepo(state);
        TransactionStatus otherTx = source.startTransaction(true);
        TermVocabulary<DefinedTermBase> otherVoc = source.getVocabularyService().find(vocUuid);
        TermVocabulary<DefinedTermBase> thisVoc = null;
        try {
            thisVoc = detach(otherVoc, state);
            if (thisVoc != otherVoc && state.getConfig().isAddMissingTerms()){ //voc already existed
                for (DefinedTermBase<?> otherTerm: otherVoc.getTerms()){
                    doSingleTerm(state, otherTerm, thisVoc);
                }
            }
        } catch (Exception e) {
            logger.warn("Exception during detache vocabulary " + otherVoc.getUuid());
            e.printStackTrace();
        }
        try {
            if (thisVoc != null){
                source.commitTransaction(otherTx);
                getVocabularyService().saveOrUpdate(thisVoc);
                getCommonService().saveOrUpdate(state.getToSave());
                state.clearToSave();
            }
        } catch (Exception e) {
            logger.warn("Exception during save vocabulary " + otherVoc.getUuid());
             e.printStackTrace();
        }
    }

    private void doSingleTerm(Cdm2CdmImportState state, DefinedTermBase<?> otherTerm, TermVocabulary<DefinedTermBase> thisVoc) {
        DefinedTermBase<?> thisTerm = null;
        if (logger.isInfoEnabled()){logger.info(otherTerm.getTitleCache());}
        try {
            if (!thisVoc.getTerms().contains(otherTerm)){
                thisTerm = detach(otherTerm, state);
                thisVoc.addTerm(thisTerm);
                state.addToSave(thisTerm);
            }
        } catch (Exception e) {
            logger.warn("Exception during detache node " + otherTerm.getUuid());
            e.printStackTrace();
        }
    }

    private void doSingleGraph(Cdm2CdmImportState state, UUID graphUuid) {
        ICdmApplication source = sourceRepo(state);
        TransactionStatus otherTx = source.startTransaction(true);
        TermTree<DefinedTermBase> otherGraph = source.getTermTreeService().find(graphUuid);
        TermTree<DefinedTermBase> thisGraph = null;
        try {
            thisGraph = detach(otherGraph, state);
            if (thisGraph != otherGraph
                    && state.getConfig().isAddMissingTerms()){ //graph already existed
                for (TermNode<DefinedTermBase> node: otherGraph.getRootChildren()){
                    doSingleNode(state, node, thisGraph.getRoot());
                }
            }
        } catch (Exception e) {
            logger.warn("Exception during detache term graph " + otherGraph.getUuid());
            e.printStackTrace();
        }
        try {
            if (thisGraph != null){
                source.commitTransaction(otherTx);
                getTermTreeService().saveOrUpdate(thisGraph);
                getCommonService().saveOrUpdate(state.getToSave());
                state.clearToSave();
            }
        } catch (Exception e) {
            logger.warn("Exception during save vocabulary " + otherGraph.getUuid());
             e.printStackTrace();
        }
    }

    private void doSingleNode(Cdm2CdmImportState state, TermNode<DefinedTermBase> otherNode,
            TermNode<DefinedTermBase> thisParent) {

        TermNode<DefinedTermBase> thisNode = null;
        try {
            if (!thisParent.getChildNodes().contains(otherNode)){
                thisNode = this.detach(otherNode, state);
                thisParent.addChild(thisNode);
                state.addToSave(thisNode);
                getTermService().saveOrUpdate(thisNode.getTerm());  //state.addToSave() may throw LIE due to linked term during a flush
                if (logger.isDebugEnabled()) {logger.debug("Added term: " + thisNode.getTerm().getTitleCache() + "/" + thisNode.getTerm().getVocabulary().getTitleCache());}
                //do recursive
                for (TermNode<DefinedTermBase> otherChild : otherNode.getChildNodes()) {
                    doSingleNode(state, otherChild, thisNode);
                }
            }
        } catch (Exception e) {
            logger.warn("Exception during detache node " + otherNode.getUuid());
            e.printStackTrace();
        }
    }

    @Override
    protected boolean doCheck(Cdm2CdmImportState state) {
        return true;
    }

    @Override
    protected boolean isIgnore(Cdm2CdmImportState state) {
        return !state.getConfig().isDoVocabularies();
    }
}