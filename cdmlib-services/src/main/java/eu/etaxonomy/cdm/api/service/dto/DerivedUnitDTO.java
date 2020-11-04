/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.CdmFormatterFactory;
import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.format.description.DefaultCategoricalDescriptionBuilder;
import eu.etaxonomy.cdm.format.description.DefaultQuantitativeDescriptionBuilder;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.ref.TypedEntityReference;


/**
 * @author pplitzner
 * @since Mar 26, 2015
 *
 */
public class DerivedUnitDTO extends SpecimenOrObservationBaseDTO{

    private static final long serialVersionUID = 2345864166579381295L;

    private String accessionNumber;
    private String specimenIdentifier;
    private TypedEntityReference<TaxonName> storedUnder;
    private URI preferredStableUri;

    private List<TypedEntityReference<Taxon>> associatedTaxa;
    private Map<String, List<String>> types;

    private List<TypedEntityReference<TaxonName>> determinedNames;

    private String originalLabelInfo;

    private String exsiccatum;

    private String mostSignificantIdentifier;


    /**
     * Constructs a new DerivedUnitDTO. All derivatives of the passed <code>DerivedUnit entity</code> will be collected and
     * added as DerivedUnitDTO to the {@link SpecimenOrObservationBaseDTO#getDerivates() derivative DTOs}.
     *
     * @param entity
     *   The entity to create the dto for
     *
     * @return <code>null</code> or the new DerivedUnitDTO
     */
    public static DerivedUnitDTO fromEntity(DerivedUnit entity){
        return fromEntity(entity, null);
    }
    /**
     * @param entity
     *   The entity to create the dto for
     * @param individualsAssociations
     *    <b>WARNING</b> This parameter will be removed in future versions. IndividualsAssociation should better retrieved in a separate
     *    action, since individualsAssociations are not accessible from the DerivedUnit side. A service level method call is needed to
     *    retrieve them, so it would be required to access the OccurrenceServiceImpl from inside of this DTO factory method, which is
     *    bad OO design. The other option is implemented here, requires all calling Objects to pass the IndividualsAssociations as parameter.
     * @return <code>null</code> or the new DerivedUnitDTO
     * @deprecated see comment on the parameter <code>individualsAssociations</code>
     */
    @Deprecated
    public static DerivedUnitDTO fromEntity(DerivedUnit entity, Collection<IndividualsAssociation> individualsAssociations){
        if(entity == null) {
            return null;
        }
        DerivedUnitDTO dto =  new DerivedUnitDTO(entity);

        // individuals associations
        if(individualsAssociations != null) {
            for (IndividualsAssociation individualsAssociation : individualsAssociations) {
                if (individualsAssociation.getInDescription() != null) {
                    if (individualsAssociation.getInDescription().isInstanceOf(TaxonDescription.class)) {
                        TaxonDescription taxonDescription = HibernateProxyHelper.deproxy(individualsAssociation.getInDescription(), TaxonDescription.class);
                        Taxon taxon = taxonDescription.getTaxon();
                        if (taxon != null) {
                            dto.addAssociatedTaxon(taxon);
                        }
                    }
                }
            }
        }
        return dto;
    }


    /**
     * @param derivedUnit
     */
    public DerivedUnitDTO(DerivedUnit derivedUnit) {
        super(derivedUnit);
        // experimental feature, not yet exposed in method signature
        boolean cleanAccessionNumber = false;
        accessionNumber = derivedUnit.getAccessionNumber();
        preferredStableUri = derivedUnit.getPreferredStableUri();
        if (derivedUnit.getCollection() != null){
            setCollectioDTo(new CollectionDTO(HibernateProxyHelper.deproxy(derivedUnit.getCollection())));
            if(cleanAccessionNumber && getCollection().getCode() != null) {
                accessionNumber = accessionNumber.replaceFirst("^" + Pattern.quote(getCollection().getCode()) + "-", "");
            }
        }
        setBarcode(derivedUnit.getBarcode());
        setCatalogNumber(derivedUnit.getCatalogNumber());
        listLabel = derivedUnit.getCatalogNumber();
        setCollectorsNumber(derivedUnit.getCollectorsNumber());
        if (derivedUnit.getDerivedFrom() != null){
            setDerivationEvent(new DerivationEventDTO(HibernateProxyHelper.deproxy(derivedUnit.getDerivedFrom(), DerivationEvent.class )));
        }
        if (derivedUnit.getPreservation()!= null){
            setPreservationMethod(derivedUnit.getPreservation().getMaterialMethodText());
        }
        setRecordBase(derivedUnit.getRecordBasis());
        setSources(derivedUnit.getSources());
        setSpecimenTypeDesignations(derivedUnit.getSpecimenTypeDesignations());
        addDeterminedNames(derivedUnit.getDeterminations());

        // -------------------------------------------------------------

        mostSignificantIdentifier = derivedUnit.getMostSignificantIdentifier();

        //specimen identifier
        FormatKey collectionKey = FormatKey.COLLECTION_CODE;
        String specimenIdentifier = CdmFormatterFactory.format(derivedUnit, collectionKey);
        if (CdmUtils.isBlank(specimenIdentifier)) {
            collectionKey = FormatKey.COLLECTION_NAME;
        }
        if(CdmUtils.isNotBlank(derivedUnit.getMostSignificantIdentifier())){
            specimenIdentifier = CdmFormatterFactory.format(derivedUnit, new FormatKey[] {
                    collectionKey, FormatKey.SPACE, FormatKey.OPEN_BRACKET,
                    FormatKey.MOST_SIGNIFICANT_IDENTIFIER, FormatKey.CLOSE_BRACKET });
        }
        if(CdmUtils.isBlank(specimenIdentifier)){
            specimenIdentifier = derivedUnit.getTitleCache();
        }
        if(CdmUtils.isBlank(specimenIdentifier)){
            specimenIdentifier = derivedUnit.getUuid().toString();
        }
        setSpecimenIdentifier(specimenIdentifier);


        //preferred stable URI
        setPreferredStableUri(derivedUnit.getPreferredStableUri());

        // citation
        Collection<FieldUnit> fieldUnits = derivedUnit.collectFieldUnits();
        if (fieldUnits.size() == 1) {
            setCitation(fieldUnits.iterator().next().getTitleCache());
        }
        else{
            setCitation("No Citation available. This specimen either has no or multiple field units.");
        }

        // character state data
        if(derivedUnit.characterData() != null) {
            Collection<DescriptionElementBase> characterDataForSpecimen = derivedUnit.characterData();
            for (DescriptionElementBase descriptionElementBase : characterDataForSpecimen) {
                String character = descriptionElementBase.getFeature().getLabel();
                ArrayList<Language> languages = new ArrayList<>(Collections.singleton(Language.DEFAULT()));
                if (descriptionElementBase instanceof QuantitativeData) {
                    QuantitativeData quantitativeData = (QuantitativeData) descriptionElementBase;
                    DefaultQuantitativeDescriptionBuilder builder = new DefaultQuantitativeDescriptionBuilder();
                    String state = builder.build(quantitativeData, languages).getText(Language.DEFAULT());
                    addCharacterData(character, state);
                }
                else if(descriptionElementBase instanceof CategoricalData){
                    CategoricalData categoricalData = (CategoricalData) descriptionElementBase;
                    DefaultCategoricalDescriptionBuilder builder = new DefaultCategoricalDescriptionBuilder();
                    String state = builder.build(categoricalData, languages).getText(Language.DEFAULT());
                    addCharacterData(character, state);
                }
            }
        }
        // check type designations
        Collection<SpecimenTypeDesignation> specimenTypeDesignations = derivedUnit.getSpecimenTypeDesignations();
        for (SpecimenTypeDesignation specimenTypeDesignation : specimenTypeDesignations) {
            TypeDesignationStatusBase<?> typeStatus = specimenTypeDesignation.getTypeStatus();
            Set<TaxonName> typifiedNames = specimenTypeDesignation.getTypifiedNames();
            List<String> typedTaxaNames = new ArrayList<>();
            for (TaxonName taxonName : typifiedNames) {
                typedTaxaNames.add(taxonName.getTitleCache());
            }
            addTypes(typeStatus!=null?typeStatus.getLabel():"", typedTaxaNames);
        }

        // assemble sub derivatives
        setDerivateDataDTO(DerivateDataDTO.fromEntity(derivedUnit, getSpecimenIdentifier()));

        if(derivedUnit.getStoredUnder() != null) {
            storedUnder = TypedEntityReference.fromEntity(derivedUnit.getStoredUnder());
        }
        originalLabelInfo = derivedUnit.getOriginalLabelInfo();
        exsiccatum = derivedUnit.getExsiccatum();

    }

    @Override
    protected Set<Media> collectMedia(SpecimenOrObservationBase<?> specimenOrObservation){
        Set<Media> collectedMedia = super.collectMedia(specimenOrObservation);
        if(specimenOrObservation instanceof MediaSpecimen) {
            if(((MediaSpecimen)specimenOrObservation).getMediaSpecimen() != null) {
            collectedMedia.add(((MediaSpecimen)specimenOrObservation).getMediaSpecimen());
            }
        }
        return collectedMedia;
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
        if(types==null){
            types = new HashMap<String, List<String>>();
        }
        types.put(typeStatus, typedTaxa);
    }

    public List<TypedEntityReference<Taxon>> getAssociatedTaxa() {
        return associatedTaxa;
    }
    public void addAssociatedTaxon(Taxon taxon){
        if(associatedTaxa==null){
            associatedTaxa = new ArrayList<>();
        }
        associatedTaxa.add(TypedEntityReference.fromEntity(taxon));
    }

    public List<TypedEntityReference<TaxonName>> getDeterminedNames() {
        return determinedNames;
    }
    public void addDeterminedNames(Set<DeterminationEvent> determinations){
        if(determinedNames==null){
            determinedNames = new ArrayList<>();
        }
        TaxonName preferredName = null;
        for (DeterminationEvent event:determinations){
            if (event.getPreferredFlag()){
                preferredName = event.getTaxonName();
            }
        }
        if (preferredName != null){
            determinedNames.add(TypedEntityReference.fromEntity(preferredName));
        }else{
            for (DeterminationEvent event:determinations){
                determinedNames.add(TypedEntityReference.fromEntity(event.getTaxonName()));
            }
        }
    }

    public void setPreferredStableUri(URI preferredStableUri) {
        this.preferredStableUri = preferredStableUri;
    }

    public URI getPreferredStableUri() {
        return preferredStableUri;
    }
    public String getSpecimenIdentifier() {
        return specimenIdentifier;
    }
    public void setSpecimenIdentifier(String specimenIdentifier) {
        this.specimenIdentifier = specimenIdentifier;
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

}
