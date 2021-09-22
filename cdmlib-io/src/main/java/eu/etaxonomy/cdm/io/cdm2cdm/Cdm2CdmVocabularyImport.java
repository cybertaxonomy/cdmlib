/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
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
    private static final Logger logger = Logger.getLogger(Cdm2CdmVocabularyImport.class);

    @Override
    protected void doInvoke(Cdm2CdmImportState state) {
        IProgressMonitor monitor = state.getConfig().getProgressMonitor();
        doData(state);
    }

    private void doData(Cdm2CdmImportState state){

        //term uuids laden
        //gegen existierende Terme abgleichen
        //fehlende Terme importieren

        List<String> propertyPaths = null;
        int totalCount = getTotalCount();
        int count = 0;

        //vocabularies
        Set<UUID> vocUuids = state.getConfig().getVocabularyFilter();
        for (UUID vocUuid : vocUuids){
            TransactionStatus tx = startTransaction();
            doSingleVocabulary(state, vocUuid);
            commitTransaction(tx);
        }

        //graphs
        Set<UUID> graphUuids = state.getConfig().getGraphFilter();
        for (UUID graphUuid : graphUuids){
            TransactionStatus tx = startTransaction();
            doSingleGraph(state, graphUuid);
            commitTransaction(tx);
        }
    }

    private int getTotalCount() {
        // TODO to be implemented
        return 100;
    }

    private void doSingleVocabulary(Cdm2CdmImportState state, UUID vocUuid) {
        ICdmRepository source = sourceRepo(state);
        TransactionStatus otherTx = source.startTransaction(true);
        TermVocabulary<DefinedTermBase> otherVoc = source.getVocabularyService().find(vocUuid);
        TermVocabulary<DefinedTermBase> thisVoc = null;
        try {
            thisVoc = detache(otherVoc, state);
            if (thisVoc != otherVoc){ //voc already existed
                for (DefinedTermBase<?> term: otherVoc.getTerms()){
                    doSingleTerm(state, term, thisVoc);
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
                thisTerm = detache(otherTerm, state);
//                if(thisTerm == otherTerm){ //term does not yet exist
                thisVoc.addTerm(thisTerm);
                state.addToSave(thisTerm);
//                }
            }
        } catch (Exception e) {
            logger.warn("Exception during detache node " + otherTerm.getUuid());
            e.printStackTrace();
        }
    }

    private void doSingleGraph(Cdm2CdmImportState state, UUID graphUuid) {
        ICdmRepository source = sourceRepo(state);
        TransactionStatus otherTx = source.startTransaction(true);
        TermTree<DefinedTermBase> otherGraph = source.getTermTreeService().find(graphUuid);
        TermTree<DefinedTermBase> thisGraph = null;
        try {
            thisGraph = detache(otherGraph, state);
            if (thisGraph != otherGraph){ //voc already existed
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

    private void doSingleNode(Cdm2CdmImportState state, TermNode<DefinedTermBase> otherNode, TermNode<DefinedTermBase> thisRoot) {
//        TermNode<DefinedTermBase> thisTerm = null;
////        try {
////            if (!thisRoot.getChilcontains(otherNode)){
////                thisTerm = detache(otherNode, state);
//////                if(thisTerm == otherTerm){ //term does not yet exist
////                thisGraph.addTerm(thisTerm);
////                state.addToSave(thisTerm);
//////                }
////            }
//        } catch (Exception e) {
//            logger.warn("Exception during detache node " + otherNode.getUuid());
//            e.printStackTrace();
//        }
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