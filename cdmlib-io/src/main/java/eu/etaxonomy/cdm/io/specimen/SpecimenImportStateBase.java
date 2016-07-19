// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen;

import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportReport;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author k.luther
 * @param <CONFIG>
 * @date 15.07.2016
 *
 */
public abstract class SpecimenImportStateBase<CONFIG extends SpecimenImportConfiguratorBase, STATE extends SpecimenImportStateBase> extends ImportStateBase<CONFIG , CdmImportBase<CONFIG , STATE >>{


    private TransactionStatus tx;

    private ICdmApplicationConfiguration cdmRepository;
    private Classification classification = null;
    private Classification defaultClassification = null;
    private Reference ref = null;

    private TaxonDescription descriptionGroup = null;
    private DerivedUnit derivedUnitBase;

    private SpecimenImportReport report;

    private SpecimenDataHolder dataHolder;


    /**
     * @param config
     */
    protected SpecimenImportStateBase(CONFIG config) {
        super(config);
        // TODO Auto-generated constructor stub
    }

    /* -----Getter/Setter ---*/

    public TransactionStatus getTx() {
        return tx;
    }

    public void setTx(TransactionStatus tx) {
        this.tx = tx;
    }

    public ICdmApplicationConfiguration getCdmRepository() {
        return cdmRepository;
    }

    public void setCdmRepository(ICdmApplicationConfiguration cdmRepository) {
        this.cdmRepository = cdmRepository;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public Classification getDefaultClassification() {
        if(defaultClassification==null){
            final String defaultClassificationAbcd = "Default Classification ABCD";
            for (Classification classif : cdmRepository.getClassificationService().list(Classification.class, null, null, null, null)){
                if (classif.getTitleCache()!=null && classif.getTitleCache().equalsIgnoreCase(defaultClassificationAbcd)
                        && classif.getCitation()!=null && classif.getCitation().equals(getRef())) {
                    defaultClassification = classif;
                    break;
                }
            }
            if(defaultClassification==null){
                defaultClassification = Classification.NewInstance(defaultClassificationAbcd);
                cdmRepository.getClassificationService().save(defaultClassification);
            }
        }
        return defaultClassification;
    }

    public void setDefaultClassification(Classification defaultClassification) {
        this.defaultClassification = defaultClassification;
    }

    public Reference getRef() {
        return ref;
    }

    public void setRef(Reference ref) {
        this.ref = ref;
    }

    public TaxonDescription getDescriptionGroup() {
        return descriptionGroup;
    }

    public void setDescriptionGroup(TaxonDescription descriptionGroup) {
        this.descriptionGroup = descriptionGroup;
    }

    public DerivedUnit getDerivedUnitBase() {
        return derivedUnitBase;
    }

    public void setDerivedUnitBase(DerivedUnit derivedUnitBase) {
        this.derivedUnitBase = derivedUnitBase;
    }

    /**
     * @return the report
     */
    public SpecimenImportReport getReport() {
        return report;
    }

    /**
     * @param report the report to set
     */
    public void setReport(SpecimenImportReport report) {
        this.report = report;
    }

    /**
     *
     */
    public abstract void reset() ;

    /**
     * @return the dataHolder
     */
    public SpecimenDataHolder getDataHolder() {
        return dataHolder;
    }

    /**
     * @param dataHolder the dataHolder to set
     */
    public void setDataHolder(SpecimenDataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }


}
