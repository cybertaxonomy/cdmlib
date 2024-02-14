/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.api.dto.DerivedUnitDTO;
import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * Loader for {@link DerivedUnitDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class DerivedUnitDtoLoader extends DerivedUnitDtoLoaderBase<DerivedUnit> {

    public static DerivedUnitDtoLoader INSTANCE() {
        return new DerivedUnitDtoLoader();
    }

    /**
     * Constructs a new DerivedUnitDTO. All derivatives of the passed <code>DerivedUnit entity</code> will be collected and
     * added as DerivedUnitDTO to the {@link SpecimenOrObservationBaseDTO#getDerivatives() derivative DTOs}.
     *
     * @param entity
     *   The entity to create the dto for
     *
     * @return <code>null</code> or the new DerivedUnitDTO
     */
    @Override
    public DerivedUnitDTO fromEntity(DerivedUnit entity){
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
    public DerivedUnitDTO fromEntity(DerivedUnit derivedUnit, Integer maxDepth,
            EnumSet<SpecimenOrObservationType> specimenOrObservationTypeFilter){

        if(derivedUnit == null) {
            return null;
        }
        DerivedUnitDTO dto = new DerivedUnitDTO(DerivedUnit.class, derivedUnit.getUuid(), derivedUnit.getTitleCache());
        load(dto, derivedUnit);


        // ---- assemble derivation tree summary
        //      this data should be sufficient in clients for showing the unit in a list view
        dto.setDerivationTreeSummary(DerivationTreeSummaryDtoLoader.fromEntity(derivedUnit, dto.getSpecimenShortTitle()));

        // ---- assemble derivatives
        //      this data is is often only required for clients in order to show the details of the derivation tree
        dto.addAllDerivatives(assembleDerivatives(dto, derivedUnit, maxDepth, specimenOrObservationTypeFilter));

        // ---- annotations
        collectOriginals(derivedUnit, new HashSet<SpecimenOrObservationBase>())
            .forEach(o -> o.getAnnotations()
                    .forEach(a -> dto.addAnnotation(AnnotationDtoLoader.INSTANCE().fromEntity(a)))
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
    private Set<SpecimenOrObservationBase> collectOriginals(SpecimenOrObservationBase<?> entity,
            Set<SpecimenOrObservationBase> originalsToRoot) {

        originalsToRoot.add(entity);
        SpecimenOrObservationBase<?> entityDeproxied = HibernateProxyHelper.deproxy(entity);
        if (entityDeproxied instanceof DerivedUnit) {
            ((DerivedUnit)entityDeproxied).getOriginals().forEach(o -> collectOriginals(o, originalsToRoot));
        }
        return originalsToRoot;
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
}