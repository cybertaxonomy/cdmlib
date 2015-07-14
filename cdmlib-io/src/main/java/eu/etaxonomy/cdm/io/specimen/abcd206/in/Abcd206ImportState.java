// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class Abcd206ImportState extends ImportStateBase<Abcd206ImportConfigurator, CdmImportBase<Abcd206ImportConfigurator,Abcd206ImportState>>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Abcd206ImportState.class);

	private TransactionStatus tx;

	private ICdmApplicationConfiguration cdmRepository;

	private String prefix;

	private Classification classification = null;
	private Classification defaultClassification = null;
	private Reference<?> ref = null;

	private Abcd206DataHolder dataHolder;
	private DerivedUnit derivedUnitBase;

	private List<OriginalSourceBase<?>> associationRefs = new ArrayList<OriginalSourceBase<?>>();
	private boolean associationSourcesSet=false;
	private List<OriginalSourceBase<?>> descriptionRefs = new ArrayList<OriginalSourceBase<?>>();
	private boolean descriptionSourcesSet=false;
	private List<OriginalSourceBase<?>> derivedUnitSources = new ArrayList<OriginalSourceBase<?>>();
	private boolean derivedUnitSourcesSet=false;
	private boolean descriptionGroupSet = false;
	private TaxonDescription descriptionGroup = null;

//****************** CONSTRUCTOR ***************************************************/

	public Abcd206ImportState(Abcd206ImportConfigurator config) {
		super(config);
	}

//************************ GETTER / SETTER *****************************************/

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

    public Reference<?> getRef() {
        return ref;
    }

    public void setRef(Reference<?> ref) {
        this.ref = ref;
    }

    public Abcd206DataHolder getDataHolder() {
        return dataHolder;
    }

    public void setDataHolder(Abcd206DataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }

    public DerivedUnit getDerivedUnitBase() {
        return derivedUnitBase;
    }

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

    public TaxonDescription getDescriptionGroup() {
        return descriptionGroup;
    }

    public void setDescriptionGroup(TaxonDescription descriptionGroup) {
        this.descriptionGroup = descriptionGroup;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void reset() {
        getDataHolder().reset();
        derivedUnitBase = null;
    }
}
