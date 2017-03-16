/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import java.util.UUID;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.name.ITaxonNameBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author k.luther
 * @date 15.03.2017
 *
 */
public class OutputModelClassificationExport
            extends CdmExportBase<OutputModelConfigurator, OutputModelExportState, IExportTransformer>
            implements ICdmExport<OutputModelConfigurator, OutputModelExportState>{


    private static final long serialVersionUID = 2518643632756927053L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(OutputModelExportState state) {
        OutputModelConfigurator config = state.getConfig();

        if (config.getClassificationUuids().isEmpty()){
            //TODO
            state.setEmptyData();
            return;
        }
        //TODO MetaData
        for (UUID classificationUuid : config.getClassificationUuids()){
            Classification classification = getClassificationService().find(classificationUuid);
            if (classification == null){
                String message = String.format("Classification for given classification UUID not found. No data imported for %s", classificationUuid.toString());
                //TODO
                state.getResult().addWarning(message);
            }else{
//                gtTaxonNodeService().
                TaxonNode root = classification.getRootNode();
                for (TaxonNode child : root.getChildNodes()){
                    handleTaxon(state, child);
                    //TODO progress monitor
                }
            }
        }
    }

    /**
     * @param state
     * @param taxon
     */
    private void handleTaxon(OutputModelExportState state, TaxonNode taxonNode) {
        Taxon taxon = taxonNode.getTaxon();
        ITaxonNameBase name = taxon.getName();
        handleName(state, name);
        for (Synonym syn : taxon.getSynonyms()){
            handleSynonym(state, syn);
        }


        OutputModelTable table = OutputModelTable.TAXON;
        String[] csvLine = new String[table.getSize()];

        csvLine[table.getIndex(OutputModelTable.TAXON_ID)] = getId(state, taxon);
        csvLine[table.getIndex(OutputModelTable.NAME_FK)] = getId(state, name);
        Taxon parent = (taxonNode.getParent()==null) ? null : taxonNode.getParent().getTaxon();
        csvLine[table.getIndex(OutputModelTable.PARENT_FK)] = getId(state, parent);
        csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE_FK)] = getId(state, taxon.getSec());
        csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE)] = getTitleCache(taxon.getSec());

        state.getProcessor().put(table, taxon, csvLine);
    }

    /**
     * @param sec
     * @return
     */
    private String getTitleCache(IIdentifiableEntity identEntity) {
        if (identEntity == null){
            return "";
        }
        //TODO refresh?
        return identEntity.getTitleCache();
    }

    /**
     * @param state
     * @param taxon
     * @return
     */
    private String getId(OutputModelExportState state, ICdmBase cdmBase) {
        if (cdmBase == null){
            return "";
        }
        //TODO make configurable
        return cdmBase.getUuid().toString();
    }

    /**
     * @param state
     * @param syn
     */
    private void handleSynonym(OutputModelExportState state, Synonym syn) {
       ITaxonNameBase name = syn.getName();
       handleName(state, name);

       OutputModelTable table = OutputModelTable.SYNONYM;
       String[] csvLine = new String[table.getSize()];

       csvLine[table.getIndex(OutputModelTable.SYNONYM_ID)] = getId(state, syn);
       csvLine[table.getIndex(OutputModelTable.TAXON_FK)] = getId(state, syn.getAcceptedTaxon());
       csvLine[table.getIndex(OutputModelTable.NAME_FK)] = getId(state, name);
       csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE_FK)] = getId(state, syn.getSec());
       csvLine[table.getIndex(OutputModelTable.SEC_REFERENCE)] = getTitleCache(syn.getSec());

       state.getProcessor().put(table, syn, csvLine);
    }

    /**
     * @param state
     * @param name
     */
    private void handleName(OutputModelExportState state, ITaxonNameBase name) {
        Rank rank = name.getRank();

    }

    /**
     * @param state
     * @param name
     */
    private void handleReference(OutputModelExportState state, Reference reference) {
        OutputModelTable table = OutputModelTable.REFERENCE;

        String[] csvLine = new String[table.getSize()];
        csvLine[table.getIndex(OutputModelTable.REFERENCE_ID)] = getId(state, reference);
        //TODO short citations correctly
        String shortCitation = reference.getAbbrevTitleCache();  //Should be Author(year) like in Taxon.sec
        csvLine[table.getIndex(OutputModelTable.BIBLIO_SHORT_CITATION)] = shortCitation;
        //TODO get preferred title
        csvLine[table.getIndex(OutputModelTable.REF_TITLE)] = reference.getTitle();
        csvLine[table.getIndex(OutputModelTable.DATE_PUBLISHED)] = reference.getDatePublishedString();
        //TBC

        state.getProcessor().put(table, reference, csvLine);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(OutputModelExportState state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(OutputModelExportState state) {
        return false;
    }



}
