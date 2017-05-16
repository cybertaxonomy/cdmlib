/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.ExportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author k.luther
 * @date 15.03.2017
 *
 */
public class OutputModelExportState extends ExportStateBase<OutputModelConfigurator, IExportTransformer>{

    private ExportResult result;

    private OutputModelResultProcessor processor = new OutputModelResultProcessor(this);

    private TaxonBase actualTaxonBase;

    private Map<Integer, HomotypicalGroup> homotypicalGroupStore = new HashMap<Integer, HomotypicalGroup>();
    private Map<Integer, TeamOrPersonBase> authorStore = new HashMap<Integer, TeamOrPersonBase>();
    private Map<Integer, DerivedUnit> specimenStore = new HashMap<Integer, DerivedUnit>();
    private Map<Integer, Reference> referenceStore = new HashMap<Integer, Reference>();

    /**
     * @param config
     */
    protected OutputModelExportState(OutputModelConfigurator config) {
        super(config);
        result = ExportResult.NewInstance(config.getResultType());
    }

    /**
     * @return the result
     */
    @Override
    public ExportResult getResult() {return result;}

    /**
     * @param result the result to set
     */
    @Override
    public void setResult(ExportResult result) {this.result = result;}

    /**
     *
     */
    public void setEmptyData() {
        this.result.setState(ExportResultState.SUCCESS_BUT_NO_DATA);
    }

    /**
     * @return the processor
     */
    public OutputModelResultProcessor getProcessor() {
        return processor;
    }

    public void setActualTaxonBase(TaxonBase actualTaxonBase){ this.actualTaxonBase = actualTaxonBase;}

    @SuppressWarnings("rawtypes")
    public TaxonBase getActualTaxonBase() {return actualTaxonBase;}

    /**
     * @return the homotypicalGroupStore
     */
    public Map<Integer, HomotypicalGroup> getHomotypicalGroupStore() {
        return homotypicalGroupStore;
    }

    /**
     * @param homotypicalGroupStore the homotypicalGroupStore to set
     */
    public void addHomotypicalGroupToStore(HomotypicalGroup homotypicalGroup) {
        this.homotypicalGroupStore.put(homotypicalGroup.getId(), homotypicalGroup);
    }

    public HomotypicalGroup getHomotypicalGroupFromStore(Integer id){
        return homotypicalGroupStore.get(id);
    }

    /**
     * @return the homotypicalGroupStore
     */
    @SuppressWarnings("rawtypes")
    public Map<Integer, TeamOrPersonBase> getAuthorStore() {
        return authorStore;
    }

    /**
     * @param homotypicalGroupStore the homotypicalGroupStore to set
     */
    @SuppressWarnings("rawtypes")
    public void addAuthorToStore(TeamOrPersonBase author) {
        this.authorStore.put(author.getId(), author);
    }

    @SuppressWarnings("rawtypes")
    public TeamOrPersonBase getAuthorFromStore(Integer id){
        return authorStore.get(id);
    }


    public void addSpecimenToStore(DerivedUnit specimen) {
        this.specimenStore.put(specimen.getId(), specimen);

    }


    public DerivedUnit getSpecimenFromStore(Integer id){
        return specimenStore.get(id);
    }

    public Reference getReferenceFromStore(Integer id){
        return referenceStore.get(id);
    }
    public void addReferenceToStore(Reference ref) {
        this.referenceStore.put(ref.getId(), ref);

    }


}
