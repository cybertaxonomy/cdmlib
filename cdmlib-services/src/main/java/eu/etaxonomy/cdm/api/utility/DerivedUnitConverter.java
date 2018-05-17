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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
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

    private SpecimenTypeDesignation specimenTypeDesignation;

    private SpecimenTypeDesignation newSpecimenTypeDesignation;

    private static final Logger logger = Logger.getLogger(DerivedUnitConverter.class);

    /**
     * @param specimenTypeDesignation
     */
    public DerivedUnitConverter(DerivedUnit source) throws Exception {
        if(source == null){
            throw new NullPointerException();
        }
        this.source = HibernateProxyHelper.deproxy(source, DerivedUnit.class);
        if(source.getSpecimenTypeDesignations().size() == 1){
            specimenTypeDesignation = source.getSpecimenTypeDesignations().iterator().next();
        } else if (source.getSpecimenDescriptions().size() > 1){
            throw new Exception("can not convert derived unit with multipe type designations");
        }
    }

    /**
     * @param specimenTypeDesignation
     */
    public DerivedUnitConverter(SpecimenTypeDesignation specimenTypeDesignation) {
        if(specimenTypeDesignation == null){
            throw new NullPointerException();
        }
        this.specimenTypeDesignation = HibernateProxyHelper.deproxy(specimenTypeDesignation);
        this.source = HibernateProxyHelper.deproxy(specimenTypeDesignation.getTypeSpecimen(), DerivedUnit.class);
    }

    /**
     * converts the <code>source</code> <code>DerivedUnit</code> this converter has been created
     * for into a <code>DerivedUnit</code> of the type <code>TARGET</code>.
     * If the <code>source</code> instance was persisted the target instance will also be written
     * into data base and the source is deleted from there.
     *
     * @param targetType
     * @param recordBasis
     * @throws DerivedUnitConversionException
     */
    @SuppressWarnings("unchecked")
    public SpecimenTypeDesignation convertTo(Class<TARGET> targetType, SpecimenOrObservationType recordBasis) throws DerivedUnitConversionException {

        if(source.getClass().equals(targetType)){
            // nothing to do
            return specimenTypeDesignation;
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

        logger.debug("convertion of " + source.instanceToString() + "<--" + specimenTypeDesignation.instanceToString()
                + " to "
                + newSpecimenTypeDesignation.getTypeSpecimen().instanceToString() +  "<--" + newSpecimenTypeDesignation.instanceToString() );
        return newSpecimenTypeDesignation;

    }

    /**
     * @param newInstance
     */
    private void copyPropertiesTo(TARGET target) {
        target.setAccessionNumber(source.getAccessionNumber());
        target.setBarcode(source.getBarcode());
        target.setCatalogNumber(source.getCatalogNumber());
        target.setCollection(source.getCollection());
        DerivationEvent derivationEvent = source.getDerivedFrom();
        if(derivationEvent != null){
            derivationEvent.getDerivatives().remove(source);
        }
        target.setDerivedFrom(source.getDerivedFrom());
        source.setDerivedFrom(null);
        target.setExsiccatum(source.getExsiccatum());
        target.setIndividualCount(source.getIndividualCount());
        target.setKindOfUnit(source.getKindOfUnit());
        target.setLifeStage(source.getLifeStage());
        target.setLsid(source.getLsid());
        target.setOriginalLabelInfo(source.getOriginalLabelInfo());
        target.setPreferredStableUri(source.getPreferredStableUri());
        target.setPreservation(source.getPreservation());
        target.setPublish(source.isPublish());
        target.setProtectedIdentityCache(source.isProtectedIdentityCache());
        target.setProtectedTitleCache(source.isProtectedTitleCache());
        // n.setRecordBasis(source.getRecordBasis()); // not to copy, this it is set for the new instance explicitly
        target.setSex(source.getSex());
        target.setStoredUnder(source.getStoredUnder());
        target.setTitleCache(source.getTitleCache(), target.isProtectedTitleCache());
        source.getSources().forEach(s -> target.addSource(s));
        source.getAnnotations().forEach(a -> target.addAnnotation(a));
        source.getCredits().forEach(c -> target.addCredit(c));
        source.getDerivationEvents().forEach(de -> target.addDerivationEvent(de));
        source.getDerivationEvents().clear();
        source.getDescriptions().forEach(d -> target.addDescription(d));
        source.getDeterminations().forEach(det -> target.addDetermination(det));
        source.getDeterminations().clear();
        source.getExtensions().forEach(e -> target.addExtension(e));
        source.getIdentifiers().forEach(i -> target.addIdentifier(i));
        source.getMarkers().forEach(m -> target.addMarker(m));
        source.getRights().forEach(r -> target.addRights(r));
        target.addSources(source.getSources());
        // need to clone the SpecimenTypeDesignation, since the SpecimenTypeDesignations are being deleted by the hibernate delete cascade
        newSpecimenTypeDesignation = (SpecimenTypeDesignation) specimenTypeDesignation.clone();
        for(Registration reg : specimenTypeDesignation.getRegistrations()){
            reg.removeTypeDesignation(specimenTypeDesignation);
        }
        for(TaxonName name : specimenTypeDesignation.getTypifiedNames()){
            name.removeTypeDesignation(specimenTypeDesignation);
        }
        target.addSpecimenTypeDesignation(newSpecimenTypeDesignation);

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

    /**
     * Returns the derived unit which was the source entity for the conversion.
     * You may want to delete this entity after the conversion if this makes sense
     * in the context of the actual use case. By this you can
     * avoid orphan derived units and type designations.
     */
    public DerivedUnit oldDerivedUnit(){
        return source;
    }

}
