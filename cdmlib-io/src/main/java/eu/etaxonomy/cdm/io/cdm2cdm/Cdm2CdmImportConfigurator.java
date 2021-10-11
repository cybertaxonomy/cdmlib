/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.ICdmImportSource;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.ITaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Configuration base class for Cdm2Cdm migration.
 *
 * @author a.mueller
 * @since 17.08.2019
 */
public  class Cdm2CdmImportConfigurator
        extends ImportConfiguratorBase<Cdm2CdmImportState, ICdmImportSource>{

    private static final long serialVersionUID = 5454400624983256935L;

    private static IInputTransformer myTransformer = null;

    private TaxonNodeFilter taxonNodeFilter = TaxonNodeFilter.NewInstance();

    private Set<UUID> vocabularyFilter = new HashSet<>();
    private Set<UUID> graphFilter = new HashSet<>();
    private ITaxonNodeOutStreamPartitioner partitioner;
    private boolean concurrent = false;  //

    private boolean doTaxa = true;
    private boolean doDescriptions = true;
    private boolean doVocabularies = true;

    private boolean addSources = true;
    private boolean removeImportSources = false;

//***************************** NewInstance ************************/

    public static Cdm2CdmImportConfigurator NewInstace(ICdmImportSource source, ICdmDataSource destination){
        return new Cdm2CdmImportConfigurator(source, destination);
    }

// ***************************** CONSTRUCTOR **********************/

    public Cdm2CdmImportConfigurator(ICdmImportSource source, ICdmDataSource destination) {
        super(myTransformer);
        this.setSource(source);
        this.setDestination(destination);
    }

// ****************************** METHODS *********************/

    @SuppressWarnings("unchecked")
    @Override
    public Cdm2CdmImportState getNewState() {
        return new Cdm2CdmImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                Cdm2CdmVocabularyImport.class,
                Cdm2CdmTaxonNodeImport.class ,
                Cdm2CdmDescriptionImport.class ,
        };
    }

    @Override
    @Deprecated
    public Reference getSourceReference() {
        return sourceReference;
    }

// ******************* GETTER / SETTER ***************************/

    public TaxonNodeFilter getTaxonNodeFilter() {
        return taxonNodeFilter;
    }
    public void setTaxonNodeFilter(TaxonNodeFilter taxonNodeFilter) {
        this.taxonNodeFilter = taxonNodeFilter;
    }

    public ITaxonNodeOutStreamPartitioner getPartitioner() {
        return partitioner;
    }
    public void setPartitioner(ITaxonNodeOutStreamPartitioner partitioner) {
        this.partitioner = partitioner;
    }

    public boolean isConcurrent() {
        return concurrent;
    }
    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public boolean isDoDescriptions() {
        return doDescriptions;
    }
    public void setDoDescriptions(boolean doDescriptions) {
        this.doDescriptions = doDescriptions;
    }

    public boolean isDoTaxa() {
        return doTaxa;
    }
    public void setDoTaxa(boolean doTaxa) {
        this.doTaxa = doTaxa;
    }

    public boolean isAddSources() {
        return addSources;
    }
    public void setAddSources(boolean addSources) {
        this.addSources = addSources;
    }

    public boolean isRemoveImportSources() {
        return removeImportSources;
    }
    public void setRemoveImportSources(boolean removeImportSources) {
        this.removeImportSources = removeImportSources;
    }

    public boolean isDoVocabularies() {
        return doVocabularies;
    }

    public void setDoVocabularies(boolean doVocabularies) {
        this.doVocabularies = doVocabularies;
    }

    public Set<UUID> getVocabularyFilter() {
        return vocabularyFilter;
    }
    public void setVocabularyFilter(Set<UUID> vocabularyFilter) {
        this.vocabularyFilter = vocabularyFilter;
    }

    public Set<UUID> getGraphFilter() {
        return graphFilter;
    }
    public void setGraphFilter(Set<UUID> graphFilter) {
        this.graphFilter = graphFilter;
    }
}
