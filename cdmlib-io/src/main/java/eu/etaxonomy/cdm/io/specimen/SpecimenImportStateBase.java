/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportReport;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author k.luther
 * @since 15.07.2016
 */
public class SpecimenImportStateBase<CONFIG extends SpecimenImportConfiguratorBase, STATE extends SpecimenImportStateBase>
        extends ImportStateBase<CONFIG , CdmImportBase<CONFIG , STATE >>{

    public SpecimenImportStateBase(CONFIG config) {
        super(config);
    }

    private TransactionStatus tx;

    private ICdmRepository cdmRepository;
    private Classification classification = null;
    private Classification defaultClassification = null;
    private Reference ref = null;

    private HashMap<UUID, TaxonDescription> individualsAssociationDescriptionPerTaxon = new HashMap<>();
    private DerivedUnit derivedUnitBase;

    private SpecimenImportReport report;

    protected SpecimenDataHolder dataHolder;

    private List<OriginalSourceBase> associationRefs = new ArrayList<>();
    private boolean associationSourcesSet=false;
    private List<OriginalSourceBase> descriptionRefs = new ArrayList<>();
    private boolean descriptionSourcesSet=false;
    private List<OriginalSourceBase> derivedUnitSources = new ArrayList<>();
    private boolean derivedUnitSourcesSet=false;
    private boolean descriptionGroupSet = false;
    protected HashMap<String, Institution> institutions = new HashMap<>();
    protected HashMap<String, Collection> collections= new HashMap<>();
    protected HashMap<String, TaxonName> names= new HashMap<>();
    private HashMap<String,FieldUnit> fieldUnits = new HashMap<>();

    MapWrapper<TeamOrPersonBase<?>> personStore;
    private Map<String, Reference> importReferences = new HashMap<>();
    private URI actualAccessPoint;
    private Set<URI> allAccesPoints = new HashSet<>();

// ************************* Getter/Setter **************/

    public MapWrapper<TeamOrPersonBase<?>> getPersonStore() {
        return personStore;
    }
    public void setPersonStore(MapWrapper<TeamOrPersonBase<?>> personStore) {
        this.personStore = personStore;
    }

    public FieldUnit getFieldUnit(String fieldNumber) {
        return fieldUnits.get(fieldNumber);
    }
    public void setFieldUnit(FieldUnit fieldUnit) {
        this.fieldUnits.put(fieldUnit.getFieldNumber(), fieldUnit);
    }

    @Override
    public CONFIG getConfig(){
        return super.getConfig();
    }

    public List<OriginalSourceBase> getAssociationRefs() {
        return associationRefs;
    }

    public void setAssociationRefs(List<OriginalSourceBase> associationRefs) {
        this.associationRefs = associationRefs;
    }

    public boolean isAssociationSourcesSet() {
        return associationSourcesSet;
    }

    public void setAssociationSourcesSet(boolean associationSourcesSet) {
        this.associationSourcesSet = associationSourcesSet;
    }

    public List<OriginalSourceBase> getDescriptionRefs() {
        return descriptionRefs;
    }

    public void setDescriptionRefs(List<OriginalSourceBase> descriptionRefs) {
        this.descriptionRefs = descriptionRefs;
    }

    public boolean isDescriptionSourcesSet() {
        return descriptionSourcesSet;
    }

    public void setDescriptionSourcesSet(boolean descriptionSourcesSet) {
        this.descriptionSourcesSet = descriptionSourcesSet;
    }

    public List<OriginalSourceBase> getDerivedUnitSources() {
        return derivedUnitSources;
    }

    public void setDerivedUnitSources(List<OriginalSourceBase> derivedUnitSources) {
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

    public TransactionStatus getTx() {
        return tx;
    }

    public void setTx(TransactionStatus tx) {
        this.tx = tx;
    }

    public ICdmRepository getCdmRepository() {
        return cdmRepository;
    }

    public void setCdmRepository(ICdmRepository cdmRepository) {
        this.cdmRepository = cdmRepository;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public Classification getDefaultClassification(boolean createIfNotExist) {
        if(defaultClassification==null && createIfNotExist){
            final String defaultClassificationAbcd = "Default Classification Specimen Import";
            for (Classification classif : cdmRepository.getClassificationService().list(Classification.class, null, null, null, null)){
                if (classif.getTitleCache()!=null && classif.getTitleCache().equalsIgnoreCase(defaultClassificationAbcd)
                        && classif.getReference()!=null && classif.getReference().equals(getRef())) {
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

    public TaxonDescription getIndividualsAssociationDescriptionPerTaxon(UUID taxonUUID) {
        return individualsAssociationDescriptionPerTaxon.get(taxonUUID);
    }

    public void setIndividualsAssociationDescriptionPerTaxon(TaxonDescription description) {
        this.individualsAssociationDescriptionPerTaxon.put(description.getTaxon().getUuid(), description);
    }

    public DerivedUnit getDerivedUnitBase() {
        return derivedUnitBase;
    }
    public void setDerivedUnitBase(DerivedUnit derivedUnitBase) {
        this.derivedUnitBase = derivedUnitBase;
    }

    public URI getActualAccessPoint() {
        return actualAccessPoint;
    }

    public Set<URI> getAllAccesPoint() {
        return allAccesPoints;
    }

    public void addActualAccesPoint(URI actualAccesPoint) {
        this.allAccesPoints.add(actualAccesPoint);
    }
    public void setActualAccessPoint(URI actualAccessPoint) {
        this.addActualAccesPoint(actualAccessPoint);
        this.actualAccessPoint = actualAccessPoint;
    }

    public SpecimenImportReport getReport() {
        if (report == null){
            report = new SpecimenImportReport();
        }
        return report;
    }
    public void setReport(SpecimenImportReport report) {
        this.report = report;
    }

    public SpecimenDataHolder getDataHolder() {
        return dataHolder;
    }
    public void setDataHolder(SpecimenDataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }
    public void reset() {
        getDataHolder().reset();
       // setDerivedUnitBase(null);
    }

    public Reference getImportReference(URI accessPoint){
        if (accessPoint == null){
            return null;
        }
        if (importReferences == null){
            importReferences = new HashMap<>();
        }
        if (this.importReferences.containsKey(accessPoint.toString())){
            return this.importReferences.get(accessPoint.toString());
        }else{
            Reference ref = ReferenceFactory.newGeneric();
            ref.setUri(accessPoint);
            ref.setTitle(accessPoint.toString());
            this.importReferences.put(accessPoint.toString(), ref);
            return ref;
        }
    }

    public void addImportReference(Reference ref) {
        if (ref.getUri() != null){
            this.importReferences.put(ref.getUri().toString(), ref);
        }
    }
}