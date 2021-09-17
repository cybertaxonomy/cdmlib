/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author a.mueller
 * @since 17.08.2019
 * NOT YET USED
 */
@Component
public class Cdm2CdmTermImport
        extends Cdm2CdmImportBase {

    private static final long serialVersionUID = 3995116783196060465L;

    private static final Logger logger = Logger.getLogger(Cdm2CdmTermImport.class);

    //TODO move to state
    private Map<UUID, CdmBase> sessionCache = new HashMap<>();
    private Map<UUID, CdmBase> permanentCache = new HashMap<>();
    private Set<UUID> movedObjects = new HashSet<>();

    private Set<CdmBase> toSave = new HashSet<>();


    @Override
    protected void doInvoke(Cdm2CdmImportState state) {
        setState(state);
        IProgressMonitor monitor = state.getConfig().getProgressMonitor();

        Cdm2CdmImportConfigurator config = state.getConfig();

        doData(state);
    }


    private void doData(Cdm2CdmImportState state){
        //term uuids laden
        //gegen existierende Terme abgleichen
        //fehlende Terme importieren

        TransactionStatus tx = startTransaction();
        List<String> propertyPaths = null;
        List<DefinedTermBase> terms = sourceRepo(state).getTermService().list(null, null, null, null, propertyPaths);
        int count = 0;
        for(DefinedTermBase term: terms){
            doSingleTerm(state, term);
            count++;
        }
        commitTransaction(tx);
    }

    private DefinedTermBase doSingleTerm(Cdm2CdmImportState state, DefinedTermBase term) {
        DefinedTermBase result = null;
        if (logger.isInfoEnabled()){logger.info(term.getTitleCache());}
        try {
            result = detache(term);
        } catch (Exception e) {
            logger.warn("Exception during detache node " + term.getUuid());
            e.printStackTrace();
        }
        try {
            if (result != null){
                getTermService().saveOrUpdate(term);
                getCommonService().saveOrUpdate(toSave);
                toSave.clear();
            }
        } catch (Exception e) {
            logger.warn("Exception during save node " + term.getUuid());
             e.printStackTrace();
        }

        return result;
    }

    @Override
    protected boolean doCheck(Cdm2CdmImportState state) {
        return false;
    }

    @Override
    protected boolean isIgnore(Cdm2CdmImportState state) {
        return false;
    }
}