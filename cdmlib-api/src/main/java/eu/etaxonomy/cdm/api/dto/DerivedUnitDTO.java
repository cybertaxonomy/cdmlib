/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceFactory;

/**
 * @author pplitzner
 * @since Mar 26, 2015
 */
public class DerivedUnitDTO
        extends SpecimenOrObservationBaseDTO<DerivedUnit> {

    private static final long serialVersionUID = 2345864166579381295L;

    private String accessionNumber;
    private TypedEntityReference<TaxonName> storedUnder;
    private String originalLabelInfo;
    private String exsiccatum;
    private CollectionDTO collection;
    private String catalogNumber;
    private String barcode;
    private String preservationMethod;
    private List<DerivedUnitStatusDto> status;

    private String specimenShortTitle;
    private List<TypedEntityReference<Taxon>> associatedTaxa = new ArrayList<>();
    private URI preferredStableUri;

    private DerivationEventDTO derivationEvent;

    private Map<String, List<String>> types = new HashMap<>();

    private String mostSignificantIdentifier;

    //TODO remove model dependency
    public DerivedUnitDTO(Class<DerivedUnit> type, UUID uuid, String label) {
        super(type, uuid, label);
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Map<String, List<String>> getTypes() {
        return types;
    }
    public void addTypes(String typeStatus, List<String> typedTaxa){
        types.put(typeStatus, typedTaxa);
    }

    public List<TypedEntityReference<Taxon>> getAssociatedTaxa() {
        return associatedTaxa;
    }
    public void addAssociatedTaxon(Taxon taxon){
        associatedTaxa.add(TypedEntityReferenceFactory.fromEntity(taxon));
    }

    public void setPreferredStableUri(URI preferredStableUri) {
        this.preferredStableUri = preferredStableUri;
    }
    public URI getPreferredStableUri() {
        return preferredStableUri;
    }

    public String getSpecimenShortTitle() {
        return specimenShortTitle;
    }
    public void setSpecimenShortTitle(String specimenIdentifier) {
        this.specimenShortTitle = specimenIdentifier;
    }

    public String getMostSignificantIdentifier() {
        return mostSignificantIdentifier;
    }
    public void setMostSignificantIdentifier(String mostSignificantIdentifier) {
        this.mostSignificantIdentifier = mostSignificantIdentifier;
    }

    public TypedEntityReference<TaxonName> getStoredUnder() {
        return storedUnder;
    }
    public void setStoredUnder(TypedEntityReference<TaxonName> storedUnder) {
        this.storedUnder = storedUnder;
    }

    public String getOriginalLabelInfo() {
        return originalLabelInfo;
    }
    public void setOriginalLabelInfo(String originalLabelInfo) {
        this.originalLabelInfo = originalLabelInfo;
    }

    public String getExsiccatum() {
        return exsiccatum;
    }
    public void setExsiccatum(String exsiccatum) {
        this.exsiccatum = exsiccatum;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }
    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPreservationMethod() {
        return preservationMethod;
    }
    public void setPreservationMethod(String preservationMethod) {
        this.preservationMethod = preservationMethod;
    }

    public CollectionDTO getCollection() {
        return collection;
    }
    public void setCollectioDTO(CollectionDTO collection) {
        this.collection = collection;
    }

    public List<DerivedUnitStatusDto> getStatus() {
		return status;
	}
	public void setStatus(List<DerivedUnitStatusDto> status) {
		this.status = status;
	}

    public DerivationEventDTO getDerivationEvent() {
        return derivationEvent;
    }
    public void setDerivationEvent(DerivationEventDTO derivationEvent) {
        this.derivationEvent = derivationEvent;
    }
}