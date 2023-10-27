/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.format.CdmFormatterFactory;
import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.format.description.DefaultCategoricalDescriptionBuilder;
import eu.etaxonomy.cdm.format.description.DefaultQuantitativeDescriptionBuilder;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author pplitzner
 * @since Mar 26, 2015
 */
public class DerivedUnitDTO extends SpecimenOrObservationBaseDTO{

    private static final long serialVersionUID = 2345864166579381295L;

    private String accessionNumber;
    private String specimenShortTitle;
    private TypedEntityReference<TaxonName> storedUnder;
    private URI preferredStableUri;

    private List<TypedEntityReference<Taxon>> associatedTaxa;
    private Map<String, List<String>> types;

    private String originalLabelInfo;
    private String exsiccatum;
    private String mostSignificantIdentifier;

    private CollectionDTO collection;

    private String catalogNumber;

    private String barcode;

    private String preservationMethod;
    private List<DerivedUnitStatusDto> status;

    /**
     * Constructs a new DerivedUnitDTO. All derivatives of the passed <code>DerivedUnit entity</code> will be collected and
     * added as DerivedUnitDTO to the {@link SpecimenOrObservationBaseDTO#getDerivatives() derivative DTOs}.
     *
     * @param entity
     *   The entity to create the dto for
     *
     * @return <code>null</code> or the new DerivedUnitDTO
     */
    public static DerivedUnitDTO fromEntity(DerivedUnit entity){
        return fromEntity(entity, null, null);
    }

    /**
     * Constructs a new DerivedUnitDTO. All derivatives of the passed <code>DerivedUnit entity</code> will be collected and
     * added as DerivedUnitDTO to the {@link SpecimenOrObservationBaseDTO#getDerivatives() derivative DTOs}.
     *
     * @param entity
     *   The entity to create the dto for
     * @param maxDepth
     *   The maximum number of derivation events levels up to which derivatives are to be collected.
     *   <code>NULL</code> means infinitely.
     * @param specimenOrObservationTypeFilter
     *     Set of SpecimenOrObservationType to be included into the collection of {@link #getDerivatives() derivative DTOs}
     * @return
     *  The DTO
     */
    @SuppressWarnings("rawtypes")
    public static DerivedUnitDTO fromEntity(DerivedUnit entity, Integer maxDepth,
            EnumSet<SpecimenOrObservationType> specimenOrObservationTypeFilter){

        if(entity == null) {
            return null;
        }
        DerivedUnitDTO dto =  new DerivedUnitDTO(entity);

        // ---- assemble derivation tree summary
        //      this data should be sufficient in clients for showing the unit in a list view
        dto.setDerivationTreeSummary(DerivationTreeSummaryDTO.fromEntity(entity, dto.getSpecimenShortTitle()));

        // ---- assemble derivatives
        //      this data is is often only required for clients in order to show the details of the derivation tree
        dto.addAllDerivatives(dto.assembleDerivatives(entity, maxDepth, specimenOrObservationTypeFilter));

        // ---- annotations
        dto.collectOriginals(entity, new HashSet<SpecimenOrObservationBase>())
            .forEach(o -> o.getAnnotations()
                    .forEach(a -> dto.addAnnotation(AnnotationDTO.fromEntity(a))
                            )
                    );

        return dto;
    }

    /**
     * Collects all originals from the given <code>entity</code> to the root of the
     * derivation graph including the <code>entity</code> itself.
     *
     * @param entity
     *            The DerivedUnit to start the collecting walk
     */
    private Set<SpecimenOrObservationBase> collectOriginals(SpecimenOrObservationBase entity, Set<SpecimenOrObservationBase> originalsToRoot) {
        originalsToRoot.add(entity);
        SpecimenOrObservationBase entityDeproxied = HibernateProxyHelper.deproxy(entity);
        if (entityDeproxied instanceof DerivedUnit) {
            ((DerivedUnit)entityDeproxied).getOriginals().forEach(o -> collectOriginals(o, originalsToRoot));
        }
        return originalsToRoot;
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
            setCollectioDTO(CollectionDTO.fromCollection(HibernateProxyHelper.deproxy(derivedUnit.getCollection())));
            if(cleanAccessionNumber && getCollection().getCode() != null) {
                accessionNumber = accessionNumber.replaceFirst("^" + Pattern.quote(getCollection().getCode()) + "-", "");
            }
        }
        setBarcode(derivedUnit.getBarcode());
        setCatalogNumber(derivedUnit.getCatalogNumber());
        setDerivationEvent(DerivationEventDTO.fromEntity(derivedUnit.getDerivedFrom()));
        if (derivedUnit.getPreservation()!= null){
            setPreservationMethod(derivedUnit.getPreservation().getMaterialMethodText());
        }
        setRecordBase(derivedUnit.getRecordBasis());
        setSources(derivedUnit.getSources());
        setSpecimenTypeDesignations(derivedUnit.getSpecimenTypeDesignations());

        // -------------------------------------------------------------

        mostSignificantIdentifier = derivedUnit.getMostSignificantIdentifier();

        //specimenShortTitle
        setSpecimenShortTitle(composeSpecimenShortTitle(derivedUnit));


        //preferred stable URI
        setPreferredStableUri(derivedUnit.getPreferredStableUri());

        // label
        setSummaryLabel(derivedUnit.getTitleCache());

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
                    String state = null;
                    if (categoricalData.getNoDataStatus()!= null) {
                        state = categoricalData.getNoDataStatus().getLabel(Language.DEFAULT());
                    }else {
                        state = builder.build(categoricalData, languages).getText(Language.DEFAULT());
                    }

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
        Collection<OccurrenceStatus> occurrenceStatus = derivedUnit.getStatus();

        if (occurrenceStatus != null && !occurrenceStatus.isEmpty()) {
        	this.status = new ArrayList<>();

	        for (OccurrenceStatus specimenStatus : occurrenceStatus) {
	            DerivedUnitStatusDto dto = new DerivedUnitStatusDto(specimenStatus.getType().getLabel());
	            dto.setStatusSource(SourceDTO.fromDescriptionElementSource(specimenStatus.getSource()) ) ;
	            this.status.add(dto);
	        }
        }
        this.setDerivationTreeSummary(DerivationTreeSummaryDTO.fromEntity(derivedUnit, this.getSpecimenShortTitle()));


        if(derivedUnit.getStoredUnder() != null) {
            storedUnder = TypedEntityReference.fromEntity(derivedUnit.getStoredUnder());
        }
        originalLabelInfo = derivedUnit.getOriginalLabelInfo();
        exsiccatum = derivedUnit.getExsiccatum();

    }

    protected String composeSpecimenShortTitle(DerivedUnit derivedUnit) {
        FormatKey collectionKey = FormatKey.COLLECTION_CODE;
        String specimenShortTitle = CdmFormatterFactory.format(derivedUnit, collectionKey);
        if (CdmUtils.isBlank(specimenShortTitle)) {
            collectionKey = FormatKey.COLLECTION_NAME;
        }
        if(CdmUtils.isNotBlank(derivedUnit.getMostSignificantIdentifier())){
            specimenShortTitle = CdmFormatterFactory.format(derivedUnit, new FormatKey[] {
                    collectionKey,
                    FormatKey.SPACE,
                    FormatKey.MOST_SIGNIFICANT_IDENTIFIER
                    });
            if(!specimenShortTitle.isEmpty() && derivedUnit instanceof MediaSpecimen) {
                Media media = ((MediaSpecimen)derivedUnit).getMediaSpecimen();
                if(media != null && !CdmUtils.isBlank(media.getTitleCache()) ) {
                    if(media.getTitle() != null && !media.getTitle().getText().isEmpty()) {
                        specimenShortTitle += " (" + media.getTitle().getText() + ")";
                    }
                }
            }
        }
        if(CdmUtils.isBlank(specimenShortTitle)){
            specimenShortTitle = derivedUnit.getTitleCache();
        }
        if(CdmUtils.isBlank(specimenShortTitle)){  //should not be necessary as titleCache should never be empty
            specimenShortTitle = derivedUnit.getUuid().toString();
        }
        return specimenShortTitle;
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
            types = new HashMap<>();
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

	@Override
    protected void updateTreeDependantData(Set<DerivedUnitDTO> derivatives) {
		for (DerivedUnitDTO derivative: derivatives) {
			this.setHasDna(this.isHasDna() || derivative.isHasDna() || !derivative.getDerivationTreeSummary().getMolecularDataList().isEmpty());
			this.setHasDetailImage(this.isHasDetailImage() || derivative.isHasDetailImage() || !derivative.getDerivationTreeSummary().getDetailImages().isEmpty());
			this.setHasSpecimenScan(isHasSpecimenScan()|| derivative.isHasSpecimenScan());
			this.setHasCharacterData(isHasCharacterData()||derivative.isHasCharacterData());
		}

        // TODO DerivationTreeSummaryDTO should be updated here once it is refactored so that it can operate on dtos
    }
}