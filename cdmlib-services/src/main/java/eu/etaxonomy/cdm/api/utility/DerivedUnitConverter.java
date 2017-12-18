/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 *
 * @author a.kohlbecker
 * @since Jun 23, 2017
 *
 */
public class DerivedUnitConverter<TARGET extends DerivedUnit> {

    private DerivedUnit source;

    IOccurrenceService service;

    /**
     * @param specimenTypeDesignation
     */
    protected DerivedUnitConverter(DerivedUnit derivedUnit, IOccurrenceService service) {
        if(derivedUnit == null){
            throw new NullPointerException();
        }
        this.service = service;
        this.source = HibernateProxyHelper.deproxy(derivedUnit, DerivedUnit.class);

    }

    /**
     * converts the <code>source</code> <code>DerivedUnit</code> this converter has been created for into a <code>DerivedUnit</code> of the type <code>TARGET</code>.
     * If the <code>source</code> instance was persisted the target instance will also be written into data base and the source is deleted from there.
     *
     * @param targetType
     * @param recordBasis
     * @throws DerivedUnitConversionException
     */
    @SuppressWarnings("unchecked")
    public TARGET convertTo(Class<TARGET> targetType, SpecimenOrObservationType recordBasis) throws DerivedUnitConversionException {

        if(source.getClass().equals(targetType)){
            // nothing to do
            return (TARGET) source;
        }

        if(!isSuppoprtedType(targetType)){
            throw new DerivedUnitConversionException(
                    String.format("Unsupported convertion target type: %s",
                            targetType.getName())
                    );
        }

        if(!canConvert()){
            throw new DerivedUnitConversionException(
                    String.format("%s can not be converted into %s as long it contains unconvertable non null properties",
                            source.toString(),
                            targetType.getName())
                    );
        }

        TARGET newInstance = null;
        try {
            Method newInstanceMethod = targetType.getDeclaredMethod("NewInstance", SpecimenOrObservationType.class);
            newInstance = (TARGET) newInstanceMethod.invoke(SpecimenOrObservationType.class, recordBasis);

            copyPropertiesTo(newInstance);

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new DerivedUnitConversionException("Error during intantiation of " + targetType.getName(), e);
        }


        if(source.getId() > 0){
            service.merge(newInstance);
            service.delete(source);
        }

        return newInstance;

    }

    /**
     * @param newInstance
     */
    private void copyPropertiesTo(TARGET n) {
        n.setAccessionNumber(source.getAccessionNumber());
        n.setBarcode(source.getBarcode());
        n.setCatalogNumber(source.getCatalogNumber());
        n.setCollection(source.getCollection());
        DerivationEvent derivationEvent = source.getDerivedFrom();
        derivationEvent.getDerivatives().remove(source);
        n.setDerivedFrom(source.getDerivedFrom());
        source.setDerivedFrom(null);
        n.setExsiccatum(source.getExsiccatum());
        n.setIndividualCount(source.getIndividualCount());
        n.setKindOfUnit(source.getKindOfUnit());
        n.setLifeStage(source.getLifeStage());
        n.setLsid(source.getLsid());
        n.setOriginalLabelInfo(source.getOriginalLabelInfo());
        n.setPreferredStableUri(source.getPreferredStableUri());
        n.setPreservation(source.getPreservation());
        n.setPublish(source.isPublish());
        n.setProtectedIdentityCache(source.isProtectedIdentityCache());
        n.setProtectedTitleCache(source.isProtectedTitleCache());
        // n.setRecordBasis(source.getRecordBasis()); // not to copy, this it is set for the new instance explicitly
        n.setSex(source.getSex());
        n.setStoredUnder(source.getStoredUnder());
        n.setTitleCache(source.getTitleCache(), n.isProtectedTitleCache());
        source.getSources().forEach(s -> n.addSource(s));
        source.getAnnotations().forEach(a -> n.addAnnotation(a));
        source.getCredits().forEach(c -> n.addCredit(c));
        source.getDerivationEvents().forEach(de -> n.addDerivationEvent(de));
        source.getDerivationEvents().clear();
        source.getDescriptions().forEach(d -> n.addDescription(d));
        source.getDeterminations().forEach(det -> n.addDetermination(det));
        source.getDeterminations().clear();
        source.getExtensions().forEach(e -> n.addExtension(e));
        source.getIdentifiers().forEach(i -> n.addIdentifier(i));
        source.getMarkers().forEach(m -> n.addMarker(m));
        source.getRights().forEach(r -> n.addRights(r));
        n.addSources(source.getSources());
        for(SpecimenTypeDesignation std :  source.getSpecimenTypeDesignations()) {
            std.setTypeSpecimen(n);
            n.addSpecimenTypeDesignation(std);
         }
        source.getSpecimenTypeDesignations().clear();

    }

    /**
     * @param targetType
     * @return
     */
    public boolean isSuppoprtedType(Class<TARGET> targetType) {
        return targetType.equals(MediaSpecimen.class) || targetType.equals(DerivedUnit.class);
    }

    /**
     * @return
     */
    private boolean canConvert() {

        if(source.getClass().equals(DerivedUnit.class)) {
            return true;
        }
        if(source.getClass().equals(MediaSpecimen.class)){
            MediaSpecimen ms = (MediaSpecimen)source;
            Media media = ms.getMediaSpecimen();
            return media == null;
        }

        return false;
    }

}
