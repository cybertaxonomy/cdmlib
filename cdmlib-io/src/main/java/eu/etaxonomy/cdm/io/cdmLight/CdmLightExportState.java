/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.ExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author k.luther
 * @since 15.03.2017
 */
public class CdmLightExportState
        extends ExportStateBase<CdmLightExportConfigurator, IExportTransformer, File>{

    private ExportResult result;

    private CdmLightExportResultProcessor processor = new CdmLightExportResultProcessor(this);

    private TaxonBase<?> actualTaxonBase;

    private ArrayList<UUID> homotypicalGroupStore = new ArrayList<>();
    private Map<Integer, TeamOrPersonBase<?>> authorStore = new HashMap<>();

    private List<UUID> specimenStore = new ArrayList<>();
    //private Map<Integer, SpecimenOrObservationBase> specimenStore = new HashMap<>();
    private List<UUID> referenceStore = new ArrayList<>();
    private Map<Integer, UUID> nameStore = new HashMap<>();
    private Map<UUID,List<TaxonNodeDto>> nodeChildrenMap = new HashMap<>();
    private Map<UUID, OrderHelper> orderHelperMap = new HashMap<>();
    private UUID classificationUUID = null;

    private UUID rootUuid;
    private int actualOrderIndex;

    protected CdmLightExportState(CdmLightExportConfigurator config) {
        super(config);
        result = ExportResult.NewInstance(config.getResultType());
    }

    @Override
    public ExportResult getResult() {
        return result;
    }
    @Override
    public void setResult(ExportResult result) {
        this.result = result;
    }

    protected void setEmptyData() {
        this.result.setState(ExportResultState.SUCCESS_BUT_NO_DATA);
    }

    protected CdmLightExportResultProcessor getProcessor() {
        return processor;
    }

    protected void setActualTaxonBase(TaxonBase<?> actualTaxonBase){
        this.actualTaxonBase = actualTaxonBase;
    }

    protected TaxonBase<?> getActualTaxonBase() {
        return actualTaxonBase;
    }

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

    protected List<UUID> getSpecimenStore() {
        return specimenStore;
    }
    protected void setSpecimenStore(List<UUID> specimenStore) {
        this.specimenStore = specimenStore;
    }
    protected void addSpecimenToStore(SpecimenOrObservationBase<?> specimen) {
        this.specimenStore.add(specimen.getUuid());
    }

    protected Map<Integer, TeamOrPersonBase<?>> getAuthorStore() {
        return authorStore;
    }
    protected void setAuthorStore(Map<Integer, TeamOrPersonBase<?>> authorStore) {
        this.authorStore = authorStore;
    }
    protected void addAuthorToStore(TeamOrPersonBase<?> author) {
        this.authorStore.put(author.getId(), author);
    }
    protected TeamOrPersonBase<?> getAuthorFromStore(Integer id){
        return authorStore.get(id);
    }

    protected void addReferenceToStore(Reference ref) {
        this.referenceStore.add(ref.getUuid());
    }
    protected void setReferenceStore(List<UUID> referenceStore) {
        this.referenceStore = referenceStore;
    }
    protected List<UUID> getReferenceStore() {
        return referenceStore;
    }

    protected Map<UUID, List<TaxonNodeDto>> getNodeChildrenMap() {
        return nodeChildrenMap;
    }
    protected void setNodeChildrenMap(Map<UUID, List<TaxonNodeDto>> nodeChildrenMap) {
        this.nodeChildrenMap = nodeChildrenMap;
    }

    protected Map<UUID, OrderHelper> getOrderHelperMap() {
        return orderHelperMap;
    }
    protected void setOrderHelperMap(Map<UUID, OrderHelper> orderHelperMap) {
        this.orderHelperMap = orderHelperMap;
    }

    protected UUID getClassificationUUID(TaxonNode root) {
        if (classificationUUID == null){
            classificationUUID = root.getClassification().getUuid();
        }
        return classificationUUID;
    }
    protected void setClassificationUUID(UUID classificationUUID) {
        this.classificationUUID = classificationUUID;
    }

    protected UUID getRootId() {
        return rootUuid;
    }
    protected void setRootId(UUID rootId) {
        this.rootUuid = rootId;
    }

    protected int getActualOrderIndexAndUpdate() {
        int returnValue = actualOrderIndex;
        actualOrderIndex++;
        return returnValue;
    }
    protected void setActualOrderIndex(int actualOrderIndex) {
        this.actualOrderIndex = actualOrderIndex;
    }

    protected Map<Integer, UUID> getNameStore() {
        return nameStore;
    }

    protected void setNameStore(Map<Integer, UUID> nameStore) {
        this.nameStore = nameStore;
    }
}
