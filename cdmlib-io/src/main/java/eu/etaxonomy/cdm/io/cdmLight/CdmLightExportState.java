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
 *
 */
public class CdmLightExportState
        extends ExportStateBase<CdmLightExportConfigurator, IExportTransformer, File>{

    private ExportResult result;

    private CdmLightExportResultProcessor processor = new CdmLightExportResultProcessor(this);

    private TaxonBase<?> actualTaxonBase;

    private ArrayList<UUID> homotypicalGroupStore = new ArrayList();
    private Map<Integer, TeamOrPersonBase<?>> authorStore = new HashMap<>();

    private List<UUID> specimenStore = new ArrayList();
    //private Map<Integer, SpecimenOrObservationBase> specimenStore = new HashMap<>();
    private List<UUID> referenceStore = new ArrayList();
    private Map<Integer, UUID> nameStore = new HashMap<>();
    private Map<UUID,List<TaxonNodeDto>> nodeChildrenMap = new HashMap<>();
    private Map<UUID, OrderHelper> orderHelperMap = new HashMap();
    private UUID classificationUUID = null;

    private UUID rootUuid;
    private int actualOrderIndex;
    /**
     * @param config
     */
    protected CdmLightExportState(CdmLightExportConfigurator config) {
        super(config);
        result = ExportResult.NewInstance(config.getResultType());

    }

    /**
     * @return the result
     */
    @Override
    public ExportResult getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    @Override
    public void setResult(ExportResult result) {
        this.result = result;
    }

    /**
     *
     */
    public void setEmptyData() {
        this.result.setState(ExportResultState.SUCCESS_BUT_NO_DATA);
    }

    /**
     * @return the processor
     */
    public CdmLightExportResultProcessor getProcessor() {
        return processor;
    }

    public void setActualTaxonBase(TaxonBase<?> actualTaxonBase){
        this.actualTaxonBase = actualTaxonBase;
    }

    public TaxonBase<?> getActualTaxonBase() {
        return actualTaxonBase;
    }

    /**
     * @return the homotypicalGroupStore
     */
    public ArrayList<UUID> getHomotypicalGroupStore() {
        return homotypicalGroupStore;
    }

    /**
     * @param homotypicalGroupStore the homotypicalGroupStore to set
     */
    public void addHomotypicalGroupToStore(HomotypicalGroup homotypicalGroup) {
        this.homotypicalGroupStore.add(homotypicalGroup.getUuid());
    }

    public boolean containsHomotypicalGroupFromStore(UUID id){
        return homotypicalGroupStore.contains(id);
    }

    /**
     * @return the specimenStore
     */
    public List<UUID> getSpecimenStore() {
        return specimenStore;
    }



    /**
     * @param specimenStore the specimenStore to set
     */
    public void setSpecimenStore(List<UUID> specimenStore) {
        this.specimenStore = specimenStore;
    }





    /**
     * @param homotypicalGroupStore the homotypicalGroupStore to set
     */
    public void setHomotypicalGroupStore(ArrayList<UUID> homotypicalGroupStore) {
        this.homotypicalGroupStore = homotypicalGroupStore;
    }

    /**
     * @param authorStore the authorStore to set
     */
    public void setAuthorStore(Map<Integer, TeamOrPersonBase<?>> authorStore) {
        this.authorStore = authorStore;
    }

    /**
     * @param homotypicalGroupStore the homotypicalGroupStore to set
     */
    public void addAuthorToStore(TeamOrPersonBase<?> author) {
        this.authorStore.put(author.getId(), author);
    }

    /**
     * @return the homotypicalGroupStore
     */
    public Map<Integer, TeamOrPersonBase<?>> getAuthorStore() {
        return authorStore;
    }


    public TeamOrPersonBase<?> getAuthorFromStore(Integer id){
        return authorStore.get(id);
    }


    public void addSpecimenToStore(SpecimenOrObservationBase specimen) {
        this.specimenStore.add(specimen.getUuid());

    }


    public void addReferenceToStore(Reference ref) {
        this.referenceStore.add(ref.getUuid());

    }
    /**
     * @param referenceStore the referenceStore to set
     */
    public void setReferenceStore(List<UUID> referenceStore) {
        this.referenceStore = referenceStore;
    }
    /**
     * @return the referenceStore
     */
    public List<UUID> getReferenceStore() {
        return referenceStore;
    }

    /**
     * @return the nodeChildrenMap
     */
    public Map<UUID, List<TaxonNodeDto>> getNodeChildrenMap() {
        return nodeChildrenMap;
    }

    /**
     * @param nodeChildrenMap the nodeChildrenMap to set
     */
    public void setNodeChildrenMap(Map<UUID, List<TaxonNodeDto>> nodeChildrenMap) {
        this.nodeChildrenMap = nodeChildrenMap;
    }

    public Map<UUID, OrderHelper> getOrderHelperMap() {
        return orderHelperMap;
    }

    public void setOrderHelperMap(Map<UUID, OrderHelper> orderHelperMap) {
        this.orderHelperMap = orderHelperMap;
    }

    public UUID getClassificationUUID(TaxonNode root) {
        if (classificationUUID == null){
            classificationUUID = root.getClassification().getUuid();
        }
        return classificationUUID;
    }

    public void setClassificationUUID(UUID classificationUUID) {
        this.classificationUUID = classificationUUID;
    }

    public UUID getRootId() {
        return rootUuid;
    }

    public void setRootId(UUID rootId) {
        this.rootUuid = rootId;
    }

    public int getActualOrderIndexAndUpdate() {
        int returnValue = actualOrderIndex;
        actualOrderIndex++;
        return returnValue;
    }

    public void setActualOrderIndex(int actualOrderIndex) {
        this.actualOrderIndex = actualOrderIndex;
    }

    public Map<Integer, UUID> getNameStore() {
        return nameStore;
    }

    public void setNameStore(Map<Integer, UUID> nameStore) {
        this.nameStore = nameStore;
    }

}
