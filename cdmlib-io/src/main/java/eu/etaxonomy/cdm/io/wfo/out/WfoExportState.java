/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.out.TaxonTreeExportStateBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * State for WFO classification export.
 *
 * @author a.mueller
 * @since 2023-12-08
 */
public class WfoExportState
        extends TaxonTreeExportStateBase<WfoExportConfigurator,WfoExportState>{

    private ExportResult result;

    private WfoExportResultProcessor processor = new WfoExportResultProcessor(this);

    private TaxonBase<?> currentTaxonBase;

    private ArrayList<UUID> homotypicalGroupStore = new ArrayList<>();

    //private Map<Integer, SpecimenOrObservationBase> specimenStore = new HashMap<>();
    private List<UUID> referenceStore = new ArrayList<>();
    private Map<Integer, UUID> nameStore = new HashMap<>();
    private Map<UUID,List<TaxonNodeDto>> nodeChildrenMap = new HashMap<>();
    private UUID classificationUUID = null;
    private String familyStr = null;
    private Map<UUID,String> taxonNodeToWfoMap = new HashMap<>();
    private Map<UUID,String> taxonToWfoMap = new HashMap<>();
    private Map<UUID,String> nameToWfoMap = new HashMap<>();

    private UUID rootUuid;

    //CONSTRUCTOR
    protected WfoExportState(WfoExportConfigurator config) {
        super(config);
        result = ExportResult.NewInstance(config.getResultType());
        familyStr = config.getFamilyStr();
    }

    //result
    @Override
    public ExportResult getResult() {
        return result;
    }
    @Override
    public void setResult(ExportResult result) {
        this.result = result;
    }

    //empty data
    protected void setEmptyData() {
        this.result.setState(ExportResultState.SUCCESS_BUT_NO_DATA);
    }

    //processor
    protected WfoExportResultProcessor getProcessor() {
        return processor;
    }

    //current taxon
    protected void setCurrentTaxonBase(TaxonBase<?> currentTaxonBase){
        this.currentTaxonBase = currentTaxonBase;
    }
    protected TaxonBase<?> getCurrentTaxonBase() {
        return currentTaxonBase;
    }

    //homotypical group store
    protected ArrayList<UUID> getHomotypicalGroupStore() {
        return homotypicalGroupStore;
    }
    protected void addHomotypicalGroupToStore(HomotypicalGroup homotypicalGroup) {
        this.homotypicalGroupStore.add(homotypicalGroup.getUuid());
    }
    protected boolean containsHomotypicalGroupFromStore(UUID id){
        return homotypicalGroupStore.contains(id);
    }
    protected void setHomotypicalGroupStore(ArrayList<UUID> homotypicalGroupStore) {
        this.homotypicalGroupStore = homotypicalGroupStore;
    }

    //reference store
    protected void addReferenceToStore(Reference ref) {
        this.referenceStore.add(ref.getUuid());
    }
    protected void setReferenceStore(List<UUID> referenceStore) {
        this.referenceStore = referenceStore;
    }
    protected List<UUID> getReferenceStore() {
        return referenceStore;
    }

    //childrenMap
    protected Map<UUID, List<TaxonNodeDto>> getNodeChildrenMap() {
        return nodeChildrenMap;
    }
    protected void setNodeChildrenMap(Map<UUID,List<TaxonNodeDto>> nodeChildrenMap) {
        this.nodeChildrenMap = nodeChildrenMap;
    }

    //classificationUuid
    protected UUID getClassificationUUID(TaxonNode root) {
        if (classificationUUID == null){
            classificationUUID = root.getClassification().getUuid();
        }
        return classificationUUID;
    }
    protected void setClassificationUUID(UUID classificationUUID) {
        this.classificationUUID = classificationUUID;
    }

    //rootID
    protected UUID getRootId() {
        return rootUuid;
    }
    protected void setRootId(UUID rootId) {
        this.rootUuid = rootId;
    }

    //nameStore
    protected Map<Integer, UUID> getNameStore() {
        return nameStore;
    }
    protected void setNameStore(Map<Integer, UUID> nameStore) {
        this.nameStore = nameStore;
    }

    //familyStr
    public String getFamilyStr() {
        return familyStr;
    }
    public void setFamilyStr(String familyStr) {
        this.familyStr = familyStr;
    }

    //taxonToWfo map
    public void putTaxonNodeWfoId(TaxonNode node, String wfoId) {
        taxonNodeToWfoMap.put(node.getUuid(), wfoId);
    }
    public String getTaxonNodeWfoId(TaxonNode node) {
        return taxonNodeToWfoMap.get(node.getUuid());
    }

    public void putTaxonWfoId(TaxonBase<?> taxon, String wfoId) {
        taxonToWfoMap.put(taxon.getUuid(), wfoId);
    }
    public String getTaxonWfoId(TaxonBase<?> taxon) {
        return taxonToWfoMap.get(taxon.getUuid());
    }

    public void putNameWfoId(TaxonName name, String wfoId) {
        nameToWfoMap.put(name.getUuid(), wfoId);
    }
    public String getNameWfoId(TaxonName name) {
        return nameToWfoMap.get(name.getUuid());
    }
}