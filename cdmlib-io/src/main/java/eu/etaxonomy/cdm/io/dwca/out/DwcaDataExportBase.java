/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public abstract class DwcaDataExportBase extends DwcaExportBase{

    private static final long serialVersionUID = -5467295551060073610L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DwcaDataExportBase.class);

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

    abstract protected void handleTaxonNode(DwcaTaxExportState state, TaxonNode node)throws IOException, FileNotFoundException, UnsupportedEncodingException;


    /**
     * {@inheritDoc}
     */
    @Override
    public int countSteps(DwcaTaxExportState state) {
        //FIXME count without initialization
        List<TaxonNode> allNodes =  allNodes(state);
        return allNodes.size();
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
    protected List<TaxonNode> allNodes(DwcaTaxExportState state) {

        Set<UUID> subtreeUuidSet = state.getConfig().getSubtreeUuids();
        if (subtreeUuidSet == null){
            subtreeUuidSet = new HashSet<>();
        }
        //handle empty list as no filter defined
        if (subtreeUuidSet.isEmpty()){
            List<Classification> classificationList = getClassificationService().list(Classification.class, null, 0, null, null);
            for (Classification classification : classificationList){
                subtreeUuidSet.add(classification.getRootNode().getUuid());
            }
        }

        //TODO memory critical to store ALL node
        if (state.getAllNodes().isEmpty()){
            makeAllNodes(state, subtreeUuidSet);
        }
        List<TaxonNode> allNodes = state.getAllNodes();
        return allNodes;
    }

    private void makeAllNodes(DwcaTaxExportState state, Set<UUID> subtreeSet) {

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
     * Creates the locationId, locality, countryCode triple
     * @param state
     * @param record
     * @param area
     */
    protected void handleArea(DwcaTaxExportState state, IDwcaAreaRecord record, NamedArea area, TaxonBase<?> taxon, boolean required) {
        if (area != null){
            record.setLocationId(area);
            record.setLocality(area.getLabel());
            if (area.isInstanceOf(Country.class)){
                Country country = CdmBase.deproxy(area, Country.class);
                record.setCountryCode(country.getIso3166_A2());
            }
        }else{
            if (required){
                String message = "Description requires area but area does not exist for taxon " + getTaxonLogString(taxon);
                state.getResult().addWarning(message);
            }
        }
    }


    protected String getTaxonLogString(TaxonBase<?> taxon) {
        return taxon.getTitleCache() + "(" + taxon.getId() + ")";
    }


    protected String getSources(ISourceable<?> sourceable, DwcaTaxExportConfigurator config) {
        String result = "";
        for (IOriginalSource<?> source: sourceable.getSources()){
            if (StringUtils.isBlank(source.getIdInSource())){//idInSource indicates that this source is only data provenance, may be changed in future
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
            }
        }
        return result;
    }

    protected String getSources3(ISourceable<?> sourceable, DwcaTaxExportConfigurator config) {
        String result = "";
        for (IOriginalSource<?> source: sourceable.getSources()){
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
        }
        return result;
    }

    protected String getSources2(Set<DescriptionElementSource> set, DwcaTaxExportConfigurator config) {
        String result = "";
        for(DescriptionElementSource source: set){
            if (StringUtils.isBlank(source.getIdInSource())){//idInSource indicates that this source is only data provenance, may be changed in future
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
            }
        }
        return result;
    }

}
