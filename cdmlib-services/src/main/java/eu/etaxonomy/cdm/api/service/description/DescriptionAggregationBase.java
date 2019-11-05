/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.media.Media;

/**
 * A common base class to run aggregation tasks on descriptive data.
 *
 * Usable for all types of descriptive data like structured descriptive data,
 * ( {@link CategoricalData and QuantitativeData}, {@link Distribution},
 * {@link Media}, etc.
 *
 * @author a.mueller
 * @since 03.11.2019
 */
public abstract class DescriptionAggregationBase<T extends DescriptionAggregationBase<T, CONFIG>, CONFIG extends DescriptionAggregationConfiguration> {

    private ICdmRepository repository;
    private CONFIG config;
    private UpdateResult result;


    public final UpdateResult invoke(CONFIG config, ICdmRepository repository){
        init(config, repository);
        return doInvoke();
    }

    protected UpdateResult doInvoke() {
        UpdateResult resultTaxon = invokeOnSingleTaxon();

        UpdateResult resultRank = invokeHigherRankAggregation();

        UpdateResult result = merge(resultTaxon, resultRank);

        return result;

    }

    private void init(CONFIG config, ICdmRepository repository) {
        this.repository = repository;
        this.config = config;
        if(config.getMonitor() == null){
            config.setMonitor(new NullProgressMonitor());
        }
        result = new UpdateResult();
    }


    //TODO or should we handle all in *one* UpdateResult
    private UpdateResult merge(UpdateResult resultTaxon, UpdateResult resultRank) {
        //TODO merge
        return null;
    }

    protected abstract UpdateResult invokeOnSingleTaxon();

    protected abstract UpdateResult removeExistingAggregationOnTaxon();

    //TODO abstract maybe not needed
    protected abstract UpdateResult invokeHigherRankAggregation();

// ******************** GETTER / SETTER *************************/

    protected IDescriptionService getDescriptionService(){
        return repository.getDescriptionService();
    }

    protected IDescriptiveDataSetService getDescriptiveDatasetService() {
        return repository.getDescriptiveDataSetService();
    }

    protected ITaxonService getTaxonService() {
        return repository.getTaxonService();
    }

    protected ITaxonNodeService getTaxonNodeService() {
        return repository.getTaxonNodeService();
    }

    protected ICdmRepository getRepository() {
        return repository;
    }

    protected CONFIG getConfig() {
        return config;
    }

    protected UpdateResult getResult() {
        return result;
    }

    protected IProgressMonitor getMonitor() {
        return config.getMonitor();
    }
}
