/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.dwca.out.DwcaTaxExportState;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author a.mueller
 * @since 01.07.2008
 */
public abstract class CdmExportBase<CONFIG extends ExportConfiguratorBase<STATE, TRANSFORM, DEST>, STATE extends ExportStateBase, TRANSFORM extends IExportTransformer, DEST extends Object>
            extends CdmIoBase<STATE, ExportResult>
            implements ICdmExport<CONFIG, STATE>{

    private static final long serialVersionUID = 3685030095117254235L;

    private static Logger logger = Logger.getLogger(CdmExportBase.class);

    protected ByteArrayOutputStream exportStream;


    @Autowired
    protected IClassificationService classificationService;

    @Autowired
    protected ITaxonNodeService taxonNodeService;


	@Override
	public  ExportDataWrapper createExportData() {
	    if (exportStream != null){
	        ExportDataWrapper<byte[]> data = ExportDataWrapper.NewByteArrayInstance();
	        data.addExportData( exportStream.toByteArray());
	        return data;
	    }else{
	        return null;
	    }
	}

    @Override
    protected ExportResult getNoDataResult(STATE state) {
        return ExportResult.NewNoDataInstance(((IExportConfigurator)state.config).getResultType());
    }

    @Override
    protected ExportResult getDefaultResult(STATE state) {
        return ExportResult.NewInstance(((IExportConfigurator)state.config).getResultType());
    }

    @Override
    public byte[] getByteArray() {
        if (this.exportStream != null){
            return this.exportStream.toByteArray();
        }
        return null;
    }


    public Object getDbId(CdmBase cdmBase, STATE state){
        logger.warn("Not yet implemented for export base class");
        return null;
    }


    /**
     * Returns the list of {@link TaxonNode taxon nodes} that correspond to the
     * given filter criteria (e.g. subtreeUUids). If no filter is given
     * all taxon nodes of all classifications are returned. If the list has been
     * computed before it is taken from the state cache. Nodes that do not have
     * a taxon attached are not returned. Instead a warning is given that the node is
     * ommitted (empty taxon nodes should not but do exist in CDM databases).
     * <BR>
     * Preliminary implementation. Better implement API method for this.
     */
    //TODO unify with similar methods for other exports
    //TODO can be removed once the partitioner is used
    protected List<TaxonNode> allNodes(DwcaTaxExportState state) {

        TaxonNodeFilter filter = state.getConfig().getTaxonNodeFilter();

        List<UUID> listUuid = taxonNodeService.uuidList(filter);

        //TODO memory critical to store ALL node
        if (state.getAllNodes().isEmpty()){
            makeAllNodes(state, listUuid);
        }
        List<TaxonNode> allNodes = state.getAllNodes();
        return allNodes;
    }


    protected void makeAllNodes(DwcaTaxExportState state, Collection<UUID> subtreeSet) {

        try {
            boolean doSynonyms = false;
            boolean recursive = true;
            Set<UUID> uuidSet = new HashSet<>();

            for (UUID subtreeUuid : subtreeSet){
                UUID tnUuuid = taxonNodeUuid(subtreeUuid);
                uuidSet.add(tnUuuid);
                List<TaxonNodeDto> records = getTaxonNodeService().pageChildNodesDTOs(tnUuuid,
                        recursive, doSynonyms, null, null, null).getRecords();
                for (TaxonNodeDto dto : records){
                    uuidSet.add(dto.getUuid());
                }
            }
            List<TaxonNode> allNodes =  getTaxonNodeService().find(uuidSet);

            List<TaxonNode> result = new ArrayList<>();
            for (TaxonNode node : allNodes){
                if(node.getParent()== null){  //root (or invalid) node
                    continue;
                }
                node = CdmBase.deproxy(node);
                Taxon taxon = CdmBase.deproxy(node.getTaxon());
                if (taxon == null){
                    String message = "There is a taxon node without taxon. id=" + node.getId();
                    state.getResult().addWarning(message);
                    continue;
                }
                result.add(node);
            }
            state.setAllNodes(result);
        } catch (Exception e) {
            String message = "Unexpected exception when trying to compute all taxon nodes";
            state.getResult().addException(e, message);
        }
    }


    /**
     * @param subtreeUuid
     * @return
     */
    private UUID taxonNodeUuid(UUID subtreeUuid) {
        TaxonNode node = taxonNodeService.find(subtreeUuid);
        if (node == null){
            Classification classification = classificationService.find(subtreeUuid);
            if (classification != null){
                node = classification.getRootNode();
            }else{
                throw new IllegalArgumentException("Subtree identifier does not exist: " + subtreeUuid);
            }
        }
        return node.getUuid();
    }


    /**
     * <code>true</code> if neither synonym has state publish nor
     * taxon node filter includes unpublished taxa.
     */
    protected boolean isUnpublished(CONFIG config, Synonym synonym) {
        return ! (synonym.isPublish()
                || config.getTaxonNodeFilter().isIncludeUnpublished());
    }

}
