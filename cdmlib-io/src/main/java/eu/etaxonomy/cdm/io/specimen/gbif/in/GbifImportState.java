// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.gbif.in;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.AbcdTransformer;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportReport;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author k.luther
 * @date 15.07.2016
 *
 */
public class GbifImportState extends SpecimenImportStateBase<GbifImportConfigurator<GbifImportState, InputStream>, GbifImportState> {

    private TransactionStatus tx;




    private ICdmApplicationConfiguration cdmRepository;

    private String prefix;

    private GbifImportReport report;

    private Classification classification = null;
    private Classification defaultClassification = null;
    private Reference ref = null;

    private GbifDataHolder dataHolder;
    private DerivedUnit derivedUnitBase;

    private List<OriginalSourceBase<?>> associationRefs = new ArrayList<OriginalSourceBase<?>>();
    private boolean associationSourcesSet=false;
    private List<OriginalSourceBase<?>> descriptionRefs = new ArrayList<OriginalSourceBase<?>>();
    private boolean descriptionSourcesSet=false;
    private List<OriginalSourceBase<?>> derivedUnitSources = new ArrayList<OriginalSourceBase<?>>();
    private boolean derivedUnitSourcesSet=false;
    private boolean descriptionGroupSet = false;
    private TaxonDescription descriptionGroup = null;




    public GbifImportState newInstance(GbifImportConfigurator config){
        GbifImportState result = new GbifImportState(config);
        return result;

    }


    /* ------Getter/Setter -----*/

    /**
     * @param config
     * @return
     */
    private GbifImportState(GbifImportConfigurator config) {
        super(config);
        setReport(new SpecimenImportReport());
        setTransformer(new AbcdTransformer());
    }

    @Override
    public TransactionStatus getTx() {
        return tx;
    }


    @Override
    public void setTx(TransactionStatus tx) {
        this.tx = tx;
    }


    @Override
    public ICdmApplicationConfiguration getCdmRepository() {
        return cdmRepository;
    }


    @Override
    public void setCdmRepository(ICdmApplicationConfiguration cdmRepository) {
        this.cdmRepository = cdmRepository;
    }


    public String getPrefix() {
        return prefix;
    }


    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    @Override
    public SpecimenImportReport getReport() {
        return report;
    }


    public void setReport(GbifImportReport report) {
        this.report = report;
    }


    @Override
    public Classification getClassification() {
        return classification;
    }


    @Override
    public void setClassification(Classification classification) {
        this.classification = classification;
    }


    @Override
    public Classification getDefaultClassification() {
        return defaultClassification;
    }


    @Override
    public void setDefaultClassification(Classification defaultClassification) {
        this.defaultClassification = defaultClassification;
    }


    @Override
    public Reference getRef() {
        return ref;
    }


    @Override
    public void setRef(Reference ref) {
        this.ref = ref;
    }


    @Override
    public GbifDataHolder getDataHolder() {
        return dataHolder;
    }


    public void setDataHolder(GbifDataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }


    @Override
    public DerivedUnit getDerivedUnitBase() {
        return derivedUnitBase;
    }


    @Override
    public void setDerivedUnitBase(DerivedUnit derivedUnitBase) {
        this.derivedUnitBase = derivedUnitBase;
    }


    public List<OriginalSourceBase<?>> getAssociationRefs() {
        return associationRefs;
    }


    public void setAssociationRefs(List<OriginalSourceBase<?>> associationRefs) {
        this.associationRefs = associationRefs;
    }


    public boolean isAssociationSourcesSet() {
        return associationSourcesSet;
    }


    public void setAssociationSourcesSet(boolean associationSourcesSet) {
        this.associationSourcesSet = associationSourcesSet;
    }


    public List<OriginalSourceBase<?>> getDescriptionRefs() {
        return descriptionRefs;
    }


    public void setDescriptionRefs(List<OriginalSourceBase<?>> descriptionRefs) {
        this.descriptionRefs = descriptionRefs;
    }


    public boolean isDescriptionSourcesSet() {
        return descriptionSourcesSet;
    }


    public void setDescriptionSourcesSet(boolean descriptionSourcesSet) {
        this.descriptionSourcesSet = descriptionSourcesSet;
    }


    public List<OriginalSourceBase<?>> getDerivedUnitSources() {
        return derivedUnitSources;
    }


    public void setDerivedUnitSources(List<OriginalSourceBase<?>> derivedUnitSources) {
        this.derivedUnitSources = derivedUnitSources;
    }


    public boolean isDerivedUnitSourcesSet() {
        return derivedUnitSourcesSet;
    }


    public void setDerivedUnitSourcesSet(boolean derivedUnitSourcesSet) {
        this.derivedUnitSourcesSet = derivedUnitSourcesSet;
    }


    public boolean isDescriptionGroupSet() {
        return descriptionGroupSet;
    }


    public void setDescriptionGroupSet(boolean descriptionGroupSet) {
        this.descriptionGroupSet = descriptionGroupSet;
    }


    @Override
    public TaxonDescription getDescriptionGroup() {
        return descriptionGroup;
    }


    @Override
    public void setDescriptionGroup(TaxonDescription descriptionGroup) {
        this.descriptionGroup = descriptionGroup;
    }



}
